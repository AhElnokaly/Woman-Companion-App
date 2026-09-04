package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.StepLog
import java.util.Calendar
import java.util.TimeZone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StepCounterService : Service() {

    private val CHANNEL_ID = "step_counter_channel"
    private val NOTIFICATION_ID = 101

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase
    private val stepsMutex = Mutex()

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var todaySteps = 0

    // Debouncing & Throttling to save battery and avoid IPC notification flooding
    private var pendingPersistJob: Job? = null
    private var lastNotificationTime = 0L
    private var lastNotifiedSteps = 0

    // Shared preferences key for robust step counting persistence
    private val PREFS_NAME = "woman_companion_prefs"
    private val KEY_LAST_SENSOR_VALUE = "last_sensor_value"
    private val KEY_DAY_START_SENSOR_VALUE = "day_start_sensor_value"
    private val KEY_LAST_KNOWN_DAY = "last_known_day"

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return
            when (event.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    val currentSensorValue = event.values[0].toInt()
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val today = getStartOfDay()
                    val lastKnownDay = prefs.getLong(KEY_LAST_KNOWN_DAY, -1L)

                    if (lastKnownDay != today) {
                        // Midnight rollover! Reset daily count baseline
                        synchronized(this@StepCounterService) {
                            todaySteps = 0
                        }
                        prefs.edit()
                            .putLong(KEY_LAST_KNOWN_DAY, today)
                            .putInt(KEY_DAY_START_SENSOR_VALUE, currentSensorValue)
                            .putInt(KEY_LAST_SENSOR_VALUE, currentSensorValue)
                            .apply()

                        saveTodaySteps(0)
                    } else {
                        var dayStartVal = prefs.getInt(KEY_DAY_START_SENSOR_VALUE, -1)
                        val lastSensorVal = prefs.getInt(KEY_LAST_SENSOR_VALUE, -1)

                        if (dayStartVal == -1 || lastSensorVal == -1) {
                            dayStartVal = currentSensorValue
                            prefs.edit()
                                .putInt(KEY_DAY_START_SENSOR_VALUE, currentSensorValue)
                                .putInt(KEY_LAST_SENSOR_VALUE, currentSensorValue)
                                .apply()
                        } else if (currentSensorValue < lastSensorVal) {
                            // Phone rebooted mid-day; adjust baseline so today's accumulated count is preserved
                            val stepsBeforeReboot = synchronized(this@StepCounterService) { todaySteps }
                            dayStartVal = currentSensorValue - stepsBeforeReboot
                            prefs.edit()
                                .putInt(KEY_DAY_START_SENSOR_VALUE, dayStartVal)
                                .putInt(KEY_LAST_SENSOR_VALUE, currentSensorValue)
                                .apply()
                        }

                        val computedSteps = (currentSensorValue - dayStartVal).coerceAtLeast(0)
                        prefs.edit().putInt(KEY_LAST_SENSOR_VALUE, currentSensorValue).apply()

                        synchronized(this@StepCounterService) {
                            todaySteps = computedSteps
                        }
                        saveTodaySteps(computedSteps)
                    }
                }
                Sensor.TYPE_STEP_DETECTOR -> {
                    if (event.values[0] == 1.0f) {
                        checkDayBoundaryFallback()
                        addSteps(1)
                    }
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    // Fallback basic step detection
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble())
                    val currentTime = System.currentTimeMillis()
                    if (magnitude > 12.5 && (currentTime - lastAccelerometerStepTime) > 350) {
                        checkDayBoundaryFallback()
                        addSteps(1)
                        lastAccelerometerStepTime = currentTime
                    }
                }
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun checkDayBoundaryFallback() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getStartOfDay()
        val lastKnownDay = prefs.getLong(KEY_LAST_KNOWN_DAY, -1L)
        if (lastKnownDay != today) {
            synchronized(this) {
                todaySteps = 0
            }
            prefs.edit().putLong(KEY_LAST_KNOWN_DAY, today).apply()
            saveTodaySteps(0)
        }
    }

    private var lastAccelerometerStepTime: Long = 0L

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(applicationContext)
        createNotificationChannel()

        // Start Foreground Service immediately with safe defaults
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, getNotification(todaySteps), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(NOTIFICATION_ID, getNotification(todaySteps))
        }

        loadTodaySteps()
        initializeSensors()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // We want the service to run continuously until explicitly stopped
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun loadTodaySteps() {
        checkDayBoundaryFallback()
        serviceScope.launch {
            stepsMutex.withLock {
                val today = getStartOfDay()
                val existing = database.womanCompanionDao().getStepLogForDate(today)
                synchronized(this@StepCounterService) {
                    todaySteps = existing?.steps ?: 0
                }
                updateNotification()
            }
        }
    }

    private fun saveTodaySteps(steps: Int, forceImmediate: Boolean = false) {
        if (forceImmediate) {
            pendingPersistJob?.cancel()
            persistStepsToDb(steps)
            return
        }

        // Debounce database writing (persisting every 5 seconds or on major batch) to protect battery and flash wear
        pendingPersistJob?.cancel()
        pendingPersistJob = serviceScope.launch {
            delay(5000)
            persistStepsToDb(steps)
        }
    }

    private fun persistStepsToDb(steps: Int) {
        serviceScope.launch {
            stepsMutex.withLock {
                val today = getStartOfDay()
                val dao = database.womanCompanionDao()
                val existing = dao.getStepLogForDate(today)
                val settings = dao.getAppLockSettings()
                val target = settings?.dailyStepTarget ?: 6000

                if (existing != null) {
                    dao.insertStepLog(existing.copy(steps = steps, targetSteps = target))
                } else {
                    dao.insertStepLog(StepLog(date = today, steps = steps, targetSteps = target))
                }
                updateNotificationThrottled(steps)
            }
        }
    }

    private fun addSteps(stepsToAdd: Int) {
        checkDayBoundaryFallback()
        val finalSteps = synchronized(this) {
            todaySteps += stepsToAdd
            todaySteps
        }
        updateNotificationThrottled(finalSteps)
        saveTodaySteps(finalSteps, forceImmediate = false)
    }

    private fun initializeSensors() {
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepSensor != null) {
                sensorManager?.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
                Log.d("StepCounterService", "Successfully registered TYPE_STEP_COUNTER listener")
            } else {
                stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
                if (stepSensor != null) {
                    sensorManager?.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
                    Log.d("StepCounterService", "Successfully registered TYPE_STEP_DETECTOR listener")
                } else {
                    stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                    stepSensor?.let {
                        sensorManager?.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_NORMAL)
                        Log.d("StepCounterService", "Successfully registered TYPE_ACCELEROMETER step detector fallback")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("StepCounterService", "Failed to init step sensor", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.step_counter_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.step_counter_channel_desc)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getNotification(steps: Int): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("جوري: عداد الخطوات نشط")
            .setContentText("لقد سرتِ $steps خطوة اليوم 🐾")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(force: Boolean = false) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val steps = synchronized(this) { todaySteps }
        lastNotifiedSteps = steps
        lastNotificationTime = System.currentTimeMillis()
        manager.notify(NOTIFICATION_ID, getNotification(steps))
    }

    private fun updateNotificationThrottled(steps: Int) {
        val now = System.currentTimeMillis()
        // Throttle IPC notification updates: update if at least 15 seconds passed OR at least 25 steps taken
        if (now - lastNotificationTime > 15_000L || Math.abs(steps - lastNotifiedSteps) >= 25) {
            updateNotification(force = true)
        }
    }

    private fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            sensorManager?.unregisterListener(stepListener)
        } catch (e: Exception) {
            Log.e("StepCounterService", "Failed to unregister step listener", e)
        }
        val currentSteps = synchronized(this) { todaySteps }
        saveTodaySteps(currentSteps, forceImmediate = true)
        serviceScope.cancel()
    }
}

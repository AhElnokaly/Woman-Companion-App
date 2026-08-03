package com.example.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Appointment
import com.example.data.MedicationAdherenceLog
import com.example.data.MedicationLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.max

enum class RecurrenceType {
    NONE,
    DAILY,
    SPECIFIC_DAYS,
    TIMES_PER_DAY
}

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"
    const val CHANNEL_ID = "woman_companion_reminders"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    const val EXTRA_RECURRENCE = "extra_recurrence"
    const val EXTRA_MED_ID = "extra_med_id"
    const val EXTRA_TRIGGER_TIME = "extra_trigger_time"

    const val ACTION_TRIGGER_REMINDER = "com.example.reminder.TRIGGER_REMINDER"
    const val ACTION_MARK_TAKEN = "com.example.reminder.MARK_TAKEN"
    const val ACTION_SNOOZE = "com.example.reminder.SNOOZE"

    fun canScheduleExact(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleReminder(
        context: Context,
        id: Int,
        triggerTimeMillis: Long,
        title: String,
        body: String,
        recurrence: RecurrenceType,
        medicationId: Int = 0
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_REMINDER_ID, id)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_RECURRENCE, recurrence.name)
            putExtra(EXTRA_MED_ID, medicationId)
            putExtra(EXTRA_TRIGGER_TIME, triggerTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExact(context)) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm", e)
        }
    }

    fun cancelReminder(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleMedicationReminders(context: Context, med: MedicationLog) {
        if (!med.isActive) return
        val now = System.currentTimeMillis()
        val baseTime = med.startDate ?: now
        val count = if (med.timesPerDay > 0) med.timesPerDay else 1

        if (count == 1) {
            var trigger = if (baseTime > now) baseTime else baseTime + 24 * 3600 * 1000L
            if (trigger <= now) {
                trigger = now + 60_000L
            }
            scheduleReminder(
                context = context,
                id = med.id * 100,
                triggerTimeMillis = trigger,
                title = "تذكير بجرعة الدواء: ${med.name}",
                body = "الجرعة: ${med.dosage ?: "حسب الوصفة"}",
                recurrence = RecurrenceType.DAILY,
                medicationId = med.id
            )
        } else {
            val interval = (24 * 3600 * 1000L) / count
            for (i in 0 until count) {
                var trigger = baseTime + i * interval
                while (trigger <= now) {
                    trigger += 24 * 3600 * 1000L
                }
                scheduleReminder(
                    context = context,
                    id = med.id * 100 + i,
                    triggerTimeMillis = trigger,
                    title = "تذكير بجرعة الدواء: ${med.name} (جرعة ${i + 1}/$count)",
                    body = "الجرعة: ${med.dosage ?: "حسب الوصفة"}",
                    recurrence = RecurrenceType.DAILY,
                    medicationId = med.id
                )
            }
        }
    }

    fun cancelMedicationReminders(context: Context, medId: Int, timesPerDay: Int = 10) {
        cancelReminder(context, medId)
        val maxCount = max(10, timesPerDay)
        for (i in 0 until maxCount + 2) {
            cancelReminder(context, medId * 100 + i)
        }
    }

    fun scheduleAppointmentReminder(context: Context, appt: Appointment) {
        val now = System.currentTimeMillis()
        if (appt.completed || appt.dateTime <= now) return
        scheduleReminder(
            context = context,
            id = 10000 + appt.id,
            triggerTimeMillis = appt.dateTime,
            title = "تذكير بموعد طبي: ${appt.title}",
            body = "الطبيب: ${appt.doctorName ?: "غير محدد"}",
            recurrence = RecurrenceType.NONE
        )
    }

    fun cancelAppointmentReminder(context: Context, apptId: Int) {
        cancelReminder(context, 10000 + apptId)
    }

    fun rescheduleAllReminders(context: Context) {
        val db = AppDatabase.getDatabase(context.applicationContext)
        val dao = db.womanCompanionDao()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeMeds = dao.getActiveMedicationsFlow().firstOrNull() ?: emptyList()
                activeMeds.forEach { med ->
                    scheduleMedicationReminders(context, med)
                }

                val appointments = dao.getAllAppointmentsFlow().firstOrNull() ?: emptyList()
                val now = System.currentTimeMillis()
                appointments.filter { !it.completed && it.dateTime > now }.forEach { appt ->
                    scheduleAppointmentReminder(context, appt)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling reminders", e)
            }
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getIntExtra(ReminderScheduler.EXTRA_REMINDER_ID, 0)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "تذكير صحي"
        val body = intent.getStringExtra(ReminderScheduler.EXTRA_BODY) ?: ""
        val medId = intent.getIntExtra(ReminderScheduler.EXTRA_MED_ID, 0)
        val recurrenceStr = intent.getStringExtra(ReminderScheduler.EXTRA_RECURRENCE) ?: RecurrenceType.NONE.name

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ReminderScheduler.CHANNEL_ID,
                "التذكيرات الصحية والدواء",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة الإشعارات للتذكير بالمواعيد وتناول الدواء"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        when (action) {
            ReminderScheduler.ACTION_TRIGGER_REMINDER -> {
                showNotification(context, notificationManager, reminderId, title, body, medId)

                // Reschedule next occurrence if recurring (DAILY or TIMES_PER_DAY)
                if (recurrenceStr == RecurrenceType.DAILY.name || recurrenceStr == RecurrenceType.TIMES_PER_DAY.name) {
                    val prevTrigger = intent.getLongExtra(ReminderScheduler.EXTRA_TRIGGER_TIME, System.currentTimeMillis())
                    val nextTrigger = if (prevTrigger > 0) prevTrigger + 24 * 3600 * 1000L else System.currentTimeMillis() + 24 * 3600 * 1000L
                    val recurrenceEnum = try { RecurrenceType.valueOf(recurrenceStr) } catch (e: Exception) { RecurrenceType.DAILY }

                    ReminderScheduler.scheduleReminder(
                        context = context,
                        id = reminderId,
                        triggerTimeMillis = nextTrigger,
                        title = title,
                        body = body,
                        recurrence = recurrenceEnum,
                        medicationId = medId
                    )
                }
            }
            ReminderScheduler.ACTION_MARK_TAKEN -> {
                notificationManager.cancel(reminderId)
                if (medId > 0) {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val dao = db.womanCompanionDao()
                            dao.decrementMedicationQuantity(medId)
                            dao.insertMedicationAdherenceLog(
                                MedicationAdherenceLog(
                                    medicationId = medId,
                                    scheduledTime = System.currentTimeMillis(),
                                    actualTime = System.currentTimeMillis(),
                                    status = "TAKEN"
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("ReminderReceiver", "Error recording adherence or decrementing quantity", e)
                        }
                    }
                }
            }
            ReminderScheduler.ACTION_SNOOZE -> {
                notificationManager.cancel(reminderId)
                val snoozeTime = System.currentTimeMillis() + (15 * 60 * 1000L)
                ReminderScheduler.scheduleReminder(
                    context = context,
                    id = reminderId,
                    triggerTimeMillis = snoozeTime,
                    title = title,
                    body = body,
                    recurrence = RecurrenceType.NONE,
                    medicationId = medId
                )
            }
        }
    }

    private fun showNotification(
        context: Context,
        notificationManager: NotificationManager,
        id: Int,
        title: String,
        body: String,
        medId: Int
    ) {
        val takenIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_MARK_TAKEN
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, id)
            putExtra(ReminderScheduler.EXTRA_MED_ID, medId)
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            id * 10 + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderScheduler.ACTION_SNOOZE
            putExtra(ReminderScheduler.EXTRA_REMINDER_ID, id)
            putExtra(ReminderScheduler.EXTRA_TITLE, title)
            putExtra(ReminderScheduler.EXTRA_BODY, body)
            putExtra(ReminderScheduler.EXTRA_MED_ID, medId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            id * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(0, "تم الأخذ", takenPendingIntent)
            .addAction(0, "تأجيل 15 دقيقة", snoozePendingIntent)

        notificationManager.notify(id, builder.build())
    }
}

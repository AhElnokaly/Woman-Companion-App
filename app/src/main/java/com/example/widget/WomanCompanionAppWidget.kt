package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.WaterLog
import com.example.util.AppLogger
import com.example.viewmodel.WomanCompanionCalculators
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WomanCompanionAppWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_QUICK_ADD_WATER = "com.example.widget.ACTION_QUICK_ADD_WATER"
        const val ACTION_UPDATE_WIDGETS = "com.example.widget.ACTION_UPDATE_WIDGETS"
        private const val TAG = "WomanCompanionWidget"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WomanCompanionAppWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isNotEmpty()) {
                val intent = Intent(context, WomanCompanionAppWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return
        
        if (action == ACTION_QUICK_ADD_WATER) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val dao = db.womanCompanionDao()
                    val todayCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val today = todayCal.timeInMillis
                    val existing = dao.getWaterLogForDate(today)
                    if (existing != null) {
                        dao.insertWaterLog(existing.copy(amountMl = existing.amountMl + 250))
                    } else {
                        dao.insertWaterLog(WaterLog(date = today, amountMl = 250))
                    }

                    // Update UI widgets
                    updateAllWidgets(context)

                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, "تم تسجيل كوب ماء بنجاح (+250 مل) 💧🌸", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    AppLogger.e(TAG, "Failed to log quick water from widget", e)
                }
            }
        } else if (action == ACTION_UPDATE_WIDGETS) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, WomanCompanionAppWidget::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, allWidgetIds)
        }
    }

    private suspend fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.app_widget_woman_companion)

        // 1. Setup Open App Intent (Default Dashboard - Tab 0)
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", 0)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_open_app, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.card_status, openAppPendingIntent)

        // 2. Setup Medication Intent (Symptoms & Meds - Tab 3)
        val openMedsIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", 3)
        }
        val openMedsPendingIntent = PendingIntent.getActivity(
            context,
            103,
            openMedsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.card_medication, openMedsPendingIntent)

        // 3. Setup Water Card Intent (Nutrition & Water - Tab 2)
        val openWaterIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", 2)
        }
        val openWaterPendingIntent = PendingIntent.getActivity(
            context,
            104,
            openWaterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.card_water, openWaterPendingIntent)

        // 4. Setup Quick Water Broadcast Intent (+250ml)
        val quickWaterIntent = Intent(context, WomanCompanionAppWidget::class.java).apply {
            action = ACTION_QUICK_ADD_WATER
        }
        val quickWaterPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            quickWaterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_quick_water, quickWaterPendingIntent)

        // 5. Read Database & SharedPreferences
        try {
            val sharedPrefs = context.getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
            val db = AppDatabase.getDatabase(context.applicationContext)
            val dao = db.womanCompanionDao()

            // Date Header
            val sdf = SimpleDateFormat("EEEE d MMMM", Locale.forLanguageTag("ar"))
            views.setTextViewText(R.id.widget_date, sdf.format(Date()))

            // Pregnancy / Cycle status
            val dbPregnancy = dao.getPregnancy()
            val isPregnant = dbPregnancy?.isPregnant ?: sharedPrefs.getBoolean("backup_is_pregnant", true)
            val lastPeriodDate = dbPregnancy?.lastPeriodDate ?: sharedPrefs.getLong("backup_last_period_date", 0L)
            val dueDate = dbPregnancy?.dueDate

            val progression = if (dbPregnancy != null) {
                WomanCompanionCalculators.getPregnancyProgression(dbPregnancy)
            } else {
                WomanCompanionCalculators.getPregnancyProgression(
                    lastPeriodDate = if (lastPeriodDate > 0L) lastPeriodDate else null,
                    dueDate = dueDate,
                    isPregnant = isPregnant
                )
            }

            if (isPregnant && progression != null && progression.weeks > 0) {
                val currentWeek = progression.weeks
                val daysIntoWeek = progression.daysIntoWeek
                val monthProg = WomanCompanionCalculators.calculateMonthProgress(currentWeek, daysIntoWeek)
                val monthNumber = when (monthProg.monthNumber) {
                    1 -> "الأول"
                    2 -> "الثاني"
                    3 -> "الثالث"
                    4 -> "الرابع"
                    5 -> "الخامس"
                    6 -> "السادس"
                    7 -> "السابع"
                    8 -> "الثامن"
                    9 -> "التاسع"
                    10 -> "العاشر"
                    else -> "${monthProg.monthNumber}"
                }
                val daysText = if (daysIntoWeek > 0) " + $daysIntoWeek أيام" else ""
                views.setTextViewText(R.id.widget_status_title, "🤰 الأسبوع $currentWeek$daysText (الشهر $monthNumber)")
                views.setTextViewText(R.id.widget_status_sub, "المرحلة ${progression.trimester} • حافظي على راحتكِ وترطيبكِ 💕")
            } else {
                views.setTextViewText(R.id.widget_status_title, "🌸 رفيقتكِ اليومية لصحة المرأة")
                views.setTextViewText(R.id.widget_status_sub, "تتبعي دورتكِ، لياقتكِ، وأدويتكِ بكل سهولة واطمئنان ✨")
            }

            // Next Medication
            val activeMeds = dao.getActiveMedicationsFlow().firstOrNull() ?: emptyList()
            if (activeMeds.isNotEmpty()) {
                val firstMed = activeMeds.first()
                views.setTextViewText(R.id.widget_next_med, firstMed.name)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar"))
                val medTime = if (firstMed.startDate != null) timeFormat.format(Date(firstMed.startDate)) else "يومياً"
                views.setTextViewText(R.id.widget_next_med_time, "$medTime (${firstMed.dosage ?: "جرعة"})")
            } else {
                views.setTextViewText(R.id.widget_next_med, "لا توجد أدوية")
                views.setTextViewText(R.id.widget_next_med_time, "صحة وعافية 🌸")
            }

            // Today's Water
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayLog = dao.getWaterLogForDate(todayCal.timeInMillis)
            val currentMl = todayLog?.amountMl ?: 0
            val targetMl = if (isPregnant) 2500 else 2000
            val cups = currentMl / 250

            views.setTextViewText(R.id.widget_water_amount, "$currentMl / $targetMl مل")
            views.setTextViewText(R.id.widget_water_cups, "$cups كؤوس مكتملة 🥛")

        } catch (e: Exception) {
            AppLogger.e(TAG, "Error binding widget views", e)
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

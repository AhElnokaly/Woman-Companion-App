package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.reminder.RecurrenceType
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class WeeklyDigestWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(context.applicationContext)
        val dao = db.womanCompanionDao()
        val activePreg = dao.getPregnancy()

        val sharedPrefs = context.getSharedPreferences("woman_companion_prefs", Context.MODE_PRIVATE)
        val streak = sharedPrefs.getInt("workout_streak", 0)

        val title = "ملخصكِ الأسبوعي الشخصي 🌸"
        val body = if (activePreg != null && activePreg.lastPeriodDate != null) {
            val diffMs = (System.currentTimeMillis() - activePreg.lastPeriodDate).coerceAtLeast(0L)
            val weeks = (diffMs / (7 * 24 * 60 * 60 * 1000L)).toInt().coerceIn(1, 42)
            val standard = com.example.ui.FetalStandardData.getStandardForWeek(weeks)
            "أنتِ في الأسبوع $weeks (الجنين بحجم ${standard.fruitComparison} ${standard.icon}). سلسلة نشاطكِ: $streak أيام! جوري تتمنى لكِ أسبوعاً ملؤه العافية 💖"
        } else {
            "سلسلة نشاطكِ والتزامكِ بصحتكِ هي $streak أيام! جوري فخورة بكِ وتتمنى لكِ أسبوعاً سعيداً 🌸"
        }

        ReminderScheduler.scheduleReminder(
            context = context,
            id = 50000,
            triggerTimeMillis = System.currentTimeMillis() + 1000L,
            title = title,
            body = body,
            recurrence = RecurrenceType.NONE
        )

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "WeeklyDigestWork"

        fun enqueuePeriodicWork(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WeeklyDigestWorker>(7, TimeUnit.DAYS)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

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

class MedicationMonitorWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(context.applicationContext)
        val dao = db.womanCompanionDao()
        val medications = dao.getActiveMedicationsFlow().firstOrNull() ?: emptyList()

        val now = System.currentTimeMillis()
        val sevenDaysMillis = 7 * 24 * 3600 * 1000L

        medications.forEach { med ->
            // Check low quantity
            if (med.remainingQuantity in 1..3) {
                ReminderScheduler.scheduleReminder(
                    context = context,
                    id = 20000 + med.id,
                    triggerTimeMillis = now + 1000L,
                    title = "تنبيه إعادة التعبئة 💊",
                    body = "تنبيه: الكمية المتبقية من دواء ${med.name} هي ${med.remainingQuantity} جرعات فقط!",
                    recurrence = RecurrenceType.NONE,
                    medicationId = med.id
                )
            }

            // Check expiry date
            val expiry = med.expiryDate
            if (expiry != null && (expiry - now) in 0..sevenDaysMillis) {
                ReminderScheduler.scheduleReminder(
                    context = context,
                    id = 30000 + med.id,
                    triggerTimeMillis = now + 2000L,
                    title = "تنبيه الصلاحية ⏳",
                    body = "تنبيه: دواء ${med.name} سيصل لتاريخ انتهائه قريباً!",
                    recurrence = RecurrenceType.NONE,
                    medicationId = med.id
                )
            }
        }

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "MedicationMonitorWork"

        fun enqueuePeriodicWork(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<MedicationMonitorWorker>(1, TimeUnit.DAYS)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

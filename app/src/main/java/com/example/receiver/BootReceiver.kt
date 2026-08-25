package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.reminder.ReminderScheduler
import com.example.service.StepCounterService
import com.example.worker.MedicationMonitorWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule pending medication & appointment reminders
            try {
                ReminderScheduler.rescheduleAllReminders(context)
                MedicationMonitorWorker.enqueuePeriodicWork(context)
                com.example.worker.WeeklyDigestWorker.enqueuePeriodicWork(context)
            } catch (e: Exception) {
                com.example.util.AppLogger.e("BootReceiver", "Failed to reschedule reminders on boot", e)
            }

            // Restart Step Counter Service
            val serviceIntent = Intent(context, StepCounterService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                com.example.util.AppLogger.e("BootReceiver", "Failed to restart StepCounterService on boot", e)
            }
        }
    }
}

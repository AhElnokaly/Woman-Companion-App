package com.example.reminder

import android.content.Context
import android.util.Log
import com.example.data.MedicationLog
import java.util.Calendar

const val LOW_STOCK_THRESHOLD = 3

/**
 * Checks if the medication is flagged as 'as needed' (PRN).
 */
fun isAsNeededMedication(notes: String?): Boolean {
    if (notes.isNullOrBlank()) return false
    return notes.contains("عند اللزوم") || notes.contains("AS_NEEDED")
}

/**
 * Parses the hourly interval from medication notes, or computes fallback interval from count.
 */
fun parseHourlyInterval(notes: String?, defaultTimesPerDay: Int): Int {
    val count = defaultTimesPerDay.coerceIn(1, 12)
    if (!notes.isNullOrBlank() && notes.contains("كل") && notes.contains("ساعات")) {
        val regex = Regex("""كل\s+(\d+)\s+ساعات""")
        val match = regex.find(notes)
        val extracted = match?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (extracted != null && extracted > 0) {
            return extracted
        }
    }
    return 24 / count
}

/**
 * Computes scheduled hours of the day (0..23) for daily doses.
 */
fun calculateDailyDoseHours(startHour: Int, count: Int): List<Int> {
    val safeCount = count.coerceIn(1, 12)
    val safeStartHour = (startHour % 24 + 24) % 24
    val intervalHours = 24 / safeCount
    return (0 until safeCount).map { i ->
        (safeStartHour + (i * intervalHours)) % 24
    }
}

/**
 * Checks if medication remaining quantity warrants a low-stock alert.
 */
fun isMedicationStockLow(remainingQuantity: Int, threshold: Int = LOW_STOCK_THRESHOLD): Boolean {
    return remainingQuantity in 1..threshold
}

object SmartMedicationScheduler {
    private const val TAG = "SmartMedScheduler"

    /**
     * Schedules intelligent reminders for a given medication log.
     * Takes into account the medication status, times per day, meal relationship, and recurrence pattern.
     */
    fun schedule(context: Context, med: MedicationLog) {
        if (!med.isActive) {
            cancel(context, med.id, med.timesPerDay)
            return
        }

        // Cancel existing alarms for this medication first to prevent duplicates
        cancel(context, med.id, 20)

        // Check if this is an "as needed" (PRN) medication
        if (isAsNeededMedication(med.notes)) {
            Log.d(TAG, "Medication ${med.name} is AS_NEEDED; skipping automatic scheduled alarms.")
            return
        }

        val count = med.timesPerDay.coerceIn(1, 12)
        val dosageLabel = med.dosage ?: "الجرعة المحددة"
        val safetyOrMeal = med.safetyWarning?.takeIf { it.isNotBlank() } ?: ""

        val bodyText = buildString {
            append("الجرعة: $dosageLabel")
            if (safetyOrMeal.isNotBlank()) {
                append(" • $safetyOrMeal")
            }
        }

        // Check if we have hourly interval pattern
        if (med.notes?.contains("كل") == true && med.notes.contains("ساعات")) {
            val intervalHours = parseHourlyInterval(med.notes, count)
            scheduleHourlyInterval(context, med, intervalHours, bodyText)
        } else {
            scheduleDailyDoses(context, med, count, bodyText)
        }
    }

    private fun scheduleDailyDoses(
        context: Context,
        med: MedicationLog,
        count: Int,
        bodyText: String
    ) {
        val now = System.currentTimeMillis()
        val baseStart = med.startDate ?: now

        // Extract base hour & minute from startDate if set
        val startCal = Calendar.getInstance().apply {
            timeInMillis = baseStart
        }
        val startHour = startCal.get(Calendar.HOUR_OF_DAY)
        val startMinute = startCal.get(Calendar.MINUTE)

        if (count == 1) {
            val doseCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, startHour)
                set(Calendar.MINUTE, startMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= now) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            ReminderScheduler.scheduleReminder(
                context = context,
                id = med.id * 100,
                triggerTimeMillis = doseCal.timeInMillis,
                title = "💊 حان وقت دواء: ${med.name}",
                body = "$bodyText 🌸",
                recurrence = RecurrenceType.DAILY,
                medicationId = med.id
            )
        } else {
            val hours = calculateDailyDoseHours(startHour, count)
            hours.forEachIndexed { i, hour ->
                val doseCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, startMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= now) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }

                ReminderScheduler.scheduleReminder(
                    context = context,
                    id = med.id * 100 + i,
                    triggerTimeMillis = doseCal.timeInMillis,
                    title = "💊 تذكير بدواء: ${med.name} (جرعة ${i + 1}/$count)",
                    body = "$bodyText 🌸",
                    recurrence = RecurrenceType.DAILY,
                    medicationId = med.id
                )
            }
        }
    }

    private fun scheduleHourlyInterval(
        context: Context,
        med: MedicationLog,
        intervalHours: Int,
        bodyText: String
    ) {
        val now = System.currentTimeMillis()
        val intervalMillis = intervalHours * 3600 * 1000L
        val dosesPerDay = (24 / intervalHours).coerceIn(1, 12)

        for (i in 0 until dosesPerDay) {
            val trigger = now + ((i + 1) * intervalMillis)
            ReminderScheduler.scheduleReminder(
                context = context,
                id = med.id * 100 + i,
                triggerTimeMillis = trigger,
                title = "💊 موعد دواء: ${med.name} (كل $intervalHours س)",
                body = "$bodyText 🌸",
                recurrence = RecurrenceType.DAILY,
                medicationId = med.id
            )
        }
    }

    fun cancel(context: Context, medId: Int, timesPerDay: Int = 20) {
        ReminderScheduler.cancelMedicationReminders(context, medId, timesPerDay)
    }
}

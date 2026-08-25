package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Single source of truth for all Date & Time operations across the application.
 * Prevents timezone bugs and ensures consistent day boundaries and date formatting.
 */
interface TimeProvider {
    fun currentTimeMillis(): Long
    fun calendar(timeZone: TimeZone = TimeZone.getDefault()): Calendar
    fun todayDateKey(timeZone: TimeZone = TimeZone.getDefault()): String
    fun formatDateKey(timeMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): String
    fun startOfDayMillis(timeMillis: Long = currentTimeMillis(), timeZone: TimeZone = TimeZone.getDefault()): Long
    fun endOfDayMillis(timeMillis: Long = currentTimeMillis(), timeZone: TimeZone = TimeZone.getDefault()): Long
}

object DefaultTimeProvider : TimeProvider {

    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override fun calendar(timeZone: TimeZone): Calendar {
        return Calendar.getInstance(timeZone, Locale.getDefault()).apply {
            timeInMillis = currentTimeMillis()
        }
    }

    override fun todayDateKey(timeZone: TimeZone): String {
        return formatDateKey(currentTimeMillis(), timeZone)
    }

    override fun formatDateKey(timeMillis: Long, timeZone: TimeZone): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            this.timeZone = timeZone
        }
        return sdf.format(Date(timeMillis))
    }

    override fun startOfDayMillis(timeMillis: Long, timeZone: TimeZone): Long {
        val cal = Calendar.getInstance(timeZone, Locale.getDefault())
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    override fun endOfDayMillis(timeMillis: Long, timeZone: TimeZone): Long {
        val cal = Calendar.getInstance(timeZone, Locale.getDefault())
        cal.timeInMillis = timeMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }
}

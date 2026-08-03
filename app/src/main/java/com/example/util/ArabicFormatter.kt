package com.example.util

object ArabicFormatter {
    /**
     * Formats an integer day count according to standard Arabic grammatical agreement:
     * 0 -> "0 يوم"
     * 1 -> "يوم واحد"
     * 2 -> "يومين"
     * 3..10 -> "X أيام"
     * 11+ -> "X يوم"
     */
    fun formatDays(count: Int): String {
        return when {
            count == 0 -> "0 يوم"
            count == 1 -> "يوم واحد"
            count == 2 -> "يومين"
            count in 3..10 -> "$count أيام"
            else -> "$count يوم"
        }
    }
}

fun formatArabicDays(count: Int): String = ArabicFormatter.formatDays(count)

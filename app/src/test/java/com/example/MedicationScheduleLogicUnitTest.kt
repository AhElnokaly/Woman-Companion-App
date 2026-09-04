package com.example

import com.example.reminder.LOW_STOCK_THRESHOLD
import com.example.reminder.calculateDailyDoseHours
import com.example.reminder.isAsNeededMedication
import com.example.reminder.isMedicationStockLow
import com.example.reminder.parseHourlyInterval
import org.junit.Assert.*
import org.junit.Test

class MedicationScheduleLogicUnitTest {

    @Test
    fun testCalculateDailyDoseHoursDistribution() {
        // 3 times a day starting at 8:00 AM -> 8:00, 16:00 (4 PM), 0:00 (12 AM)
        val hours3 = calculateDailyDoseHours(startHour = 8, count = 3)
        assertEquals(3, hours3.size)
        assertEquals(listOf(8, 16, 0), hours3)

        // 4 times a day starting at 6:00 AM -> 6, 12, 18, 0
        val hours4 = calculateDailyDoseHours(startHour = 6, count = 4)
        assertEquals(4, hours4.size)
        assertEquals(listOf(6, 12, 18, 0), hours4)

        // 2 times a day starting at 9:00 AM -> 9, 21 (9 PM)
        val hours2 = calculateDailyDoseHours(startHour = 9, count = 2)
        assertEquals(2, hours2.size)
        assertEquals(listOf(9, 21), hours2)
    }

    @Test
    fun testParseHourlyIntervalFromNotes() {
        // Explicit "كل 6 ساعات"
        val interval6 = parseHourlyInterval(notes = "تناول كبسولة كل 6 ساعات بعد الأكل", defaultTimesPerDay = 1)
        assertEquals(6, interval6)

        // Explicit "كل 8 ساعات"
        val interval8 = parseHourlyInterval(notes = "كل 8 ساعات بانتظام", defaultTimesPerDay = 3)
        assertEquals(8, interval8)

        // No explicit hourly phrase -> fallback to 24 / defaultTimesPerDay
        val fallbackInterval = parseHourlyInterval(notes = "يومياً صباحاً ومساءً", defaultTimesPerDay = 2)
        assertEquals(12, fallbackInterval)
    }

    @Test
    fun testIsAsNeededMedicationDetection() {
        // Regular scheduled medications
        assertFalse(isAsNeededMedication("حبة يومياً صباحاً"))
        assertFalse(isAsNeededMedication(null))
        assertFalse(isAsNeededMedication(""))

        // PRN / As-needed medications
        assertTrue(isAsNeededMedication("قرص مسكن عند اللزوم للصداع"))
        assertTrue(isAsNeededMedication("AS_NEEDED - 1 tablet"))
    }

    @Test
    fun testIsMedicationStockLowThreshold() {
        // Standard threshold is LOW_STOCK_THRESHOLD (3)
        assertEquals(3, LOW_STOCK_THRESHOLD)

        // Zero or negative remaining quantity is handled separately as empty stock
        assertFalse(isMedicationStockLow(remainingQuantity = 0))

        // 1..3 remaining is low stock warning
        assertTrue(isMedicationStockLow(remainingQuantity = 1))
        assertTrue(isMedicationStockLow(remainingQuantity = 2))
        assertTrue(isMedicationStockLow(remainingQuantity = 3))

        // 4 or more is sufficient stock
        assertFalse(isMedicationStockLow(remainingQuantity = 4))
        assertFalse(isMedicationStockLow(remainingQuantity = 30))
    }
}

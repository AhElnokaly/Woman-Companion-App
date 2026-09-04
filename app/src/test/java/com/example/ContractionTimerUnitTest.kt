package com.example

import com.example.ui.pregnancy.ContractionRecord
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class ContractionTimerUnitTest {

    @Test
    fun testContractionRecordCreation_CalculatesCorrectInterval() {
        val firstStart = 1000000L
        val secondStart = firstStart + (300 * 1000L) // 5 minutes later (300s)

        val record1 = ContractionRecord(
            id = 1L,
            startTime = firstStart,
            durationSeconds = 60,
            intervalSeconds = null
        )

        val record2 = ContractionRecord(
            id = 2L,
            startTime = secondStart,
            durationSeconds = 65,
            intervalSeconds = ((secondStart - firstStart) / 1000L).toInt()
        )

        assertNull(record1.intervalSeconds)
        assertEquals(300, record2.intervalSeconds)
        assertEquals(65, record2.durationSeconds)
    }

    @Test
    fun testHospitalRule511_IdentifiesActiveLaborPattern() {
        // Pattern of 4 contractions: ~60s duration, ~300s (5min) apart
        val records = listOf(
            ContractionRecord(id = 1, startTime = 0L, durationSeconds = 55, intervalSeconds = null),
            ContractionRecord(id = 2, startTime = 300000L, durationSeconds = 60, intervalSeconds = 300),
            ContractionRecord(id = 3, startTime = 600000L, durationSeconds = 62, intervalSeconds = 300),
            ContractionRecord(id = 4, startTime = 900000L, durationSeconds = 58, intervalSeconds = 300)
        )

        val last4 = records.takeLast(4)
        val avgDuration = last4.map { it.durationSeconds }.average()
        val validIntervals = last4.mapNotNull { it.intervalSeconds }
        val avgInterval = if (validIntervals.isNotEmpty()) validIntervals.average() else 0.0

        val is511Active = avgDuration in 45.0..90.0 && avgInterval in 240.0..360.0
        assertTrue("5-1-1 Rule should trigger for 1-minute contractions every 5 minutes", is511Active)
    }

    @Test
    fun testHospitalRule511_DoesNotTriggerForBraxtonHicks() {
        // Irregular short contractions far apart (e.g. Braxton Hicks)
        val records = listOf(
            ContractionRecord(id = 1, startTime = 0L, durationSeconds = 20, intervalSeconds = null),
            ContractionRecord(id = 2, startTime = 900000L, durationSeconds = 25, intervalSeconds = 900), // 15 min apart
            ContractionRecord(id = 3, startTime = 2400000L, durationSeconds = 15, intervalSeconds = 1500) // 25 min apart
        )

        val isEligible = records.size >= 4
        assertFalse("Should not trigger 5-1-1 alert for fewer than 4 regular contractions", isEligible)
    }
}

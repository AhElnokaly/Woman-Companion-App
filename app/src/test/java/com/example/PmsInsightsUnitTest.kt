package com.example

import com.example.data.PeriodLog
import com.example.ui.period.computeTopPmsSymptoms
import org.junit.Assert.*
import org.junit.Test

class PmsInsightsUnitTest {

    @Test
    fun testPmsSymptomFrequencyExtraction() {
        val logs = listOf(
            PeriodLog(id = 1, startDate = 1000L, flowIntensity = "medium", symptoms = "مغص, صداع, تعب", painLevel = 4),
            PeriodLog(id = 2, startDate = 2000L, flowIntensity = "medium", symptoms = "مغص, تقلب مزاجي, صداع", painLevel = 5),
            PeriodLog(id = 3, startDate = 3000L, flowIntensity = "heavy", symptoms = "مغص, انتفاخ", painLevel = 6)
        )

        val topSymptoms = computeTopPmsSymptoms(logs, limit = 2)

        assertEquals(2, topSymptoms.size)
        assertEquals("مغص", topSymptoms[0]) // Appeared 3 times
        assertEquals("صداع", topSymptoms[1]) // Appeared 2 times
    }

    @Test
    fun testPmsSymptomFallbackWhenEmpty() {
        val emptyLogs = emptyList<PeriodLog>()
        val defaultSymptoms = computeTopPmsSymptoms(emptyLogs, limit = 3)
        assertEquals(3, defaultSymptoms.size)
        assertTrue(defaultSymptoms.contains("تقلب مزاجي خفيف"))
    }
}

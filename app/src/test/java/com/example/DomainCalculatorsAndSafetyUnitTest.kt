package com.example

import com.example.data.*
import com.example.ui.symptoms.getBpStatus
import com.example.util.DefaultTimeProvider
import com.example.viewmodel.WomanCompanionCalculators
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DomainCalculatorsAndSafetyUnitTest {

    @Test
    fun testGetBpStatus_StandardUser_120_80_IsNormalAndIdeal() {
        val status = getBpStatus(systolic = 120, diastolic = 80, isChronicLowBp = false)
        assertTrue(status.label.contains("مثالي وطبيعي"))
        assertFalse(status.label.contains("مرحلة ١"))
        assertFalse(status.label.contains("ما قبل الارتفاع"))
    }

    @Test
    fun testGetBpStatus_ChronicLowBpUser_120_80_IsIdealAndBalanced() {
        val status = getBpStatus(systolic = 120, diastolic = 80, isChronicLowBp = true)
        assertTrue(status.label.contains("مثالي ومتوازن"))
        assertTrue(status.advice.contains("دورتكِ الدموية نشطة"))
    }

    @Test
    fun testGetBpStatus_ChronicLowBpUser_LowReading_ProvidesAppropriateAdvice() {
        val status = getBpStatus(systolic = 85, diastolic = 55, isChronicLowBp = true)
        assertTrue(status.label.contains("منخفض"))
        assertTrue(status.advice.contains("الوقوف المفاجئ"))
    }

    @Test
    fun testGetBpStatus_Stage1AndStage2Hypertension() {
        val stage1 = getBpStatus(systolic = 135, diastolic = 85, isChronicLowBp = false)
        assertTrue(stage1.label.contains("مرحلة ١"))

        val stage2 = getBpStatus(systolic = 145, diastolic = 95, isChronicLowBp = false)
        assertTrue(stage2.label.contains("مرحلة ٢"))
    }

    @Test
    fun testCycleStats_DefaultWhenInsufficientLogs() {
        val emptyLogs = emptyList<PeriodLog>()
        val stats = WomanCompanionCalculators.getCycleStats(emptyLogs)
        assertEquals(28, stats.averageCycleLength)
        assertEquals(5, stats.averagePeriodDuration)
        assertTrue(stats.isEstimated)
        assertEquals(0, stats.totalCycles)
    }

    @Test
    fun testCycleStats_CalculatesAccurateAveragesFromMultipleLogs() {
        val now = 1700000000000L
        val dayMs = 86400000L

        // 3 consecutive cycles: 28 days apart, then 30 days apart
        val log1 = PeriodLog(id = 1, startDate = now, endDate = now + 5 * dayMs, flowIntensity = "medium", symptoms = "Cramps", painLevel = 3)
        val log2 = PeriodLog(id = 2, startDate = now + 28 * dayMs, endDate = now + 28 * dayMs + 6 * dayMs, flowIntensity = "heavy", symptoms = "Fatigue", painLevel = 4)
        val log3 = PeriodLog(id = 3, startDate = now + 58 * dayMs, endDate = now + 58 * dayMs + 4 * dayMs, flowIntensity = "light", symptoms = "", painLevel = 2)

        val stats = WomanCompanionCalculators.getCycleStats(listOf(log1, log2, log3))
        assertEquals(29, stats.averageCycleLength) // (28 + 30) / 2 = 29
        assertEquals(5, stats.averagePeriodDuration) // (5 + 6 + 4) / 3 = 5
        assertFalse(stats.isEstimated)
        assertEquals(2, stats.totalCycles)
    }

    @Test
    fun testCyclePhase_AccuratePhasesClassification() {
        val now = 1700000000000L
        val dayMs = 86400000L

        val log = PeriodLog(id = 1, startDate = now, endDate = now + 5 * dayMs, flowIntensity = "medium", symptoms = "None", painLevel = 2)
        val logs = listOf(log)

        // Day 2 (Menstruation)
        val mPhase = WomanCompanionCalculators.getCurrentCyclePhase(logs, currentTime = now + 2 * dayMs)
        assertEquals("Menstruation", mPhase.phaseName)
        assertFalse(mPhase.isLate)

        // Day 8 (Follicular)
        val fPhase = WomanCompanionCalculators.getCurrentCyclePhase(logs, currentTime = now + 8 * dayMs)
        assertEquals("Follicular", fPhase.phaseName)

        // Day 14 (Ovulation)
        val oPhase = WomanCompanionCalculators.getCurrentCyclePhase(logs, currentTime = now + 14 * dayMs)
        assertEquals("Ovulation", oPhase.phaseName)

        // Day 22 (Luteal)
        val lPhase = WomanCompanionCalculators.getCurrentCyclePhase(logs, currentTime = now + 22 * dayMs)
        assertEquals("Luteal", lPhase.phaseName)

        // Day 36 (Late > 7 days past 28 day cycle)
        val latePhase = WomanCompanionCalculators.getCurrentCyclePhase(logs, currentTime = now + 36 * dayMs)
        assertEquals("Late", latePhase.phaseName)
        assertTrue(latePhase.isLate)
        assertEquals(8, latePhase.daysInPhase) // 36 - 28 = 8 days delay
    }

    @Test
    fun testDetectCycleIrregularityPatterns_DetectsSeverePainAndHeavyBleeding() {
        val now = 1700000000000L
        val dayMs = 86400000L

        val heavyPainLogs = listOf(
            PeriodLog(id = 1, startDate = now, endDate = now + 5 * dayMs, flowIntensity = "غزير", symptoms = "ألم حاد", painLevel = 9),
            PeriodLog(id = 2, startDate = now + 28 * dayMs, endDate = now + 33 * dayMs, flowIntensity = "heavy", symptoms = "مغص", painLevel = 8),
            PeriodLog(id = 3, startDate = now + 56 * dayMs, endDate = now + 61 * dayMs, flowIntensity = "كثيف", symptoms = "تعب", painLevel = 10)
        )

        val notices = WomanCompanionCalculators.detectCycleIrregularityPatterns(heavyPainLogs, emptyList())
        val noticeIds = notices.map { it.id }

        assertTrue("Should flag heavy bleeding pattern", noticeIds.contains("heavy_bleeding"))
        assertTrue("Should flag severe pain pattern", noticeIds.contains("severe_pain"))
    }

    @Test
    fun testSymptomTriage_FlagsCriticalEmergencyAccurately() {
        val emergencyAlert = SymptomTriage.checkUrgentSymptoms(
            input = "نزيف مهبلي مفاجئ مع ألم شديد في البطن",
            isPregnant = true
        )
        assertNotNull("Should detect emergency for bleeding during pregnancy", emergencyAlert)
        assertEquals(SymptomTriage.UrgencyLevel.CRITICAL, emergencyAlert?.urgencyLevel)
        assertTrue(emergencyAlert?.title?.contains("تنبيه طبي عاجل") == true)
    }

    @Test
    fun testSymptomTriage_FlagsWarningForPreeclampsiaSigns() {
        val warningAlert = SymptomTriage.checkUrgentSymptoms(
            input = "صداع شديد وزغللة في العين مع تورم",
            isPregnant = true
        )
        assertNotNull("Should detect alert for severe headache and vision changes", warningAlert)
        assertTrue(warningAlert?.message?.contains("ضغط الدم") == true || warningAlert?.title?.contains("ضغط الدم") == true)
    }

    @Test
    fun testPregnancyProgression_CalculatesTrimestersAndWeeks() {
        val lmp = 1700000000000L
        val dayMs = 86400000L
        val pregnancy = PregnancyEntity(
            id = 1,
            isPregnant = true,
            lastPeriodDate = lmp,
            dueDate = lmp + 280 * dayMs
        )

        // At 10 weeks
        val prog10 = WomanCompanionCalculators.getPregnancyProgression(pregnancy, currentTime = lmp + 70 * dayMs)
        assertNotNull(prog10)
        assertEquals(10, prog10?.weeks)
        assertEquals(1, prog10?.trimester)

        // At 20 weeks
        val prog20 = WomanCompanionCalculators.getPregnancyProgression(pregnancy, currentTime = lmp + 140 * dayMs)
        assertNotNull(prog20)
        assertEquals(20, prog20?.weeks)
        assertEquals(2, prog20?.trimester)

        // At 32 weeks
        val prog32 = WomanCompanionCalculators.getPregnancyProgression(pregnancy, currentTime = lmp + 224 * dayMs)
        assertNotNull(prog32)
        assertEquals(32, prog32?.weeks)
        assertEquals(3, prog32?.trimester)
    }

    @Test
    fun testTimeProvider_ProducesConsistentDateKeysAndBoundaries() {
        val timeZone = TimeZone.getTimeZone("UTC")
        val timestamp = 1700000000000L // 2023-11-14 22:13:20 UTC

        val dateKey = DefaultTimeProvider.formatDateKey(timestamp, timeZone)
        assertEquals("2023-11-14", dateKey)

        val startOfDay = DefaultTimeProvider.startOfDayMillis(timestamp, timeZone)
        val endOfDay = DefaultTimeProvider.endOfDayMillis(timestamp, timeZone)

        assertTrue(startOfDay <= timestamp)
        assertTrue(endOfDay >= timestamp)
        assertEquals(86399999L, endOfDay - startOfDay)
    }
}

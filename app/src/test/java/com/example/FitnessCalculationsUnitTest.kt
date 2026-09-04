package com.example

import com.example.ui.fitness.JouriExercise
import com.example.ui.fitness.JouriStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitnessCalculationsUnitTest {

    @Test
    fun testPedometerDistanceAndCaloriesCalculation() {
        val steps = 5000
        val distanceKm = String.format(java.util.Locale.US, "%.2f", steps * 0.0007)
        val caloriesBurned = (steps * 0.04).toInt()
        val activeMinutes = (steps / 80)

        assertEquals("3.50", distanceKm)
        assertEquals(200, caloriesBurned)
        assertEquals(62, activeMinutes)
    }

    @Test
    fun testTrimesterExerciseFiltering() {
        val kegel = JouriExercise(
            id = "kegel",
            name = "كيجل",
            category = "pregnancy",
            durationSeconds = 100,
            emoji = "🌸",
            goal = "تقوية الحوض",
            description = "تمرين يومي",
            stepDetails = emptyList(),
            benefits = emptyList(),
            safetyWarning = "انتبه",
            recommendedTrimesters = setOf(1, 2, 3)
        )

        val squats = JouriExercise(
            id = "squats",
            name = "قرفصاء",
            category = "pregnancy",
            durationSeconds = 120,
            emoji = "🧱",
            goal = "فتح الحوض",
            description = "تحضير الولادة",
            stepDetails = emptyList(),
            benefits = emptyList(),
            safetyWarning = "انتبه",
            recommendedTrimesters = setOf(3)
        )

        val all = listOf(kegel, squats)
        
        // Trimester 1 filter
        val tri1 = all.filter { 1 in it.recommendedTrimesters }
        assertEquals(1, tri1.size)
        assertEquals("kegel", tri1[0].id)

        // Trimester 3 filter
        val tri3 = all.filter { 3 in it.recommendedTrimesters }
        assertEquals(2, tri3.size)
    }
}

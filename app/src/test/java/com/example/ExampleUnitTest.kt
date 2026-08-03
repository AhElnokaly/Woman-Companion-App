package com.example

import com.example.ui.FetalStandardData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun fetalWeightDeviation_calculationAndReplacement() {
    // Week 20 standard weight = 300.0g
    val log1Week = 20
    val log1Weight = 330.0 // +10% deviation
    val dev1 = FetalStandardData.calculateWeightDeviation(log1Week, log1Weight)
    assertEquals(10.0, dev1, 0.001)

    // Estimate for Week 25 (standard = 660.0g) with +10%
    val est1 = FetalStandardData.getEstimatedWeightGrams(25, dev1)
    assertEquals(726.0, est1, 0.001)

    // Second log entry replaces first completely (-5% deviation)
    val log2Week = 22 // standard = 430.0g
    val log2Weight = 408.5 // 408.5 - 430 = -21.5 / 430 = -5%
    val dev2 = FetalStandardData.calculateWeightDeviation(log2Week, log2Weight)
    assertEquals(-5.0, dev2, 0.001)

    // Estimate for Week 25 with -5% (must be 627.0g, not averaged with +10%)
    val est2 = FetalStandardData.getEstimatedWeightGrams(25, dev2)
    assertEquals(627.0, est2, 0.001)
  }

  @Test
  fun fetalWeightDeviation_resetOnNewPregnancy() {
    val oldPregnancyLmp = 1000000000000L // Old pregnancy timestamp
    val oldLogDate = 1005000000000L
    val oldLogs = listOf(
        com.example.data.FetalGrowthLog(
            id = 1,
            date = oldLogDate,
            pregnancyWeek = 20,
            weightGrams = 400.0, // +33.3% deviation
            lengthCm = 25.0
        )
    )

    // When starting a new pregnancy with a new LMP timestamp (e.g. much later)
    val newPregnancyLmp = 1700000000000L
    val cutoff = newPregnancyLmp - (14 * 86400000L)

    val validLogsForNewPregnancy = oldLogs.filter { it.date >= cutoff }
    assertTrue(validLogsForNewPregnancy.isEmpty())

    // Without logs in current pregnancy, estimated weight reverts to standard table value
    val devForNewPregnancy: Double? = if (validLogsForNewPregnancy.isNotEmpty()) {
        val l = validLogsForNewPregnancy.maxByOrNull { it.date }!!
        FetalStandardData.calculateWeightDeviation(l.pregnancyWeek, l.weightGrams)
    } else null

    assertNull(devForNewPregnancy)
    val currentEstimatedWeight = FetalStandardData.getEstimatedWeightGrams(20, devForNewPregnancy)
    // Week 20 standard weight is 300.0g (pure standard value)
    assertEquals(300.0, currentEstimatedWeight, 0.001)
  }
}

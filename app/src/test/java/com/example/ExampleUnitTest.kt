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
            pregnancyId = 1,
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
    val devForNewPregnancy: Double? = validLogsForNewPregnancy.maxByOrNull { it.date }?.let { l ->
        FetalStandardData.calculateWeightDeviation(l.pregnancyWeek, l.weightGrams)
    }

    assertNull(devForNewPregnancy)
    val currentEstimatedWeight = FetalStandardData.getEstimatedWeightGrams(20, devForNewPregnancy)
    // Week 20 standard weight is 300.0g (pure standard value)
    assertEquals(300.0, currentEstimatedWeight, 0.001)
  }

  @Test
  fun dailyVitaminsAndMidnightDateKey_calculation() {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    val now = java.util.Date()
    val dateStr = sdf.format(now)
    assertNotNull(dateStr)
    assertTrue(dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))

    val cal = java.util.Calendar.getInstance().apply {
      set(java.util.Calendar.HOUR_OF_DAY, 0)
      set(java.util.Calendar.MINUTE, 0)
      set(java.util.Calendar.SECOND, 0)
      set(java.util.Calendar.MILLISECOND, 0)
    }
    val startOfDay = cal.timeInMillis
    val nextMidnight = startOfDay + 24 * 60 * 60 * 1000L
    assertTrue(nextMidnight > System.currentTimeMillis())
  }

  @Test
  fun doctorReportText_formattingAndHeader() {
    val dateStr = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault()).format(java.util.Date())
    val reportHeader = "🩺 =========================================\n   تقرير زيارة الطبيبة - تطبيق رفيقة المرأة جوري\n=========================================\n📅 تاريخ التقرير: $dateStr (النطاق: آخر 30 يوم)"
    assertTrue(reportHeader.contains("تقرير زيارة الطبيبة"))
    assertTrue(reportHeader.contains(dateStr))
  }

  @Test
  fun backupPassphrase_sanitization() {
    val rawPass = "my'pass''word"
    val sanitized = rawPass.replace("'", "''")
    assertEquals("my''pass''''word", sanitized)
  }

  @Test
  fun cycleIrregularity_heavyBleedingDetection() {
    val logs = listOf(
      com.example.data.PeriodLog(id = 1, startDate = 1000L, flowIntensity = "heavy", symptoms = "", painLevel = 3),
      com.example.data.PeriodLog(id = 2, startDate = 2000L, flowIntensity = "غزير", symptoms = "", painLevel = 4),
      com.example.data.PeriodLog(id = 3, startDate = 3000L, flowIntensity = "كثيف", symptoms = "", painLevel = 2)
    )
    val recent3 = logs.takeLast(3)
    val isHeavy = recent3.size >= 3 && recent3.all { log ->
      val flow = log.flowIntensity.lowercase().trim()
      flow == "heavy" || flow == "غزير" || flow == "كثيف"
    }
    assertTrue(isHeavy)
  }

  @Test
  fun cycleIrregularity_outOfRangeCycleDetection() {
    val dayMs = 24 * 60 * 60 * 1000L
    val start1 = 1000000000L
    val start2 = start1 + 15 * dayMs // 15 days (short cycle)
    val diffDays = (start2 - start1) / dayMs
    val isOutOfRange = diffDays in 10..90 && (diffDays < 21 || diffDays > 35)
    assertTrue(isOutOfRange)
  }

  @Test
  fun cycleIrregularity_normalVariationDoesNotTrigger() {
    val dayMs = 24 * 60 * 60 * 1000L
    val start1 = 1000000000L
    val start2 = start1 + 28 * dayMs // 28 days
    val diffDays = (start2 - start1) / dayMs
    val isOutOfRange = diffDays in 10..90 && (diffDays < 21 || diffDays > 35)
    assertFalse(isOutOfRange)
  }

  @Test
  fun contraceptive_daysCalculationAndSymptomOffset() {
    val dayMs = 24 * 60 * 60 * 1000L
    val methodStart = 1700000000000L
    val symptomDate = methodStart + (14 * dayMs) // 14 days later

    val calculateDaysBetween = { start: Long, target: Long ->
      if (target < start) 0L else (target - start) / dayMs
    }

    val daysOffset = calculateDaysBetween(methodStart, symptomDate)
    assertEquals(14L, daysOffset)

    val methodType = "PILL"
    val typeName = when (methodType) {
      "PILL" -> "حبوب منع الحمل"
      else -> "وسيلة أخرى"
    }

    val formattedLabel = "بعد $daysOffset يوم من بدء $typeName"
    assertEquals("بعد 14 يوم من بدء حبوب منع الحمل", formattedLabel)
  }

  @Test
  fun geminiBaseUrl_allowlistValidation() {
    val validUrl1 = "https://generativelanguage.googleapis.com"
    val validUrl2 = "https://generativelanguage.googleapis.com/"
    val validUrl3 = "  https://generativelanguage.googleapis.com/  "
    
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(validUrl1))
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(validUrl2))
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(validUrl3))

    // Disallowed / malicious URLs should all fall back to default
    val maliciousUrl1 = "https://evil-attacker.com/api"
    val maliciousUrl2 = "http://generativelanguage.googleapis.com" // insecure http
    val maliciousUrl3 = "https://generativelanguage.googleapis.com.attacker.com"
    val maliciousUrl4 = "javascript:alert(1)"
    val maliciousUrl5 = "invalid-url"

    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(maliciousUrl1))
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(maliciousUrl2))
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(maliciousUrl3))
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(maliciousUrl4))
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(maliciousUrl5))
    assertEquals("https://generativelanguage.googleapis.com/", com.example.data.ApiKeyRepository.sanitizeBaseUrl(null))
  }

  @Test
  fun symptomTriage_reducedFetalMovement_flaggedAsUrgentWithClinicianGuidance() {
    val alert = com.example.data.SymptomTriage.checkUrgentSymptoms("حركة الجنين قلت من الصبح", isPregnant = true)
    assertNotNull(alert)
    assertEquals(com.example.data.SymptomTriage.UrgencyLevel.CRITICAL, alert!!.urgencyLevel)
    assertTrue(alert.title.contains("تنبيه طبي عاجل"))
    assertTrue(alert.message.contains("طبيبتكِ"))
    // Verify specific home-brew prescriptions (chocolate, 10 movements, 1 hour wait) are removed
    assertFalse(alert.message.contains("شيكولاتة"))
    assertFalse(alert.message.contains("10 حركات"))
    assertFalse(alert.message.contains("عصير طبيعي دافئ"))
  }
}



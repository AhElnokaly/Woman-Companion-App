package com.example.viewmodel

import com.example.data.*
import com.example.ui.FetalStandardData
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Data Classes for Domain Calculations
data class CycleStats(
    val averageCycleLength: Int,
    val averagePeriodDuration: Int,
    val totalCycles: Int,
    val isEstimated: Boolean
)

data class CyclePhaseInfo(
    val phaseName: String,
    val phaseArabic: String,
    val daysInPhase: Int,
    val progressFraction: Float,
    val description: String,
    val isLate: Boolean = false
)

data class PregnancyProgression(
    val weeks: Int,
    val daysIntoWeek: Int,
    val remainingDays: Int,
    val trimester: Int,
    val dueDate: Long,
    val comparisonName: String,
    val comparisonIcon: String,
    val developmentTip: String
)

data class MonthProgress(
    val monthNumber: Int,
    val monthName: String,
    val progressFraction: Float,
    val totalMonths: Int
)

data class FetalComparison(
    val name: String,
    val icon: String,
    val developmentTip: String
)

data class CalorieGoal(
    val target: Int,
    val details: String
)

data class NutrientTarget(
    val name: String,
    val targetVal: Double,
    val unit: String,
    val isLimit: Boolean = false
)

object WomanCompanionCalculators {

    fun calculateAge(birthDateMs: Long): Int {
        val birthCal = Calendar.getInstance()
        birthCal.timeInMillis = birthDateMs
        val todayCal = Calendar.getInstance()
        var calculatedAge = todayCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        if (todayCal.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
            calculatedAge--
        }
        return calculatedAge.coerceAtLeast(0)
    }

    fun calculateDaysBetween(startDateMs: Long, targetDateMs: Long): Long {
        if (targetDateMs < startDateMs) return 0L
        return (targetDateMs - startDateMs) / (24 * 60 * 60 * 1000L)
    }

    fun getContraceptiveTypeName(type: String): String {
        return when (type) {
            "PILL" -> "حبوب منع الحمل"
            "HORMONAL_IUD" -> "اللولب الهرموني"
            "COPPER_IUD" -> "اللولب النحاسي"
            "INJECTION" -> "حقنة منع الحمل"
            "IMPLANT" -> "شريحة منع الحمل"
            "CONDOM" -> "الواقي الذكري"
            else -> "وسيلة أخرى"
        }
    }

    fun getCycleStats(periodLogs: List<PeriodLog>): CycleStats {
        val logs = periodLogs.sortedBy { it.startDate }
        if (logs.size < 2) {
            return CycleStats(averageCycleLength = 28, averagePeriodDuration = 5, totalCycles = logs.size, isEstimated = true)
        }

        var totalCycleLength = 0L
        var cycleCount = 0
        for (i in 1 until logs.size) {
            val diffMs = logs[i].startDate - logs[i-1].startDate
            val diffDays = diffMs / (24 * 60 * 60 * 1000)
            if (diffDays in 15..50) {
                totalCycleLength += diffDays
                cycleCount++
            }
        }

        var totalPeriodDuration = 0L
        var validDurationCount = 0
        for (log in logs) {
            if (log.endDate != null) {
                val durDays = (log.endDate - log.startDate) / (24 * 60 * 60 * 1000)
                if (durDays in 1..15) {
                    totalPeriodDuration += durDays
                    validDurationCount++
                }
            }
        }

        val avgCycle = if (cycleCount > 0) (totalCycleLength / cycleCount).toInt() else 28
        val avgDuration = if (validDurationCount > 0) (totalPeriodDuration / validDurationCount).toInt() else 5

        return CycleStats(
            averageCycleLength = avgCycle,
            averagePeriodDuration = avgDuration,
            totalCycles = cycleCount,
            isEstimated = cycleCount == 0
        )
    }

    fun detectCycleIrregularityPatterns(
        periodLogs: List<PeriodLog>,
        symptomLogs: List<SymptomLog>
    ): List<IrregularityNotice> {
        val notices = mutableListOf<IrregularityNotice>()
        val logs = periodLogs.sortedBy { it.startDate }

        if (logs.size >= 4) {
            val cycleLengths = mutableListOf<Long>()
            for (i in 1 until logs.size) {
                val diffDays = (logs[i].startDate - logs[i - 1].startDate) / (24 * 60 * 60 * 1000)
                if (diffDays in 10..90) {
                    cycleLengths.add(diffDays)
                }
            }
            if (cycleLengths.size >= 3) {
                val recent3 = cycleLengths.takeLast(3)
                val olderCycles = cycleLengths.dropLast(3)
                val baseline = if (olderCycles.isNotEmpty()) olderCycles.average().toInt() else getCycleStats(periodLogs).averageCycleLength
                if (recent3.all { kotlin.math.abs(it - baseline) > 7 }) {
                    notices.add(
                        IrregularityNotice(
                            id = "length_deviation",
                            title = "ملاحظة حول انتظام طول الدورة 🌸",
                            message = "لوحظ تباين في أطوال الدورات الأخيرة مقارنة بالمعدل الشخصي. يُنصح بمشاركة هذه الملاحظة مع الطبيبة للاطمئنان."
                        )
                    )
                }
            }
        }

        val recentLogs = logs.takeLast(3)
        if (recentLogs.size >= 3 && recentLogs.all { log ->
                val flow = log.flowIntensity.lowercase().trim()
                flow == "heavy" || flow == "غزير" || flow == "كثيف"
            }) {
            notices.add(
                IrregularityNotice(
                    id = "heavy_bleeding",
                    title = "ملاحظة حول كثافة التدفق 🩸",
                    message = "لوحظ استمرار التدفق الغزير في آخر 3 دورات مسجلة. يُفضل استشارة الطبيبة للاطمئنان ومتابعة مستوى صبغة الدم والحديد."
                )
            )
        }

        if (logs.size >= 2) {
            val lastCycleDays = (logs.last().startDate - logs[logs.size - 2].startDate) / (24 * 60 * 60 * 1000)
            if (lastCycleDays in 10..90 && (lastCycleDays < 21 || lastCycleDays > 35)) {
                notices.add(
                    IrregularityNotice(
                        id = "out_of_range_length",
                        title = "ملاحظة حول مدة الدورة ⏱️",
                        message = "لوحظ أن طول الدورة الأخيرة ($lastCycleDays يوماً) خارج النطاق الشائع المعتاد (21-35 يوماً). يُنصح بمتابعة ذلك مع طبيبتكِ."
                    )
                )
            }
        }

        val highPainLogsCount = logs.count { it.painLevel >= 8 }
        val highPainSymptomsCount = symptomLogs.count { it.severity >= 8 && (it.symptom.contains("ألم") || it.symptom.contains("مغص") || it.symptom.contains("pain") || it.symptom.contains("cramp")) }
        if (highPainLogsCount >= 3 || highPainSymptomsCount >= 3) {
            notices.add(
                IrregularityNotice(
                    id = "severe_pain",
                    title = "ملاحظة حول ألم الدورة 🌿",
                    message = "تم تسجيل آلام شديدة متكررة في عدة دورات. نقترح مناقشة خيارات التخفيف الآمنة والمناسبة مع طبيبتكِ المعالجة."
                )
            )
        }

        return notices
    }

    fun getCurrentCyclePhase(
        periodLogs: List<PeriodLog>,
        currentTime: Long = System.currentTimeMillis()
    ): CyclePhaseInfo {
        val stats = getCycleStats(periodLogs)
        val logs = periodLogs.sortedByDescending { it.startDate }
        if (logs.isEmpty()) {
            return CyclePhaseInfo(
                phaseName = "غير محدد",
                phaseArabic = "بيانات غير كافية",
                daysInPhase = 0,
                progressFraction = 0f,
                description = "سجّلي أول دورة شهرية لبدء التتبع التنبؤي الذكي للخصوبة والأطوار."
            )
        }

        val lastLog = logs.first()
        val daysSinceStart = ((currentTime - lastLog.startDate) / (24 * 60 * 60 * 1000)).toInt().coerceAtLeast(0)

        val delayDays = daysSinceStart - stats.averageCycleLength
        if (delayDays >= 7) {
            return CyclePhaseInfo(
                phaseName = "Late",
                phaseArabic = "متأخرة عن موعدها ⚠️",
                daysInPhase = delayDays,
                progressFraction = 1f,
                description = "الدورة متأخرة بـ $delayDays أيام عن متوسط دورتك المعتاد. هل هناك احتمال للحمل؟ يمكنكِ إجراء اختبار منزلي وتحديث حالتكِ.",
                isLate = true
            )
        }

        val cycleLength = stats.averageCycleLength
        val duration = stats.averagePeriodDuration

        return when {
            daysSinceStart < duration -> {
                CyclePhaseInfo(
                    phaseName = "Menstruation",
                    phaseArabic = "طمث / حيض 🩸",
                    daysInPhase = daysSinceStart + 1,
                    progressFraction = (daysSinceStart + 1).toFloat() / duration.toFloat(),
                    description = "أنتِ الآن في طور الحيض. ركّزي على الراحة، اشربي سوائل دافئة، والعبادات معفاة منها حالياً."
                )
            }
            daysSinceStart < (cycleLength - 16) -> {
                CyclePhaseInfo(
                    phaseName = "Follicular",
                    phaseArabic = "الطور الجريبي 🌱",
                    daysInPhase = daysSinceStart - duration + 1,
                    progressFraction = (daysSinceStart - duration + 1).toFloat() / (cycleLength - 16 - duration).toFloat().coerceAtLeast(1f),
                    description = "يبدأ الجسم بالاستعداد لإنتاج البويضة. طاقة أعلى وتألق مستمر."
                )
            }
            daysSinceStart <= (cycleLength - 12) -> {
                val ovulationDays = daysSinceStart - (cycleLength - 16) + 1
                CyclePhaseInfo(
                    phaseName = "Ovulation",
                    phaseArabic = "طور الإباضة ✨",
                    daysInPhase = ovulationDays,
                    progressFraction = ovulationDays.toFloat() / 5f,
                    description = "فترة الخصوبة العالية والتبويض. مثالية لمتابعة الخصوبة وفرص الحمل."
                )
            }
            else -> {
                val lutealDays = daysSinceStart - (cycleLength - 12) + 1
                CyclePhaseInfo(
                    phaseName = "Luteal",
                    phaseArabic = "الطور اللوتيني 🪵",
                    daysInPhase = lutealDays,
                    progressFraction = lutealDays.toFloat() / 12f,
                    description = "فترة ما قبل الدورة التالية. قد تظهر بعض أعراض متلازمة ما قبل الطمث. كوني لطيفة مع نفسك."
                )
            }
        }
    }

    fun getPregnancyProgression(
        pregnancy: PregnancyEntity?,
        currentTime: Long = System.currentTimeMillis()
    ): PregnancyProgression? {
        if (pregnancy == null || !pregnancy.isPregnant) return null
        val lastPeriod = pregnancy.lastPeriodDate ?: return null

        val totalDurationDays = 280L
        val passedDays = ((currentTime - lastPeriod) / (24L * 60 * 60 * 1000)).coerceAtLeast(0)
        val passedWeeks = (passedDays / 7).toInt()
        val remainingDays = (totalDurationDays - passedDays).coerceAtLeast(0)

        val currentTrimester = when {
            passedWeeks < 13 -> 1
            passedWeeks < 27 -> 2
            else -> 3
        }

        val clampedWeek = passedWeeks.coerceIn(1, 42)
        val standard = FetalStandardData.getStandardForWeek(clampedWeek)
        val comparison = FetalComparison(
            name = standard.fruitComparison,
            icon = standard.icon,
            developmentTip = standard.description
        )

        return PregnancyProgression(
            weeks = passedWeeks,
            daysIntoWeek = (passedDays % 7).toInt(),
            remainingDays = remainingDays.toInt(),
            trimester = currentTrimester,
            dueDate = pregnancy.dueDate ?: (lastPeriod + totalDurationDays * 24 * 60 * 60 * 1000),
            comparisonName = comparison.name,
            comparisonIcon = comparison.icon,
            developmentTip = comparison.developmentTip
        )
    }

    fun getPregnancyProgression(
        lastPeriodDate: Long?,
        dueDate: Long?,
        isPregnant: Boolean,
        currentTime: Long = System.currentTimeMillis()
    ): PregnancyProgression? {
        if (!isPregnant || lastPeriodDate == null || lastPeriodDate <= 0L) return null
        val totalDurationDays = 280L
        val passedDays = ((currentTime - lastPeriodDate) / (24L * 60 * 60 * 1000)).coerceAtLeast(0)
        val passedWeeks = (passedDays / 7).toInt()
        val remainingDays = (totalDurationDays - passedDays).coerceAtLeast(0)

        val currentTrimester = when {
            passedWeeks < 13 -> 1
            passedWeeks < 27 -> 2
            else -> 3
        }

        val clampedWeek = passedWeeks.coerceIn(1, 42)
        val standard = FetalStandardData.getStandardForWeek(clampedWeek)
        val comparison = FetalComparison(
            name = standard.fruitComparison,
            icon = standard.icon,
            developmentTip = standard.description
        )

        return PregnancyProgression(
            weeks = passedWeeks,
            daysIntoWeek = (passedDays % 7).toInt(),
            remainingDays = remainingDays.toInt(),
            trimester = currentTrimester,
            dueDate = dueDate ?: (lastPeriodDate + totalDurationDays * 24 * 60 * 60 * 1000),
            comparisonName = comparison.name,
            comparisonIcon = comparison.icon,
            developmentTip = comparison.developmentTip
        )
    }

    fun calculateMonthProgress(weeks: Int, daysIntoWeek: Int): MonthProgress {
        val ranges = listOf(
            1 to 4,    // Month 1
            5 to 8,    // Month 2
            9 to 13,   // Month 3
            14 to 17,  // Month 4
            18 to 22,  // Month 5
            23 to 27,  // Month 6
            28 to 31,  // Month 7
            32 to 35,  // Month 8
            36 to 40,  // Month 9
            41 to 42   // Month 10 (Post-term)
        )
        
        var currentMonth = 9
        var progressFraction = 0f
        
        for (i in ranges.indices) {
            val (startWeek, endWeek) = ranges[i]
            if (weeks in startWeek..endWeek) {
                currentMonth = i + 1
                val totalWeeksInMonth = (endWeek - startWeek + 1)
                val totalDaysInMonth = totalWeeksInMonth * 7
                val daysCompleted = ((weeks - startWeek) * 7 + daysIntoWeek).coerceIn(0, totalDaysInMonth)
                progressFraction = daysCompleted.toFloat() / totalDaysInMonth.toFloat()
                break
            }
        }
        
        if (weeks >= 41) {
            currentMonth = 10
            val daysCompleted = ((weeks - 41) * 7 + daysIntoWeek).coerceIn(0, 14)
            progressFraction = daysCompleted.toFloat() / 14f
        }
        
        val monthNames = listOf(
            "الشهر الأول", "الشهر الثاني", "الشهر الثالث",
            "الشهر الرابع", "الشهر الخامس", "الشهر السادس",
            "الشهر السابع", "الشهر الثامن", "الشهر التاسع", "الشهر العاشر ⚠️"
        )
        
        val name = if (currentMonth <= monthNames.size) monthNames[currentMonth - 1] else "الشهر العاشر ⚠️"
        val total = if (weeks >= 41) 10 else 9
        
        return MonthProgress(currentMonth, name, progressFraction, total)
    }

    fun getWaterTarget(isPregnant: Boolean, extraWaterMl: Int): Int {
        val base = if (isPregnant) 2500 else 2000
        return base + extraWaterMl
    }

    fun getNutrientTargets(isPregnant: Boolean): Map<String, NutrientTarget> {
        return mapOf(
            "protein" to NutrientTarget("البروتين", if (isPregnant) 75.0 else 60.0, "جم"),
            "carbs" to NutrientTarget("النشويات", if (isPregnant) 195.0 else 150.0, "جم"),
            "fat" to NutrientTarget("الدهون", if (isPregnant) 75.0 else 65.0, "جم"),
            "sugar" to NutrientTarget("السكريات", 30.0, "جم", isLimit = true),
            "fiber" to NutrientTarget("الألياف", if (isPregnant) 28.0 else 25.0, "جم"),
            "iron" to NutrientTarget("الحديد", if (isPregnant) 27.0 else 18.0, "ملجم"),
            "calcium" to NutrientTarget("الكالسيوم", 1000.0, "ملجم"),
            "folate" to NutrientTarget("الفوليك", if (isPregnant) 600.0 else 400.0, "مكجم"),
            "potassium" to NutrientTarget("البوتاسيوم", 4700.0, "ملجم"),
            "sodium" to NutrientTarget("الصوديوم", 2000.0, "ملجم", isLimit = true),
            "magnesium" to NutrientTarget("الماغنسيوم", if (isPregnant) 360.0 else 320.0, "ملجم"),
            "vitaminC" to NutrientTarget("فيتامين سي", if (isPregnant) 85.0 else 75.0, "ملجم"),
            "vitaminA" to NutrientTarget("فيتامين أ", if (isPregnant) 770.0 else 700.0, "مكجم")
        )
    }
}

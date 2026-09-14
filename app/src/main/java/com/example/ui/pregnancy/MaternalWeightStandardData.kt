package com.example.ui.pregnancy

import com.example.ui.fetal.FetalStandardData
import java.util.Locale

enum class MaternalBmiCategory(val labelArabic: String, val totalGainRangeArabic: String) {
    UNDERWEIGHT("وزن أقل من الطبيعي (BMI < 18.5)", "12.5 - 18.0 كجم"),
    NORMAL("وزن طبيعي وصحي (BMI 18.5 - 24.9)", "11.5 - 16.0 كجم"),
    OVERWEIGHT("وزن زائد (BMI 25.0 - 29.9)", "7.0 - 11.5 كجم"),
    OBESE("سمنة (BMI ≥ 30.0)", "5.0 - 9.0 كجم")
}

enum class WeightGainStatus {
    OPTIMAL,            // ضمن النطاق الصحي الموصى به
    BELOW_RECOMMENDED,  // أقل من النطاق الموصى به
    ABOVE_RECOMMENDED   // أعلى من النطاق الموصى به
}

data class WeightGainEvaluation(
    val actualGainKg: Double,
    val minRecommendedGainKg: Double,
    val maxRecommendedGainKg: Double,
    val status: WeightGainStatus,
    val statusTitle: String,
    val adviceMessage: String,
    val category: MaternalBmiCategory
)

data class PregnancyWeightBreakdown(
    val week: Int,
    val babyKg: Double,
    val placentaKg: Double,
    val amnioticFluidKg: Double,
    val uterusAndBreastsKg: Double,
    val bloodAndFluidsKg: Double,
    val maternalStoresKg: Double,
    val totalEstimatedGainKg: Double
)

object MaternalWeightStandardData {

    fun determineBmiCategory(prePregnancyWeightKg: Double?, heightCm: Double?): MaternalBmiCategory {
        if (prePregnancyWeightKg == null || heightCm == null || heightCm <= 0.0) {
            return MaternalBmiCategory.NORMAL // Default reference
        }
        val heightMeters = heightCm / 100.0
        val bmi = prePregnancyWeightKg / (heightMeters * heightMeters)
        return when {
            bmi < 18.5 -> MaternalBmiCategory.UNDERWEIGHT
            bmi < 25.0 -> MaternalBmiCategory.NORMAL
            bmi < 30.0 -> MaternalBmiCategory.OVERWEIGHT
            else -> MaternalBmiCategory.OBESE
        }
    }

    /**
     * حساب النطاق الصحي الأدنى والأقصى للزيادة في وزن الأم بالكيلوجرام استناداً لمعايير IOM.
     */
    fun getRecommendedGainRange(week: Int, category: MaternalBmiCategory): ClosedFloatingPointRange<Double> {
        val clampedWeek = week.coerceIn(1, 42)
        
        // معدلات الزيادة في الثلث الأول (الأسابيع 1-13) والأسابيع اللاحقة (14-42)
        val (firstTriMin, firstTriMax, weeklyMin, weeklyMax) = when (category) {
            MaternalBmiCategory.UNDERWEIGHT -> Quadruple(0.5, 2.0, 0.44, 0.58)
            MaternalBmiCategory.NORMAL -> Quadruple(0.5, 2.0, 0.35, 0.50)
            MaternalBmiCategory.OVERWEIGHT -> Quadruple(0.5, 1.5, 0.23, 0.33)
            MaternalBmiCategory.OBESE -> Quadruple(0.5, 1.0, 0.17, 0.27)
        }

        val minGain: Double
        val maxGain: Double

        if (clampedWeek <= 13) {
            val progress = clampedWeek.toDouble() / 13.0
            minGain = progress * firstTriMin
            maxGain = progress * firstTriMax
        } else {
            val weeksAfterFirst = (clampedWeek - 13).toDouble()
            minGain = firstTriMin + (weeksAfterFirst * weeklyMin)
            maxGain = firstTriMax + (weeksAfterFirst * weeklyMax)
        }

        val roundedMin = Math.round(minGain * 10.0) / 10.0
        val roundedMax = Math.round(maxGain * 10.0) / 10.0
        return roundedMin..roundedMax
    }

    /**
     * تقييم الزيادة الحالية وتقديم رسالة رعاية ذكية ومطمئنة من جوري
     */
    fun evaluateGain(
        currentWeightKg: Double,
        prePregnancyWeightKg: Double,
        week: Int,
        category: MaternalBmiCategory
    ): WeightGainEvaluation {
        val actualGain = Math.round((currentWeightKg - prePregnancyWeightKg) * 10.0) / 10.0
        val range = getRecommendedGainRange(week, category)

        val (status, title, advice) = when {
            actualGain < (range.start - 0.5) -> {
                Triple(
                    WeightGainStatus.BELOW_RECOMMENDED,
                    "زيادة أبطأ من المتوسط 🌿",
                    "زيادة وزنكِ الحالية (${String.format(Locale.US, "%.1f", actualGain)} كجم) أقل قليلاً من النطاق التقديري (${range.start} - ${range.endInclusive} كجم). اهتمي بوجبات متوازنة غنية بالبروتين والمكسرات والدهون الصحية وراجعي طبيبتك للاطمئنان."
                )
            }
            actualGain > (range.endInclusive + 0.8) -> {
                Triple(
                    WeightGainStatus.ABOVE_RECOMMENDED,
                    "زيادة أعلى من المتوسط المعتاد 💧",
                    "زيادة وزنكِ الحالية (${String.format(Locale.US, "%.1f", actualGain)} كجم) أعلى من النطاق التقديري (${range.start} - ${range.endInclusive} كجم). قد يكون جزء كبير منها احتباس سوائل طبيعي؛ احرصي على شرب الماء بانتظام، والمشي الخفيف وتجنب الموالح والسكريات السريعة."
                )
            }
            else -> {
                Triple(
                    WeightGainStatus.OPTIMAL,
                    "زيادة صحية ومثالية تماماً ✨",
                    "ما شاء الله! زيادة وزنكِ (${String.format(Locale.US, "%.1f", actualGain)} كجم) تقع في النطاق الصحي المثالي والموصى به طبياً للأسبوع $week (${range.start} - ${range.endInclusive} كجم). استمري في نمط حياتك المتوازن."
                )
            }
        }

        return WeightGainEvaluation(
            actualGainKg = actualGain,
            minRecommendedGainKg = range.start,
            maxRecommendedGainKg = range.endInclusive,
            status = status,
            statusTitle = title,
            adviceMessage = advice,
            category = category
        )
    }

    /**
     * توزيع الوزن أين يذهب؟ (Dual-Weight & Tissue Correlation)
     * يوضح للأم بدقة أين تذهب الكيلوجرامات في أسبوع محدد
     */
    fun calculateWeightBreakdown(week: Int, totalGainKg: Double? = null): PregnancyWeightBreakdown {
        val clampedWeek = week.coerceIn(1, 42)
        val progress = (clampedWeek.toDouble() / 40.0).coerceIn(0.0, 1.0)

        // وزن الجنين الفعلي المرجعي
        val fetalStd = FetalStandardData.getStandardForWeek(clampedWeek)
        val babyKg = Math.round((fetalStd.weightGrams / 1000.0) * 100.0) / 100.0

        // المشيمة: تنمو حتى ~0.7 كجم عند الأسبوع 40
        val placentaKg = Math.round((0.1 + progress * 0.6) * 100.0) / 100.0

        // السائل السلوي (الأمنيوسي): يبلغ ذروته حوالي الأسبوع 34-36 (~0.8 كجم)
        val amnioticFluidKg = Math.round((progress * 0.8) * 100.0) / 100.0

        // تضخم الرحم وأنسجة الثدي استعداداً للرضاعة (~1.4 كجم عند اكتمال الحمل)
        val uterusAndBreastsKg = Math.round((0.2 + progress * 1.2) * 100.0) / 100.0

        // زيادة حجم الدم وسوائل الجسم الإضافية لحماية الأم (~2.8 كجم)
        val bloodAndFluidsKg = Math.round((0.3 + progress * 2.5) * 100.0) / 100.0

        // مخزون الطاقة الطبيعي والدهون لدعم الرضاعة الطبيعية
        val defaultEstimatedTotal = babyKg + placentaKg + amnioticFluidKg + uterusAndBreastsKg + bloodAndFluidsKg + (progress * 3.5)
        val effectiveTotal = totalGainKg ?: defaultEstimatedTotal

        val currentNonStores = babyKg + placentaKg + amnioticFluidKg + uterusAndBreastsKg + bloodAndFluidsKg
        val maternalStoresKg = (effectiveTotal - currentNonStores).coerceAtLeast(0.0)

        return PregnancyWeightBreakdown(
            week = clampedWeek,
            babyKg = babyKg,
            placentaKg = placentaKg,
            amnioticFluidKg = amnioticFluidKg,
            uterusAndBreastsKg = uterusAndBreastsKg,
            bloodAndFluidsKg = bloodAndFluidsKg,
            maternalStoresKg = Math.round(maternalStoresKg * 10.0) / 10.0,
            totalEstimatedGainKg = Math.round(effectiveTotal * 10.0) / 10.0
        )
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}

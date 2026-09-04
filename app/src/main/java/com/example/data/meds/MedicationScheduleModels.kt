package com.example.data.meds

/**
 * Data structures and Enums for the Smart Medication System.
 */

enum class MedicationDosageUnit(val label: String, val icon: String) {
    TABLET("قرص", "💊"),
    CAPSULE("كبسولة", "💊"),
    ML("مل", "🧪"),
    DROPS("نقط", "💧"),
    SPRAY("بخة", "💨"),
    INJECTION("حقنة", "💉"),
    SACHET("كيس فوار", "🥤"),
    TABLESPOON("ملعقة كبيرة", "🥄"),
    TEASPOON("ملعقة صغيرة", "🥄"),
    IU("وحدة دولية (IU)", "✨"),
    CREAM("دهان / مرهم", "🧴")
}

enum class MealRelation(val label: String, val description: String, val icon: String) {
    AFTER_MEAL("بعد الأكل", "بعد الوجبة بـ 15-30 دقيقة", "🍽️"),
    BEFORE_MEAL("قبل الأكل", "قبل الوجبة بـ 30 دقيقة على معدة شبه فارغة", "⏳"),
    WITH_MEAL("مع الأكل", "أثناء تناول الطعام مباشرة", "🥗"),
    EMPTY_STOMACH("على الريق", "أول ما تصحي صباحاً مع كوب ماء", "🌅"),
    BEFORE_BED("قبل النوم", "قبل النوم مباشرة بنصف ساعة", "🌙"),
    ANYTIME("غير مرتبط بالأكل", "في أي وقت بدون اشتراط الطعام", "✨")
}

enum class MedicationRecurrenceType(val label: String, val icon: String) {
    DAILY("يومياً", "📅"),
    EVERY_X_HOURS("كل عدد ساعات", "⏱️"),
    EVERY_X_DAYS("كل عدد أيام", "🗓️"),
    SPECIFIC_WEEK_DAYS("أيام محددة أسبوعياً", "📌"),
    MONTHLY("شهرياً", "📆"),
    AS_NEEDED("عند اللزوم فقط", "⚡")
}

data class SpecificDoseTime(
    val hour24: Int,
    val minute: Int,
    val label: String = ""
) {
    fun formatFormattedArabic(): String {
        val isPm = hour24 >= 12
        val h12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val period = if (isPm) "م" else "ص"
        return "%02d:%02d %s".format(h12, minute, period)
    }
}

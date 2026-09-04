package com.example.ui.fetal

import java.util.Locale

data class FetalStandard(
    val week: Int,
    val weightGrams: Double,
    val lengthCm: Double,
    val fruitComparison: String,
    val description: String,
    val icon: String = "🌱"
)

object FetalStandardData {
    val standards = mapOf(
        1 to FetalStandard(1, 0.1, 0.1, "تخصيب خلايا", "الجسم يستعد للملحمة المذهلة! ركّزي على حمض الفوليك والراحة.", "🧬"),
        2 to FetalStandard(2, 0.1, 0.1, "تخصيب خلايا", "الجسم يستعد للملحمة المذهلة! ركّزي على حمض الفوليك والراحة.", "🧬"),
        3 to FetalStandard(3, 0.1, 0.1, "تخصيب خلايا", "الجسم يستعد للملحمة المذهلة! ركّزي على حمض الفوليك والراحة.", "🧬"),
        4 to FetalStandard(4, 0.1, 0.2, "بذرة خشخاش", "بدأت الخلايا بالانقسام لتشكيل الجنين والمشيمة الغنية.", "🪹"),
        5 to FetalStandard(5, 0.2, 0.3, "بذرة سمسم", "يبدأ تشكل الأنبوب العصبي والقلب البدائي النابض.", "🪺"),
        6 to FetalStandard(6, 0.4, 0.5, "حبة عدس", "ينبض القلب الصغير الآن بمعدل 150 نبضة في الدقيقة كمعجزة صغيرة.", "🫘"),
        7 to FetalStandard(7, 0.8, 0.8, "حبة حمص", "تبدأ براعم الأطراف (اليدين والرجلين) في البروز والظهور بوضوح.", "🫘"),
        8 to FetalStandard(8, 1.0, 1.6, "حبة توت", "تتكون ملامح الوجه البدائية وتبدأ الأصابع الدقيقة في التمايز والنمو.", "🫐"),
        9 to FetalStandard(9, 2.0, 2.3, "حبة عنب", "يتحرك الجنين حركات خفيفة جداً ومبهجة في رحمكِ الدافئ.", "🍇"),
        10 to FetalStandard(10, 4.0, 3.1, "حبة مشمش مجفف", "اكتمال تشكل معظم الأعضاء الحيوية الأساسية للطفل الصغير.", "🍑"),
        11 to FetalStandard(11, 7.0, 4.1, "حبة تين", "تنمو الأظافر الصغيرة جداً ويبدأ الطفل ببلع السائل السلوي بلطف.", "🫓"),
        12 to FetalStandard(12, 14.0, 5.4, "حبة ليمون بلدي", "يمكن الآن سماع نبضات قلب جنينكِ الدافئة والجميلة عبر السونار.", "🍋"),
        13 to FetalStandard(13, 23.0, 7.4, "حبة خوخ", "أصابع الطفل تشكلت بالكامل وبصمات الأصابع الفريدة تبدأ بالظهور.", "🍑"),
        14 to FetalStandard(14, 43.0, 8.7, "حبة ليمون أضاليا", "يبدأ طفلكِ في عمل تعابير بوجهه الجميل مثل العبوس والابتسام البسيط.", "🍋"),
        15 to FetalStandard(15, 70.0, 10.1, "حبة تفاح صغير", "جلد الجنين رقيق جداً وشفاف وتظهر الأوعية الدموية من خلاله كالحرير.", "🍎"),
        16 to FetalStandard(16, 100.0, 11.6, "ثمرة أفوكادو", "تستطيعين في هذه الفترة استشعار ركلات لطيفة وخفيفة كرفرفة الفراشة.", "🥑"),
        17 to FetalStandard(17, 140.0, 13.0, "حبة لفت أحمر", "الهيكل العظمي يتحول تدريجياً من غضاريف مرنة إلى عظام قوية.", "🧅"),
        18 to FetalStandard(18, 190.0, 14.2, "ثمرة فلفل رومي", "جهاز السمع يتطور بوضوح، وصوت دقات قلبكِ وصوتكِ يؤنسه في كل لحظة.", "🫑"),
        19 to FetalStandard(19, 240.0, 15.3, "ثمرة مانجو بلدي", "طبقة بيضاء واقية تُعرف بالـ (Vernix) تحمي جلد طفلكِ داخل السائل.", "🥭"),
        20 to FetalStandard(20, 300.0, 16.4, "ثمرة خرشوف", "منتصف الرحلة المباركة! طفلكِ يبتلع السائل بانتظام ويتمرن على الهضم.", "🪴"),
        21 to FetalStandard(21, 360.0, 25.6, "جزرة كبيرة", "حركات منتظمة ولطيفة وحازمة تؤكد لكِ حيوية طفلكِ وسلامته.", "🥕"),
        22 to FetalStandard(22, 430.0, 27.8, "ثمرة جوز هند خضراء", "حاسة اللمس مكتملة ويلمس وجهه الصغير وحبل السرة باستمرار.", "🥥"),
        23 to FetalStandard(23, 500.0, 28.9, "ثمرة باذنجان رومي", "تتكون خلايا الدم الحمراء في نخاع العظام لدعم مناعته القوية.", "🍆"),
        24 to FetalStandard(24, 600.0, 30.0, "كوز ذرة", "رئة الجنين تبدأ بإنتاج مادة السورفاكتانت التي ستساعده على التنفس عند الولادة.", "🌽"),
        25 to FetalStandard(25, 660.0, 34.6, "ثمرة كرنب سلطة", "الأوعية الدموية بالرئة تنمو والأطراف تتشكل بالكامل بتناسق رائع.", "🥬"),
        26 to FetalStandard(26, 760.0, 35.6, "ثمرة خيار صيفي", "يفتح طفلكِ عينيه الصغيرتين ويبدأ في التفاعل مع مصادر الضوء القوية.", "🥒"),
        27 to FetalStandard(27, 875.0, 36.6, "قرنبيطة صغيرة", "يتدرب على التنفس عن طريق إدخال وإخراج السائل السلوي في رئتيه.", "🥦"),
        28 to FetalStandard(28, 1005.0, 37.6, "باذنجان كبير", "بداية الثلث الأخير العظيم! رموش طفلكِ اكتملت وتزداد الدهون الواقية تحت جلده.", "🍆"),
        29 to FetalStandard(29, 1150.0, 38.6, "ثمرة قرع عسلي صغيرة", "عضلات ورئتا طفلكِ تواصل النضج استعداداً لاستقبال العالم الخارجي.", "🎃"),
        30 to FetalStandard(30, 1320.0, 39.9, "ثمرة كابوتشا", "يتطور الدماغ بسرعة فائقة وتتشكل التلافيف الدماغية الذكية.", "🥬"),
        31 to FetalStandard(31, 1500.0, 41.1, "حبة أناناس", "يستطيع طفلكِ إدارة رأسه من جهة لأخرى ويميز الإيقاعات الصوتية المألوفة.", "🍍"),
        32 to FetalStandard(32, 1700.0, 42.4, "ثمرة كنتالوب", "تزداد طبقة الدهون لنعومة بشرته ودفء جسمه بعد لحظة اللقاء.", "🍈"),
        33 to FetalStandard(33, 1920.0, 43.7, "ثمرة دوريان", "عظام الجمجمة تبقى مرنة وغير ملتحمة لتسهيل الولادة الطبيعية بيسر وأمان.", "🍈"),
        34 to FetalStandard(34, 2150.0, 45.0, "ثمرة شمام عريشة", "جهازه المناعي يستقبل الأجسام المضادة منكِ لحمايته بعد الولادة.", "🍈"),
        35 to FetalStandard(35, 2380.0, 46.2, "ثمرة بطيخ صغيرة", "كليتا الجنين تعملان بكفاءة تامة وتكتمل معظم الأعضاء الحيوية.", "🍉"),
        36 to FetalStandard(36, 2620.0, 47.4, "حزمة سلق بلدي", "يأخذ طفلكِ وضعيته في الحوض استعداداً للنزول بسلام وبركة.", "🥬"),
        37 to FetalStandard(37, 2860.0, 48.6, "ثمرة بطيخ ناضجة", "اكتمل نمو الجنين تقريباً ويُعتبر في المدى الآمن والمطمئن للولادة.", "🍉"),
        38 to FetalStandard(38, 3080.0, 49.8, "قرع عسلي كبير", "تتساقط معظم الزغب والطبقات الدهنية ويبقى جلده ناعماً ومكتنزاً.", "🎃"),
        39 to FetalStandard(39, 3290.0, 50.7, "بطيخة صيفية كبيرة", "تستمر عضلاته في التناسق ورئتاه على أتم الاستعداد لأول شهيق في الحياة.", "🍉"),
        40 to FetalStandard(40, 3460.0, 51.2, "بطيخة مكتملة النمو", "موعد اللقاء المبارك! طفلكِ مكتمل الجمال بانتظار لحظة الخروج المبهجة.", "🍉"),
        41 to FetalStandard(41, 3600.0, 51.7, "بطيخة مكتملة", "متابعة مستمرة ودقيقة مع طبيبتكِ للتأكد من سلامة السائل والمشيمة.", "🍉"),
        42 to FetalStandard(42, 3700.0, 52.0, "طفل مكتمل", "استشيري طبيبتكِ بانتظام لتحديد الوقت الأفضل والآمن لقدوم قرة عينكِ.", "👶")
    )

    fun getStandardForWeek(week: Int): FetalStandard {
        val clamped = week.coerceIn(1, 42)
        return standards[clamped] ?: FetalStandard(clamped, 2000.0, 45.0, "جنين مكتمل", "رحلة الحمل المباركة تتقدم بسلام", "👶")
    }

    fun calculateWeightDeviation(week: Int, actualWeightGrams: Double): Double {
        val std = getStandardForWeek(week)
        if (std.weightGrams <= 0.0) return 0.0
        return ((actualWeightGrams - std.weightGrams) / std.weightGrams) * 100.0
    }

    fun calculateLengthDeviation(week: Int, actualLengthCm: Double): Double {
        val std = getStandardForWeek(week)
        if (std.lengthCm <= 0.0) return 0.0
        return ((actualLengthCm - std.lengthCm) / std.lengthCm) * 100.0
    }

    fun getEstimatedWeightGrams(week: Int, deviationPercent: Double? = 0.0): Double {
        val std = getStandardForWeek(week)
        val factor = 1.0 + ((deviationPercent ?: 0.0) / 100.0)
        return std.weightGrams * factor
    }

    fun getEstimatedLengthCm(week: Int, deviationPercent: Double? = 0.0): Double {
        val std = getStandardForWeek(week)
        val factor = 1.0 + ((deviationPercent ?: 0.0) / 100.0)
        val raw = std.lengthCm * factor
        return Math.round(raw * 10.0) / 10.0
    }

    fun estimateLengthFromWeight(week: Int, actualWeightGrams: Double): Double {
        val weightDev = calculateWeightDeviation(week, actualWeightGrams)
        return getEstimatedLengthCm(week, weightDev)
    }

    fun estimateWeightFromLength(week: Int, actualLengthCm: Double): Double {
        val lengthDev = calculateLengthDeviation(week, actualLengthCm)
        return Math.round(getEstimatedWeightGrams(week, lengthDev)).toDouble()
    }
}

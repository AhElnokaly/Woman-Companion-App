package com.example.data

object SymptomTriage {
    data class SafetyAlert(
        val title: String,
        val message: String,
        val urgencyLevel: UrgencyLevel = UrgencyLevel.CRITICAL
    )

    enum class UrgencyLevel {
        CRITICAL, WARNING
    }

    /**
     * Checks user input against symptom combinations that require immediate safety triage.
     * Returns a SafetyAlert if an emergency condition is matched, otherwise null.
     */
    fun checkUrgentSymptoms(
        input: String,
        isPregnant: Boolean
    ): SafetyAlert? {
        val normalized = EgyptianFoodRepository.normalizeText(input.trim().lowercase())

        fun matchesAny(vararg phrases: String): Boolean {
            return phrases.any { normalized.contains(EgyptianFoodRepository.normalizeText(it)) }
        }
        
        if (isPregnant) {
            // 1. Bleeding / spotting during pregnancy
            if (matchesAny("نزيف", "دم", "نزف", "بقع دم", "ينزف")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: توجهي لطبيبتكِ أو الطوارئ",
                    message = "يا حبيبة قلبي، نزول دم أو حدوث نزيف أثناء الحمل يتطلب تقييماً طبياً عاجلاً وفورياً. يرجى التوقف عن أي مجهود والتوجه فوراً لأقرب مستشفى أو وحدة طوارئ نساء وولادة أو التواصل المباشر مع طبيبتكِ المعالجة. 💕"
                )
            }
            // 2. Severe abdominal pain / cramps
            if (matchesAny("الم شديد", "ألم شديد", "مغص حاد", "وجع شديد") && matchesAny("بطن", "رحم")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: استشيري طبيبتكِ فوراً",
                    message = "يا غالية، الآلام الحادة والتقلصات الشديدة المستمرة في البطن أو أسفل الرحم تتطلب تقييماً طبياً عاجلاً. يرجى التوقف التام عن أي مجهود والتواصل مع طبيبتكِ أو التوجه لأقرب مركز طوارئ للاطمئنان! 🌸"
                )
            }
            // 3. High fever during pregnancy
            if (matchesAny("سخونية", "حرارة عالية", "حمى", "حرارتي عاليه", "سخونة")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: ارتفاع درجة الحرارة",
                    message = "يا روحي، ارتفاع درجة حرارة الجسم أثناء الحمل يتطلب مراجعة طبية عاجلة لتحديد السبب وعلاجه بأمان. يرجى التواصل مع طبيبتكِ والتوجه للاستشارة الطبية فوراً. 💕"
                )
            }
            // 4. Fluid leakage / waters broke
            if (matchesAny("نزول ماء", "تسرب مياه", "مية الجنين", "ماء الجنين", "نزول مية")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: تسرب السائل الأمنيوسي",
                    message = "يا غالية، نزول أو تدفق السوائل المهبلية قد يشير لتمزق الأغشية المحيطة بالجنين (ماء الجنين). يرجى التوجه فوراً لوحدة طوارئ الولادة أو التواصل المباشر مع طبيبتكِ دون تأخير! 🏥"
                )
            }
            // 5. Severe headache + vision changes / swelling (preeclampsia)
            if (matchesAny("صداع شديد", "صداع حاد") && matchesAny("زغللة", "عين", "تورم", "تنفخ")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: علامات تتطلب تقييماً عاجلاً لضغط الدم",
                    message = "يا حبيبة قلبي، الصداع الشديد المصحوب بزغللة في الرؤية أو تورم مفاجئ في الوجه واليدين يتطلب قياساً فورياً لضغط الدم وتقييماً طبياً عاجلاً من قِبل الفريق الطبي. توجهي لأقرب مستشفى أو مركز رعاية عاجل! 🩺"
                )
            }
            // 6. Decreased or absent fetal movement - URGENT safety triage
            val hasFetalSubject = matchesAny("حركة الجنين", "حركة البيبي", "حركة طفلي", "حركة جنيني")
            val hasReducedQualifier = matchesAny("قلت", "ضعفت", "توقفت", "ما بيتحركش", "مش بيتحرك", "قليلة", "نقصت", "بطيئة", "عدم حركة", "واقفة")
            val hasDirectPhrase = matchesAny("الجنين ما بيتحرك", "الجنين مش بيتحرك", "البيبي ما بيتحرك", "البيبي مش بيتحرك")

            if ((hasFetalSubject && hasReducedQualifier) || hasDirectPhrase) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: انخفاض أو تغير حركة الجنين",
                    message = "يا غالية، ملاحظة أي انخفاض أو تغير ملحوظ في نمط حركة جنينكِ هو أمر عاجل يتطلب التواصل الفوري مع طبيبتكِ المعالجة أو التوجه المباشر لوحدة طوارئ الولادة للاطمئنان على سلامة الجنين ونبضه دون تأخير. يرجى دائماً اتباع تعليمات طبيبتكِ الخاصة بحساب ومتابعة حركات الجنين وتوجيهاتها الطبية. 🏥👶",
                    urgencyLevel = UrgencyLevel.CRITICAL
                )
            }
        }
        return null
    }
}

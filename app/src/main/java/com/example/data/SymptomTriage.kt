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
        
        if (isPregnant) {
            // 1. Bleeding / spotting during pregnancy
            if (normalized.contains("نزيف") || normalized.contains("دم") || normalized.contains("نزف") || normalized.contains("بقع دم") || normalized.contains("ينزف")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: توجهي لطبيبتكِ الآن",
                    message = "يا حبيبة قلبي، نزول دم أو حدوث نزيف أثناء الحمل يعتبر من العلامات الطبية التي تتطلب فحصاً فورياً بالسونار للاطمئنان على سلامة المشيمة والجنين. يرجى الاستلقاء فوراً والتوجه لأقرب مستشفى أو الاتصال بطبيبتكِ فوراً! 💕"
                )
            }
            // 2. Severe abdominal pain / cramps
            if ((normalized.contains("الم شديد") || normalized.contains("ألم شديد") || normalized.contains("مغص حاد") || normalized.contains("وجع شديد")) && (normalized.contains("بطن") || normalized.contains("رحم"))) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: استشيري طبيبتكِ فوراً",
                    message = "يا غالية، الآلام الحادة والتقلصات الشديدة المستمرة في البطن أو أسفل الرحم تتطلب تقييماً طبياً مباشراً للاطمئنان على تماسك الرحم والمشيمة. يرجى التوقف عن أي مجهود والتواصل مع طبيبتكِ الآن! 🌸"
                )
            }
            // 3. High fever during pregnancy
            if (normalized.contains("سخونية") || normalized.contains("حرارة عالية") || normalized.contains("حمى") || normalized.contains("حرارتي عاليه") || normalized.contains("سخونة")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: ارتفاع درجة الحرارة",
                    message = "يا روحي، ارتفاع درجة حرارة الجسم أثناء الحمل فوق 38°م قد يؤثر على بيئة الجنين. يرجى استخدام كمادات دافئة (ليست باردة جداً) وتناول خافض حرارة آمن بحسب تعليمات طبيبتكِ والتوجه للطبيب للاطمئنان! 💕"
                )
            }
            // 4. Fluid leakage / waters broke
            if (normalized.contains("نزول ماء") || normalized.contains("تسرب مياه") || normalized.contains("مية الجنين") || normalized.contains("ماء الجنين") || normalized.contains("نزول مية")) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: تسرب السائل الأمنيوسي",
                    message = "يا غالية، نزول أو تسرب السوائل بكثرة قد يكون إشارة لتمزق غشاء الجنين (مية الرأس). يرجى التوجه فوراً لغرفة الطوارئ أو التواصل المباشر مع طبيبتكِ! 🏥"
                )
            }
            // 5. Severe headache + vision changes / swelling (preeclampsia)
            if ((normalized.contains("صداع شديد") || normalized.contains("صداع حاد")) && (normalized.contains("زغللة") || normalized.contains("عين") || normalized.contains("تورم") || normalized.contains("تنفخ"))) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: علامات ارتفاع الضغط / تسمم الحمل",
                    message = "يا حبيبة قلبي، الصداع الشديد المصحوب بزغللة أو تورم مفاجئ في الوجه واليدين قد يكون علامة على ارتفاع حاد في ضغط الدم (تسمم الحمل). قياس الضغط ومراجعة الطبيب فوراً أمر حيوي لسلامتكِ وجنينكِ! 🩺"
                )
            }
            // 6. Decreased fetal movement
            if (normalized.contains("حركة الجنين") && (normalized.contains("قلت") || normalized.contains("ضعفت") || normalized.contains("توقفت") || normalized.contains("ما بيتحركش") || normalized.contains("مش بيتحرك"))) {
                return SafetyAlert(
                    title = "🚨 تنبيه طبي عاجل: متابعة حركة الجنين",
                    message = "يا روحي، انخفاض حركة الجنين بشكل مفاجئ يتطلب الاستلقاء على الجانب الأيسر، تناول عصير طبيعي دافئ أو قطعة شيكولاتة، ومراقبة الحركة لمدة ساعة. إذا لم تشعري بـ 10 حركات، يرجى إجراء تخطيط لقلب الجنين (CTG) فوراً! 👶"
                )
            }
        }
        return null
    }
}

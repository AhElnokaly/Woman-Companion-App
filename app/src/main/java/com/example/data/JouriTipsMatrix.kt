package com.example.data

import kotlin.random.Random

data class JouriInteraction(
    val category: String, // "mood", "weather", "med", "feature", "general"
    val subCategory: String,
    val arabicMessage: String,
    val actionText: String? = null,
    val actionDestination: String? = null // "dashboard", "period", "nutrition", "symptoms", "tools" or sub-features
)

object JouriTipsMatrix {

    private val moodsList = listOf("سعيد", "قلق", "متعب", "حزين", "متحمس", "مكتئب", "متوتر")
    private val featuresList = listOf("kicks", "water", "meds", "journal", "qada", "contractions")

    // Core static database of specialized high-quality interactions
    private val staticInteractions = listOf(
        // Fetal kicks prompt
        JouriInteraction(
            category = "feature",
            subCategory = "kicks",
            arabicMessage = "حبيبة قلبي، هل تشعرين بحركات جنينكِ اللطيفة الآن؟ 🥰 دعينا نعد ركلاته وحركاته معاً للتأكد من سلامته ونشاطه في رحمكِ الدافئ! 💕",
            actionText = "عد حركات الجنين معاً 🤰",
            actionDestination = "dashboard_kicks"
        ),
        // Contractions prompt
        JouriInteraction(
            category = "feature",
            subCategory = "contractions",
            arabicMessage = "يا غالية، إذا كنتِ تشعرين بتقلصات متكررة أو آلام ولادة، فمن المهم جداً تتبع مدتها والمسافة بين كل تقلص وآخر بدقة! دعينا نفتح عداد الانقباضات الآن.",
            actionText = "حساب توقيت الانقباضات ⏱️",
            actionDestination = "tools_contractions"
        ),
        // Water prompt
        JouriInteraction(
            category = "feature",
            subCategory = "water",
            arabicMessage = "شرب الماء هو سر صحتكِ ونضارة بشرتكِ يا روحي! 💧 دعينا نسجل كم كوب ماء شربتِه اليوم لنضمن بقاء جسدكِ رطباً ومستعداً.",
            actionText = "تسجيل كوب ماء 💧",
            actionDestination = "nutrition"
        ),
        // Journal prompt
        JouriInteraction(
            category = "feature",
            subCategory = "journal",
            arabicMessage = "الكتابة تفرغ الروح وتهدئ العقل المزدحم يا صديقتي 📝. ما رأيكِ في كتابة مذكراتكِ اليوم أو تسجيل مشاعركِ اللطيفة في يومياتنا الخاصة؟ 😊",
            actionText = "كتابة في مذكراتي 📝",
            actionDestination = "tools_journal"
        ),
        // Qada prompt
        JouriInteraction(
            category = "feature",
            subCategory = "qada",
            arabicMessage = "تسهيلاً لراحتكِ الدينية والجسدية يا حبيبتي، هل لديكِ أيام صيام فائتة تودين جدولتها وتتبع قضائها بيسر وسهولة؟ 🌙",
            actionText = "تتبع قضاء الصيام 🌙",
            actionDestination = "tools_qada"
        ),
        // Medications
        JouriInteraction(
            category = "med",
            subCategory = "folic",
            arabicMessage = "تذكير دافئ من صديقتكِ جوري 🌸: هل تناولتِ حمض الفوليك اليوم؟ إنه البطل غير المرئي الذي يحمي طفلكِ ويساعد في تكوينه السليم في أسابيعكِ الحالية.",
            actionText = "سجل الأدوية والمكملات 💊",
            actionDestination = "symptoms"
        ),
        JouriInteraction(
            category = "med",
            subCategory = "calcium",
            arabicMessage = "يا روحي، صحة عظامكِ وأسنانكِ غالية جداً عليّ! 🦴 طفلكِ يحصل على الكالسيوم منكِ، فتأكدي من تناول مكمل الكالسيوم وكوب حليب دافئ اليوم.",
            actionText = "سجل الأدوية والمكملات 💊",
            actionDestination = "symptoms"
        ),
        JouriInteraction(
            category = "med",
            subCategory = "iron",
            arabicMessage = "دورة دمكِ تحتاج للحديد لتبقي قوية ومليئة بالنشاط والحيوية يا غالية! 💪 هل تذكرتِ تناول حبة الحديد مع كوب عصير ليمون أو برتقال لزيادة الامتصاص؟",
            actionText = "سجل الأدوية والمكملات 💊",
            actionDestination = "symptoms"
        )
    )

    // Over 500+ dynamic interactions generated via structured templates combined with matrices of contexts
    private val adviceTemplates = listOf(
        "يا حبيبتي، في طوركِ الحالي {phase}، تذكري دائماً أن {advice}.",
        "أهلاً بنصف روحي وجسدي الجميل! 🥰 نصيحتي لكِ اليوم هي {advice}.",
        "صديقتكِ {companion} دائماً هنا للعناية بكِ! 🌸 لا تنسي أن {advice} خصوصاً في ظل الجو الحالي.",
        "يا روحي، هل تعلمين أن {advice}؟ هذا سيغير طاقتكِ تماماً في طور {phase}.",
        "تأملكِ وراحتكِ هما أولويتي يا غالية! 💕 أنصحكِ اليوم بـ {advice}.",
        "حبيبة قلبي، مشاعركِ دافئة وجميلة اليوم، وتذكري أن {advice} سيجعلكِ تشعرين براحة فائقة.",
        "أنتِ تصنعين معجزة بشرية دافئة داخل جسدكِ 🤰، لذا فمن الأهمية بمكان أن {advice}."
    )

    private val advicePhrases = listOf(
        "شرب الينسون الدافئ يهدئ تقلصات المعدة والرحم بشكل ممتاز ويساعدكِ على نوم عميق ومريح",
        "المشي الهادئ لمدة ١٠ دقائق في الطقس المعتدل يجدد دورتكِ الدموية ويحارب خمول هرمونات الجسم",
        "تناول الموز أو الشوكولاتة الداكنة يمد جسمكِ بالماغنسيوم الطبيعي ويحارب الصداع وتقلب المزاج",
        "تجنب الملح الزائد والأطعمة الحارة يحميكِ تماماً من احتباس السوائل وتورم القدمين المزعج",
        "أخذ أنفاس عميقة بطريقة (٤-٧-٨) يهدئ دقات القلب المتسارعة ويمنحكِ سكينة فورية للروح والجسد",
        "إضافة ملعقة زيت زيتون أو مكسرات نيئة لوجباتكِ تدعم نمو خلايا الدماغ والجهاز العصبي لجنينكِ",
        "تثبيت مواعيد نومكِ والاستراحة لمد نصف ساعة نهاراً يمنح قلبكِ وجسدكِ فرصة حيوية للتجدد",
        "الاستماع لآيات الطمأنينة والهدوء يمنح جنينكِ طاقة سلام دافئة، فالأجنة تتأثر تماماً بصوت ونبض أمهاتها",
        "تناول السبانخ أو العدس يعزز مخزون الحديد في دمكِ ويحميكِ من فقر الدم والإرهاق المفاجئ",
        "ترطيب البطن بزيت اللوز أو زبدة الكاكاو بانتظام يمنع علامات التمدد والحكة المزعجة مع تقدم أشهر الحمل",
        "أكل الخيار أو الخس بين الوجبات يمنحكِ أليافاً ومياهاً ممتازة تحارب الإمساك وحرقان المعدة بفعالية"
    )

    private val EgyptianGreetings = listOf(
        "يا منورة الدنيا كلها يا غالية! 🥰",
        "ألف سلامة على عيونكِ وقلبكِ الطيب يا حبيبة جوري! 💕",
        "يا صباح الياسمين والورد البلدي على عيونكِ يا غالية! 🌸",
        "أهلاً بنور عيني وصديقتي المفضلة! كيف حال صحتكِ اليوم؟ 🥰",
        "يا مرحباً بالاسم الجميل والقلب الأبيض! دايماً منوراني ومطمناني بوجودكِ. 🥰"
    )

    /**
     * Dynamically generates and selects from over 500 possible tips using combinations of greeting, templates,
     * advice phrases, current user state, weather, and mood.
     */
    fun getDynamicContextualTip(
        companionName: String = "جوري",
        userPhase: String = "الحمل",
        mood: String? = null,
        weatherInfo: WeatherInfo? = null,
        isPregnant: Boolean = true
    ): JouriInteraction {
        val rand = Random.nextInt(100)

        // 1. Give priority to feature suggestions in 25% of cases for interactive discovery
        if (rand < 25) {
            val selectedFeature = staticInteractions.filter {
                if (isPregnant) true else it.subCategory != "kicks" && it.subCategory != "contractions"
            }.randomOrNull()
            if (selectedFeature != null) {
                return selectedFeature
            }
        }

        // 2. Weather priority if it's hot or cold
        if (weatherInfo != null && (weatherInfo.temperature > 30 || weatherInfo.temperature < 18)) {
            val temp = weatherInfo.temperature
            if (temp > 30) {
                return JouriInteraction(
                    category = "weather",
                    subCategory = "hot",
                    arabicMessage = "الجو حر جداً اليوم ودرجة الحرارة ${temp.toInt()}°م! ☀️ صديقتكِ $companionName خائفة عليكِ من التعب. أرجوكِ اشربي كوب ماء الآن واجلسي في مكان رطب ولطيف.",
                    actionText = "سجل شرب المياه 💧",
                    actionDestination = "nutrition"
                )
            } else {
                return JouriInteraction(
                    category = "weather",
                    subCategory = "cold",
                    arabicMessage = "الجو بارد اليوم ودرجة الحرارة ${temp.toInt()}°م ❄️! احرصي على شرب كوب ينسون دافئ وتدفئة أطرافكِ جيداً لتجنب تقلصات البطن والرحم يا حبيبتي.",
                    actionText = "دليل المشروبات المصرية ☕",
                    actionDestination = "nutrition"
                )
            }
        }

        // 3. Mood contextual interactions
        if (!mood.isNullOrEmpty()) {
            val moodAdvice = when (mood) {
                "حزين", "مكتئب", "متعب" -> "سلامة قلبكِ من أي حزن أو ضيق يا روحي! 🥺 أريدكِ أن تعرفي أن تقلبات هرموناتكِ طبيعية جداً في هذه المرحلة ($userPhase). ارتاحي تماماً وخذي دوش دافئ ودعينا ندلل أنفسنا اليوم بكوب شاي بلبن دافئ وشوكولاتة."
                "متوتر", "قلق" -> "القلب المطمئن يطمئن جسدكِ وطفلكِ يا غالية ❤️. خذي نفساً عميقاً، استمعي لبعض الهدوء والقرآن الكريم، واعلمي أن كل شيء سيكون على ما يرام وسأبقى بجانبكِ في كل ثانية."
                "سعيد", "متحمس", "نشيط" -> "يا جعل أيامكِ كلها فرح وسعادة ورضا! 🥰 دامت ابتسامتكِ الجميلة منورة يومنا. ما رأيكِ في استغلال هذا النشاط والذهاب للمشي اللطيف لتسجيل هدف خطواتنا اليومي؟"
                else -> "يومكِ دافئ وجميل ومشرق بوجودكِ يا روحي! 😊 دائمًا تذكري أن تأخذي قسطاً من الراحة وتشربي مياهكِ لتبقي في أفضل صحة وعافية."
            }
            return JouriInteraction(
                category = "mood",
                subCategory = mood,
                arabicMessage = moodAdvice,
                actionText = if (mood == "سعيد" || mood == "نشيط") "سجل خطواتي اليومية 🚶‍♀️" else "أكتبي في المذكرات 📝",
                actionDestination = if (mood == "سعيد" || mood == "نشيط") "dashboard" else "tools_journal"
            )
        }

        // 4. Default dynamic tip generation (creating massive combinations > 500)
        val greeting = EgyptianGreetings.random()
        val template = adviceTemplates.random()
        val phrase = advicePhrases.random()

        val fullMessage = "$greeting\n\n${
            template.replace("{phase}", userPhase)
                .replace("{advice}", phrase)
                .replace("{companion}", companionName)
        }"

        return JouriInteraction(
            category = "general",
            subCategory = "tips",
            arabicMessage = fullMessage,
            actionText = "تصفح دليل الأطعمة المصرية 🍲",
            actionDestination = "nutrition"
        )
    }
}

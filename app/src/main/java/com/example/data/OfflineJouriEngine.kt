package com.example.data

import com.example.viewmodel.CyclePhaseInfo
import org.json.JSONObject

object OfflineJouriEngine {

    enum class QuestionForm {
        DEFINITION,   // What is it? Why?
        TREATMENT,    // How to treat? What to eat/drink?
        SYMPTOMS,     // What are the symptoms/risks?
        GENERAL       // Default
    }

    fun detectQuestionForm(input: String): QuestionForm {
        val normalized = EgyptianFoodRepository.normalizeText(input)
        return when {
            normalized.contains("علاج") || normalized.contains("حل") || normalized.contains("ازاي") || 
            normalized.contains("طريقة") || normalized.contains("كيف") || normalized.contains("اشرب") || 
            normalized.contains("اكل ايه") || normalized.contains("وصفة") || normalized.contains("اكلات") || 
            normalized.contains("اطعمه") || normalized.contains("أطعمة") || normalized.contains("أغذية") ||
            normalized.contains("اغذيه") || normalized.contains("انزل") || normalized.contains("ارفع") ||
            normalized.contains("اخفض") || normalized.contains("تنزيل") || normalized.contains("رفع") ||
            normalized.contains("اشهى") || normalized.contains("اشهي") || normalized.contains("اصنع") ||
            normalized.contains("طريقه") || normalized.contains("عايز اكل") || normalized.contains("أكل ايه") -> QuestionForm.TREATMENT
            
            normalized.contains("اعراض") || normalized.contains("أعراض") || normalized.contains("خطر") || 
            normalized.contains("علامات") || normalized.contains("وجع") || normalized.contains("الم") || 
            normalized.contains("ألم") || normalized.contains("امتى") || normalized.contains("اضرار") || 
            normalized.contains("أضرار") || normalized.contains("مشكلة") || normalized.contains("مشكله") ||
            normalized.contains("يضر") || normalized.contains("تأثير") || normalized.contains("تاثير") -> QuestionForm.SYMPTOMS
            
            normalized.contains("ما هو") || normalized.contains("ما هى") || normalized.contains("ايه هو") || 
            normalized.contains("يعني ايه") || normalized.contains("اهمية") || normalized.contains("فوائد") || 
            normalized.contains("فايدة") || normalized.contains("ليه") || normalized.contains("لماذا") || 
            normalized.contains("معنى") || normalized.contains("اهميه") || normalized.contains("معني") ||
            normalized.contains("تعريف") || normalized.contains("فايده") -> QuestionForm.DEFINITION
            
            else -> QuestionForm.GENERAL
        }
    }

    class OfflineResponse(
        val replyText: String,
        val actionType: String? = null, // "water", "symptom", "profile", "food_list"
        val actionValue: Any? = null,
        val isSpecificMatch: Boolean = false
    )

    fun getResponse(
        userInput: String,
        motherName: String?,
        phaseInfo: CyclePhaseInfo,
        pregnancyState: PregnancyEntity?,
        todayWaterLogged: Int,
        companionName: String = "جوري",
        weatherInfo: WeatherInfo? = null,
        todaySteps: Int = 0,
        targetSteps: Int = 6000
    ): OfflineResponse {
        // Use nickname as motherName if present to make Jouri address the user with her preferred pet name
        val nameToUseInRaw = if (!pregnancyState?.nickname.isNullOrEmpty()) {
            pregnancyState?.nickname
        } else {
            motherName
        }

        val rawResponse = getResponseRaw(
            userInput = userInput,
            motherName = nameToUseInRaw,
            phaseInfo = phaseInfo,
            pregnancyState = pregnancyState,
            todayWaterLogged = todayWaterLogged,
            companionName = companionName,
            weatherInfo = weatherInfo,
            todaySteps = todaySteps,
            targetSteps = targetSteps
        )
        
        // Append chronic condition alerts / supportive notes from Jouri
        val finalReplyText = buildString {
            append(rawResponse.replyText)
            
            if (pregnancyState != null) {
                val conditions = mutableListOf<String>()
                if (pregnancyState.hasHighBp) conditions.add("الضغط العالي 📈")
                if (pregnancyState.hasLowBp) conditions.add("الضغط الواطي 📉")
                if (pregnancyState.hasDiabetes) conditions.add("السكري 🩸")
                if (!pregnancyState.chronicOthers.isNullOrEmpty()) {
                    conditions.add(pregnancyState.chronicOthers)
                }
                
                if (conditions.isNotEmpty()) {
                    append("\n\n🩺 **ملاحظة رعاية خاصة من جوري:**")
                    append("\nيا روحي، بناءً على حالتكِ المسجلة: (${conditions.joinToString(" • ")})، ")
                    if (pregnancyState.hasHighBp) {
                        append("يرجى شرب كميات كافية من الكركديه البارد والماء، والابتعاد تماماً عن الصوديوم والمخللات الفلاحي لحمايتكِ من ارتفاع الضغط. ")
                    }
                    if (pregnancyState.hasLowBp) {
                        append("تذكري شرب السوائل بانتظام وتجنب الوقوف المفاجئ لعدم الشعور بالدوار، ويمكن تناول وجبات صغيرة على فترات متقاربة. ")
                    }
                    if (pregnancyState.hasDiabetes) {
                        append("احرصي على موازنة النشويات، والابتعاد عن السكريات المضافة والمخبوزات البيضاء لحماية مستويات الأنسولين لديكِ. ")
                    }
                    append("صحتكِ وجنينكِ هما أغلى ما لديّ! 🥰🌸")
                }
            }
        }
        
        return OfflineResponse(
            replyText = finalReplyText,
            actionType = rawResponse.actionType,
            actionValue = rawResponse.actionValue,
            isSpecificMatch = rawResponse.isSpecificMatch
        )
    }

    fun getResponseRaw(
        userInput: String,
        motherName: String?,
        phaseInfo: CyclePhaseInfo,
        pregnancyState: PregnancyEntity?,
        todayWaterLogged: Int,
        companionName: String = "جوري",
        weatherInfo: WeatherInfo? = null,
        todaySteps: Int = 0,
        targetSteps: Int = 6000
    ): OfflineResponse {
        val input = userInput.trim().lowercase()
        val normalizedInput = EgyptianFoodRepository.normalizeText(input)
        val nameToUse = motherName ?: "يا غالية"
        val isPregnant = pregnancyState != null
        val phaseArabicName = if (isPregnant) "حامل" else phaseInfo.phaseArabic

        // Prepare weather details if available
        val weatherAdvice = if (weatherInfo != null) {
            "الطقس اليوم ${weatherInfo.description} ودرجة الحرارة حوالي ${weatherInfo.temperature}°م."
        } else {
            ""
        }
        val qForm = detectQuestionForm(userInput)

        // 1. High Blood Pressure (الضغط العالي) offline advice and foods
        if (normalizedInput.contains("ضغط عالي") || normalizedInput.contains("ضغط مرتفع") || normalizedInput.contains("الضغط العالي")) {
            val highBpFoods = EgyptianFoodRepository.presetFoods.filter { 
                it.potassiumMg >= 250.0 && it.sodiumMg < 150.0 
            }.take(4)
            
            val foodListStr = highBpFoods.joinToString("\n") { 
                "• **${it.name}** (${it.category}): غني بالبوتاسيوم (${it.potassiumMg} ملجم) والماغنسيوم (${it.magnesiumMg} ملجم) ومثالي للضغط. *فوائده:* ${it.healthBenefits}"
            }

            val reply = when (qForm) {
                QuestionForm.TREATMENT -> """
                    يا صديقتي الغالية $nameToUse، سلامتكِ ألف سلامة! ⚠️ لعلاج وضبط الضغط العالي بسرعة والوقاية منه أوفلاين:
                    
                    **🥤 مشروبات سريعة المفعول لتخفيض الضغط ينصحكِ بها $companionName:**
                    • **كركديه بارد ومثلج**: يعتبر بمثابة موسّع طبيعي للأوعية الدموية ويخفض الضغط فوراً. (تجنبي الكركديه الساخن لأنه قد يرفعه!).
                    • **دوم بلدي باللبن**: غني جداً بالبوتاسيوم الطبيعي ويحمي القلب والشرايين.
                    • **عصير الرمان الطازج**: يحتوي على مضادات أكسدة رائعة لمرونة الأوعية الدموية.
                    
                    **💡 أهم التغييرات الغذائية الفورية:**
                    1. **الابتعاد التام عن الصوديوم**: تجنبي المخللات الفلاحي، المأكولات السريعة، الرنجة والفسيخ، والمش.
                    2. **رفع نسبة البوتاسيوم**: تناولي الموز، الكيوي، والبطاطس المسلوقة لتدعمي طرد الصوديوم الزائد.
                    3. **الراحة وشرب الماء**: اشربي 3 لترات ماء يومياً لزيادة الترطيب وتجنبي تماماً التوتر والانفعال.
                    
                    **🥦 خيارات طعام غنية ومناسبة من كتالوجنا أوفلاين:**
                    $foodListStr
                    
                    *دمتِ دافئة وقوية، وإذا ارتفع الضغط عن 140/90 يرجى استشارة الطبيب فوراً يا روحي! 🌸*
                """.trimIndent()

                QuestionForm.SYMPTOMS -> """
                    يا صديقتي الغالية $nameToUse، سلامة قلبكِ! ⚠️ من المهم جداً مراقبة أعراض وعلامات خطر الضغط العالي لتفادي أي مضاعفات كـ تسمم الحمل (Pre-eclampsia):
                    
                    **🚨 الأعراض التي يجب مراقبتها بدقة:**
                    - صداع شديد مستمر ومتمركز في الجبهة أو مؤخرة الرأس ولا يزول بمسكنات خفيفة.
                    - زغللة في العين أو عدم وضوح الرؤية (رؤية بقع ضوئية).
                    - طنين مستمر في الأذن ودوخة شديدة.
                    - تورم مفاجئ وملحوظ في الوجه، اليدين، أو القدمين.
                    - ألم شديد في أعلى البطن (تحت الضلوع مباشرة).
                    
                    **💡 ما العمل فوراً عند شعوركِ بهذه الأعراض؟**
                    - قيسي ضغطكِ فوراً؛ إذا كان 140/90 أو أكثر، يرجى التوجه للمستشفى أو الاتصال بطبيبتكِ فوراً دون تأخير!
                    - استلقي على جانبكِ الأيسر في غرفة هادئة ومظلمة لتخفيف العبء على الدورة الدموية.
                    
                    *حفظكِ الله ورعاكِ وجعل حملكِ سهلاً ميسراً يا قلبي! 🌸*
                """.trimIndent()

                QuestionForm.DEFINITION -> """
                    يا صديقتي الغالية $nameToUse، سؤالكِ مهم وممتاز! 💡 الضغط العالي (ارتفاع ضغط الدم) هو ارتفاع ضغط الدم الانقباضي عن 140 ملم زئبق أو الانبساطي عن 90 ملم زئبق.
                    
                    **🤔 ما أهمية متابعة الضغط بدقة؟**
                    أثناء طوركِ الحالي ($phaseArabicName)، يحافظ الضغط المتوازن على تدفق الدم والأكسجين والمغذيات بشكل سليم عبر المشيمة لتغذية الجنين. الارتفاع الشديد يعيق هذا التدفق وقد يؤدي لـ تسمم الحمل أو الولادة المبكرة.
                    
                    **💡 كيف تحافظين على ضغطكِ متوازناً؟**
                    - المتابعة الدورية للقياس بجهاز الضغط المنزلي.
                    - تجنب الموالح الشديدة والمخللات والوجبات السريعة.
                    - التركيز على البوتاسيوم والماغنسيوم في نظامك الغذائي لتهدئة الأوعية الدموية.
                    
                    *أنا بجانبكِ دوماً $companionName لنضمن عافيتكِ وسلامة جنينكِ يا روحي! 🥰🌸*
                """.trimIndent()

                else -> """
                    يا صديقتي الغالية $nameToUse، سلامتكِ ألف سلامة! ⚠️ تفاصيل الضغط العالي (ارتفاع ضغط الدم) بالغة الأهمية أثناء الحمل أو الدورة لتجنب متاعب تسمم الحمل (Pre-eclampsia).
                    
                    **🚨 الأعراض التي يجب مراقبتها:**
                    - صداع شديد مستمر، طنين الأذن، زغللة العين، وتورم مفاجئ في اليدين والقدمين.
                    
                    **💡 النصائح والإرشادات الطبية أوفلاين:**
                    1. **تقليل الصوديوم (الملح) فوراً**: تجنبي تماماً المخللات الفلاحي، الأسماك المملحة (الفسيخ والرنجة)، والمش، والمأكولات السريعة.
                    2. **رفع البوتاسيوم والماغنسيوم**: فهما الأقوى في طرد الصوديوم وإرخاء الشرايين.
                    3. **الراحة التامة والترطيب**: اشربي ما لا يقل عن ٣ لتر ماء يومياً وتجنبي الانفعال أو الإجهاد.
                    
                    **🥤 مشروبات وأطعمة مصرية ينصحكِ بها $companionName للضغط العالي:**
                    • **كركديه بارد بالثلج**: قنبلة طبيعية لتوسيع الأوعية وتخفيض الضغط فوراً.
                    • **دوم بلدي باللبن**: غني جداً بالبوتاسيوم الطبيعي ويحمي القلب والضغط.
                    • **دبس الرمان الطبيعي** أو **عصير الرمان الطازج**: يقي الشرايين ويخفض الضغط.
                    
                    **🥦 خيارات طعام غنية ومناسبة من كتالوجنا أوفلاين:**
                    $foodListStr
                    
                    *دمتِ دافئة وقوية، وإذا ارتفع الضغط عن 140/90 يرجى استشارة الطبيب فوراً يا روحي! 🌸*
                """.trimIndent()
            }
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 2. Low Blood Pressure (الضغط الواطي) offline advice and foods
        if (normalizedInput.contains("ضغط واطي") || normalizedInput.contains("ضغط منخفض") || normalizedInput.contains("الضغط الواطي")) {
            val lowBpFoods = EgyptianFoodRepository.presetFoods.filter { 
                it.category == "drink" || it.name.contains("شوربة") || it.name.contains("مرقة") || it.name.contains("عسل") 
            }.take(4)
            
            val foodListStr = lowBpFoods.joinToString("\n") { 
                "• **${it.name}**: ${it.healthBenefits} (${it.calories} سعرة)."
            }

            val reply = when (qForm) {
                QuestionForm.TREATMENT -> """
                    يا روحي ونبض قلبي، للتخلص من هبوط الضغط والدوخة سريعاً أوفلاين:
                    
                    **🍲 مشروبات وأطعمة دافئة لرفع الضغط بلطف:**
                    • **شوربة كوارع فلاحي دافئة** أو **شوربة لسان عصفور بمرقة الدجاج**: تمد الجسم بالسوائل والصوديوم المعتدل لرفع حجم الدم وضغطه فوراً بطريقة آمنة.
                    • **مغات مصري دافئ بالسمسم المالح**: يمنحكِ طاقة فورية مذهلة وينشط دورتكِ الدموية.
                    • **حلبة دافئة** أو **زنجبيل دافئ بالليمون**: يساعدان على موازنة الأوعية الدموية وضبط الهبوط.
                    
                    **💡 نصائح عملية فورية من $companionName:**
                    1. **الترطيب المستمر**: اشربي كوب ماء كبير فوراً، فالسوائل ترفع الضغط بشكل طبيعي وسريع.
                    2. **رفع القدمين**: استلقي على ظهركِ وارفعي قدميكِ قليلاً على وسادة لتنشيط وصول الدم للمخ وتفادي الدوار.
                    3. **الوقوف والتحرك بتمهل**: تجنبي تماماً القيام فجأة من وضع الجلوس أو الاستلقاء.
                    
                    **🍯 خيارات طعام تمنع الهبوط من كتالوجنا أوفلاين:**
                    $foodListStr
                    
                    *ارتاحي تماماً يا روحي وحفظ الله صحتكِ من كل مكروه! 🥰🌸*
                """.trimIndent()

                QuestionForm.SYMPTOMS -> """
                    يا روحي، سلامة قلبكِ من الهبوط! 📉 إليكِ أبرز أعراض انخفاض ضغط الدم الشائعة أثناء طوركِ الحالي ($phaseArabicName):
                    
                    **🚨 الأعراض الأكثر شيوعاً:**
                    - الدوخة والدوار الشديد، خاصة عند الوقوف المفاجئ (هبوط الضغط الانتصابي).
                    - الشعور بالخمول التام، الكسل والنعاس المستمر، وعدم القدرة على بذل مجهود.
                    - زغللة العين المؤقتة أو الغشاوة على الرؤية.
                    - برودة في اليدين والقدمين مع إحساس بضيق طفيف في التنفس.
                    
                    **💡 ماذا تفعلين إذا شعرتِ بالدوخة أو الهبوط؟**
                    - اجلسي أو استلقي فوراً لتجنب السقوط أو فقدان التوازن.
                    - اشربي كوباً كبيراً من الماء أو شوربة دافئة بملح معتدل، وارفعي قدميكِ عن مستوى قلبكِ لثوانٍ معدودة.
                    
                    *دمتِ دافئة ونشيطة وبكامل عافيتكِ يا غالية! 🥰🌸*
                """.trimIndent()

                QuestionForm.DEFINITION -> """
                    يا نبض قلبي، سؤالكِ مهم جداً! 💡 الضغط الواطي (انخفاض ضغط الدم) هو انخفاض القياس عن 90/60 ملم زئبق.
                    
                    **🤔 لماذا يحدث انخفاض الضغط أثناء الحمل أو الدورة؟**
                    - في الحمل (خصوصاً الثلثين الأول والثاني)، تتمدد الأوعية الدموية في جسمكِ بشكل طبيعي لتوجيه تدفق الدم إلى المشيمة لتغذية جنينكِ، مما يسبب انخفاضاً طفيفاً في الضغط وهبوطاً عابراً.
                    - في فترة الحيض، قد يؤدي فقدان كميات من الدم والحديد إلى هبوط مؤقت في الضغط والشعور بالدوار والتعب.
                    
                    *لا تقلقي أبداً يا روحي، فهو عرض شائع جداً طالما أنه لا يصحبه إغماء. أنا بجانبكِ دوماً $companionName لرعايتكِ! 🥰🌸*
                """.trimIndent()

                else -> """
                    يا عمري وروحي، سلامة قلبكِ من الهبوط والدوار! 📉 تفاصيل الضغط الواطي (انخفاض ضغط الدم) شائعة جداً في الثلثين الأول والثاني من الحمل وفي فترة الحيض بسبب تمدد الأوعية الدموية.
                    
                    **🚨 الأعراض الشائعة:**
                    - الدوخة الشديدة عند الوقوف المفاجئ، الخمول التام، عدم التركيز، والرغبة في النوم والكسل.
                    
                    **💡 النصائح والإرشادات الطبية أوفلاين:**
                    1. **الترطيب المستمر**: شرب السوائل الدافئة والشوربة يرفع حجم الدم وضغطه فوراً بطريقة آمنة.
                    2. **زيادة الملح المعتدل**: تناولي أطعمة بملح طبيعي دون حرمان.
                    3. **الوقوف والتحرك بتمهل**: تجنبي القيام فجأة من وضع الاستلقاء لتفادي الدوار الهبوطي.
                    
                    **🍲 مشروبات وأطعمة مصرية ينصحكِ بها $companionName للضغط الواطي:**
                    • **شوربة كوارع فلاحي دافئة** أو **شوربة لسان عصفور**: تمد الجسم بالسوائل والأملاح لرفع الضغط بلطف.
                    • **مغات مصري دافئ بالسمسم**: يمنحكِ طاقة فورية وينشط دورتكِ الدموية.
                    • **حلبة دافئة** أو **كركديه دافئ**: يساعدان على موازنة الأوعية الدموية وضبط الهبوط.
                    
                    **🍯 خيارات طعام تمنع الهبوط من كتالوجنا أوفلاين:**
                    $foodListStr
                    
                    *ارتاحي تماماً وارفعي قدميكِ قليلاً عن مستوى قلبكِ يا غالية لتنشيط المخ! 🥰🌸*
                """.trimIndent()
            }
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 3. Iron & Anemia Queries (حديد، أنيميا، فقر دم)
        if (normalizedInput.contains("حديد") || normalizedInput.contains("انيميا") || normalizedInput.contains("فقر دم") || normalizedInput.contains("الدم")) {
            val ironFoods = EgyptianFoodRepository.presetFoods.filter { it.ironMg >= 2.0 }.sortedByDescending { it.ironMg }.take(5)
            val listStr = ironFoods.joinToString("\n") { 
                "• **${it.name}**: يحتوي على **${it.ironMg} ملجم حديد** (${it.servingSize}). *الفوائد:* ${it.healthBenefits}"
            }

            val reply = when (qForm) {
                QuestionForm.TREATMENT -> """
                    يا حبيبة قلبي، لعلاج الأنيميا ورفع مخزون الحديد في دمكِ بسرعة وبطرق طبيعية أوفلاين:
                    
                    **💡 سر $companionName الذهبي لامتصاص فائق للحديد:**
                    • **فيتامين C هو المفتاح 🍋**: تناولي مصادر الحديد دائماً مع عصرة ليمون طازج، أو كوب عصير برتقال، لأن فيتامين ج يزيد امتصاص الحديد غير الحيواني بـ ٣ أضعاف!
                    • **احذري الشاي والقهوة ☕**: تجنبي تماماً شرب الشاي، القهوة، أو المشروبات الغازية قبل أو بعد تناول الوجبات الغنية بالحديد بمسافة لا تقل عن ساعة ونصف، لأن التانين والكافيين يمنعان امتصاص الحديد تماماً.
                    
                    **🍲 أقوى الأطعمة والمأكولات المصرية الغنية بالحديد أوفلاين:**
                    $listStr
                    
                    *احرصي على هذه النصائح اليومية لتعزيز قوة دمكِ واستعادة كامل طاقتكِ ونشاطكِ يا روحي! 🥰🌸*
                """.trimIndent()

                QuestionForm.SYMPTOMS -> """
                    يا حبيبة قلبي، تتبع أعراض نقص الحديد أمر بالغ الأهمية! إليكِ أبرز أعراض أنيميا نقص الحديد وفقر الدم الشائعة:
                    
                    **🚨 الأعراض التي تدل على نقص الحديد:**
                    - الشعور المستمر بالإجهاد والتعب الشديد والكسل حتى بعد النوم الكافي.
                    - شحوب واصفرار ملحوظ في البشرة، الوجه، وداخل جفن العين.
                    - تساقط شعر مفرط وضعف عام في الأظافر وتكسرها.
                    - ضيق وتعب في التنفس وسرعة ضربات القلب مع أقل مجهود بدني.
                    - اشتهاء تناول أشياء غريبة (كالرغبة في مضغ الثلج أو التراب).
                    
                    *إذا لاحظتِ هذه الأعراض، يرجى إجراء تحليل صورة دم كاملة (CBC) ومخزون الحديد (Ferritin) فوراً واستشارة طبيبتكِ يا روحي! 🌸🥰*
                """.trimIndent()

                QuestionForm.DEFINITION -> """
                    يا روحي، سؤالكِ غاية في الأهمية! 💡 الحديد هو المعدن الأساسي المسؤول عن إنتاج (الهيموجلوبين)، وهو البروتين الموجود في خلايا الدم الحمراء والذي ينقل الأكسجين من رئتيكِ إلى باقي خلايا جسمكِ، وإلى جنينكِ الغالي عبر المشيمة.
                    
                    **🤔 لماذا تحتاج المرأة للحديد بشكل مضاعف؟**
                    - أثناء الحمل: يتضاعف حجم دم الأم بنسبة 50% لتغذية الجنين وبناء مشيمته وتكوين دمه، مما يجعل نقص الحديد يسبب فقر دم الحمل ويؤثر على وزن الجنين أو يزيد من خطر الولادة المبكرة.
                    - أثناء الدورة الشهرية: يفقد الجسم كمية من الدم والحديد شهرياً، مما يتطلب تعويضاً مستمراً لتجنب الهبوط والتعب.
                    
                    *أنا هنا $companionName لأذكركِ دائماً بالتغذية السليمة لتبقى صحتكِ حديداً! 🥰🌸*
                """.trimIndent()

                else -> """
                    يا حبيبة قلبي، تتبع مخزون الحديد أمر بالغ الأهمية لنمو طفلكِ وتفادي أنيميا الحمل أو تعب الدورة الشهيرة! 🩸
                    
                    **💡 نصيحة $companionName الذهبية لامتصاص الحديد:**
                    تناولي الأطعمة الغنية بالحديد مع **فيتامين C** (مثل عصر الليمون على العدس، الكبدة، أو تناول كوب ليموناضة دافئ) وتجنبي شرب الشاي أو القهوة مباشرة بعد الأكل لأن الكافيين يمنع امتصاص الحديد.
                    
                    **🍲 أقوى الأطعمة والمأكولات المصرية الغنية بالحديد أوفلاين:**
                    $listStr
                    
                    احرصي على تناول هذه الوجبات الغنية لتعزيز الهيموجلوبين واستعادة نشاطكِ وقوتكِ يا روحي! 🌸🥰
                """.trimIndent()
            }
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 4. Calcium & Bones Queries (كالسيوم، عظام، أسنان، أسنان)
        if (normalizedInput.contains("كالسيوم") || normalizedInput.contains("عظام") || normalizedInput.contains("سنان") || normalizedInput.contains("اسنان") || normalizedInput.contains("العظام")) {
            val calciumFoods = EgyptianFoodRepository.presetFoods.filter { it.calciumMg >= 50.0 }.sortedByDescending { it.calciumMg }.take(5)
            val listStr = calciumFoods.joinToString("\n") { 
                "• **${it.name}**: يحتوي على **${it.calciumMg} ملجم كالسيوم** (${it.servingSize}). *الفوائد:* ${it.healthBenefits}"
            }

            val reply = when (qForm) {
                QuestionForm.TREATMENT -> """
                    يا روحي، لرفع مستويات الكالسيوم وحماية عظامكِ وأسنانكِ بطريقة طبيعية وعلاجية أوفلاين:
                    
                    **💡 سر $companionName الذهبي لامتصاص الكالسيوم:**
                    • **فيتامين د هو شريك الكالسيوم ☀️**: لكي يستفيد جسمكِ من الكالسيوم الذي تتناولينه، يحتاج لفيتامين د! احرصي على التعرض اللطيف لأشعة الشمس في الصباح الباكر أو تناول البيض والأسماك.
                    • **الابتعاد عن الكافيين الزائد ☕**: تقليل الشاي والقهوة يساعد في تقليل إفراز الكالسيوم خارج الجسم ويحمي مخزون عظامكِ.
                    
                    **🥛 أقوى الأغذية المصرية الغنية بالكالسيوم أوفلاين:**
                    $listStr
                    
                    *التزمي بوجبة غنية بالكالسيوم يومياً لتبقي قوية ومشرقة دائمًا يا روحي! 🥰🌸*
                """.trimIndent()

                QuestionForm.SYMPTOMS -> """
                    يا قلبي، انتبهي جيداً لعلامات وأعراض نقص الكالسيوم في جسمكِ:
                    
                    **🚨 الأعراض التي تشير لنقص الكالسيوم:**
                    - تشنجات وشد عضلي مؤلم ومفاجئ في عضلات الساق (خصوصاً أثناء النوم ليلاً).
                    - آلام مستمرة في المفاصل، عظام الحوض، وأسفل الظهر.
                    - ضعف وهشاشة في الأظافر وتكسرها بسهولة، أو آلام وتخلخل في الأسنان.
                    - تنميل أو وخز خفيف في أطراف الأصابع أو حول الفم.
                    
                    **🤔 حقيقة طبية هامة من جوري:**
                    إذا قلّ الكالسيوم في غذائكِ اليومي، فإن جنينكِ سيسحب احتياجه لبناء هيكله العظمي من مخزون عظامكِ وأسنانكِ مباشرة، مما يسبب لكِ آلاماً ومشاكل عظام لاحقاً! احرصي على تعويضه باستمرار.
                    
                    *دمتِ متينة وقوية وبألف صحة وعافية يا قلبي! 🥰🌸*
                """.trimIndent()

                QuestionForm.DEFINITION -> """
                    يا نبض قلبي، سؤالكِ رائع ومهم جداً! 💡 الكالسيوم هو المعدن الأكثر وفرة في جسمكِ، وهو الدعامة الأساسية لبناء الهيكل العظمي المتين والأسنان القوية لطفلكِ الغالي في طور الحمل.
                    
                    **🤔 ما هي فوائد الكالسيوم الكبرى لكِ ولجنينكِ؟**
                    - يساعد في تنظيم ضربات القلب ونقل الإشارات العصبية العضلية بشكل سليم.
                    - يساهم في الوقاية من حدوث تشنجات عضلية مؤلمة للأم الحامل.
                    - يلعب دوراً مهماً في توازن ضغط الدم وحمايتكِ من الارتفاعات المفاجئة.
                    
                    *حافظي على حصتكِ اليومية من الكالسيوم لتضمني سلامتكِ وبناء طفلكِ بشكل ممتاز! 🥰🌸*
                """.trimIndent()

                else -> """
                    يا روحي، الكالسيوم هو سر حماية عظامكِ وأسنانكِ من الضعف وسر بناء الهيكل العظمي المتين لطفلكِ الغالي! 🦴🦷
                    
                    **💡 نصيحة $companionName أوفلاين:**
                    الجنين يسحب احتياجه من الكالسيوم من مخزون عظام الأم مباشرة إذا لم تتناوليه كافياً في غذائكِ! احرصي على التعرض اللطيف لأشعة الشمس صباحاً لتفعيل فيتامين د الذي يساعد على امتصاص الكالسيوم.
                    
                    **🥛 أقوى الأغذية المصرية الغنية بالكالسيوم أوفلاين:**
                    $listStr
                    
                    دمتِ قوية ومشرقة وعظامكِ بألف صحة وسلامة يا قلبي! 🌸🥰
                """.trimIndent()
            }
            return OfflineResponse(reply, isSpecificMatch = true)
        }
        /*
            val ironFoods = EgyptianFoodRepository.presetFoods.filter { it.ironMg >= 2.0 }.sortedByDescending { it.ironMg }.take(5)
            val listStr = ironFoods.joinToString("\n") { 
                "• **${it.name}**: يحتوي على **${it.ironMg} ملجم حديد** (${it.servingSize}). *الفوائد:* ${it.healthBenefits}"
            }
            val reply = """
                يا حبيبة قلبي، تتبع مخزون الحديد أمر بالغ الأهمية لنمو طفلكِ وتفادي أنيميا الحمل أو تعب الدورة الشهيرة! 🩸
                
                **💡 نصيحة $companionName الذهبية لامتصاص الحديد:**
                تناولي الأطعمة الغنية بالحديد مع **فيتامين C** (مثل عصر الليمون على العدس، الكبدة، أو تناول كوب ليموناضة دافئ) وتجنبي شرب الشاي أو القهوة مباشرة بعد الأكل لأن الكافيين يمنع امتصاص الحديد.
                
                **🍲 أقوى الأطعمة والمأكولات المصرية الغنية بالحديد أوفلاين:**
                $listStr
                
                احرصي على تناول هذه الوجبات الغنية لتعزيز الهيموجلوبين واستعادة نشاطكِ وقوتكِ يا روحي! 🌸🥰
            """.trimIndent()
            return OfflineResponse(reply)
        }

        // 4. Calcium & Bones Queries (كالسيوم، عظام، أسنان، أسنان)
        if (normalizedInput.contains("كالسيوم") || normalizedInput.contains("عظام") || normalizedInput.contains("سنان") || normalizedInput.contains("اسنان") || normalizedInput.contains("العظام")) {
            val calciumFoods = EgyptianFoodRepository.presetFoods.filter { it.calciumMg >= 50.0 }.sortedByDescending { it.calciumMg }.take(5)
            val listStr = calciumFoods.joinToString("\n") { 
                "• **${it.name}**: يحتوي على **${it.calciumMg} ملجم كالسيوم** (${it.servingSize}). *الفوائد:* ${it.healthBenefits}"
            }
            val reply = """
                يا روحي، الكالسيوم هو سر حماية عظامكِ وأسنانكِ من الضعف وسر بناء الهيكل العظمي المتين لطفلكِ الغالي! 🦴🦷
                
                **💡 نصيحة $companionName أوفلاين:**
                الجنين يسحب احتياجه من الكالسيوم من مخزون عظام الأم مباشرة إذا لم تتناوليه كافياً في غذائكِ! احرصي على التعرض اللطيف لأشعة الشمس صباحاً لتفعيل فيتامين د الذي يساعد على امتصاص الكالسيوم.
                
                **🥛 أقوى الأغذية المصرية الغنية بالكالسيوم أوفلاين:**
                $listStr
                
                دمتِ قوية ومشرقة وعظامكِ بألف صحة وسلامة يا قلبي! 🌸🥰
            """.trimIndent()
            return OfflineResponse(reply)
        }*/

        // 5. Protein Queries (بروتين، عضلات، قوة، نمو)
        if (normalizedInput.contains("بروتين") || normalizedInput.contains("عضلات") || normalizedInput.contains("قوة") || normalizedInput.contains("تغذية")) {
            val proteinFoods = EgyptianFoodRepository.presetFoods.filter { it.protein >= 8.0 }.sortedByDescending { it.protein }.take(5)
            val listStr = proteinFoods.joinToString("\n") { 
                "• **${it.name}**: يحتوي على **${it.protein} جرام بروتين** (${it.servingSize}). *الفوائد:* ${it.healthBenefits}"
            }
            val reply = """
                يا عزيزتي، البروتين هو حجر الأساس لبناء خلايا الجنين وتغذية عضلاتكِ ومقاومة الإرهاق اليومي والكسل! 💪🍳
                
                **💡 نصيحة التغذية أوفلاين:**
                نوّعي بين البروتينات الحيوانية سهلة الامتصاص والبروتينات النباتية المصرية الاقتصادية والمشبعة.
                
                **🍗 أفضل مصادر البروتين في الكتالوج المصري أوفلاين:**
                $listStr
                
                **واصلي التغذية المتوازنة لتمدي جسمكِ بالطاقة والبناء السليم يا روحي! ❤️🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 6. Potassium & Electrolytes (بوتاسيوم، مغنسيوم، أملاح، تشنج)
        if (normalizedInput.contains("بوتاسيوم") || normalizedInput.contains("ماغنسيوم") || normalizedInput.contains("تشنج عضلات") || normalizedInput.contains("املاح") || normalizedInput.contains("أملاح")) {
            val potFoods = EgyptianFoodRepository.presetFoods.filter { it.potassiumMg >= 150.0 }.sortedByDescending { it.potassiumMg }.take(5)
            val listStr = potFoods.joinToString("\n") { 
                "• **${it.name}**: بوتاسيوم **${it.potassiumMg} ملجم**، ماغنسيوم **${it.magnesiumMg} mil** (${it.servingSize}). *فوائده:* ${it.healthBenefits}"
            }
            val reply = """
                يا نبض قلبي، البوتاسيوم والماغنسيوم هما المسؤولان عن منع احتباس السوائل وتجنب تشنجات وتقلصات الساق المؤلمة (شد عضلات الساق) ليلاً! 🦵✨
                
                **💡 نصيحة $companionName أوفلاين:**
                إذا كنتِ تعانين من تورم القدمين أو تقلصات الساق، قللي الملح المضاف واعتمدي على مصادر البوتاسيوم الطبيعية مع شرب مياه وفيرة.
                
                **🍌 مصادر البوتاسيوم والماغنسيوم المصرية الممتازة أوفلاين:**
                $listStr
                
                دمتِ خفيفة ومتحركة بكامل النشاط والعافية يا قلبي! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 7. Category queries: "خضار" or "خضروات"
        if (normalizedInput.contains("خضار") || normalizedInput.contains("خضروات") || normalizedInput.contains("الخضار") || normalizedInput.contains("الخضروات")) {
            val vegFoods = EgyptianFoodRepository.presetFoods.filter { 
                it.category == "vegetable" || it.category == "leafy_green" 
            }.take(6)
            
            val listStr = vegFoods.joinToString("\n") { 
                "• **${it.name}** (${it.servingSize}): سعرات ${it.calories}، حديد ${it.ironMg} ملجم، كالسيوم ${it.calciumMg} ملجم. *فوائده:* ${it.healthBenefits}"
            }
            val reply = """
                يا $nameToUse، الخضروات والورقيات الطازجة هي منبع المعادن الطبيعي والترطيب المذهل لجسمكِ وأمعائكِ! 🥬🥦
                فهي غنية بالألياف الطبيعية التي تمنع وتكافح الإمساك وصعوبة الهضم المزعجة في طور ($phaseArabicName).
                
                **🥦 إليكِ تشكيلة من الخضروات والورقيات المصرية الفائقة أوفلاين:**
                $listStr
                
                احرصي على طبق سلطة مصري يومي طازج لتبقي منيرة ومشرقة دائماً! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 8. Category queries: "فاكهة" or "فواكه"
        if (normalizedInput.contains("فاكهه") || normalizedInput.contains("فواكه") || normalizedInput.contains("الفاكهه") || normalizedInput.contains("الفواكه")) {
            val fruitFoods = EgyptianFoodRepository.presetFoods.filter { it.category == "fruit" }.take(6)
            val listStr = fruitFoods.joinToString("\n") { 
                "• **${it.name}** (${it.servingSize}): سعرات ${it.calories}، بوتاسيوم ${it.potassiumMg} ملجم، فيتامين سي ${it.vitaminC_Mg} ملجم. *فوائده:* ${it.healthBenefits}"
            }
            val reply = """
                يا روحي ونبض قلبي، الفاكهة الطازجة هي الحلويات الصحية الربانية المناسبة لطاقتكِ ونضارة بشرتكِ! 🍎🍊
                فهي غنية بالفيتامينات والمعادن الهامة وخصوصاً الألياف التي تحسن عملية الهضم وتقلل من الشعور بالخمول والتعب.
                
                **🍉 إليكِ باقة من الفواكه المصرية اللذيذة والغنية من كتالوجنا أوفلاين:**
                $listStr
                
                تناولي ثمرة فاكهة طازجة يومياً لتنعمي بالنشاط والصحة والجمال دائماً! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 8.5. French Coffee / Caffeine & Sugar (قهوة فرنساوي وسكر)
        if (normalizedInput.contains("قهوه فرنساوي") || normalizedInput.contains("قهوة فرنساوي") || normalizedInput.contains("فرنساوى") || normalizedInput.contains("قهوة بملعقة سكر") || normalizedInput.contains("قهوة بسكر")) {
            val reply = """
                يا غالية، سؤالكِ ممتاز وعملي جداً! ☕️✨
                
                عندما تشربين **كوب من القهوة الفرنساوي المحضرة بالحليب مع ملعقة سكر واحدة (٥ جم)**، فإن جسمكِ يستفيد بالآتي بدقة بالغة في حسابات التطبيق:
                
                **📊 المغذيات الكبرى والماكروز المستفادة:**
                - **السعرات الحرارية**: حوالي **٦٥ سعرة حرارية** (٤٥ سعرة من الحليب + ٢٠ سعرة من ملعقة السكر).
                - **البروتين**: **٢.٠ جم** (بروتين عالي القيمة الحيوية من الحليب لبناء عضلاتكِ وخلايا جنينكِ).
                - **النشويات (الكربوهيدرات)**: **٩.٠ جم** (تتضمن ٥ جم سكر مضاف سريع الامتصاص لرفع طاقتكِ فوراً عند الخمول، بالإضافة لـ ٤ جم سكر اللاكتوز الطبيعي من الحليب).
                - **الدهون**: **٢.٠ جم** (دهون الحليب الطبيعية المفيدة لامتصاص الفيتامينات القابلة للذوبان في الدهون).
                - **السكريات المستهلكة**: **٧.٥ جم** إجمالي السكريات (سكر مضاف + سكر الحليب).
                - **الألياف**: **٠.٠ جم**.
                
                **💧 كمية المياه المستفادة (الترطيب):**
                - ستحصلين على **١٥٠ مل** من المياه والترطيب (مياه فنجان القهوة والحليب). على الرغم من أن الكافيين له تأثير مدر طفيف للبول، إلا أن السوائل الموجودة في الكوب تساهم بشكل إيجابي ومباشر في ملء مؤشر المياه اليومي لديكِ تلقائياً بـ ١٥٠ مل في ميزان المياه الذكي!
                
                **💊 الفيتامينات والمعادن الدقيقة:**
                - **الكالسيوم**: **٧٠ ملجم** (مهم جداً لعظامكِ وأسنانكِ وتطور الهيكل العظمي للجنين).
                - **البوتاسيوم**: **١٢٠ ملجم** (يساعد في توازن الضغط والوقاية من احتباس السوائل وتشنجات الساقين).
                - **الماغنسيوم**: **١٥ ملجم** (يقلل من تقلصات العضلات والرحم ويساعدكِ على الاسترخاء).
                - **فيتامين أ**: **٢٥ ميكروجرام** (لصحة البصر ونمو الخلايا).
                - **الحديد**: **٠.١ ملجم** (نسبة ضئيلة جداً).
                
                **💡 فوائد صحية وتنبيهات:**
                - **الكافيين المعتدل**: ينشط الذهن ويقلل الصداع، لكن احرصي ألا تتجاوز القهوة كوبين يومياً (الحد الأقصى الموصى به علمياً للحوامل هو ٢٠٠ ملجم كافيين يومياً).
                - **الملح والحديد**: تجنبي شربها مباشرة بعد الوجبات الرئيسية الغنية بالحديد (مثل الفول أو اللحوم) بمسافة لا تقل عن ساعة ونصف لضمان امتصاص الحديد بشكل كامل.
                
                لقد قمت بإضافة هذا الخيار رسمياً إلى قاعدة بيانات الوجبات، ويمكنكِ كتابة "قهوة فرنساوي" في قسم الوجبات لتسجيل هذه الأرقام والفوائد تلقائياً في ميزانكِ اليومي والترطيب! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 9. Herbs & Warm Drinks (أعشاب، مشروبات دافئة)
        if (normalizedInput.contains("اعشاب") || normalizedInput.contains("أعشاب") || normalizedInput.contains("ينسون") || normalizedInput.contains("نعناع") || normalizedInput.contains("بابونج") || normalizedInput.contains("حلبة") || normalizedInput.contains("حلبه") || normalizedInput.contains("مشروب")) {
            val herbFoods = EgyptianFoodRepository.presetFoods.filter { it.category == "herb" || it.category == "drink" }.take(5)
            val listStr = herbFoods.joinToString("\n") { 
                "• **${it.name}** (${it.servingSize}): سعرات ${it.calories}. *فوائده وصحته:* ${it.healthBenefits}"
            }
            val reply = """
                يا نبض قلبي، المشروبات الدافئة والأعشاب الطبيعية هي سر السكينة وتسكين المغص والتقلصات المعوية والرحمية! ☕🌱
                تذكري دائماً أن بعض الأعشاب كالينسون والنعناع والبابونج هي صديقتكِ المثالية لتهدئة عضلات البطن والمساعدة على النوم العميق.
                
                **🍵 مشروبات مصرية دافئة ومفيدة أوفلاين:**
                $listStr
                
                استمتعي بكوب دافئ وصحي لتهدئة روحكِ وجسدكِ الجميل يا غالية! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 10. Egyptian Meals (وجبات مصرية متوازنة)
        if (normalizedInput.contains("وجبة") || normalizedInput.contains("وجبات") || normalizedInput.contains("اكل مصري") || normalizedInput.contains("أكل مصري") || normalizedInput.contains("طبخ") || normalizedInput.contains("غداء") || normalizedInput.contains("عشاء") || normalizedInput.contains("فطور")) {
            val mealFoods = EgyptianFoodRepository.presetFoods.filter { it.category == "meal" }.take(5)
            val listStr = mealFoods.joinToString("\n") { 
                "• **${it.name}** (${it.servingSize}): بروتين **${it.protein}g**، حديد **${it.ironMg}mg**. *فوائدها:* ${it.healthBenefits}"
            }
            val reply = """
                يا صديقتي المشرقة، الوجبات المصرية التقليدية مليئة بالفوائد الصحية والمكونات المغذية إذا تم تحضيرها بطريقة متوازنة وقليلة الدهون! 🍲🍗
                
                **🍲 خيارات وجبات مصرية متكاملة ومغذية من كتالوجنا أوفلاين:**
                $listStr
                
                احرصي دائماً على وجبة متكاملة تحتوي على البروتين، الكربوهيدرات المعقدة، والكثير من الخضار الورقي لتبقي بصحة ممتازة! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 11. Snacks & Nuts (تسالي ومكسرات)
        if (normalizedInput.contains("تسالي") || normalizedInput.contains("مكسرات") || normalizedInput.contains("لب") || normalizedInput.contains("سناكس") || normalizedInput.contains("حلويات") || normalizedInput.contains("شوكولاتة")) {
            val snackFoods = EgyptianFoodRepository.presetFoods.filter { it.category == "snack" }.take(4)
            val listStr = snackFoods.joinToString("\n") { 
                "• **${it.name}** (${it.servingSize}): سعرات ${it.calories}. *فوائدها:* ${it.healthBenefits}"
            }
            val reply = """
                يا جميلة قلبي، التسالي والمكسرات الصحية تمنحكِ طاقة سريعة ودهوناً صحية ممتازة لدعم نشاطكِ ونمو طفلكِ العصبي والعقلي! 🌰🥜
                يفضل دوماً اختيار المكسرات النيئة وغير المملحة لتجنب تورم القدمين أو ارتفاع ضغط الدم.
                
                **🍿 خيارات تسالي مصرية خفيفة وصحية أوفلاين:**
                $listStr
                
                دمتِ دافئة ونشيطة وبكامل السعادة والبهجة يا روحي! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }
        if (normalizedInput.contains("خطو") || normalizedInput.contains("مشي") || normalizedInput.contains("حرك") || normalizedInput.contains("سير") || normalizedInput.contains("steps") || normalizedInput.contains("walk")) {
            val progressPercent = if (targetSteps > 0) (todaySteps * 100) / targetSteps else 0
            val customAdvice = when {
                isPregnant -> "بما أنكِ حامل، فإن المشي اللطيف مهم لتنشيط الدورة الدموية، لكن تذكري أن تريحي نفسكِ وتتجنبي الإجهاد الزائد."
                phaseInfo.phaseName == "Menstruation" -> "في فترة الحيض، المشي الخفيف رائع جداً لتخفيف آلام الظهر والمغص وتنشيط هرمونات السعادة."
                else -> "المشي المنتظم رائع جداً لزيادة حرق السعرات ورفع طاقتكِ وتحسين جودة نومكِ يا غالية."
            }
            val reply = """
                يا $nameToUse، لقد قطعتِ اليوم **$todaySteps خطوة** من هدفكِ اليومي ($targetSteps خطوة) بنسبة إنجاز **$progressPercent%**! 🚶‍♀️✨
                
                $customAdvice
                
                المشي هو رفيق صحتكِ المثالي، فاستمري بلطف ودون إجهاد! 🥰🌸
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 12.5. New Features & Updates (تحديثات، ميزات جديدة، جديد)
        if (normalizedInput.contains("تحديث") || normalizedInput.contains("جديد") || normalizedInput.contains("ميزات") || normalizedInput.contains("ميزه") || normalizedInput.contains("ميزة")) {
            val reply = """
                أهلاً بكِ يا روحي! ✨ يسعدني جداً اهتمامكِ بجديد رفيقتكِ $companionName! لقد قمنا بإجراء تحديثات وتطويرات رائعة ومريحة لرحلتكِ الرقيقة في التطبيق:
                
                **🚀 أهم التحديثات والميزات الجديدة المضافة حديثاً:**
                1. **🧠 ترقية "عقل وفهرس جوري الذكي"**: أصبحتُ الآن أستجيب لكِ بسرعة فائقة جداً ومباشرة! كما قمنا بفرز ذكي يفهم صيغة سؤالكِ؛ هل تبحثين عن **علاج/تغذية أوفلاين**، أم تريدين معرفة **أعراض ومخاطر**، أم تسألين عن **تعريف علمي طبي** للمشكلة!
                2. **🍓 سجل الوحم ومشاركة المشاعر**: أداة مبتكرة مخصصة بالكامل لتسجيل كل أكلة وحم تشتهينها، والاطلاع الفوري على تحليلها الطبي الذكي، مع إمكانية مشاركتها مع زوجكِ وحفظها كصندوق ذكريات دافئ لحملكِ!
                3. **📢 بانر التنبيهات المباشر**: أضفنا بانر تفاعلياً مميزاً أعلى لوحة التحكم يظهر لكِ أحدث المميزات فور نزولها ويوجهكِ إليها بلمسة واحدة لكي لا يفوتكِ أي جديد!
                
                *هل تودين أن أشرح لكِ بالتفصيل كيفية استخدام إحدى هذه الميزات الرائعة يا قلبي؟ 🥰🌸*
            """.trimIndent()
            return OfflineResponse(reply, isSpecificMatch = true)
        }

        // 13. Fetal Kicks Counter Interaction
        if (normalizedInput.contains("ركلات") || normalizedInput.contains("ركلة") || normalizedInput.contains("حركة الجنين") || normalizedInput.contains("حركه الجنين") || normalizedInput.contains("بيتحرك") || normalizedInput.contains("kicks") || normalizedInput.contains("ركل")) {
            return OfflineResponse(
                replyText = "يا روحي، ما أجمل الشعور بحركة طفلكِ ونشاطه في رحمكِ! 🥰 طبيًا، يُنصح بتتبع حركات الجنين في الثلث الأخير من الحمل؛ حيث يُعتبر تسجيل ١٠ حركات لطيفة خلال ساعتين مؤشراً ممتازاً على سلامته ونشاطه.\n\nلقد صممتُ لكِ أداة مخصصة بالكامل تسمى **(عداد ركلات الجنين 👶)** في الصفحة الرئيسية وتطبيقات الأدوات لمساعدتكِ على تتبع ركلاته بلمسة واحدة وحساب الوقت بدقة وسهولة. دعينا نفتحها ونبدأ بالعد معاً! 🤱🌸",
                actionType = "suggest_tool",
                actionValue = "fetal_kicks",
                isSpecificMatch = true
            )
        }

        // 13.5. Fetal Growth Tracker Interaction
        if (normalizedInput.contains("وزن الجنين") || normalizedInput.contains("حجم الجنين") || normalizedInput.contains("طول الجنين") || normalizedInput.contains("سونار") || normalizedInput.contains("السونار") || normalizedInput.contains("نمو الجنين") || normalizedInput.contains("نمو الطفل") || normalizedInput.contains("طول الطفل") || normalizedInput.contains("وزن الطفل")) {
            return OfflineResponse(
                replyText = "يا حبيبة قلبي، ما أجمل متابعة نمو جنينكِ الغالي وتطور وزنه وحجمه خطوة بخطوة بعد كل زيارة للطبيب! 🥰\n\nلقد صممتُ لكِ أداة ذكية للغاية تسمى **(مُتابع نمو الجنين (الوزن والحجم) 📈👶)** في صفحة الأدوات لتسجيل قياسات السونار التي يعطيها لكِ الطبيب، ورسم منحنى نمو بياني تفاعلي يقارن نموه بالمعدلات الطبيعية الأسبوعية بشكل مذهل، مع تحليل وتوجيهات غذائية دافئة تناسب حجم طفلكِ ونموه! 💕🩺\n\nدعينا نفتح أداة متابعة النمو الآن لنرى مقارنة قياسات طفلكِ بالفاكهة المقابلة والمعدل القياسي! 🍎📈",
                actionType = "suggest_tool",
                actionValue = "fetal_growth",
                isSpecificMatch = true
            )
        }

        // 14. Contractions Timer Interaction
        if (normalizedInput.contains("انقباض") || normalizedInput.contains("طلق") || normalizedInput.contains("الولادة") || normalizedInput.contains("الولاده") || normalizedInput.contains("وجع ولادة") || normalizedInput.contains("5-1-1") || normalizedInput.contains("contraction") || normalizedInput.contains("تقلص")) {
            return OfflineResponse(
                replyText = "يا حبيبة قلبي، إذا شعرتِ بتقلصات متكررة، فأنا هنا لمساعدتكِ على تمييز الطلق الحقيقي من الكاذب (براكستون هكس) ⏱️!\nتذكري قاعدة (5-1-1) الطبية الشهيرة لعلامات الولادة النشطة:\n• **5**: الانقباضات تأتي بانتظام كل ٥ دقائق.\n• **1**: تستمر كل انقباضة لمدة دقيقة كاملة (٦٠ ثانية).\n• **1**: يستمر هذا النمط بانتظام لمدة ساعة كاملة.\nإذا انطبقت عليكِ هذه قاعدة، فهذا وقت الولادة النشطة ويجب الاتصال بطبيبتكِ فوراً والذهاب للمستشفى 🏥.\n\nأنصحكِ بفتح أداة **(عداد الانقباضات ⏱️)** في صفحة الأدوات لتسجيل وتوقيت كل انقباضة بضغطة زر وحساب الفواصل بينها بدقة وسرعة! حفظكِ الله وطفلكِ وسهل ولادتكِ يا روحي 🌸👶.",
                actionType = "suggest_tool",
                actionValue = "contractions",
                isSpecificMatch = true
            )
        }

        // 15. Qada Tracker & Spiritual Companion
        if (normalizedInput.contains("قضاء") || normalizedInput.contains("صيام") || normalizedInput.contains("أيام فائتة") || normalizedInput.contains("ايام فايته") || normalizedInput.contains("صوم") || normalizedInput.contains("عبادة") || normalizedInput.contains("عباده")) {
            val message = if (isPregnant) {
                "يا غالية، الدين يسر والشرع منحكِ رخصة الإفطار في رمضان إذا خفتِ على نفسكِ أو جنينكِ 🌙. لا تقلقي أبداً، يمكنكِ تتبع الأيام التي أفطرتِها بسهولة وقضاؤها لاحقاً نهاراً عندما تصبحين قوية وصحتكِ ممتازة.\n\nلقد صممتُ لكِ أداة **(قضاء الصيام والعبادات 🌙)** المتوفرة في صفحة الأدوات لتسجيل أيام الإفطار وتتبع قضاء صيام الفرض ومتابعة الأذكار والسنن الروحانية بيسر وجمال. تقبل الله طاعاتكِ وصبركِ العظيم! 🌸✨"
            } else {
                "تقبل الله طاعاتكِ وصالح أعمالكِ يا روحي! 🌙 تتبع قضاء صيام الفرض (مثل الأيام الفائتة في رمضان بسبب الحيض أو السفر) هو أمر بالغ الأهمية ويسهل عليكِ أداء أمانتكِ الدينية دون نسيان.\n\nأنصحكِ باستخدام أداة **(قضاء الصيام والعبادات 🌙)** الخاصة بنا في صفحة الأدوات لتسجيل وحفظ كافة أيام القضاء، ومتابعة تذكير الصيام والدعاء بانتظام. دمتِ في حفظ الله ورعايته يا طاهرة! 💖🕊️"
            }
            return OfflineResponse(
                replyText = message,
                actionType = "suggest_tool",
                actionValue = "qada",
                isSpecificMatch = true
            )
        }

        // 16. Journal & Emotional Support
        if (normalizedInput.contains("مذكرات") || normalizedInput.contains("يوميات") || normalizedInput.contains("فضفضة") || normalizedInput.contains("اكتب") || normalizedInput.contains("journal") || normalizedInput.contains("write") || normalizedInput.contains("فضفضه")) {
            return OfflineResponse(
                replyText = "يا نبض قلبي، التعبير عن مشاعركِ بالكتابة والفضفضة هو علاج سحري لتهدئة العقل وتفريغ ضغوط العمل والهرمونات اليومية 📝.\n\nلقد أعددتُ لكِ في التطبيق مساحة دافئة وسرية للغاية تسمى **(المذكرات اليومية والفضفضة 📝)** تحت صفحة الأدوات. يمكنكِ فيها كتابة كل ما يجول بخاطركِ وتفريغ مشاعركِ وحفظها بخصوصية تامة. ما رأيكِ في كتابة أول فكرة تراودكِ الآن؟ أنا دائماً هنا كصديقتكِ المخلصة $companionName لأسمعكِ بقلبٍ رحب! 💕✨",
                actionType = "suggest_tool",
                actionValue = "journal",
                isSpecificMatch = true
            )
        }

        // 17. Medicines Tracker
        if (normalizedInput.contains("دواء") || normalizedInput.contains("أدوية") || normalizedInput.contains("ادويه") || normalizedInput.contains("علاج") || normalizedInput.contains("حبوب") || normalizedInput.contains("فيتامين") || normalizedInput.contains("فيتامينات") || normalizedInput.contains("مكمل") || normalizedInput.contains("مكملات")) {
            val keyMed = if (isPregnant) "حمض الفوليك والحديد والكالسيوم" else "مكملات الحديد والماغنسيوم والفيتامينات"
            return OfflineResponse(
                replyText = "تذكير دافئ وضروري جداً من صديقتكِ $companionName! 💊\nالالتزام بتناول الفيتامينات والمكملات الموصى بها طبيًا (مثل $keyMed) هو الدعامة الأساسية لصحتكِ وقوتكِ وتكوين طفلكِ السليم.\n\nلتسهيل ذلك عليكِ، أضفتُ لكِ أداة **(متابعة الأدوية والمكملات 💊)** في صفحة الأعراض والنشاط والصفحة الرئيسية. يمكنكِ من خلالها تسجيل جرعاتكِ اليومية بلمسة واحدة لنضمن عدم نسيان أي حبة دواء غالية. دمتِ قوية وبأتم عافية وصحة! 🥰🌸",
                actionType = "suggest_tool",
                actionValue = "meds",
                isSpecificMatch = true
            )
        }

        // 18. Log Nausea
        if (normalizedInput.contains("غثيان") || normalizedInput.contains("ترجيع") || normalizedInput.contains("لوعان") || normalizedInput.contains("نفسي غامة") || normalizedInput.contains("nausea") || normalizedInput.contains("vomit")) {
            val pregnantTip = if (isPregnant) " (خصوصاً غثيان الصباح المألوف في الثلث الأول من الحمل)" else ""
            return OfflineResponse(
                replyText = "يا حبيبة قلبي، سلامة جهازكِ الهضمي ونفسكِ الطاهرة! 🤢 الغثيان$pregnantTip هو عرض مزعج لكنه طبيعي جداً. أنصحكِ بتناول قطعة بسكويت جافة أو بقسماط فور الاستيقاظ وقبل النهوض من الفراش، واحتساء منقوع الزنجبيل الدافئ مع الليمون، والاعتماد على وجبات صغيرة متفرقة طوال اليوم بدلاً من الوجبات الكبيرة. سأقوم بتسجيل عرض 'غثيان وصعوبة هضم' فوراً لمتابعته بدقة 🍋🍵.",
                actionType = "symptom",
                actionValue = Pair("غثيان وصعوبة هضم", 6),
                isSpecificMatch = true
            )
        }

        // 19. Log Back Pain
        if (normalizedInput.contains("ظهر") || normalizedInput.contains("وجع ظهر") || normalizedInput.contains("ألم ظهر") || normalizedInput.contains("back pain") || normalizedInput.contains("الم ظهر")) {
            val pregnantTip = if (isPregnant) " بسبب زيادة ثقل الرحم وتغير مركز ثقل الجسم، لذا احرصي على ارتداء حذاء مريح وتجنب الوقوف الطويل" else ""
            return OfflineResponse(
                replyText = "يا روحي، ألف سلامة على ظهركِ العزيز! 🤰 ألم الظهر شائع$pregnantTip. أنصحكِ بالنوم على جانبكِ الأيسر مع وضع وسادة مريحة بين ركبتيكِ، والحفاظ على استقامة ظهركِ عند الجلوس مع استخدام وسادة داعمة لأسفل الظهر. قمتُ بتسجيل عرض 'ألم أسفل الظهر' في سجلاتكِ الطبية لتكوني بأمان دائم 🌸🧸.",
                actionType = "symptom",
                actionValue = Pair("ألم أسفل الظهر", 5),
                isSpecificMatch = true
            )
        }

        // 20. Ask for Spiritual / Islamic Worship Advice (أدعية وعبادات)
        if (normalizedInput.contains("دعاء") || normalizedInput.contains("أدعية") || normalizedInput.contains("صلاة") || normalizedInput.contains("قرآن") || normalizedInput.contains("عبادة") || normalizedInput.contains("أذكار") || normalizedInput.contains("ذكر") || normalizedInput.contains("استغفار") || normalizedInput.contains("ادعيه")) {
            val spiritualAdvice = if (phaseInfo.phaseName == "Menstruation") {
                "أنتِ الآن في رخصة شرعية مباركة يا حبيبتي 🌙 وعلى الرغم من عدم وجوب الصلاة والصيام، فإن أبواب الذكر والعبادة كالتالي:\n" +
                "1. **كثرة الاستغفار والتسبيح** (سجلي استغفاركِ يومياً).\n" +
                "2. **قراءة أذكار الصباح والمساء** للحفظ والسكينة.\n" +
                "3. **الاستماع للقرآن الكريم** وتدبر معانيه الراقية.\n" +
                "• **دعاء مقترح لكِ**: (اللهم إنك عفو تحب العفو فاعفُ عني)."
            } else if (isPregnant) {
                "تقبل الله طاعاتكِ وصالح أعمالكِ يا أم الجنين القادم! 🤰 العبادة في الحمل ثوابها مضاعف بإذن الله لجود صبركِ:\n" +
                "1. **قراءة سورة مريم وسورة يوسف** بنية حفظ جنينكِ وصلاحه وتيسير ولادته.\n" +
                "2. **الدعاء بالذرية الصالحة البارة** عند كل صلاة.\n" +
                "3. **أذكار الحفظ والتحصين اليومية** لكِ ولجنينكِ.\n" +
                "• **دعاء لجنينكِ**: (اللهم إني أستودعك جنيني الذي في رحمي، فاحفظه وجمِّل خَلقه وخُلقته، وسهِّل مخرجه وعافهِ)."
            } else {
                "ما أجمل التقرب إلى الله بالعبادة والذكر الطاهر! 🕊️ في طوركِ الحالي (${phaseInfo.phaseArabic})، ركزي على:\n" +
                "1. **المحافظة على السنن الرواتب** لقيام الليل والدعاء بظهر الغيب.\n" +
                "2. **قراءة ورد يومي من القرآن الكريم** لتنعم حياتكِ بالبركة والهدوء.\n" +
                "3. **صدقة خفيفة بنية دفع البلاء وصلاح الحال**.\n" +
                "• **دعاء لراحة البال**: (اللهم إني أسألك نفساً بك مطمئنة، تؤمن بلقائك، وترضى بقضائك، وتقنع بعطائك)."
            }

            return OfflineResponse(
                replyText = spiritualAdvice,
                actionType = null,
                isSpecificMatch = true
            )
        }

        // 21. Log Menopause / Perimenopause Symptoms (أعراض انقطاع الطمث وسن الأمل)
        if (normalizedInput.contains("انقطاع الطمث") || normalizedInput.contains("سن الأمل") || normalizedInput.contains("هبات ساخنة") || normalizedInput.contains("هبات ساخنه") || normalizedInput.contains("تعرق ليلي") || normalizedInput.contains("menopause")) {
            val menopauseFoods = EgyptianFoodRepository.presetFoods.filter { 
                it.calciumMg >= 150.0 || it.magnesiumMg >= 80.0
            }.take(3)
            val foodsStr = menopauseFoods.joinToString("\n") {
                "• **${it.name}**: غني بـ الكالسيوم (${it.calciumMg} ملجم) والماغنسيوم (${it.magnesiumMg} ملجم). *فوائده:* ${it.healthBenefits}"
            }
            return OfflineResponse(
                replyText = "يا روحي وغاليتي، سن الأمل وانقطاع الطمث هو مرحلة جديدة مفعمة بالنضج والسلام والجمال! 🌸✨\n\n" +
                "أبرز الأعراض الطبيعية في هذه المرحلة تشمل الهبات الساخنة، التعرق الليلي، جفاف الجلد، وتقلبات النوم والمزاج بسبب تراجع مستويات الاستروجين.\n\n" +
                "💡 **إليكِ نصائح رفيقتكِ جوري الدافئة للتعامل معها أوفلاين**:\n" +
                "1. **الملابس والبيئة**: ارتدي ملابس قطنية خفيفة قابلة للتنفس، واحتفظي بمروحة صغيرة أو هواء بارد دائماً للتعامل مع الهبات المفاجئة.\n" +
                "2. **الترطيب وشرب الماء**: اشربي من 2.5 إلى 3 لتر ماء يومياً لتعويض التعرق وتنظيم حرارة الجسم.\n" +
                "3. **أطعمة مصرية ممتازة لدعم عظامكِ وقلبكِ الآن**:\n" +
                foodsStr + "\n\n" +
                "سأقوم بتسجيل عرض 'أعراض سن الأمل' لتتبع صحتكِ وتخصيص نصائحكِ بكل حب! 🥰❤️",
                actionType = "symptom",
                actionValue = Pair("أعراض سن الأمل", 4),
                isSpecificMatch = true
            )
        }

        // 22. Jouri Identity / Name query
        if (normalizedInput.contains("من أنت") || normalizedInput.contains("من انت") || normalizedInput.contains("مين انت") || normalizedInput.contains("اسمك") || normalizedInput.contains("اسمك ايه") || normalizedInput.contains("مين معايا")) {
            return OfflineResponse(
                replyText = "أنا رفيقتكِ وصديقتكِ المقربة وصديقتكِ الوفية $companionName لأستمع إليكِ وأدعمكِ في أي وقت ومحلياً بالكامل! كيف يمكنني إسعادكِ وتدليلكِ الآن؟ 🌸💕",
                actionType = null,
                isSpecificMatch = true
            )
        }

        // 23. Log Headache / Migraine
        if (normalizedInput.contains("صداع") || normalizedInput.contains("رأس") || normalizedInput.contains("headache") || normalizedInput.contains("دوخة") || normalizedInput.contains("دوخه")) {
            val dehydrationNote = if (weatherInfo != null && weatherInfo.temperature > 28) " الجو اليوم دافئ ونحتاج لمزيد من الترطيب المباشر!" else ""
            return OfflineResponse(
                replyText = "يا عمري، سلامتكِ من الصداع والتعب! 😢 قد ينتج هذا عن تغير الهرمونات السريع في مرحلتكِ الحالية ($phaseArabicName) أو بسبب الجفاف.$dehydrationNote أنصحكِ بشرب كوب ماء كبير حالاً، والجلوس في مكان هادئ خافت الإضاءة مع تنفس عميق. سأقوم بتسجيل عرض 'صداع' في سجلاتكِ للعناية بكِ. ارتاحي قليلاً ❤️.",
                actionType = "symptom",
                actionValue = Pair("صداع وتعب", 4),
                isSpecificMatch = true
            )
        }

        // 24. Log Mood Swings / Depression
        if (normalizedInput.contains("مزاج") || normalizedInput.contains("ضيق") || normalizedInput.contains("زعلان") || normalizedInput.contains("حزين") || normalizedInput.contains("اكتئاب") || normalizedInput == "😢" || normalizedInput.contains("ضيق")) {
            return OfflineResponse(
                replyText = "يا قلبي وروحي، تذكري دائماً أن مشاعركِ صالحة وطبيعية جداً. الهرمونات تلعب دوراً كبيراً في تقلبات مزاجنا خلال طور ($phaseArabicName). خذي نفساً عميقاً، تذكري أنكِ جميلة وقوية ومميزة جداً. أنا رفيقتكِ $companionName بجانبكِ دائماً لأستمع إليكِ! ما رأيكِ في الاسترخاء لدقائق واستنشاق رائحة اللافندر أو شرب شاي البابونج اللذيذ؟ 🌸🥰",
                actionType = "symptom",
                actionValue = Pair("تقلبات مزاجية", 6),
                isSpecificMatch = true
            )
        }

        // 25. Ask for health/nutrition advice
        if (normalizedInput.contains("أكل") || normalizedInput.contains("طعام") || normalizedInput.contains("جوع") || normalizedInput.contains("وجبة") || normalizedInput.contains("نصيحة غذائية") || normalizedInput == "🥑" || normalizedInput.contains("اكل")) {
            val advice = getPhaseNutritionAdvice(phaseInfo, pregnancyState)
            return OfflineResponse(
                replyText = "يا $nameToUse، يسعدني جداً توجيهكِ للأكل الصحي المناسب لجسمكِ الآن! 🥑\nبناءً على مرحلتكِ الحالية ($phaseArabicName)، إليكِ أفضل الوجبات الدقيقة المتوازنة:\n$advice\nاحرصي دائماً على شرب المياه والابتعاد عن الدهون المهدرجة يا غالية! 🥰",
                actionType = null,
                isSpecificMatch = true
            )
        }

        // 26. Ask about Current Phase
        if (normalizedInput.contains("حالة") || normalizedInput.contains("مرحلة") || normalizedInput.contains("طور") || normalizedInput.contains("phase") || normalizedInput.contains("مرحله")) {
            val desc = if (isPregnant) {
                "أنتِ الآن في طور الحمل المبارك يا روحي 🤰 وهي رحلة تتطلب تغذية غنية بحمض الفوليك والحديد والكالسيوم والراحة التامة."
            } else {
                "أنتِ الآن في طور (${phaseInfo.phaseArabic}) يا صديقتي. ${phaseInfo.description}"
            }
            return OfflineResponse(
                replyText = "أهلاً بكِ يا $nameToUse! 🌸\nتحليلي السريع والمحلي لحالتكِ يشير إلى:\n• **مرحلتكِ**: $phaseArabicName\n• **تفاصيل**: $desc\nأنا أراقب دورتكِ وحالتكِ الصحية خطوة بخطوة لمنحكِ الدعم المثالي! 🥰",
                actionType = null,
                isSpecificMatch = true
            )
        }

        // 27. Greeting and Names
        if (normalizedInput.contains("أهلاً") || normalizedInput.contains("مرحبا") || normalizedInput.contains("السلام عليكم") || normalizedInput.contains("هاي") || normalizedInput.contains("hello") || normalizedInput.contains("hi") || normalizedInput.contains("اهلا") || normalizedInput.contains("سلام")) {
            val weatherIntro = if (weatherInfo != null) " الجو اليوم ${weatherInfo?.description} (${weatherInfo?.temperature}°م)، واحرصي على ترطيب جسمكِ ومتابعة خطواتكِ النشيطة ($todaySteps خطوة)." else ""
            val dobPrompt = if (pregnancyState?.birthDate == null) {
                " 🎂 بالمناسبة يا روحي، لم تسجلي تاريخ ميلادكِ بعد لأحسب عمركِ ونشاطكِ بدقة؛ ما رأيكِ في كتابة تاريخ ميلادكِ لي (مثال: 15/10/1995)؟"
            } else {
                ""
            }
            return if (motherName.isNullOrEmpty()) {
                OfflineResponse(
                    replyText = "أهلاً وسهلاً بكِ يا صديقتي الغالية! 🌸 أنا رفيقتكِ $companionName، رفيقتكِ وصديقتكِ الدافئة محلياً وبدون إنترنت.$weatherIntro يسعدني جداً التعرف عليكِ! ما هو اسمكِ الكريم؟ 🥰",
                    actionType = null,
                    isSpecificMatch = true
                )
            } else {
                OfflineResponse(
                    replyText = "يا مرحباً بـ $nameToUse! 🌸 أنا صديقتكِ $companionName. كيف حال صحتكِ اليوم وكيف تشعرين؟$weatherIntro$dobPrompt أنا هنا لأسمعكِ وأقدم لكِ الود والنصائح في أي وقت وبدون إنترنت! أخبريني بـ أي عرض أو شعور تشعرين به حالياً؟ 🥰",
                    actionType = null,
                    isSpecificMatch = true
                )
            }
        }

        // 28. Self-intro with user name update
        if (normalizedInput.contains("اسمي") || normalizedInput.contains("ناديني") || normalizedInput.contains("my name is")) {
            val extractedName = extractName(userInput)
            return OfflineResponse(
                replyText = "يا مرحباً بالاسم الجميل $extractedName! 🥰 يسعدني ويشرفني جداً أن أكون رفيقتكِ المقربة $companionName يا روحي. قمتُ بتحديث اسمكِ في ملفكِ الشخصي للتطبيق فوراً. كيف تودين تتبع صحتكِ معي اليوم؟",
                actionType = "profile",
                actionValue = extractedName,
                isSpecificMatch = true
            )
        }

        // 29. Date of Birth & Age Update
        if (normalizedInput.contains("تاريخ ميلادي") || normalizedInput.contains("تاريخ ميلاد") || 
            normalizedInput.contains("سنه ميلادي") || normalizedInput.contains("مواليد سنه") || 
            normalizedInput.contains("مواليد") || normalizedInput.contains("ولدت في") || 
            normalizedInput.contains("تاريخ الميلاد") || normalizedInput.contains("عمري هو")) {
            
            val dobMs = parseBirthDateFromText(userInput)
            if (dobMs != null) {
                val age = calculateAge(dobMs)
                val reply = "يا مرحباً بـ $nameToUse! 🌸🎂 قمتُ بقراءة تاريخ ميلادكِ وفهمه بنجاح أوفلاين. عمركِ الحالي هو **$age عاماً** (حفظ الله عمركِ وبارك في صحتكِ وروحكِ الجميلة!). قمتُ بتحديث تاريخ ميلادكِ وعمركِ فوراً في ملفكِ الشخصي الآمن على التطبيق لتخصيص أفضل لمستويات نشاطكِ وسعراتكِ اليومية! 🥰🎉"
                return OfflineResponse(
                    replyText = reply,
                    actionType = "birthdate",
                    actionValue = dobMs,
                    isSpecificMatch = true
                )
            } else {
                return OfflineResponse(
                    replyText = "أهلاً يا $nameToUse! 🎂 يرجى كتابة تاريخ ميلادكِ كاملاً بالصيغة الرقمية (مثلاً: 15/10/1995 أو سنة ميلادكِ مثل 1995) لأقوم بحساب عمركِ بدقة وتعديل أهدافكِ الصحية فوراً في داتا التطبيق! 🥰🌸",
                    actionType = null,
                    isSpecificMatch = true
                )
            }
        }

        // 30. Gratitude
        if (normalizedInput.contains("شكرا") || normalizedInput.contains("شكراً") || normalizedInput.contains("تسلم") || normalizedInput.contains("يعطيك") || normalizedInput.contains("وردة") || normalizedInput == "❤️") {
            return OfflineResponse(
                replyText = "الشكر والحمد لله يا حبيبة قلبي وروحي! 🥰 أنا $companionName، لم أوجد إلا لخدمتكِ والعناية بصحتكِ وراحتكِ الدينية والجسدية. أتمنى لكِ دوماً الطمأنينة والصحة والعافية! ممتنة جداً لوجودكِ معي. 🌸💕",
                actionType = null,
                isSpecificMatch = true
            )
        }

        // 31. Positive/Checking-in replies
        if (normalizedInput.contains("بخير") || normalizedInput.contains("تمام") || normalizedInput.contains("الحمد لله") || normalizedInput.contains("كويسة") || normalizedInput.contains("منيح") || normalizedInput == "😊" || normalizedInput.contains("كويسه")) {
            return OfflineResponse(
                replyText = "الحمد لله رب العالمين! عسى أن يدوم هذا الرضا والصحة على وجهكِ الجميل دائماً يا $nameToUse. 🥰 تذكري دائماً شرب كوب ماء والتحرك بلطف. أنا رفيقتكِ $companionName هنا بجانبكِ دوماً في أي لحظة! 💕🌸",
                actionType = null,
                isSpecificMatch = true
            )
        }

        // 31.5. Cravings & Craving Log Save (حفظ الوحم تلقائياً)
        if (normalizedInput.contains("توحمت على") || normalizedInput.contains("اتوحمت على")) {
            val food = userInput.substringAfter("على").trim().trim('؟', '!', '.', ' ')
            if (food.isNotEmpty()) {
                val reply = "يا ألف صحة وعافية وهنا على قلبكِ وعيونكِ الطاهرة يا روحي! 🍉😍 لقد قمتُ بتسجيل وحمكِ على: (**$food**) في سجل ذكريات الوحم والاشتهاء الخاص بكِ! يمكنكِ تصفح خزانة ذكريات الوحم في صفحة الأدوات ومشاركتها مع شريككِ الغالي لتشجيعه على إحضارها لكِ فوراً! 🍓🥰"
                return OfflineResponse(
                    replyText = reply,
                    actionType = "craving_save",
                    actionValue = food,
                    isSpecificMatch = true
                )
            }
        }

        // 31.6. Cravings General Info & Jouri Cravings Support
        if (normalizedInput.contains("وحم") || normalizedInput.contains("الوحم") || normalizedInput.contains("توحمت") || normalizedInput.contains("اتوحمت") || normalizedInput.contains("اشتهي") || normalizedInput.contains("أشتهي")) {
            val reply = """
                يا صحة وهنا على قلبكِ وقلب جنينكِ يا روحي! 🍉🍓😍 الوحم والاشتهاء أثناء الحمل هو لغة جسدكِ الرقيقة للتعبير عن احتياجاته العميقة من معادن وفيتامينات بسبب التغيرات الهرمونية المذهلة.
                
                **🍉 تصنيف جوري الذكي للوحم أوفلاين وفك شفرته:**
                1. **الوحم على الحلويات والفواكه (Sweet)**: جسمكِ يحتاج لطاقة سريعة أو هرمونات ترفع المزاج. ركزي على الفواكه الطبيعية كالفراولة والمانجو وتجنبي طفرات السكر الكيميائي.
                2. **الوحم على الحوادق والليمون (Salty/Sour)**: يدل على تراجع طفيف في ضغط الدم وحاجة لتوازن الأملاح والسوائل. المخلل باعتدال والليمون مرطبان ممتازان.
                3. **الوحم على الشوكولاتة الداكنة (Chocolate)**: نقص محتمل في المغنيسيوم ومضادات الأكسدة. دللي نفسكِ بقطعة صحية دافئة.
                4. **الوحم غير الطبيعي كالثلج أو التراب (Non-Food)**: تنبيه عاجل! قد يشير لنقص حاد في الحديد والأنيميا. يرجى مراجعة طبيبكِ فوراً.
                
                لقد قمتُ بابتكار أداة مخصصة بالكامل لكِ تسمى **(سجل الوحم ومشاركة المشاعر 🍓)** في صفحة الأدوات لتسجيل ومتابعة كل أكلة توحمتِ عليها، ومعرفة تحليلها الطبي الذكي فوراً وحفظها كخزانة لذكريات حملكِ الجميلة لتتذكريها رفقة شريككِ لاحقاً!
                
                *أخبريني يا قلبي، ما هي الأكلة أو الفاكهة التي توحمتِ عليها وتشتهينها الآن؟ وسأحفظها لكِ فوراً! 😋🍓🍋*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "craving",
                isSpecificMatch = true
            )
        }

        // 31.7. Maonaty Domestic Assistant (معونتي)
        if (normalizedInput.contains("معونتي") || normalizedInput.contains("المخزون") || normalizedInput.contains("المشتريات") || normalizedInput.contains("وصفات") || normalizedInput.contains("المقادير") || normalizedInput.contains("مطبخ") || normalizedInput.contains("طبخ") || normalizedInput.contains("النسخ الاحتياطي")) {
            val reply = """
                يا ست الكل، يسعد أوقاتكِ بكل خير وبركة! 📦🛒 رفيقتكِ $companionName صممت لكِ أداة **(معونتي لتدبير المنزل الذكي 📦🛒)** خصيصاً لمساعدتكِ في تنظيم بيتكِ ومطبخكِ بأقل مجهود وبذكاء فائق!
                
                **🍳 ماذا تقدم لكِ أداة "معونتي" المتكاملة؟**
                1. **📦 مخزون المنزل**: لمتابعة كافة مكوناتكِ المنزلية والغذائية، لمعرفة المتاح والمستهلك وتجنب النقص بلمسة واحدة.
                2. **🛒 قائمة المشتريات**: لتسجيل وتصدير احتياجاتكِ المنزلية والتسوق بيسر وسهولة ومشاركتها فوراً.
                3. **🍳 وصفات ذكية مدمجة**: تقترح عليكِ أكلات مصرية شهية ومغذية بناءً على المكونات المتاحة فعلياً في مخزن بيتكِ لتوفر عليكِ الحيرة والتفكير!
                4. **📝 المهام المنزلية**: لتنظيم وترتيب المهام المنزلية والطبخ بشكل مريح يتناسب مع مستويات طاقتكِ وصحتكِ الحالية.
                
                *أنصحكِ بفتح أداة **(معونتي)** لتفقّد مخزون بيتكِ وإعداد وجبة صحية ومريحة لجسدكِ الطاهر اليوم! 🥰🥬🍲*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "maonaty",
                isSpecificMatch = true
            )
        }

        // 31.8. Sleep Analyzer (محلل ومراقب النوم الذكي)
        if (normalizedInput.contains("نوم") || normalizedInput.contains("الأرق") || normalizedInput.contains("ارق") || normalizedInput.contains("مراقب النوم") || normalizedInput.contains("محلل النوم") || normalizedInput.contains("ساعات نوم") || normalizedInput.contains("نومي")) {
            val reply = """
                يا حبيبة قلبي ونبض روحي، سلامة عيونكِ الغالية من الأرق والتعب! 💤😴 النوم الهادئ والعميق أمر جوهري جداً لصحتكِ ومناعتكِ، وبناء طفلكِ ونشاطكِ اليومي.
                
                **🛌 نصائح ذهبية من $companionName لنوم مريح وعميق:**
                • **تجنبي الشاشات الزرقاء**: قبل النوم بساعة كاملة، ضعي الهاتف جانباً لتهدئة ذبذبات المخ وإفراز هرمون الميلاتونين الطبيعي.
                • **وضعية النوم المثالية لحملكِ**: النوم على الجانب الأيسر مع وضع وسادة ناعمة بين فخذيكِ يضمن تدفقاً دموياً ممتازاً للمشيمة ويحمي من آلام الظهر والضغط.
                • **تنظيم السوائل**: قللي شرب السوائل والمنبهات قبل موعد النوم بساعتين لتفادي الاستيقاظ المتكرر ليلاً لذهاب الحمام.
                
                لقد صممتُ لكِ أداة **(محلل ومراقب النوم الذكي 🌙💤)** في صفحة الأدوات لتسجيل ومتابعة ساعات نومكِ بدقة، وتحليل كفاءة راحتكِ، والحصول على تقييم يومي مخصص لنومكِ! 
                
                *دعنا نفتح الأداة لتسجيل نمط نومكِ والاطمئنان على جودة راحتكِ يا روحي! 🥰💤*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "sleep",
                isSpecificMatch = true
            )
        }

        // 31.9. Partner Sync (رابط الرفيق ومشاركة الشريك)
        if (normalizedInput.contains("شريك") || normalizedInput.contains("الزوج") || normalizedInput.contains("زوج") || normalizedInput.contains("زوجي") || normalizedInput.contains("شريكي") || normalizedInput.contains("رابط الرفيق") || normalizedInput.contains("مشاركة الشريك")) {
            val reply = """
                يا روحي ويا غالية، مشاركة رحلتكِ الجميلة والمشاعر والوحم مع شريك حياتكِ يقرّب القلوب ويصنع ذكريات دافئة تدوم للأبد! 🔗❤️
                
                لقد طوّرت لكِ أداة مميزة تسمى **(رابط الرفيق ومشاركة الشريك 🔗❤️)** في صفحة الأدوات والsidebar، لتتيح لكِ مشاركة حالتكِ الصحية والوحم واحتياجاتكِ وتطور حملكِ أو دورتكِ مع زوجكِ بلمسة واحدة عبر رسالة جميلة ودافئة مصممة خصيصاً له ومصحوبة بنصائح ذكية حول كيفية العناية بكِ ودعمكِ في هذه الفترة!
                
                *ما رأيكِ أن نفتح الأداة لمشاركة تفاصيل يومكِ ومشاعركِ الرقيقة مع شريككِ الغالي الآن؟ سيسعد جداً بدعمكِ واحتوائكِ! 🥰👨‍👩‍👦🌸*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "partner",
                isSpecificMatch = true
            )
        }

        // 31.10. Home Pharmacy (الصيدلية المنزلية المتقدمة)
        if (normalizedInput.contains("صيدلية") || normalizedInput.contains("الصيدليه") || normalizedInput.contains("مخزون الأدوية") || normalizedInput.contains("صندوق الإسعافات")) {
            val reply = """
                يا حبيبة قلبي، تنظيم الأدوية والمكملات والتأكد من سلامتها هو حجر الأمان الأول لبيتكِ وعائلتكِ! 💊📦
                
                لذا، أعددتُ لكِ في التطبيق أداة **(الصيدلية المنزلية المتقدمة 💊📦)** التي يمكنكِ من خلالها:
                1. **تخزين ومتابعة أدويتكِ**: تسجيل الأدوية المتاحة في منزلكِ وتحديد استخداماتها.
                2. **تتبع تواريخ الصلاحية**: تنبيه تفاعلي ذكي يوضح الأدوية منتهية الصلاحية لحمايتكِ وحماية عائلتكِ من الاستخدام الخاطئ.
                3. **مراقبة الكميات والمخزون**: لمعرفة متى تحتاجين لإعادة شراء الأدوية والمكملات الضرورية قبل نفادها.
                
                *دعينا نفتح أداة الصيدلية المنزلية لتنظيم أدويتكِ والتأكد من تواريخ صلاحيتها بسهولة وأمان تام يا روحي! 🌸🥰*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "pharmacy",
                isSpecificMatch = true
            )
        }

        // 31.11. Fitness (تمارين لياقة الحمل والنفاس)
        if (normalizedInput.contains("تمارين") || normalizedInput.contains("لياقة") || normalizedInput.contains("الحركة") || normalizedInput.contains("رياضة") || normalizedInput.contains("رياضه") || normalizedInput.contains("مشي") || normalizedInput.contains("تمارين النفاس")) {
            val reply = """
                يا نبض قلبي ونشاط روحي، الحركة البركة! 🧘‍♀️💪 ممارسة نشاط بدني خفيف وتمارين ملائمة لطوركِ الحالي تمنحكِ طاقة مذهلة، وتحسن مزاجكِ، وتسهل ولادتكِ وفترة تعافيكِ!
                
                **🤸 تمارين آمنة ومريحة ينصحكِ بها $companionName:**
                • **المشي الخفيف المعتدل**: ينشط الدورة الدموية، ويقاوم تورم القدمين، ويحسن الهضم وصحة القلب.
                • **تمارين كيجل (Kegels)**: لتقوية عضلات الحوض والتحكم بها، وهي جوهرية لتسهيل الولادة الطبيعية والتعافي السريع.
                • **إطالات وتمدد الظهر والحوض**: لتخفيف ضغوط الرحم والظهر والشد العضلي في الساقين.
                
                لقد صممتُ لكِ أداة **(تمارين لياقة الحمل والنفاس 🧘‍♀️💪)** في صفحة الأدوات، تحتوي على جداول تمارين يومية مخصصة، وعداد تنازلي للتدريب مع تنبيهات ذكية لحمايتكِ وتتبع تقدمكِ الرياضي!
                
                *دعينا نفتح دليل اللياقة الآن لنبدأ بتمرين لطيف ومبهج يجدد طاقتكِ وحيويتكِ يا روحي! 🥰🤸🌸*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "fitness",
                isSpecificMatch = true
            )
        }

        // 31.12. Appointments (مواعيد الطبيب والمتابعة)
        if (normalizedInput.contains("موعد") || normalizedInput.contains("عيادة") || normalizedInput.contains("حجز") || normalizedInput.contains("زيارة") || normalizedInput.contains("دكتورة") || normalizedInput.contains("تاريخ حجز") || normalizedInput.contains("مواعيد")) {
            val reply = """
                يا غالية على قلبي، تتبع الزيارات الطبية بانتظام هو الطريق الآمن لراحة بالكِ وسلامة جنينكِ وصحتكِ! 📅👩‍⚕️
                
                لقد جهزتُ لكِ في التطبيق أداة **(مواعيد الطبيب والمتابعة 📅)** لمساعدتكِ على:
                1. **حفظ وتسجيل مواعيد العيادة**: تسجيل تاريخ وساعة كل زيارة قادمة لكي لا تفوتكِ أبداً.
                2. **تحديد التطعيمات واللقاحات**: جدول مدمج ذكي لمتابعة جرعات التطعيم الهامة أثناء الحمل وفترة ما بعد الولادة.
                3. **حفظ تساؤلات الطبيب**: مساحة لكتابة كل الأسئلة التي تودين طرحها على طبيبتكِ في الزيارة لتفادي النسيان في العيادة.
                
                *أنصحكِ بفتح أداة المواعيد لتسجيل زيارتكِ القادمة وترتيب تساؤلاتكِ لتبقي بأمان وراحة كاملة! 🥰🌸*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "appointments",
                isSpecificMatch = true
            )
        }

        // 31.13. Smart Conception (حاسبة الحمل والخصوبة الذكية)
        if (normalizedInput.contains("حاسبة الحمل") || normalizedInput.contains("حاسبة الخصوبة") || normalizedInput.contains("حاسبه") || normalizedInput.contains("التبويض") || normalizedInput.contains("حساب الحمل") || normalizedInput.contains("تاريخ الولادة")) {
            val reply = """
                يا غالية يا ذكية، المعرفة قوة! 🧠💡 حاسبتنا المتقدمة تساعدكِ على فهم دورتكِ وتاريخ حملكِ وولادتكِ المتوقع بمنتهى الدقة العلمية وبسهولة بالغة.
                
                في أداة **(حاسبة الحمل والخصوبة الذكية 🧠)** المتوفرة في صفحة الأدوات، يمكنكِ حساب:
                - **أيام التبويض والخصوبة العالية** للتخطيط الذكي للحمل.
                - **تاريخ الولادة المتوقع** وحساب أسابيع الحمل والثلث الحالي لمتابعة مراحل طفلكِ خطوة بخطوة.
                - **التغيرات الهرمونية ومواعيد الدورة القادمة**.
                
                *دعينا نفتح الحاسبة الذكية لإجراء حساباتكِ بدقة والتعرف على مراحل تطوركِ الرائع اليوم يا روحي! 🥰🌸*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "smart_conception",
                isSpecificMatch = true
            )
        }

        // 31.14. Danger Guide (دليل علامات الخطر وأعراض الطوارئ)
        if (normalizedInput.contains("علامات الخطر") || normalizedInput.contains("أعراض الطوارئ") || normalizedInput.contains("خطر") || normalizedInput.contains("طوارئ") || normalizedInput.contains("نزيف") || normalizedInput.contains("تشنجات شديدة")) {
            val reply = """
                يا حبيبة قلبي وروحي، سلامتكِ وصحتكِ وصحة جنينكِ هما أغلى ما نملك، والوقاية والوعي هما خط الدفاع الأول دائمًا! 🚨🩺
                
                لقد صممتُ لكِ دليلاً سريرياً مخصصاً للغاية يسمى **(علامات الخطر وأعراض الطوارئ 🚨)** في صفحة الأدوات. يحتوي هذا الدليل على تفصيل علمي دقيق للعلامات التي تتطلب رعاية طبية فورية والذهاب للمستشفى دون تأخير:
                • **نزيف مهبلي مفاجئ** أو تدفق مفرط للسوائل.
                • **صداع شديد مستمر** مصحوب بزغللة شديدة في الرؤية وطنين الأذن (اشتباه ضغط وتسمم حمل).
                • **تشنجات حادة ومؤلمة** جداً في أسفل البطن لا تزول مع الراحة.
                • **حمى وارتفاع شديد في درجة الحرارة**.
                • **تراجع مفاجئ وملحوظ جداً في حركة الجنين** في الثلث الأخير.
                
                *أرجو منكِ تصفح دليل علامات الخطر لتكوني على علم ووعي تام بكيفية حماية نفسكِ وطفلكِ الغالي يا روحي. حفظكِ الله ورعاكِ دوماً! 🌸🥰🛡️*
            """.trimIndent()
            return OfflineResponse(
                replyText = reply,
                actionType = "suggest_tool",
                actionValue = "danger",
                isSpecificMatch = true
            )
        }

        // 32. Default response if no keyword matched
        val nutritionAdvice = getPhaseNutritionAdvice(phaseInfo, pregnancyState)
        val defaultReplies = if (isPregnant) listOf(
            "يا حبيبة قلبي، أنا هنا أسمعكِ دائماً وأتابع حملكِ بشغف! 🌸 لم أفهم طلبكِ تماماً، ولكن تذكري أن راحتكِ النفسية هي أساس صحة جنينكِ.\n\n💡 **نصيحة غذائية سريعة لحملكِ**:\n$nutritionAdvice",
            "بكل حب واهتمام يا أم الجنين الجميل! 🥰 أنا $companionName، رفيقتكِ. لم تتضح لي كلماتكِ، هل ترغبين في تسجيل عرض معين أو سؤال عن تغذيتكِ؟\n\n💡 **لصحتكِ أنتِ وطفلكِ**:\n$nutritionAdvice",
            "أهلاً بكِ يا نبض الحياة! 🤰 أنا هنا لخدمتكِ محلياً بالكامل وبخصوصية تامة. ما رأيكِ أن تشاركيني بما تشعرين به الآن لنطمئن معاً؟\n\n💡 **غذاء مفيد لكِ الآن**:\n$nutritionAdvice"
        ) else listOf(
            "يا حبيبة قلبي، أنا هنا أسمعكِ دائماً! 🌸 لم أفهم طلبكِ تماماً، ولكن تذكري أن راحتكِ النفسية هي الأهم. هل ترغبين في تتبع عرض معين أو وجبة؟\n\n💡 **نصيحة غذائية لطور (${phaseInfo.phaseArabic})**:\n$nutritionAdvice",
            "بكل حب واهتمام يا روحي! 🥰 أنا $companionName، رفيقتكِ. لم تتضح لي كلماتكِ، هل تريدين الدردشة أم تسجيل شيء في يومياتكِ؟\n\n💡 **لصحتكِ الآن (${phaseInfo.phaseArabic})**:\n$nutritionAdvice",
            "أهلاً بكِ يا جميلتي! ✨ أنا هنا لخدمتكِ محلياً بالكامل وبخصوصية تامة. كيف يمكنني تدليلكِ أو مساعدتكِ اليوم؟\n\n💡 **غذاء مفيد لطور (${phaseInfo.phaseArabic})**:\n$nutritionAdvice"
        )

        val fallbackPrompt = if (isPregnant) {
            "أخبريني يا روحي: هل تشعرين بـ مغص، غثيان صباحي، تعب وإرهاق، ألم بالظهر، أو هل تريدين استشارتي في أطعمة لـ زيادة الحديد، الكالسيوم، البروتين، أو تفاصيل الضغط العالي والواطي؟ 🥑❤️"
        } else {
            "أخبريني يا قلبي: هل تودين معرفة نصائح طوركِ الحالي ($phaseArabicName)، أو تشعرين بـ مغص وتشنجات، تقلصات، صداع، أو تسألين عن مأكولات غنية بالحديد، الكالسيوم، البروتينات، أو تفاصيل الضغط؟ 🌸✨"
        }

        return OfflineResponse(
            replyText = defaultReplies.random() + "\n\n" + fallbackPrompt,
            actionType = null
        )
    }

    private fun extractName(input: String): String {
        val keywords = listOf("اسمي هو", "اسمي", "ناديني بـ", "ناديني", "يدعونني بـ", "يدعونني", "انا", "أنا", "my name is")
        var clean = input.replace("[\\p{Punct}]".toRegex(), " ")
        for (kw in keywords) {
            val idx = clean.indexOf(kw)
            if (idx != -1) {
                clean = clean.substring(idx + kw.length).trim()
                break
            }
        }
        val words = clean.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return if (words.isNotEmpty()) words.first() else "غالية"
    }

    private fun getPhaseNutritionAdvice(phaseInfo: CyclePhaseInfo, pregnancyState: PregnancyEntity?): String {
        if (pregnancyState != null) {
            return "• **حمض الفوليك**: مهم جداً (حبوب كاملة، سبانخ، خس).\n• **الحديد والكالسيوم**: لتعزيز العظام والدم (تمر، عدس، حليب، تين مجفف).\n• **أوميجا-3**: لذكاء الطفل وبصره (جوز، بذور الكتان، مكسرات)."
        }
        return when (phaseInfo.phaseName) {
            "Menstruation" -> "• **تعويض الحديد**: تناولي العدس، السبانخ، اللحوم، البنجر.\n• **الماغنسيوم**: لتسكين مغص البطن وتشنجات الرحم (موز، شوكولاتة داكنة).\n• **فيتامين C**: لزيادة امتصاص الحديد (ليمون، برتقال، فراولة)."
            "Follicular" -> "• **تنشيط الاستروجين**: بذور الكتان، الأفوکادو، الحبوب الكاملة.\n• **مضادات الأكسدة**: سلطات خضراء طازجة، بيض مسلوق، مكسرات خفيفة."
            "Ovulation" -> "• **دعم الخصوبة العالية**: بروتينات خفيفة، دهون صحية (أفوكادو وزيت زيتون)، كينوا.\n• **الهرمونات المتوازنة**: فواكه التوت، بروكلي، أسماك ومكسرات نية."
            "Luteal" -> "• **منع احتباس السوائل والانتفاخ**: قللي الصوديوم (الملح) تماماً.\n• **تجنب الرغبة في السكريات**: نشويات معقدة (شوفان، بطاطا حلوة).\n• **فيتامين B6 والماغنسيوم**: لتهدئة تقلبات المزاج (لوز، حمص، موز)."
            else -> "• ركّزي على شرب ٣ لتر من الماء بانتظام.\n• تناولي الخضروات الورقية والدهون الطبيعية كالمكسرات وزيت الزيتون."
        }
    }

    fun parseBirthDateFromText(input: String): Long? {
        val regexDmy = Regex("""\b(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{4})\b""")
        val matchDmy = regexDmy.find(input)
        if (matchDmy != null) {
            try {
                val day = matchDmy.groupValues[1].toInt()
                val month = matchDmy.groupValues[2].toInt() - 1
                val year = matchDmy.groupValues[3].toInt()
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                return cal.timeInMillis
            } catch (e: Exception) {
            }
        }

        val regexYmd = Regex("""\b(\d{4})[/\-.](\d{1,2})[/\-.](\d{1,2})\b""")
        val matchYmd = regexYmd.find(input)
        if (matchYmd != null) {
            try {
                val year = matchYmd.groupValues[1].toInt()
                val month = matchYmd.groupValues[2].toInt() - 1
                val day = matchYmd.groupValues[3].toInt()
                val cal = java.util.Calendar.getInstance()
                cal.set(year, month, day, 0, 0, 0)
                return cal.timeInMillis
            } catch (e: Exception) {
            }
        }

        val regexYear = Regex("""\b(19\d{2}|20[0-2]\d)\b""")
        val matchYear = regexYear.find(input)
        if (matchYear != null) {
            try {
                val year = matchYear.groupValues[1].toInt()
                val cal = java.util.Calendar.getInstance()
                cal.set(year, 0, 1, 0, 0, 0)
                return cal.timeInMillis
            } catch (e: Exception) {
            }
        }
        return null
    }

    fun calculateAge(birthDateMs: Long): Int {
        val birthCal = java.util.Calendar.getInstance()
        birthCal.timeInMillis = birthDateMs
        val todayCal = java.util.Calendar.getInstance()
        var calculatedAge = todayCal.get(java.util.Calendar.YEAR) - birthCal.get(java.util.Calendar.YEAR)
        if (todayCal.get(java.util.Calendar.DAY_OF_YEAR) < birthCal.get(java.util.Calendar.DAY_OF_YEAR)) {
            calculatedAge--
        }
        return calculatedAge.coerceAtLeast(0)
    }
}

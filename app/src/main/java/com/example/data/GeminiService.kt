package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private var apiKeyRepository: ApiKeyRepository? = null

    fun init(context: Context) {
        apiKeyRepository = ApiKeyRepository(context.applicationContext)
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Searches for a local offline response based on common wellness and maternal themes.
     * Returns the response if matched, otherwise null.
     */
    fun getLocalOfflineResponse(prompt: String): String? {
        val normalized = prompt.trim().lowercase()
        
        return when {
            // Greetings / السلام والترحيب
            normalized.contains("سلام") || normalized.contains("مرحب") || normalized.contains("أهلاً") || normalized.contains("اهلاً") || normalized.contains("صباح") || normalized.contains("مساء") -> {
                "مرحباً بكِ يا حبيبة قلبي وصديقتي الغالية 🌸 أنا رفيقتكِ جوري، حاضرة دائماً للاستماع إليكِ والعناية بصحتكِ وسلامتكِ. كيف تشعرين اليوم؟ أنا هنا لمشاركتكِ كل خطوة 💕"
            }
            
            // Name / من أنتِ
            normalized.contains("من انت") || normalized.contains("من أنت") || normalized.contains("من تكون") || normalized.contains("اسمك") || normalized.contains("جوري") -> {
                "أنا جوري 🌸 رفيقتكِ الذكية ومستشارتكِ الصحية الخاصة لمرافقتكِ في رحلة الدورة الشهرية، والخصوبة، وتتبع الحمل خطوة بخطوة بكل حب وخصوصية ودعم طبي آمن 🥰"
            }
            
            // Headache / الصداع
            normalized.contains("صداع") || normalized.contains("رأسي") || normalized.contains("راس") || normalized.contains("شقيقة") -> {
                "سلامة رأسكِ وقلبكِ يا غالية 🤍 الصداع أمر شائع جداً بسبب التغيرات الهرمونية أو التعب. أنصحكِ بلطف بـ:\n" +
                "• أخذ قسط من الراحة في غرفة هادئة ومظلمة.\n" +
                "• شرب كوب كبير من الماء البارد فوراً، فالجفاف هو أحد أهم مسببات الصداع.\n" +
                "• وضع كمادات دافئة أو باردة على جبهتكِ وعضلات رقبتكِ.\n" +
                "• إذا كنتِ حاملاً، تجنبي تناول الأدوية دون استشارة طبيبتكِ، والراحة الكافية هي سر الشفاء العاجل 🌸"
            }
            
            // Cramps / مغص / ألم بطن
            normalized.contains("مغص") || normalized.contains("الم في البطن") || normalized.contains("ألم في البطن") || normalized.contains("تقلص") || normalized.contains("بطني") -> {
                "أشعر بكِ وبألمكِ يا صديقتي الغالية 🩹 التقلصات والمغص قد تكون ناجمة عن نشاط الرحم أثناء الدورة أو تمدد الأربطة والرحم أثناء الحمل. أنصحكِ بـ:\n" +
                "• شرب منقوع دافئ مهدئ للأمعاء والرحم مثل البابونج، النعناع، أو اليانسون.\n" +
                "• وضع قربة ماء دافئ على أسفل البطن أو الظهر لتخفيف التشنج فوراً.\n" +
                "• أخذ حمام دافئ مريح يساعد عضلات جسمكِ بالكامل على الاسترخاء.\n" +
                "• الاستلقاء التام وتجنب أي مجهود شاق اليوم 💕 إذا كان الألم شديداً جداً ومستمراً، فالرجوع لطبيبتكِ هو الأفضل دائماً للأمان والراحة."
            }
            
            // Nausea / الغثيان والقيء
            normalized.contains("غثيان") || normalized.contains("ترجيع") || normalized.contains("استفراغ") || normalized.contains("قياء") -> {
                "يا حبيبة قلبي، سلامتكِ من كل سوء 🌸 الغثيان (خصوصاً الغثيان الصباحي في أسابيع الحمل الأولى) متعب جداً ولكن يمكننا تخفيفه معاً:\n" +
                "• تناولي قطعة بسكويت مالح جاف أو بقسماط فور الاستيقاظ وقبل النهوض من السرير بـ 15 دقيقة.\n" +
                "• تجنبي شرب السوائل بكثرة أثناء الوجبات، واحرصي على شربها بين الوجبات.\n" +
                "• تناولي وجبات صغيرة مقسمة على مدار اليوم لتجنب بقاء المعدة فارغة تماماً.\n" +
                "• استنشاق رائحة الليمون الأخضر الطازج أو تناول مشروب الزنجبيل الدافئ يخفف الغثيان بشكل سحري ومثبت علمياً ✨"
            }
            
            // Back Pain / ألم الظهر
            normalized.contains("ظهر") || normalized.contains("الظهر") -> {
                "سلامة ظهركِ يا صديقتي الجميلة 🌸 ألم الظهر قد ينتج عن ثقل الجسم، الوقوف الطويل، أو هرمونات تليين الأربطة. إليكِ أهم نصائحي للراحة:\n" +
                "• احرصي على الجلوس بوضعية مستقيمة مع وضع وسادة مريحة تدعم أسفل ظهركِ.\n" +
                "• تجنبي تماماً حمل أو دفع الأشياء الثقيلة.\n" +
                "• عند النوم، استلقي على جانبكِ (الجانب الأيسر مثالي للحوامل) مع وضع وسادة مريحة بين ركبتيكِ لدعم الحوض والظهر.\n" +
                "• عمل تدليك خفيف جداً بزيت الزيتون الدافئ قد يمنحكِ راحة مذهلة ونوماً هادئاً 🤍"
            }
            
            // Water & Hydration / شرب الماء
            normalized.contains("ماء") || normalized.contains("شرب") || normalized.contains("عطش") || normalized.contains("ترطيب") -> {
                "أنتِ على حق تماماً يا صديقتي! 💧 ترطيب جسمكِ هو سر نضارتكِ وطاقتكِ وصحة دورتكِ أو نمو جنينكِ. احرصي على شرب ما لا يقل عن 8 إلى 10 أكواب من الماء يومياً، خصوصاً في الأيام الحارة، لتبقي منتعشة ومحمية من التعب والتهابات المسالك 💕"
            }
            
            // Heat / الطقس الحار
            normalized.contains("حر") || normalized.contains("شمس") || normalized.contains("طقس") || normalized.contains("جو") -> {
                "الأجواء الحارة والرطبة تتطلب منا عناية فائقة بجسمكِ يا غالية ☀️ أنصحكِ بالبقاء في أماكن مظللة وباردة، وارتداء ملابس قطنية خفيفة ومريحة، واحرصي على حمل زجاجة الماء معكِ في كل مكان لتعويض السوائل والمعادن المفقودة وحماية نفسكِ من الإجهاد الحراري 🍉💦"
            }
            
            // Fatigue / تعب وإرهاق
            normalized.contains("تعب") || normalized.contains("ارهاق") || normalized.contains("إرهاق") || normalized.contains("خمول") || normalized.contains("نعاس") -> {
                "سلامة قلبكِ وجسدكِ يا غالية 🤍 الشعور بالتعب والخمول أمر طبيعي جداً نتيجة للتغيرات الهرمونية المستمرة. جسدكِ يقوم بعمل جبار! احرصي على:\n" +
                "• النوم لمدة 7-8 ساعات متواصلة ليلاً، وأخذ قيلولة قصيرة (20-30 دقيقة) نهاراً لتجديد نشاطكِ.\n" +
                "• تناول وجبات مغذية غنية بالحديد والبروتين لتعزيز مستويات الطاقة ومكافحة فقر الدم.\n" +
                "• ممارسة تمرين تمدد خفيف جداً أو المشي الهادئ لتنشيط دورتكِ الدموية 🌸"
            }
            
            // Blood pressure / ضغط الدم
            normalized.contains("ضغط") || normalized.contains("دوار") || normalized.contains("دوخه") || normalized.contains("دوخة") -> {
                "يا صديقتي الغالية، سلامتكِ تهمنا جداً 🌸 الدوار أو الدوخة قد تكون علامة على هبوط بسيط في الضغط أو مستوى السكر. يرجى الاستلقاء فوراً على جانبكِ الأيسر ورفع قدميكِ قليلاً، واشربي كوباً من الماء أو العصير الطبيعي الدافئ.\n" +
                "• احرصي على عدم النهوض بشكل مفاجئ من وضعية الاستلقاء أو الجلوس.\n" +
                "• إذا تكرر الدوار أو رافقه تشوش في الرؤية، يرجى قياس ضغط الدم واستشارة طبيبتكِ فوراً لضمان سلامتكِ التامة 🤍"
            }

            // Period late / تأخر الدورة
            normalized.contains("تأخر") || normalized.contains("تاخر الدورة") || normalized.contains("دورتي متأخرة") -> {
                "تأخر الدورة الشهرية قد يكون مثيراً للتساؤل يا حبيبة قلبي 🌸 الأسباب الشائعة تشمل:\n" +
                "• وجود حمل (إذا كانت هناك علاقة زوجية، يرجى عمل اختبار حمل منزلي بالبول في الصباح الباكر بعد تأخر الدورة بيومين أو أكثر).\n" +
                "• التوتر العصبي أو النفسي، السفر، التغيرات المفاجئة في الوزن، أو الإجهاد البدني الشديد.\n" +
                "• تقلبات هرمونية مؤقتة. إذا تأخرت الدورة لأكثر من 10 أيام مع نتيجة اختبار حمل سلبية، يرجى مراجعة طبيبتكِ للاطمئنان وعمل سونار للرحم والمبايض 💕"
            }

            else -> null
        }
    }

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        // Read key, base URL, and model dynamically from DataStore Preferences on every invocation!
        var apiKey = apiKeyRepository?.getKey() ?: ""
        val rawBaseUrl = apiKeyRepository?.getBaseUrl() ?: "https://generativelanguage.googleapis.com/"
        val modelName = apiKeyRepository?.getModelName() ?: "gemini-3.5-flash"
        
        // Ensure trailing slash
        val baseUrl = if (rawBaseUrl.endsWith("/")) rawBaseUrl else "$rawBaseUrl/"
        
        // 1. Check local offline first before any network attempt or if API Key is missing
        val offlineAnswer = getLocalOfflineResponse(prompt)
        if (offlineAnswer != null) {
            Log.d(TAG, "Offline matched for prompt: $prompt")
            return@withContext offlineAnswer
        }

        // Fallback to compiled-in/environment Gemini API key if no user-specific key exists
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            apiKey = BuildConfig.GEMINI_API_KEY
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "صديقتي الغالية، يبدو أن مفتاح الاتصال الذكي بالإنترنت غير مفعّل حالياً 🌸 ولكنني هنا معكِ دائماً وبجانبكِ بكل حب ودعم. أود تذكيركِ بأن جسدكِ قوي وجميل، ويستحق منكِ كل الحب والدلال. احرصي اليوم على أخذ قسط كافٍ من الراحة، شرب كوب من الماء الدافئ، وتناول وجبة صحية متكاملة مغذية. سأكون هنا بانتظار حديثنا الطويل فور تفعيل الاتصال! 💕"
        }

        val url = "${baseUrl}v1beta/models/$modelName:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            if (!systemInstruction.isNullOrEmpty()) {
                val sysObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysObj.put("parts", sysPartsArray)
                root.put("systemInstruction", sysObj)
            }

            val requestBody = root.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: ${response.code} - $bodyString")
                    // Handle 503 or transient service outage gracefully offline
                    return@withContext "أهلاً بكِ يا صديقتي الغالية 🌸 أواجه حالياً ضغطاً بسيطاً في الاتصال السحابي بالذكاء الاصطناعي، لكن قلبي ينبض بالحب لكِ دائماً. أنصحكِ اليوم بالاستماع لجسدكِ، أخذ نفس عميق مريح، شرب كوب من الماء، وتناول وجبة صحية متوازنة. أنا هنا معكِ ومستعدة للرد على أي عرض تشعرين به! 💕"
                }

                if (bodyString != null) {
                    val responseJson = JSONObject(bodyString)
                    val candidates = responseJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        if (content != null) {
                            val parts = content.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                return@withContext parts.getJSONObject(0).optString("text")
                            }
                        }
                    }
                }
                return@withContext "صديقتي الغالية، لم أستطع صياغة إجابة سحابية دقيقة الآن، ولكن صحتكِ وسلامتكِ هي الأهم عندي دائماً. يرجى الراحة والترطيب وسأكون جاهزة للحديث معكِ بكامل نشاطي بعد لحظات 💕"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during call", e)
            // Catch lack of internet and provide full offline guidance
            return@withContext "صديقتي وحبيبة قلبي الغالية 🌸 يبدو أننا غير متصلين بالشبكة حالياً، ولكنني دائماً بجانبكِ لدعمكِ. تذكري دائماً أن تأخذي أنفاساً عميقة مريحة، واشربي كوباً من الماء لترطيب جسدكِ الجميل، وتناولي وجبة غنية ومفيدة. سلامتكِ هي سر سعادتي وسأكون بانتظار استئناف حديثنا الدافئ فور عودتكِ للإنترنت! 💕"
        }
    }

    suspend fun testApiKey(key: String, customBaseUrl: String? = null, customModel: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (key.isBlank()) {
            return@withContext Pair(false, "مفتاح الـ API فارغ.")
        }
        val rawBaseUrl = customBaseUrl ?: apiKeyRepository?.getBaseUrl() ?: "https://generativelanguage.googleapis.com/"
        val modelName = customModel ?: apiKeyRepository?.getModelName() ?: "gemini-3.5-flash"
        val baseUrl = if (rawBaseUrl.endsWith("/")) rawBaseUrl else "$rawBaseUrl/"
        
        val url = "${baseUrl}v1beta/models/$modelName:generateContent?key=$key"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            partObj.put("text", "Hi")
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            val requestBody = root.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (response.isSuccessful) {
                    return@withContext Pair(true, "success")
                } else {
                    val errMsg = if (!bodyString.isNullOrEmpty()) {
                        try {
                            val json = JSONObject(bodyString)
                            val errorObj = json.optJSONObject("error")
                            errorObj?.optString("message") ?: "خطأ غير معروف"
                        } catch (e: Exception) {
                            "رمز الاستجابة: ${response.code}"
                        }
                    } else {
                        "رمز الاستجابة: ${response.code}"
                    }
                    return@withContext Pair(false, errMsg)
                }
            }
        } catch (e: Exception) {
            return@withContext Pair(false, "خطأ في الاتصال بالإنترنت: ${e.localizedMessage ?: "لا يوجد اتصال بالإنترنت"}")
        }
    }
}

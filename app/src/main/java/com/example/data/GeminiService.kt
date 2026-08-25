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

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        history: List<Pair<String, String>> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        // Read key, base URL, and model dynamically from DataStore Preferences on every invocation!
        var apiKey = apiKeyRepository?.getKey() ?: ""
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            apiKey = BuildConfig.GEMINI_API_KEY
        }
        val rawBaseUrl = apiKeyRepository?.getBaseUrl() ?: ApiKeyRepository.DEFAULT_BASE_URL
        val modelName = apiKeyRepository?.getModelName() ?: "gemini-3.5-flash"
        
        // Ensure valid allowed host and trailing slash
        val baseUrl = ApiKeyRepository.sanitizeBaseUrl(rawBaseUrl)

        val defaultPhase = com.example.viewmodel.CyclePhaseInfo("Follicular", "الطور الجريبي 🌸", 5, 0.5f, "")

        // If no user-configured key exists, return OfflineJouriEngine response
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext OfflineJouriEngine.getResponse(
                userInput = prompt,
                motherName = null,
                phaseInfo = defaultPhase,
                pregnancyState = null,
                todayWaterLogged = 0
            ).replyText
        }

        val url = "${baseUrl}v1beta/models/$modelName:generateContent?key=$apiKey"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        try {
            val root = JSONObject()
            val contentsArray = JSONArray()
            
            // Task B3: Include session history (last 2-3 turns)
            for (turn in history) {
                if (turn.first.isNotBlank()) {
                    val userObj = JSONObject()
                    userObj.put("role", "user")
                    val userParts = JSONArray().put(JSONObject().put("text", turn.first))
                    userObj.put("parts", userParts)
                    contentsArray.put(userObj)
                }
                if (turn.second.isNotBlank()) {
                    val modelObj = JSONObject()
                    modelObj.put("role", "model")
                    val modelParts = JSONArray().put(JSONObject().put("text", turn.second))
                    modelObj.put("parts", modelParts)
                    contentsArray.put(modelObj)
                }
            }

            val contentObj = JSONObject()
            contentObj.put("role", "user")
            val partsArray = JSONArray().put(JSONObject().put("text", prompt))
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
                    return@withContext OfflineJouriEngine.getResponse(
                        userInput = prompt,
                        motherName = null,
                        phaseInfo = defaultPhase,
                        pregnancyState = null,
                        todayWaterLogged = 0
                    ).replyText
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
                return@withContext OfflineJouriEngine.getResponse(
                    userInput = prompt,
                    motherName = null,
                    phaseInfo = defaultPhase,
                    pregnancyState = null,
                    todayWaterLogged = 0
                ).replyText
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during call", e)
            return@withContext OfflineJouriEngine.getResponse(
                userInput = prompt,
                motherName = null,
                phaseInfo = defaultPhase,
                pregnancyState = null,
                todayWaterLogged = 0
            ).replyText
        }
    }

    suspend fun testApiKey(key: String, customBaseUrl: String? = null, customModel: String? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val targetKey = if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else {
            val saved = apiKeyRepository?.getKey()
            if (!saved.isNullOrBlank() && saved != "MY_GEMINI_API_KEY") saved else BuildConfig.GEMINI_API_KEY
        }
        if (targetKey.isBlank() || targetKey == "MY_GEMINI_API_KEY") {
            return@withContext Pair(false, "مفتاح الـ API فارغ.")
        }
        val rawBaseUrl = customBaseUrl ?: apiKeyRepository?.getBaseUrl() ?: ApiKeyRepository.DEFAULT_BASE_URL
        val modelName = customModel ?: apiKeyRepository?.getModelName() ?: "gemini-3.5-flash"
        val baseUrl = ApiKeyRepository.sanitizeBaseUrl(rawBaseUrl)
        
        val url = "${baseUrl}v1beta/models/$modelName:generateContent?key=$targetKey"
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

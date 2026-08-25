package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ApiKeyRepository(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context.applicationContext,
            "secure_api_key_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/"
        const val ALLOWED_HOST = "generativelanguage.googleapis.com"
        const val DEFAULT_MODEL = "gemini-3.5-flash"

        val ALLOWED_MODELS = setOf(
            "gemini-3.5-flash",
            "gemini-3.0-flash",
            "gemini-2.5-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        )

        fun sanitizeModelName(rawModel: String?): String {
            if (rawModel.isNullOrBlank()) return DEFAULT_MODEL
            val trimmed = rawModel.trim().lowercase(java.util.Locale.US)
            return if (ALLOWED_MODELS.contains(trimmed) || trimmed.matches(Regex("^[a-z0-9.-]{3,40}$"))) {
                trimmed
            } else {
                DEFAULT_MODEL
            }
        }

        fun sanitizeBaseUrl(rawUrl: String?): String {
            if (rawUrl.isNullOrBlank()) return DEFAULT_BASE_URL
            return try {
                val trimmed = rawUrl.trim()
                val uri = java.net.URI(trimmed)
                val host = uri.host?.lowercase(java.util.Locale.US)
                val scheme = uri.scheme?.lowercase(java.util.Locale.US)
                if (scheme == "https" && host == ALLOWED_HOST) {
                    if (trimmed.endsWith("/")) trimmed else "$trimmed/"
                } else {
                    DEFAULT_BASE_URL
                }
            } catch (e: Exception) {
                DEFAULT_BASE_URL
            }
        }
    }

    private val keyApiKey = "gemini_api_key"
    private val keyApiBaseUrl = "gemini_api_base_url"
    private val keyModelName = "gemini_model_name"
    private val keyCloudAiConsent = "gemini_cloud_ai_consent"

    val cloudAiConsentFlow: Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
            if (changedKey == keyCloudAiConsent) {
                trySend(sharedPreferences.getBoolean(keyCloudAiConsent, false))
            }
        }
        trySend(prefs.getBoolean(keyCloudAiConsent, false))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    suspend fun setCloudAiConsent(consented: Boolean) {
        prefs.edit().putBoolean(keyCloudAiConsent, consented).apply()
    }

    suspend fun getCloudAiConsent(): Boolean {
        return prefs.getBoolean(keyCloudAiConsent, false)
    }

    val apiKeyFlow: Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
            if (changedKey == keyApiKey) {
                trySend(sharedPreferences.getString(keyApiKey, null))
            }
        }
        trySend(prefs.getString(keyApiKey, null))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val apiBaseUrlFlow: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
            if (changedKey == keyApiBaseUrl) {
                val stored = sharedPreferences.getString(keyApiBaseUrl, DEFAULT_BASE_URL)
                trySend(sanitizeBaseUrl(stored))
            }
        }
        trySend(sanitizeBaseUrl(prefs.getString(keyApiBaseUrl, DEFAULT_BASE_URL)))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val modelNameFlow: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
            if (changedKey == keyModelName) {
                val raw = sharedPreferences.getString(keyModelName, DEFAULT_MODEL)
                trySend(sanitizeModelName(raw))
            }
        }
        trySend(sanitizeModelName(prefs.getString(keyModelName, DEFAULT_MODEL)))
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    suspend fun saveKey(value: String) {
        prefs.edit().putString(keyApiKey, value).apply()
    }

    suspend fun saveBaseUrl(value: String) {
        prefs.edit().putString(keyApiBaseUrl, sanitizeBaseUrl(value)).apply()
    }

    suspend fun saveModelName(value: String) {
        prefs.edit().putString(keyModelName, sanitizeModelName(value)).apply()
    }

    suspend fun getKey(): String? {
        return prefs.getString(keyApiKey, null)
    }

    suspend fun getBaseUrl(): String {
        return sanitizeBaseUrl(prefs.getString(keyApiBaseUrl, DEFAULT_BASE_URL))
    }

    suspend fun getModelName(): String {
        return sanitizeModelName(prefs.getString(keyModelName, DEFAULT_MODEL))
    }

    suspend fun clearKey() {
        prefs.edit().remove(keyApiKey).apply()
    }
}

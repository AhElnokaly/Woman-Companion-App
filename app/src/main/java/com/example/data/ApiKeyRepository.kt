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

    private val keyApiKey = "gemini_api_key"
    private val keyApiBaseUrl = "gemini_api_base_url"
    private val keyModelName = "gemini_model_name"

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
                trySend(sharedPreferences.getString(keyApiBaseUrl, "https://generativelanguage.googleapis.com/") ?: "https://generativelanguage.googleapis.com/")
            }
        }
        trySend(prefs.getString(keyApiBaseUrl, "https://generativelanguage.googleapis.com/") ?: "https://generativelanguage.googleapis.com/")
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val modelNameFlow: Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
            if (changedKey == keyModelName) {
                trySend(sharedPreferences.getString(keyModelName, "gemini-3.5-flash") ?: "gemini-3.5-flash")
            }
        }
        trySend(prefs.getString(keyModelName, "gemini-3.5-flash") ?: "gemini-3.5-flash")
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    suspend fun saveKey(value: String) {
        prefs.edit().putString(keyApiKey, value).apply()
    }

    suspend fun saveBaseUrl(value: String) {
        prefs.edit().putString(keyApiBaseUrl, value).apply()
    }

    suspend fun saveModelName(value: String) {
        prefs.edit().putString(keyModelName, value).apply()
    }

    suspend fun getKey(): String? {
        return prefs.getString(keyApiKey, null)
    }

    suspend fun getBaseUrl(): String {
        return prefs.getString(keyApiBaseUrl, "https://generativelanguage.googleapis.com/") ?: "https://generativelanguage.googleapis.com/"
    }

    suspend fun getModelName(): String {
        return prefs.getString(keyModelName, "gemini-3.5-flash") ?: "gemini-3.5-flash"
    }

    suspend fun clearKey() {
        prefs.edit().remove(keyApiKey).apply()
    }
}

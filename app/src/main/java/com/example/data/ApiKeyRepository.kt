package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// +++ أضيف بناءً على طلبك لتسجيل مفتاح الـ API في DataStore Preferences +++
val Context.apiDataStore: DataStore<Preferences> by preferencesDataStore(name = "jouri_api_key_prefs")

class ApiKeyRepository(private val context: Context) {
    private val apiKeyPrefKey = stringPreferencesKey("gemini_api_key")
    private val apiBaseUrlPrefKey = stringPreferencesKey("gemini_api_base_url")
    private val modelNamePrefKey = stringPreferencesKey("gemini_model_name")

    val apiKeyFlow: Flow<String?> = context.apiDataStore.data.map { preferences ->
        preferences[apiKeyPrefKey]
    }

    val apiBaseUrlFlow: Flow<String> = context.apiDataStore.data.map { preferences ->
        preferences[apiBaseUrlPrefKey] ?: "https://generativelanguage.googleapis.com/"
    }

    val modelNameFlow: Flow<String> = context.apiDataStore.data.map { preferences ->
        preferences[modelNamePrefKey] ?: "gemini-3.5-flash"
    }

    suspend fun saveKey(value: String) {
        context.apiDataStore.edit { preferences ->
            preferences[apiKeyPrefKey] = value
        }
    }

    suspend fun saveBaseUrl(value: String) {
        context.apiDataStore.edit { preferences ->
            preferences[apiBaseUrlPrefKey] = value
        }
    }

    suspend fun saveModelName(value: String) {
        context.apiDataStore.edit { preferences ->
            preferences[modelNamePrefKey] = value
        }
    }

    suspend fun getKey(): String? {
        return context.apiDataStore.data.map { preferences ->
            preferences[apiKeyPrefKey]
        }.first()
    }

    suspend fun getBaseUrl(): String {
        return context.apiDataStore.data.map { preferences ->
            preferences[apiBaseUrlPrefKey] ?: "https://generativelanguage.googleapis.com/"
        }.first()
    }

    suspend fun getModelName(): String {
        return context.apiDataStore.data.map { preferences ->
            preferences[modelNamePrefKey] ?: "gemini-3.5-flash"
        }.first()
    }

    suspend fun clearKey() {
        context.apiDataStore.edit { preferences ->
            preferences.remove(apiKeyPrefKey)
        }
    }
}

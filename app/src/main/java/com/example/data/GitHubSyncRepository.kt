package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object GitHubSyncRepository {
    suspend fun syncJouriMatrixFromServer(rawJsonUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            // Read raw JSON content from URL
            URL(rawJsonUrl).readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

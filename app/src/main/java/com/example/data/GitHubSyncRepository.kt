package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Locale

object GitHubSyncRepository {
    private const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024 // 2 MB safety cap
    private const val TIMEOUT_MILLIS = 10000 // 10 seconds

    private val ALLOWED_HOSTS = setOf(
        "raw.githubusercontent.com",
        "github.com",
        "gist.githubusercontent.com"
    )

    fun isValidSyncUrl(rawUrl: String): Boolean {
        return try {
            val uri = URI(rawUrl.trim())
            val scheme = uri.scheme?.lowercase(Locale.US)
            val host = uri.host?.lowercase(Locale.US)
            scheme == "https" && host != null && ALLOWED_HOSTS.contains(host)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncJouriMatrixFromServer(rawJsonUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            if (!isValidSyncUrl(rawJsonUrl)) {
                com.example.util.AppLogger.w("GitHubSyncRepository", "Rejected URL outside allowed HTTPS hosts: $rawJsonUrl")
                return@withContext null
            }

            val url = URL(rawJsonUrl.trim())
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MILLIS
                readTimeout = TIMEOUT_MILLIS
                instanceFollowRedirects = false
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json, text/plain")
            }

            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                com.example.util.AppLogger.w("GitHubSyncRepository", "Server returned HTTP $responseCode")
                return@withContext null
            }

            val inputStream = connection.inputStream
            val buffer = ByteArray(4096)
            var bytesRead: Int
            var totalRead = 0
            val outputStream = java.io.ByteArrayOutputStream()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                totalRead += bytesRead
                if (totalRead > MAX_RESPONSE_BYTES) {
                    com.example.util.AppLogger.w("GitHubSyncRepository", "Response exceeded max size limit of $MAX_RESPONSE_BYTES bytes")
                    return@withContext null
                }
                outputStream.write(buffer, 0, bytesRead)
            }

            val responseString = outputStream.toString(Charsets.UTF_8.name())
            
            // Validate that content is a valid JSON structure (Object or Array)
            val isJson = try {
                val trimmed = responseString.trim()
                if (trimmed.startsWith("{")) {
                    JSONObject(trimmed)
                    true
                } else if (trimmed.startsWith("[")) {
                    JSONArray(trimmed)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }

            if (!isJson) {
                com.example.util.AppLogger.w("GitHubSyncRepository", "Downloaded payload is not a valid JSON structure")
                return@withContext null
            }

            responseString
        } catch (e: Exception) {
            com.example.util.AppLogger.w("GitHubSyncRepository", "Failed to sync Jouri matrix from server", e)
            null
        }
    }
}


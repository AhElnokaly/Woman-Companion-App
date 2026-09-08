package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class GitHubReleaseInfo(
    val tagName: String,
    val versionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val fileName: String,
    val publishedAt: String,
    val hasUpdate: Boolean
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class UpdateAvailable(val release: GitHubReleaseInfo) : UpdateStatus()
    object NoUpdateAvailable : UpdateStatus()
    data class Downloading(val progressPercent: Int) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

object GitHubAppUpdater {
    private const val TAG = "GitHubAppUpdater"
    private const val GITHUB_REPO_OWNER = "AhElnokaly"
    private const val GITHUB_REPO_NAME = "Woman-Companion-App"
    private const val LATEST_RELEASE_API = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"

    /**
     * يقارن نسختين (مثلاً "1.2" مقابل "1.3" أو "v1.3.0")
     * يعيد true إذا كانت نسخة GitHub أحدث من النسخة المثبتة
     */
    fun isNewerVersion(currentVer: String, remoteVer: String): Boolean {
        return try {
            val cleanCurrent = currentVer.trim().removePrefix("v").removePrefix("V")
            val cleanRemote = remoteVer.trim().removePrefix("v").removePrefix("V")

            val currentParts = cleanCurrent.split(".", "-", "_").mapNotNull { it.toIntOrNull() }
            val remoteParts = cleanRemote.split(".", "-", "_").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(currentParts.size, remoteParts.size)
            for (i in 0 until maxLen) {
                val curr = currentParts.getOrElse(i) { 0 }
                val rem = remoteParts.getOrElse(i) { 0 }
                if (rem > curr) return true
                if (rem < curr) return false
            }
            false
        } catch (e: Exception) {
            AppLogger.w(TAG, "Error parsing versions: $currentVer vs $remoteVer", e)
            false
        }
    }

    /**
     * فحص أحدث إصدار من GitHub Releases
     */
    suspend fun checkForUpdates(): Result<GitHubReleaseInfo?> = withContext(Dispatchers.IO) {
        try {
            val url = URL(LATEST_RELEASE_API)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Woman-Companion-Android")
                connectTimeout = 12000
                readTimeout = 12000
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                // لم يتم إنشاء Release على المستودع بعد
                AppLogger.i(TAG, "No releases found on GitHub repo yet (404)")
                return@withContext Result.success(null)
            }

            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext Result.failure(Exception("GitHub API HTTP $responseCode"))
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseBody)

            val tagName = json.optString("tag_name", "")
            val title = json.optString("name", tagName)
            val body = json.optString("body", "")
            val publishedAt = json.optString("published_at", "")

            // استخراج أول ملف بصيغة apk من assets
            val assets = json.optJSONArray("assets")
            var apkDownloadUrl = ""
            var apkFileName = "app-release.apk"

            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", "")
                        apkFileName = name
                        break
                    }
                }
            }

            // لو مفيش ملف APK مرفوع في الـ assets
            if (apkDownloadUrl.isEmpty()) {
                AppLogger.w(TAG, "Release found ($tagName) but contains no .apk asset")
                return@withContext Result.success(null)
            }

            val currentVersion = BuildConfig.VERSION_NAME
            val hasUpdate = isNewerVersion(currentVer = currentVersion, remoteVer = tagName)

            val releaseInfo = GitHubReleaseInfo(
                tagName = tagName,
                versionName = tagName.removePrefix("v").removePrefix("V"),
                releaseTitle = title,
                releaseNotes = body,
                downloadUrl = apkDownloadUrl,
                fileName = apkFileName,
                publishedAt = publishedAt,
                hasUpdate = hasUpdate
            )

            Result.success(releaseInfo)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Check update failed", e)
            Result.failure(e)
        }
    }

    /**
     * تحميل ملف الـ APK مع إرسال نسبة التقدم
     */
    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        targetFileName: String,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val destinationDir = File(context.getExternalFilesDir(null) ?: context.filesDir, "updates")
            if (!destinationDir.exists()) {
                destinationDir.mkdirs()
            }

            val apkFile = File(destinationDir, targetFileName)
            if (apkFile.exists()) {
                apkFile.delete()
            }

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "Woman-Companion-Android")
                instanceFollowRedirects = true
                connectTimeout = 15000
                readTimeout = 30000
            }

            val responseCode = connection.responseCode
            // معالجة الـ redirects (GitHub releases redirects to AWS S3)
            val finalConnection = if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == 307 || responseCode == 308
            ) {
                val newUrl = connection.getHeaderField("Location")
                (URL(newUrl).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Woman-Companion-Android")
                    connectTimeout = 15000
                    readTimeout = 30000
                }
            } else {
                connection
            }

            val fileLength = finalConnection.contentLength
            val inputStream = finalConnection.inputStream
            val outputStream = FileOutputStream(apkFile)

            val buffer = ByteArray(8192)
            var totalBytesRead: Long = 0
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                if (fileLength > 0) {
                    val progress = ((totalBytesRead * 100) / fileLength).toInt()
                    onProgress(progress.coerceIn(0, 100))
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Result.success(apkFile)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Download APK failed", e)
            Result.failure(e)
        }
    }

    /**
     * تشغيل معالج تثبيت الحزم التابع لأندرويد مباشرة
     */
    fun launchApkInstaller(context: Context, apkFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
        } catch (e: Exception) {
            AppLogger.e(TAG, "Launch APK installer failed", e)
        }
    }
}

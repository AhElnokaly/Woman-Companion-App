package com.example.util

import android.util.Log
import com.example.BuildConfig

object AppLogger {
    private const val TAG_PREFIX = "WomanCompanion_"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG_PREFIX$tag", message)
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i("$TAG_PREFIX$tag", message)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.w("$TAG_PREFIX$tag", message, throwable)
            } else {
                Log.w("$TAG_PREFIX$tag", message)
            }
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e("$TAG_PREFIX$tag", message, throwable)
            } else {
                Log.e("$TAG_PREFIX$tag", message)
            }
        } else {
            // In release builds, ensure no PII or raw health messages leak to logcat
            val safeMessage = message.take(80).replace(Regex("[^a-zA-Z0-9 _.:/-]"), "")
            Log.e("$TAG_PREFIX$tag", safeMessage)
        }
    }
}

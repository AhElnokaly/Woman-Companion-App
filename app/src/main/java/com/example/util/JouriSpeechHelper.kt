package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * مدير النطق الصوتي العربي لنصائح جوري (Jouri Text-to-Speech Engine)
 */
class JouriSpeechManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    val isSpeaking = mutableStateOf(false)

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Arabic locale first
                val arabicLocale = Locale("ar")
                val result = tts?.setLanguage(arabicLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to default
                    tts?.language = Locale.getDefault()
                }
                tts?.setSpeechRate(0.9f) // Warm, gentle pace for maternal reassurance
                tts?.setPitch(1.05f)    // Pleasant voice pitch

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        isSpeaking.value = false
                    }
                })
                isInitialized = true
            } else {
                Log.w("JouriSpeechManager", "TTS initialization failed: $status")
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized || tts == null) return

        if (isSpeaking.value) {
            stop()
        } else {
            // Clean text from complex emojis for smoother reading
            val cleanText = text.replace(Regex("[\\p{So}\\p{Cn}]"), " ").trim()
            val utteranceId = "jouri_tip_${System.currentTimeMillis()}"
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            isSpeaking.value = true
        }
    }

    fun stop() {
        tts?.stop()
        isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isSpeaking.value = false
        isInitialized = false
    }
}

/**
 * Composable Helper لتذكر واستخدام JouriSpeechManager بأمان مع دورة الحياة
 */
@Composable
fun rememberJouriSpeechManager(): JouriSpeechManager {
    val context = LocalContext.current
    val speechManager = remember { JouriSpeechManager(context) }

    DisposableEffect(speechManager) {
        onDispose {
            speechManager.shutdown()
        }
    }

    return speechManager
}

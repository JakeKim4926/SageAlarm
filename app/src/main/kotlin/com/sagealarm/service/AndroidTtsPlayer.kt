package com.sagealarm.service

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class AndroidTtsPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) : TtsPlayer {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null
    private var pendingLocale: Locale? = null

    override fun initialize() {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
            if (isReady) {
                val text = pendingText
                val locale = pendingLocale
                pendingText = null
                pendingLocale = null
                if (text != null && locale != null) {
                    speak(text, locale)
                }
            }
        }
    }

    override fun speak(text: String, locale: Locale) {
        if (text.isBlank()) return
        if (!isReady) {
            pendingText = text
            pendingLocale = locale
            return
        }
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.shutdown()
        tts = null
        isReady = false
    }

    companion object {
        private const val UTTERANCE_ID = "sage_alarm_tts"
    }
}

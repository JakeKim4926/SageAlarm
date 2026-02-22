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

    override fun initialize() {
        if (tts != null) return
        tts = TextToSpeech(context) { status ->
            isReady = (status == TextToSpeech.SUCCESS)
        }
    }

    override fun speak(text: String, locale: Locale) {
        if (!isReady || text.isBlank()) return
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

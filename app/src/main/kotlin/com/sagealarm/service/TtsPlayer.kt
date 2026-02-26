package com.sagealarm.service

import java.util.Locale

interface TtsPlayer {
    fun initialize()
    fun speak(text: String, locale: Locale = Locale.getDefault())
    fun isSpeaking(): Boolean
    fun stop()
    fun release()
}

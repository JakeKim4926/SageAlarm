package com.sagealarm.service

import java.util.Locale

interface TtsPlayer {
    fun speak(text: String, locale: Locale = Locale.getDefault())
    fun stop()
    fun release()
}

package com.sagealarm.domain.model

data class PresetSound(
    val id: String,
    val name: String,
    val category: SoundCategory,
    val rawResName: String? = null,
    val ttsText: String? = null,
)

enum class SoundCategory {
    ANIMAL,
    TTS_PRESET,
    MUSIC,
}

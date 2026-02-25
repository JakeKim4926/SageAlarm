package com.sagealarm.domain.model

data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val ttsMessage: String = "",
    val repeatDays: Set<Int> = emptySet(), // Calendar.SUNDAY=1 ~ Calendar.SATURDAY=7
    val musicUri: String? = null,
    val isEnabled: Boolean = true,
    val isPuzzleEnabled: Boolean = false,
)

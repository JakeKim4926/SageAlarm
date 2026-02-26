package com.sagealarm.data.local.db

import com.sagealarm.domain.model.Alarm

fun AlarmEntity.toDomain(): Alarm = Alarm(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    ttsMessage = ttsMessage,
    repeatDays = if (repeatDays.isBlank()) emptySet()
    else repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
    musicUri = musicUri,
    isEnabled = isEnabled,
    isPuzzleEnabled = isPuzzleEnabled,
    alarmIntervalMinutes = alarmIntervalMinutes,
    repeatCount = repeatCount,
    isVibrationEnabled = isVibrationEnabled,
    isTtsEnabled = isTtsEnabled,
    isMusicEnabled = isMusicEnabled,
)

fun Alarm.toEntity(): AlarmEntity = AlarmEntity(
    id = id,
    hour = hour,
    minute = minute,
    label = label,
    ttsMessage = ttsMessage,
    repeatDays = repeatDays.joinToString(","),
    musicUri = musicUri,
    isEnabled = isEnabled,
    isPuzzleEnabled = isPuzzleEnabled,
    alarmIntervalMinutes = alarmIntervalMinutes,
    repeatCount = repeatCount,
    isVibrationEnabled = isVibrationEnabled,
    isTtsEnabled = isTtsEnabled,
    isMusicEnabled = isMusicEnabled,
)

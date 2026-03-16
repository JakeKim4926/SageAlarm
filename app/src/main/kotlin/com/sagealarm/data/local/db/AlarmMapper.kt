package com.sagealarm.data.local.db

import com.sagealarm.domain.model.Alarm
import com.sagealarm.domain.model.PuzzleType

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
    puzzleType = runCatching { PuzzleType.valueOf(puzzleType) }.getOrDefault(PuzzleType.NUMBER_ORDER),
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
    puzzleType = puzzleType.name,
    alarmIntervalMinutes = alarmIntervalMinutes,
    repeatCount = repeatCount,
    isVibrationEnabled = isVibrationEnabled,
    isTtsEnabled = isTtsEnabled,
    isMusicEnabled = isMusicEnabled,
)

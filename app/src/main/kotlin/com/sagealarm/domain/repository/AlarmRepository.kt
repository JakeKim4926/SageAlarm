package com.sagealarm.domain.repository

import com.sagealarm.domain.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: Long): Alarm?
    suspend fun saveAlarm(alarm: Alarm): Long
    suspend fun deleteAlarm(alarm: Alarm)
    suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean): Alarm?
    suspend fun getEnabledAlarms(): List<Alarm>
}

package com.sagealarm.data.repository

import com.sagealarm.data.local.db.AlarmDao
import com.sagealarm.data.local.db.toDomain
import com.sagealarm.data.local.db.toEntity
import com.sagealarm.domain.model.Alarm
import com.sagealarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao,
) : AlarmRepository {

    override fun getAlarms(): Flow<List<Alarm>> =
        dao.getAlarms().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAlarmById(id: Long): Alarm? =
        dao.getAlarmById(id)?.toDomain()

    override suspend fun saveAlarm(alarm: Alarm): Long =
        dao.insertAlarm(alarm.toEntity())

    override suspend fun deleteAlarm(alarm: Alarm) =
        dao.deleteAlarm(alarm.toEntity())

    override suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean) =
        dao.setAlarmEnabled(id, isEnabled)

    override suspend fun getEnabledAlarms(): List<Alarm> =
        dao.getEnabledAlarms().map { it.toDomain() }
}

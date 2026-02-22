package com.sagealarm.domain.usecase

import com.sagealarm.domain.model.Alarm
import com.sagealarm.domain.repository.AlarmRepository
import com.sagealarm.service.AlarmScheduler
import javax.inject.Inject

class SaveAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(alarm: Alarm): Long {
        val savedId = repository.saveAlarm(alarm)
        val savedAlarm = alarm.copy(id = savedId)
        if (savedAlarm.isEnabled) {
            scheduler.schedule(savedAlarm)
        } else {
            scheduler.cancel(savedAlarm)
        }
        return savedId
    }
}

package com.sagealarm.domain.usecase

import com.sagealarm.domain.model.Alarm
import com.sagealarm.domain.repository.AlarmRepository
import com.sagealarm.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class DeleteAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(alarm: Alarm) {
        scheduler.cancel(alarm)
        repository.deleteAlarm(alarm)
    }
}

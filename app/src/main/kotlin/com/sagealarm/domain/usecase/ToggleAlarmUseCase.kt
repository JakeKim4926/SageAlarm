package com.sagealarm.domain.usecase

import com.sagealarm.domain.repository.AlarmRepository
import com.sagealarm.service.AlarmScheduler
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(id: Long, isEnabled: Boolean) {
        repository.setAlarmEnabled(id, isEnabled)
        val alarm = repository.getAlarmById(id) ?: return
        if (isEnabled) {
            scheduler.schedule(alarm)
        } else {
            scheduler.cancel(alarm)
        }
    }
}

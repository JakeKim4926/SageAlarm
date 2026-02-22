package com.sagealarm.domain.usecase

import com.sagealarm.domain.repository.AlarmRepository
import com.sagealarm.domain.scheduler.AlarmScheduler
import javax.inject.Inject

class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
) {
    suspend operator fun invoke(id: Long, isEnabled: Boolean) {
        val alarm = repository.setAlarmEnabled(id, isEnabled) ?: return
        if (isEnabled) {
            scheduler.schedule(alarm)
        } else {
            scheduler.cancel(alarm)
        }
    }
}

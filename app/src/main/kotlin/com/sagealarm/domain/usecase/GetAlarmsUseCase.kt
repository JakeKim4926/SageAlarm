package com.sagealarm.domain.usecase

import com.sagealarm.domain.model.Alarm
import com.sagealarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository,
) {
    operator fun invoke(): Flow<List<Alarm>> = repository.getAlarms()
}

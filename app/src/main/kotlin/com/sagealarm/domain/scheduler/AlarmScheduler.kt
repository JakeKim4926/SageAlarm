package com.sagealarm.domain.scheduler

import com.sagealarm.domain.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)
}

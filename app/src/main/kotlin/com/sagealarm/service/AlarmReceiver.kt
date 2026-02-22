package com.sagealarm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.sagealarm.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ALARM_TRIGGER -> handleAlarmTrigger(context, intent)
            Intent.ACTION_BOOT_COMPLETED -> rescheduleAllAlarms()
        }
    }

    private fun handleAlarmTrigger(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        scope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId) ?: return@launch

            // Reschedule for next occurrence if repeating
            if (alarm.repeatDays.isNotEmpty()) {
                alarmScheduler.schedule(alarm)
            }

            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    private fun rescheduleAllAlarms() {
        scope.launch {
            alarmRepository.getEnabledAlarms().forEach { alarm ->
                alarmScheduler.schedule(alarm)
            }
        }
    }

    companion object {
        const val ACTION_ALARM_TRIGGER = "com.sagealarm.ACTION_ALARM_TRIGGER"
        const val EXTRA_ALARM_ID = "alarm_id"
    }
}

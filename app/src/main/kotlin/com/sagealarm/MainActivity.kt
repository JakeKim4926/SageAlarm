package com.sagealarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.rememberNavController
import com.sagealarm.presentation.navigation.NavGraph
import com.sagealarm.presentation.navigation.Screen
import com.sagealarm.presentation.theme.SageAlarmTheme
import com.sagealarm.service.AlarmReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // replay=0: 화면 재생성(회전 등) 시 재내비게이션 방지
    // extraBufferCapacity=1: onNewIntent()가 collector보다 먼저 호출돼도 유실 없음
    private val dismissAlarmFlow = MutableSharedFlow<Long>(extraBufferCapacity = 1)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val showDismiss = intent.getBooleanExtra(EXTRA_SHOW_DISMISS, false)
        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)
        if (showDismiss && alarmId != -1L) {
            dismissAlarmFlow.tryEmit(alarmId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupScreenFlags()
        enableEdgeToEdge()

        val startRoute = resolveStartRoute()

        setContent {
            val navController = rememberNavController()

            // 앱이 백그라운드에 살아있을 때 알람이 오면 onNewIntent()로 해제화면 이동
            LaunchedEffect(Unit) {
                dismissAlarmFlow.collect { alarmId ->
                    navController.navigate(Screen.Dismiss.createRoute(alarmId)) {
                        launchSingleTop = true
                    }
                }
            }

            SageAlarmTheme {
                NavGraph(navController = navController, startRoute = startRoute)
            }
        }
    }

    private fun setupScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            // API 27+: 잠금화면 위에 표시 및 화면 켜기
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
    }

    private fun resolveStartRoute(): String {
        val showDismiss = intent?.getBooleanExtra(EXTRA_SHOW_DISMISS, false) ?: false
        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L) ?: -1L

        return if (showDismiss && alarmId != -1L) {
            Screen.Dismiss.createRoute(alarmId)
        } else {
            Screen.AlarmList.route
        }
    }

    companion object {
        const val EXTRA_SHOW_DISMISS = "show_dismiss"
    }
}

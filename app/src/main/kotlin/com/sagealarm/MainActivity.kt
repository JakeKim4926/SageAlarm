package com.sagealarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sagealarm.presentation.navigation.NavGraph
import com.sagealarm.presentation.navigation.Screen
import com.sagealarm.presentation.theme.SageAlarmTheme
import com.sagealarm.service.AlarmReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startRoute = resolveStartRoute()

        setContent {
            SageAlarmTheme {
                NavGraph(startRoute = startRoute)
            }
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

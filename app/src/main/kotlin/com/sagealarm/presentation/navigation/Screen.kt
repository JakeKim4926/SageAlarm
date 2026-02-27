package com.sagealarm.presentation.navigation

sealed class Screen(val route: String) {
    data object AlarmList : Screen("alarm_list")
    data object AlarmEdit : Screen("alarm_edit/{alarmId}") {
        fun createRoute(alarmId: Long = -1L) = "alarm_edit/$alarmId"
    }
    data object Dismiss : Screen("dismiss/{alarmId}") {
        fun createRoute(alarmId: Long) = "dismiss/$alarmId"
    }
    data object Settings : Screen("settings")
    data object SoundPick : Screen("sound_pick")
}

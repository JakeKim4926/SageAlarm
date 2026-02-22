package com.sagealarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sagealarm.presentation.alarm.edit.AlarmEditScreen
import com.sagealarm.presentation.alarm.list.AlarmListScreen
import com.sagealarm.presentation.dismiss.DismissScreen
import com.sagealarm.presentation.settings.SettingsScreen

@Composable
fun NavGraph(startRoute: String = Screen.AlarmList.route) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startRoute,
    ) {
        composable(Screen.AlarmList.route) {
            AlarmListScreen(
                onAddAlarm = { navController.navigate(Screen.AlarmEdit.createRoute()) },
                onEditAlarm = { alarmId -> navController.navigate(Screen.AlarmEdit.createRoute(alarmId)) },
                onSettings = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(
            route = Screen.AlarmEdit.route,
            arguments = listOf(navArgument("alarmId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong("alarmId") ?: -1L
            AlarmEditScreen(
                alarmId = alarmId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.Dismiss.route,
            arguments = listOf(navArgument("alarmId") { type = NavType.LongType }),
        ) {
            DismissScreen(
                onDismissed = {
                    navController.navigate(Screen.AlarmList.route) {
                        popUpTo(Screen.AlarmList.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

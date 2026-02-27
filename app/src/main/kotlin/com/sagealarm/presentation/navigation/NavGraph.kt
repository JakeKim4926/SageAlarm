package com.sagealarm.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sagealarm.presentation.alarm.edit.AlarmEditScreen
import com.sagealarm.presentation.alarm.edit.AlarmEditViewModel
import com.sagealarm.presentation.alarm.list.AlarmListScreen
import com.sagealarm.presentation.alarm.soundpick.RESULT_MUSIC_URI
import com.sagealarm.presentation.alarm.soundpick.RESULT_TTS_TEXT
import com.sagealarm.presentation.alarm.soundpick.SoundPickScreen
import com.sagealarm.presentation.dismiss.DismissScreen
import com.sagealarm.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startRoute: String = Screen.AlarmList.route,
) {
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
            val viewModel: AlarmEditViewModel = hiltViewModel(backStackEntry)

            // SoundPickScreen에서 돌아올 때 결과 처리
            LaunchedEffect(Unit) {
                backStackEntry.savedStateHandle
                    .getStateFlow<String?>(RESULT_MUSIC_URI, null)
                    .collect { uriValue ->
                        uriValue ?: return@collect
                        viewModel.updateMusicUri(uriValue.ifEmpty { null })
                        viewModel.updateMusicEnabled(true)
                        backStackEntry.savedStateHandle.remove<String>(RESULT_MUSIC_URI)
                    }
            }
            LaunchedEffect(Unit) {
                backStackEntry.savedStateHandle
                    .getStateFlow<String?>(RESULT_TTS_TEXT, null)
                    .collect { text ->
                        text ?: return@collect
                        viewModel.updateTtsMessage(text)
                        viewModel.updateTtsEnabled(true)
                        backStackEntry.savedStateHandle.remove<String>(RESULT_TTS_TEXT)
                    }
            }

            AlarmEditScreen(
                alarmId = alarmId,
                onBack = { navController.popBackStack() },
                onSoundPick = { navController.navigate(Screen.SoundPick.route) },
                viewModel = viewModel,
            )
        }

        composable(Screen.SoundPick.route) {
            SoundPickScreen(
                onBack = { navController.popBackStack() },
                onMusicSelected = { uri ->
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set(RESULT_MUSIC_URI, uri ?: "")
                    navController.popBackStack()
                },
                onTtsSelected = { text ->
                    navController.previousBackStackEntry?.savedStateHandle
                        ?.set(RESULT_TTS_TEXT, text)
                    navController.popBackStack()
                },
            )
        }

        composable(
            route = Screen.Dismiss.route,
            arguments = listOf(navArgument("alarmId") { type = NavType.LongType }),
        ) {
            DismissScreen(
                onDismissed = {
                    navController.navigate(Screen.AlarmList.route) {
                        // cold start 시 startDestination이 Dismiss일 수 있어
                        // popUpTo(AlarmList)는 AlarmList가 스택에 없으면 동작 안 함.
                        // 0(graph root)을 지정해 스택 전체를 비운 뒤 AlarmList로 이동.
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

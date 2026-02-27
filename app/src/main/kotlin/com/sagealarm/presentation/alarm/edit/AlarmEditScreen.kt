package com.sagealarm.presentation.alarm.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.R
import com.sagealarm.presentation.theme.Beige
import com.sagealarm.presentation.theme.BeigeMuted
import com.sagealarm.presentation.theme.Ivory
import com.sagealarm.presentation.theme.Taupe
import com.sagealarm.presentation.theme.WarmBrownMuted
import com.sagealarm.presentation.theme.WarmWhite
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long,
    onBack: () -> Unit,
    onSoundPick: () -> Unit = {},
    viewModel: AlarmEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val timePickerState = remember(uiState.isDataLoaded) {
        TimePickerState(
            initialHour = uiState.hour,
            initialMinute = uiState.minute,
            is24Hour = android.text.format.DateFormat.is24HourFormat(context),
        )
    }
    LaunchedEffect(alarmId) { viewModel.loadAlarm(alarmId) }
    LaunchedEffect(uiState.isNavigateBack) { if (uiState.isNavigateBack) onBack() }
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        viewModel.clearDuplicateError()
    }
    LaunchedEffect(uiState.isDuplicateTime) {
        if (uiState.isDuplicateTime) {
            snackbarHostState.showSnackbar(
                message = "이미 같은 시간의 알람이 있습니다",
                duration = SnackbarDuration.Short,
            )
            viewModel.clearDuplicateError()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "알람 삭제",
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Text(
                    text = "이 알람을 삭제할까요?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAlarm()
                    showDeleteDialog = false
                }) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "취소",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (alarmId == -1L) stringResource(R.string.add_alarm) else stringResource(R.string.edit_alarm))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (alarmId != -1L) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth(),
                colors = TimePickerDefaults.colors(
                    clockDialColor = Beige,
                    clockDialSelectedContentColor = WarmWhite,
                    clockDialUnselectedContentColor = WarmBrownMuted,
                    selectorColor = Taupe,
                    containerColor = Ivory,
                    timeSelectorSelectedContainerColor = Taupe,
                    timeSelectorUnselectedContainerColor = Beige,
                    timeSelectorSelectedContentColor = WarmWhite,
                    timeSelectorUnselectedContentColor = WarmBrownMuted,
                    periodSelectorBorderColor = BeigeMuted,
                    periodSelectorSelectedContainerColor = Taupe,
                    periodSelectorUnselectedContainerColor = Beige,
                    periodSelectorSelectedContentColor = WarmWhite,
                    periodSelectorUnselectedContentColor = WarmBrownMuted,
                ),
            )

            OutlinedTextField(
                value = uiState.label,
                onValueChange = viewModel::updateLabel,
                label = { Text(stringResource(R.string.label_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("${uiState.label.length}/${AlarmEditViewModel.MAX_LABEL_LENGTH}") },
            )

            // 반복 요일
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "반복 요일",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val allDaysSelected = DayChip.entries.all { it.calendarValue in uiState.repeatDays }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (allDaysSelected) Taupe else Beige)
                        .clickable { viewModel.toggleAllDays() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "매일",
                        fontSize = 13.sp,
                        color = if (allDaysSelected) WarmWhite else WarmBrownMuted,
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                DayChip.entries.forEach { day ->
                    val selected = day.calendarValue in uiState.repeatDays
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(3.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(if (selected) Taupe else Beige)
                            .clickable { viewModel.toggleRepeatDay(day.calendarValue) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day.label,
                            fontSize = 13.sp,
                            color = if (selected) WarmWhite else WarmBrownMuted,
                        )
                    }
                }
            }

            // 재울림 간격
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "재울림 간격",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlarmEditViewModel.ALARM_INTERVAL_OPTIONS.forEach { minutes ->
                        val selected = uiState.alarmIntervalMinutes == minutes
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) Taupe else Beige)
                                .clickable { viewModel.updateAlarmInterval(minutes) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${minutes}분",
                                fontSize = 13.sp,
                                color = if (selected) WarmWhite else WarmBrownMuted,
                            )
                        }
                    }
                }
            }

            // 반복 횟수
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "반복 횟수",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlarmEditViewModel.REPEAT_COUNT_OPTIONS.forEach { count ->
                        val selected = uiState.repeatCount == count
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) Taupe else Beige)
                                .clickable { viewModel.updateRepeatCount(count) }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (count == -1) "무한" else "${count}회",
                                fontSize = 13.sp,
                                color = if (selected) WarmWhite else WarmBrownMuted,
                            )
                        }
                    }
                }
            }

            // 진동
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "진동",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isVibrationEnabled,
                    onCheckedChange = viewModel::updateVibrationEnabled,
                    colors = switchColors(),
                )
            }

            // TTS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "TTS",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isTtsEnabled,
                    onCheckedChange = viewModel::updateTtsEnabled,
                    colors = switchColors(),
                )
            }
            if (uiState.isTtsEnabled) {
                OutlinedTextField(
                    value = uiState.ttsMessage,
                    onValueChange = viewModel::updateTtsMessage,
                    label = { Text(stringResource(R.string.tts_message_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = { Text("${uiState.ttsMessage.length}/${AlarmEditViewModel.MAX_TTS_LENGTH}") },
                )
            }

            // 음악
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "음악",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isMusicEnabled,
                    onCheckedChange = viewModel::updateMusicEnabled,
                    colors = switchColors(),
                )
            }
            if (uiState.isMusicEnabled) {
                OutlinedButton(
                    onClick = onSoundPick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(musicDisplayName(uiState.musicUri))
                }
            }

            // 알람 해제 퍼즐
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "알람 해제 퍼즐",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = uiState.isPuzzleEnabled,
                    onCheckedChange = viewModel::updatePuzzleEnabled,
                    colors = switchColors(),
                )
            }

            Button(
                onClick = { viewModel.saveAlarm(timePickerState.hour, timePickerState.minute) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedTrackColor = Taupe,
    checkedThumbColor = WarmWhite,
    checkedBorderColor = Color.Transparent,
    uncheckedTrackColor = Beige,
    uncheckedThumbColor = WarmBrownMuted,
    uncheckedBorderColor = BeigeMuted,
)

private fun musicDisplayName(uri: String?): String = when {
    uri == null -> "기본 알람음"
    "/raw/" in uri -> uri.substringAfterLast("/").replace("_", " ")
    uri.startsWith("content://") -> "기기 음악"
    else -> "기본 알람음"
}

private enum class DayChip(val label: String, val calendarValue: Int) {
    MON("월", Calendar.MONDAY),
    TUE("화", Calendar.TUESDAY),
    WED("수", Calendar.WEDNESDAY),
    THU("목", Calendar.THURSDAY),
    FRI("금", Calendar.FRIDAY),
    SAT("토", Calendar.SATURDAY),
    SUN("일", Calendar.SUNDAY),
}

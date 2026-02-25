package com.sagealarm.presentation.alarm.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.R
import com.sagealarm.presentation.theme.Beige
import com.sagealarm.presentation.theme.BeigeMuted
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
    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? -> viewModel.updateMusicUri(uri?.toString()) }

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

            Column {
                OutlinedTextField(
                    value = uiState.label,
                    onValueChange = viewModel::updateLabel,
                    label = { Text(stringResource(R.string.label_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("${uiState.label.length}/${AlarmEditViewModel.MAX_LABEL_LENGTH}") },
                )
                OutlinedTextField(
                    value = uiState.ttsMessage,
                    onValueChange = viewModel::updateTtsMessage,
                    label = { Text(stringResource(R.string.tts_message_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = { Text("${uiState.ttsMessage.length}/${AlarmEditViewModel.MAX_TTS_LENGTH}") },
                )
            }

            Text("반복 요일", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { musicPicker.launch("audio/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (uiState.musicUri != null) stringResource(R.string.select_music)
                    else stringResource(R.string.no_music_selected)
                )
            }

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
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Taupe,
                        checkedThumbColor = WarmWhite,
                        checkedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        uncheckedTrackColor = Beige,
                        uncheckedThumbColor = WarmBrownMuted,
                        uncheckedBorderColor = BeigeMuted,
                    ),
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

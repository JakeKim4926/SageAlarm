package com.sagealarm.presentation.alarm.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long,
    onBack: () -> Unit,
    viewModel: AlarmEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timePickerState = rememberTimePickerState(
        initialHour = uiState.hour,
        initialMinute = uiState.minute,
    )
    val musicPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? -> viewModel.updateMusicUri(uri?.toString()) }

    LaunchedEffect(alarmId) { viewModel.loadAlarm(alarmId) }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onBack() }
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        viewModel.updateHour(timePickerState.hour)
        viewModel.updateMinute(timePickerState.minute)
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
                        IconButton(onClick = { viewModel.deleteAlarm() }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth())

            OutlinedTextField(
                value = uiState.label,
                onValueChange = viewModel::updateLabel,
                label = { Text(stringResource(R.string.label_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.ttsMessage,
                onValueChange = viewModel::updateTtsMessage,
                label = { Text(stringResource(R.string.tts_message_hint)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                supportingText = { Text("${uiState.ttsMessage.length}/${AlarmEditViewModel.MAX_TTS_LENGTH}") },
            )

            Text("반복 요일", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DayChip.entries.forEach { day ->
                    FilterChip(
                        selected = day.calendarValue in uiState.repeatDays,
                        onClick = { viewModel.toggleRepeatDay(day.calendarValue) },
                        label = { Text(day.label) },
                    )
                }
            }

            OutlinedButton(
                onClick = { musicPicker.launch("audio/*") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (uiState.musicUri != null) stringResource(R.string.select_music)
                    else stringResource(R.string.no_music_selected)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveAlarm() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save))
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

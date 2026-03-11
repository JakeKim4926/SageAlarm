package com.sagealarm.presentation.alarm.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.material3.TimePickerSelectionMode
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.R
import com.sagealarm.data.catalog.DefaultSoundCatalog
import com.sagealarm.presentation.theme.Beige
import com.sagealarm.presentation.theme.BeigeMuted
import com.sagealarm.presentation.theme.Ivory
import com.sagealarm.presentation.theme.Taupe
import com.sagealarm.presentation.theme.WarmBrown
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
    var editingField by remember { mutableStateOf<TimePickerSelectionMode?>(null) }
    var textInputValue by remember { mutableStateOf("") }

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

    if (editingField != null) {
        val isHour = editingField == TimePickerSelectionMode.Hour
        val focusRequester = remember { FocusRequester() }

        fun applyInput() {
            val value = textInputValue.toIntOrNull() ?: return
            if (isHour) {
                val is24h = timePickerState.is24hour
                val validRange = if (is24h) 0..23 else 1..12
                if (value !in validRange) return
                val newHour = if (is24h) value else {
                    val currentIsPm = timePickerState.hour >= 12
                    when {
                        value == 12 && !currentIsPm -> 0
                        value == 12 && currentIsPm -> 12
                        currentIsPm -> value + 12
                        else -> value
                    }
                }
                timePickerState.hour = newHour
            } else {
                if (value !in 0..59) return
                timePickerState.minute = value
            }
            editingField = null
        }

        AlertDialog(
            onDismissRequest = { editingField = null },
            title = { Text(text = if (isHour) "시간 입력" else "분 입력", color = WarmBrown) },
            text = {
                val hintRange = if (isHour) {
                    if (timePickerState.is24hour) "0 – 23" else "1 – 12"
                } else "0 – 59"
                OutlinedTextField(
                    value = textInputValue,
                    onValueChange = { textInputValue = it.filter { c -> c.isDigit() }.take(2) },
                    placeholder = { Text(hintRange, color = WarmBrownMuted) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { applyInput() }),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            },
            confirmButton = {
                TextButton(onClick = { applyInput() }) {
                    Text("확인", color = Taupe)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingField = null }) {
                    Text("취소", color = WarmBrownMuted)
                }
            },
            containerColor = Ivory,
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "알람 삭제", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(text = "이 알람을 삭제할까요?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAlarm()
                    showDeleteDialog = false
                }) {
                    Text(text = "삭제", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
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
                // TimePicker 헤더(시/분 버튼) 영역 위 투명 오버레이.
                // 선택 상태 변화가 아닌 탭 이벤트를 직접 감지해 항상 다이얼로그를 열기 위함.
                // 헤더 높이 ≈ 96dp. 12시간제: 시(1/3) | 분(1/3) | AM/PM(1/3, 미차단)
                Row(modifier = Modifier.fillMaxWidth().height(96.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                val h = timePickerState.hour
                                textInputValue = if (timePickerState.is24hour) h.toString()
                                    else (if (h == 0 || h == 12) 12 else h % 12).toString()
                                editingField = TimePickerSelectionMode.Hour
                                timePickerState.selection = TimePickerSelectionMode.Hour
                            },
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                textInputValue = timePickerState.minute.toString()
                                editingField = TimePickerSelectionMode.Minute
                                timePickerState.selection = TimePickerSelectionMode.Minute
                            },
                    )
                    if (!timePickerState.is24hour) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // ── 기본 정보 ──
            SectionCard(title = "기본 정보") {
                OutlinedTextField(
                    value = uiState.label,
                    onValueChange = viewModel::updateLabel,
                    label = { Text(stringResource(R.string.label_hint)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    singleLine = true,
                    supportingText = { Text("${uiState.label.length}/${AlarmEditViewModel.MAX_LABEL_LENGTH}") },
                )

                HorizontalDivider(color = BeigeMuted, thickness = 0.5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "반복 요일",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmBrownMuted,
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
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
            }

            // ── 재울림 ──
            SectionCard(title = "재울림") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "간격",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmBrownMuted,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

                HorizontalDivider(color = BeigeMuted, thickness = 0.5.dp)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "횟수",
                        style = MaterialTheme.typography.labelLarge,
                        color = WarmBrownMuted,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            }

            // ── 기능 ──
            SectionCard(title = "기능") {
                SwitchSettingRow(
                    label = "진동",
                    checked = uiState.isVibrationEnabled,
                    onCheckedChange = viewModel::updateVibrationEnabled,
                )

                HorizontalDivider(color = BeigeMuted, thickness = 0.5.dp)

                SwitchSettingRow(
                    label = "TTS 멘트",
                    subtitle = uiState.ttsMessage.takeIf { uiState.isTtsEnabled && it.isNotBlank() },
                    checked = uiState.isTtsEnabled,
                    onCheckedChange = viewModel::updateTtsEnabled,
                )
                if (uiState.isTtsEnabled) {
                    OutlinedTextField(
                        value = uiState.ttsMessage,
                        onValueChange = viewModel::updateTtsMessage,
                        label = { Text(stringResource(R.string.tts_message_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        maxLines = 3,
                        supportingText = { Text("${uiState.ttsMessage.length}/${AlarmEditViewModel.MAX_TTS_LENGTH}") },
                    )
                }

                HorizontalDivider(color = BeigeMuted, thickness = 0.5.dp)

                SwitchSettingRow(
                    label = "알람음",
                    subtitle = musicDisplayName(uiState.musicUri),
                    checked = uiState.isMusicEnabled,
                    onCheckedChange = viewModel::updateMusicEnabled,
                )
                if (uiState.isMusicEnabled) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, BeigeMuted, RoundedCornerShape(10.dp))
                            .clickable(onClick = onSoundPick)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "알람음 선택하기",
                            fontSize = 13.sp,
                            color = WarmBrownMuted,
                            modifier = Modifier.weight(1f),
                        )
                        Text(text = "›", fontSize = 18.sp, color = BeigeMuted)
                    }
                }

                HorizontalDivider(color = BeigeMuted, thickness = 0.5.dp)

                SwitchSettingRow(
                    label = "해제 퍼즐",
                    checked = uiState.isPuzzleEnabled,
                    onCheckedChange = viewModel::updatePuzzleEnabled,
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
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, BeigeMuted, RoundedCornerShape(16.dp))
            .background(WarmWhite),
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = WarmBrownMuted,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp),
        )
        content()
    }
}

@Composable
private fun SwitchSettingRow(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = WarmBrown,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = WarmBrownMuted,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = switchColors(),
        )
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
    "/raw/" in uri -> {
        val rawResName = uri.substringAfterLast("/")
        val allPresets = DefaultSoundCatalog.animals + DefaultSoundCatalog.ttsPresets + DefaultSoundCatalog.music
        allPresets.firstOrNull { it.rawResName == rawResName }?.name ?: rawResName
    }
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

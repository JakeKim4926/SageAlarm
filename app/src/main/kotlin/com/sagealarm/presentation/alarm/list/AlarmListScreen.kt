package com.sagealarm.presentation.alarm.list

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.R
import com.sagealarm.domain.model.Alarm
import com.sagealarm.presentation.theme.Beige
import com.sagealarm.presentation.theme.BeigeMuted
import com.sagealarm.presentation.theme.Taupe
import com.sagealarm.presentation.theme.WarmBrown
import com.sagealarm.presentation.theme.WarmBrownMuted
import com.sagealarm.presentation.theme.WarmWhite
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onSettings: () -> Unit,
    viewModel: AlarmListViewModel = hiltViewModel(),
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val canAddAlarm by viewModel.canAddAlarm.collectAsStateWithLifecycle()
    var alarmToDelete by remember { mutableStateOf<Alarm?>(null) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ─── 권한 상태 ────────────────────────────────────────────────────────────
    var showNotifBanner by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED,
        )
    }
    var showExactAlarmBanner by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms(),
        )
    }
    var showBatteryBanner by remember {
        mutableStateOf(
            !(context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .isIgnoringBatteryOptimizations(context.packageName),
        )
    }

    // 설정에서 돌아올 때 재확인
    DisposableEffect(lifecycleOwner.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                showNotifBanner = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED
                showExactAlarmBanner = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
                showBatteryBanner = !(context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                    .isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 알림 권한은 시스템 다이얼로그로 직접 요청
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> showNotifBanner = !granted }

    // ─── 삭제 다이얼로그 ───────────────────────────────────────────────────────
    alarmToDelete?.let { alarm ->
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
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
                    viewModel.deleteAlarm(alarm)
                    alarmToDelete = null
                }) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
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
                    Column {
                        Text("SageAlarm")
                        Text(
                            text = "${alarms.size} / ${AlarmListViewModel.MAX_ALARM_COUNT}",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmBrownMuted,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { if (canAddAlarm) onAddAlarm() },
                containerColor = if (canAddAlarm) MaterialTheme.colorScheme.primary
                                 else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "알람 추가")
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.ic_background),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.825f)
                    .aspectRatio(1f)
                    .align(Alignment.Center),
                alpha = 0.18f,
                contentScale = ContentScale.Fit,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                // ─── 권한 안내 배너 ──────────────────────────────────────────
                if (showNotifBanner) {
                    PermissionBanner(
                        message = "알림 권한이 없으면 알람 화면이 뜨지 않아요.",
                        actionLabel = "허용",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                    )
                }
                if (showExactAlarmBanner) {
                    PermissionBanner(
                        message = "정확한 알람 권한이 없으면 시간이 맞지 않을 수 있어요.",
                        actionLabel = "설정",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                context.startActivity(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    },
                                )
                            }
                        },
                    )
                }
                if (showBatteryBanner) {
                    PermissionBanner(
                        message = "배터리 최적화로 인해 알람이 울리지 않을 수 있어요.",
                        actionLabel = "제외",
                        onAction = {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                },
                            )
                        },
                    )
                }

                // ─── 알람 목록 ───────────────────────────────────────────────
                if (alarms.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(items = alarms, key = { it.id }) { alarm ->
                            AlarmItem(
                                alarm = alarm,
                                onToggle = { isEnabled -> viewModel.toggleAlarm(alarm.id, isEnabled) },
                                onEdit = { onEditAlarm(alarm.id) },
                                onDelete = { alarmToDelete = alarm },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionBanner(
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BeigeMuted)
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = WarmBrown,
        )
        TextButton(onClick = onAction) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Taupe,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AlarmItem(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onDelete,
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatTime(alarm.hour, alarm.minute),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Light,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    Text(
                        text = if (alarm.hour < 12) "오전" else "오후",
                        fontSize = 14.sp,
                        color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 7.dp),
                    )
                }
                if (alarm.label.isNotBlank()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
                if (alarm.repeatDays.isNotEmpty()) {
                    Text(
                        text = formatRepeatDays(alarm.repeatDays),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Taupe,
                    checkedThumbColor = WarmWhite,
                    checkedBorderColor = Color.Transparent,
                    uncheckedTrackColor = Beige,
                    uncheckedThumbColor = WarmBrownMuted,
                    uncheckedBorderColor = BeigeMuted,
                ),
            )
        }
    }
}

private val DAY_NAMES = mapOf(
    Calendar.MONDAY to "월",
    Calendar.TUESDAY to "화",
    Calendar.WEDNESDAY to "수",
    Calendar.THURSDAY to "목",
    Calendar.FRIDAY to "금",
    Calendar.SATURDAY to "토",
    Calendar.SUNDAY to "일",
)

private val DAY_ORDER = listOf(
    Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
    Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY,
)

private fun formatTime(hour: Int, minute: Int): String {
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$hour12:%02d".format(minute)
}

private fun formatRepeatDays(days: Set<Int>): String =
    DAY_ORDER.filter { it in days }.joinToString(" ") { DAY_NAMES[it] ?: "" }

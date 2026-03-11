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
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onPuzzlePreview: () -> Unit,
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
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED,
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

    DisposableEffect(lifecycleOwner.lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                showNotifBanner = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                showExactAlarmBanner = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !(context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
                showBatteryBanner = !(context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                    .isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> showNotifBanner = !granted }

    // ─── 권한 다이얼로그 ───────────────────────────────────────────────────────
    when {
        showNotifBanner -> AlertDialog(
            onDismissRequest = { showNotifBanner = false },
            title = { Text("알림 권한 필요", color = WarmBrown) },
            text = { Text("알림 권한이 없으면 알람 화면이 표시되지 않아요.", color = WarmBrownMuted) },
            confirmButton = {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) { Text("허용", color = WarmBrown) }
            },
            dismissButton = {
                TextButton(onClick = { showNotifBanner = false }) {
                    Text("나중에", color = WarmBrownMuted)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
        showExactAlarmBanner -> AlertDialog(
            onDismissRequest = { showExactAlarmBanner = false },
            title = { Text("정확한 알람 권한 필요", color = WarmBrown) },
            text = { Text("정확한 알람 권한이 없으면 설정한 시간과 다르게 울릴 수 있어요.", color = WarmBrownMuted) },
            confirmButton = {
                TextButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            },
                        )
                    }
                }) { Text("설정으로 이동", color = WarmBrown) }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmBanner = false }) {
                    Text("나중에", color = WarmBrownMuted)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
        showBatteryBanner -> AlertDialog(
            onDismissRequest = { showBatteryBanner = false },
            title = { Text("배터리 최적화 제외 필요", color = WarmBrown) },
            text = { Text("배터리 최적화가 켜져 있으면 알람이 울리지 않을 수 있어요.", color = WarmBrownMuted) },
            confirmButton = {
                TextButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        },
                    )
                }) { Text("제외하기", color = WarmBrown) }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryBanner = false }) {
                    Text("나중에", color = WarmBrownMuted)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    // ─── 삭제 다이얼로그 ───────────────────────────────────────────────────────
    alarmToDelete?.let { alarm ->
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            title = { Text(text = "알람 삭제", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(text = "이 알람을 삭제할까요?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAlarm(alarm)
                    alarmToDelete = null
                }) { Text(text = "삭제", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
                    Text(text = "취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "알람",
                            fontWeight = FontWeight.SemiBold,
                            color = WarmBrownMuted,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onPuzzlePreview) {
                            Icon(
                                imageVector = Icons.Outlined.Extension,
                                contentDescription = "퍼즐 미리보기",
                                tint = WarmBrownMuted,
                            )
                        }
                    },
                    actions = {
                        if (alarms.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Beige)
                                    .border(1.dp, BeigeMuted, CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "${alarms.size} / ${AlarmListViewModel.MAX_ALARM_COUNT}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WarmBrownMuted,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = WarmBrownMuted,
                    ),
                )
                HorizontalDivider(color = Beige, thickness = 1.dp)
            }
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
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(color = Beige, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Project SAGE ALARM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarmBrownMuted,
                        letterSpacing = 0.4.sp,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(BeigeMuted),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "© 2026 J크",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = BeigeMuted,
                        letterSpacing = 0.6.sp,
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (alarms.isNotEmpty()) {
                // ─── 배경 워터마크 ────────────────────────────────────────────────
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
                // ─── 알람 목록 ────────────────────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
            } else {
                // ─── 빈 상태: 로고 + 텍스트를 광학 중심(정중앙보다 20% 위)에 묶어서 배치 ───
                Column(
                    modifier = Modifier.align(BiasAlignment(0f, -0.2f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_background),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .aspectRatio(1f),
                        alpha = 0.35f,
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "알람이 없어요",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarmBrownMuted,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "아래 + 버튼으로 첫 알람을 추가해 보세요",
                        fontSize = 12.sp,
                        color = BeigeMuted,
                    )
                }
            }
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
            .height(96.dp)
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onDelete,
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        // 시간 Row: Alignment.Center → 카드 정중앙 고정 (라벨 유무 무관)
        // 라벨: Alignment.TopStart + padding(top) → 독립적으로 위치 제어
        // padding(start=2dp): 38sp 폰트의 side bearing으로 인한 좌측 오프셋 보정
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = alarm.label.ifBlank { "placeholder" },
                fontSize = 12.sp,
                color = WarmBrownMuted.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 2.dp, top = 7.dp)
                    .alpha(if (alarm.label.isNotBlank()) 1f else 0f),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = formatTime(alarm.hour, alarm.minute),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Light,
                        color = if (alarm.isEnabled) WarmBrownMuted else BeigeMuted,
                        lineHeight = 38.sp,
                    )
                    Text(
                        text = if (alarm.hour < 12) "오전" else "오후",
                        fontSize = 13.sp,
                        color = if (alarm.isEnabled) WarmBrownMuted else BeigeMuted,
                        modifier = Modifier.padding(start = 4.dp, bottom = 5.dp),
                    )
                }
                if (alarm.repeatDays.isNotEmpty()) {
                    Text(
                        text = formatRepeatDays(alarm.repeatDays),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (alarm.isEnabled) Taupe else BeigeMuted,
                        letterSpacing = 0.3.sp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
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

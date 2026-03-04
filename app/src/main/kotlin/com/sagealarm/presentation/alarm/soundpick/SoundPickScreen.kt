package com.sagealarm.presentation.alarm.soundpick

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sagealarm.data.catalog.DefaultSoundCatalog
import com.sagealarm.domain.model.PresetSound
import com.sagealarm.presentation.theme.BeigeMuted
import com.sagealarm.presentation.theme.Taupe
import com.sagealarm.presentation.theme.WarmBrown
import com.sagealarm.presentation.theme.WarmBrownMuted

const val RESULT_MUSIC_URI = "sound_pick_music_uri"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickScreen(
    onBack: () -> Unit,
    onMusicSelected: (uri: String?) -> Unit,
    viewModel: SoundPickViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val packageName = context.packageName
    val previewUri by viewModel.previewUri.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.progress.collectAsState()

    val selectAndStop: (String?) -> Unit = { uri ->
        viewModel.stopPreview()
        onMusicSelected(uri)
    }

    val devicePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? -> uri?.let { selectAndStop(it.toString()) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알람음 선택") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopPreview()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // 기기에서 선택: 외부 파일 피커를 여는 액션 → Card + 화살표로 "액션" 성격 표시
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { devicePicker.launch("audio/*") }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "기기에서 선택",
                            style = MaterialTheme.typography.bodyLarge,
                            color = WarmBrown,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = WarmBrownMuted,
                        )
                    }
                }
            }

            // 기본 알람음: 선택 시 null 반환 → AlarmEditScreen에서 기본값으로 복원
            item { SectionHeader("기본") }
            item { SoundRow("기본 알람음") { selectAndStop(null) } }

            // 동물 소리
            item { SectionHeader("동물 소리") }
            items(DefaultSoundCatalog.animals) { preset ->
                val uri = buildRawUri(packageName, preset)
                PreviewableSoundRow(
                    name = preset.name,
                    isAvailable = isRawResAvailable(context, packageName, preset.rawResName),
                    isPreviewActive = previewUri == uri,
                    isPlaying = isPlaying && previewUri == uri,
                    progress = if (previewUri == uri) progress else 0f,
                    onSelect = { selectAndStop(uri) },
                    onTogglePreview = { viewModel.togglePreview(context, uri) },
                    onSeek = { viewModel.seekTo(it) },
                )
            }

            // TTS 멘트
            item { SectionHeader("TTS 멘트") }
            if (DefaultSoundCatalog.ttsPresets.isEmpty()) {
                item { EmptyLabel() }
            } else {
                items(DefaultSoundCatalog.ttsPresets) { preset ->
                    val uri = buildRawUri(packageName, preset)
                    PreviewableSoundRow(
                        name = preset.name,
                        isAvailable = isRawResAvailable(context, packageName, preset.rawResName),
                        isPreviewActive = previewUri == uri,
                        isPlaying = isPlaying && previewUri == uri,
                        progress = if (previewUri == uri) progress else 0f,
                        onSelect = { selectAndStop(uri) },
                        onTogglePreview = { viewModel.togglePreview(context, uri) },
                        onSeek = { viewModel.seekTo(it) },
                    )
                }
            }

            // 기본 음악
            item { SectionHeader("기본 음악") }
            if (DefaultSoundCatalog.music.isEmpty()) {
                item { EmptyLabel() }
            } else {
                items(DefaultSoundCatalog.music) { preset ->
                    val uri = buildRawUri(packageName, preset)
                    PreviewableSoundRow(
                        name = preset.name,
                        isAvailable = isRawResAvailable(context, packageName, preset.rawResName),
                        isPreviewActive = previewUri == uri,
                        isPlaying = isPlaying && previewUri == uri,
                        progress = if (previewUri == uri) progress else 0f,
                        onSelect = { selectAndStop(uri) },
                        onTogglePreview = { viewModel.togglePreview(context, uri) },
                        onSeek = { viewModel.seekTo(it) },
                    )
                }
            }
        }
    }
}

private fun buildRawUri(packageName: String, preset: PresetSound): String =
    "android.resource://$packageName/raw/${preset.rawResName}"

private fun isRawResAvailable(context: Context, packageName: String, rawResName: String): Boolean =
    context.resources.getIdentifier(rawResName, "raw", packageName) != 0

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = WarmBrownMuted,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@Composable
private fun SoundRow(
    name: String,
    onClick: () -> Unit,
) {
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        color = WarmBrown,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

@Composable
private fun PreviewableSoundRow(
    name: String,
    isAvailable: Boolean,
    isPreviewActive: Boolean,
    isPlaying: Boolean,
    progress: Float,
    onSelect: () -> Unit,
    onTogglePreview: () -> Unit,
    onSeek: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSelect)
                .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = WarmBrown,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = onTogglePreview,
                enabled = isAvailable,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "일시정지" else "미리 듣기",
                    tint = when {
                        !isAvailable -> BeigeMuted
                        isPreviewActive -> Taupe
                        else -> WarmBrownMuted
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        if (isPreviewActive) {
            SeekBar(
                progress = progress,
                onSeek = onSeek,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun SeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val width = size.width.toFloat()
                    down.consume()
                    onSeek((down.position.x / width).coerceIn(0f, 1f))
                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        onSeek((change.position.x / width).coerceIn(0f, 1f))
                        change.consume()
                        pressed = event.changes.any { it.pressed }
                    }
                }
            },
    ) {
        val lineY = center.y
        val lineStroke = 3.dp.toPx()
        drawLine(
            color = BeigeMuted,
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = lineStroke,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Taupe,
            start = Offset(0f, lineY),
            end = Offset(size.width * progress.coerceIn(0f, 1f), lineY),
            strokeWidth = lineStroke,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun EmptyLabel() {
    Text(
        text = "준비 중이에요",
        style = MaterialTheme.typography.bodyMedium,
        color = WarmBrownMuted,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 12.dp),
    )
}

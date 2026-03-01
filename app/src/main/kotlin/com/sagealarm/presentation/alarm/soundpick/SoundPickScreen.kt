package com.sagealarm.presentation.alarm.soundpick

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sagealarm.data.catalog.DefaultSoundCatalog
import com.sagealarm.domain.model.PresetSound
import com.sagealarm.presentation.theme.BeigeMuted
import com.sagealarm.presentation.theme.WarmBrown
import com.sagealarm.presentation.theme.WarmBrownMuted

const val RESULT_MUSIC_URI = "sound_pick_music_uri"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickScreen(
    onBack: () -> Unit,
    onMusicSelected: (uri: String?) -> Unit,
) {
    val packageName = LocalContext.current.packageName
    val devicePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? -> uri?.let { onMusicSelected(it.toString()) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("알람음 선택") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            // 고정 옵션: 기기 음악 + 기본 알람음
            // Android 기본 링톤 선택 UI와 동일한 패턴 — Card로 묶어 프리셋 섹션과 명확히 구분
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Column {
                        // 기기에서 선택: 외부 파일 피커를 여는 액션 → 화살표 아이콘으로 표시
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
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = BeigeMuted,
                        )
                        // 기본 알람음: 선택 시 null 반환 → AlarmEditScreen에서 기본값으로 복원
                        Text(
                            text = "기본 알람음",
                            style = MaterialTheme.typography.bodyLarge,
                            color = WarmBrown,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMusicSelected(null) }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                        )
                    }
                }
            }

            // 동물 소리
            item { SectionHeader("동물 소리") }
            items(DefaultSoundCatalog.animals) { preset ->
                SoundRow(preset.name) { onMusicSelected(buildRawUri(packageName, preset)) }
            }

            // TTS 멘트
            item { SectionHeader("TTS 멘트") }
            if (DefaultSoundCatalog.ttsPresets.isEmpty()) {
                item { EmptyLabel() }
            } else {
                items(DefaultSoundCatalog.ttsPresets) { preset ->
                    SoundRow(preset.name) { onMusicSelected(buildRawUri(packageName, preset)) }
                }
            }

            // 기본 음악
            item { SectionHeader("기본 음악") }
            if (DefaultSoundCatalog.music.isEmpty()) {
                item { EmptyLabel() }
            } else {
                items(DefaultSoundCatalog.music) { preset ->
                    SoundRow(preset.name) { onMusicSelected(buildRawUri(packageName, preset)) }
                }
            }
        }
    }
}

private fun buildRawUri(packageName: String, preset: PresetSound): String =
    "android.resource://$packageName/raw/${preset.rawResName}"

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
private fun EmptyLabel() {
    Text(
        text = "준비 중이에요",
        style = MaterialTheme.typography.bodyMedium,
        color = WarmBrownMuted,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 12.dp),
    )
}

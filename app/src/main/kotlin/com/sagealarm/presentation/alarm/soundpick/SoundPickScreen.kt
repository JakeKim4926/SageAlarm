package com.sagealarm.presentation.alarm.soundpick

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sagealarm.data.catalog.DefaultSoundCatalog
import com.sagealarm.domain.model.PresetSound
import com.sagealarm.presentation.theme.WarmBrown
import com.sagealarm.presentation.theme.WarmBrownMuted

const val RESULT_MUSIC_URI = "sound_pick_music_uri"
const val RESULT_TTS_TEXT = "sound_pick_tts_text"

// 빈 문자열 = 시스템 기본 알람음 (musicUri = null 과 동일)
const val SYSTEM_DEFAULT_SOUND = ""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundPickScreen(
    onBack: () -> Unit,
    onMusicSelected: (uri: String?) -> Unit,
    onTtsSelected: (text: String) -> Unit,
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
            item {
                SoundRow(name = "기본 알람음") { onMusicSelected(null) }
                HorizontalDivider()
            }

            item {
                SoundRow(name = "기기에서 선택") { devicePicker.launch("audio/*") }
                HorizontalDivider()
            }

            item { SectionHeader(title = "동물 소리") }
            items(DefaultSoundCatalog.animals) { preset ->
                SoundRow(name = preset.name) {
                    onMusicSelected(buildRawUri(packageName, preset))
                }
            }

            item { SectionHeader(title = "TTS 멘트") }
            items(DefaultSoundCatalog.ttsPresets) { preset ->
                SoundRow(name = preset.name) {
                    preset.ttsText?.let { onTtsSelected(it) }
                }
            }

            item { SectionHeader(title = "기본 음악") }
            if (DefaultSoundCatalog.music.isEmpty()) {
                item {
                    Text(
                        text = "준비 중이에요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmBrownMuted,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
            } else {
                items(DefaultSoundCatalog.music) { preset ->
                    SoundRow(name = preset.name) {
                        onMusicSelected(buildRawUri(packageName, preset))
                    }
                }
            }
        }
    }
}

private fun buildRawUri(packageName: String, preset: PresetSound): String =
    "android.resource://$packageName/raw/${preset.rawResName}"

@Composable
private fun SectionHeader(title: String) {
    Column {
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = WarmBrownMuted,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
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
            .padding(horizontal = 16.dp, vertical = 16.dp),
    )
}

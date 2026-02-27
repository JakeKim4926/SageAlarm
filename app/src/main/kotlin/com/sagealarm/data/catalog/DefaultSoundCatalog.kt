package com.sagealarm.data.catalog

import com.sagealarm.domain.model.PresetSound
import com.sagealarm.domain.model.SoundCategory

object DefaultSoundCatalog {

    val animals: List<PresetSound> = listOf(
        PresetSound(id = "animal_dog", name = "강아지", category = SoundCategory.ANIMAL, rawResName = "sound_dog"),
        PresetSound(id = "animal_horse", name = "말", category = SoundCategory.ANIMAL, rawResName = "sound_horse"),
        PresetSound(id = "animal_cow", name = "소", category = SoundCategory.ANIMAL, rawResName = "sound_cow"),
    )

    val ttsPresets: List<PresetSound> = listOf(
        PresetSound(id = "tts_wakeup", name = "일어날 시간이에요!", category = SoundCategory.TTS_PRESET, ttsText = "일어날 시간이에요!"),
        PresetSound(id = "tts_exercise", name = "운동할 시간!", category = SoundCategory.TTS_PRESET, ttsText = "운동할 시간이에요!"),
        PresetSound(id = "tts_medicine", name = "약 먹을 시간", category = SoundCategory.TTS_PRESET, ttsText = "약 먹을 시간이에요."),
        PresetSound(id = "tts_water", name = "물 마실 시간", category = SoundCategory.TTS_PRESET, ttsText = "물 한 잔 마실 시간이에요."),
        PresetSound(id = "tts_sleep", name = "잘 시간이에요", category = SoundCategory.TTS_PRESET, ttsText = "이제 잘 시간이에요. 오늘도 수고했어요."),
    )

    // 추후 저작권 프리 음악 추가
    val music: List<PresetSound> = emptyList()
}

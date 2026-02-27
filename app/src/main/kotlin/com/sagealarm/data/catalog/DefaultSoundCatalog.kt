package com.sagealarm.data.catalog

import com.sagealarm.domain.model.PresetSound
import com.sagealarm.domain.model.SoundCategory

object DefaultSoundCatalog {

    val animals: List<PresetSound> = listOf(
        PresetSound(id = "animal_dog", name = "강아지", category = SoundCategory.ANIMAL, rawResName = "sound_dog"),
        PresetSound(id = "animal_horse", name = "말", category = SoundCategory.ANIMAL, rawResName = "sound_horse"),
        PresetSound(id = "animal_cow", name = "소", category = SoundCategory.ANIMAL, rawResName = "sound_cow"),
    )

    // 추후 직접 녹음한 음성 파일 추가
    val ttsPresets: List<PresetSound> = emptyList()

    // 추후 저작권 프리 음악 추가
    val music: List<PresetSound> = emptyList()
}

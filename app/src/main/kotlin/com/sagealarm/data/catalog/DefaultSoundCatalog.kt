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

    val music: List<PresetSound> = listOf(
        PresetSound(id = "music_nice_morning_1", name = "Nice Morning 1", category = SoundCategory.MUSIC, rawResName = "nice_morning_1"),
        PresetSound(id = "music_nice_morning_2", name = "Nice Morning 2", category = SoundCategory.MUSIC, rawResName = "nice_morning_2"),
        PresetSound(id = "music_nice_morning_3", name = "Nice Morning 3", category = SoundCategory.MUSIC, rawResName = "nice_morning_3"),
        PresetSound(id = "music_nice_morning_4", name = "Nice Morning 4", category = SoundCategory.MUSIC, rawResName = "nice_morning_4"),
        PresetSound(id = "music_warm_slow_breathing_1", name = "Warm Morning, Slow Breathing 1", category = SoundCategory.MUSIC, rawResName = "warm_morning_slow_breathing_1"),
        PresetSound(id = "music_warm_slow_breathing_2", name = "Warm Morning, Slow Breathing 2", category = SoundCategory.MUSIC, rawResName = "warm_morning_slow_breathing_2"),
        PresetSound(id = "music_warm_slow_smiles_1", name = "Warm Morning, Slow Smiles 1", category = SoundCategory.MUSIC, rawResName = "warm_morning_slow_smiles_1"),
        PresetSound(id = "music_warm_slow_smiles_2", name = "Warm Morning, Slow Smiles 2", category = SoundCategory.MUSIC, rawResName = "warm_morning_slow_smiles_2"),
        PresetSound(id = "music_ileonawa_1", name = "일어나 1", category = SoundCategory.MUSIC, rawResName = "ileonawa_1"),
        PresetSound(id = "music_ileonawa_2", name = "일어나 2", category = SoundCategory.MUSIC, rawResName = "ileonawa_2"),
        PresetSound(id = "music_ileonawa_3", name = "일어나 3", category = SoundCategory.MUSIC, rawResName = "ileonawa_3"),
        PresetSound(id = "music_ileonawa_4", name = "일어나 4", category = SoundCategory.MUSIC, rawResName = "ileonawa_4"),
    )
}

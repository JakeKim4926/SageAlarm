package com.sagealarm.domain.repository

import com.sagealarm.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateDismissPuzzleEnabled(enabled: Boolean)
}

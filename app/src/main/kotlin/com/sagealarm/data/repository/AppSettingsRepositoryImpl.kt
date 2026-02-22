package com.sagealarm.data.repository

import com.sagealarm.data.local.datastore.AppSettingsDataStore
import com.sagealarm.domain.model.AppSettings
import com.sagealarm.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AppSettingsRepositoryImpl @Inject constructor(
    private val dataStore: AppSettingsDataStore,
) : AppSettingsRepository {

    override fun getSettings(): Flow<AppSettings> = dataStore.getSettings()

    override suspend fun updateDismissPuzzleEnabled(enabled: Boolean) =
        dataStore.updateDismissPuzzleEnabled(enabled)
}

package com.sagealarm.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.sagealarm.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val DISMISS_PUZZLE_ENABLED = booleanPreferencesKey("dismiss_puzzle_enabled")
    }

    fun getSettings(): Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            isDismissPuzzleEnabled = prefs[Keys.DISMISS_PUZZLE_ENABLED] ?: true,
        )
    }

    suspend fun updateDismissPuzzleEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DISMISS_PUZZLE_ENABLED] = enabled
        }
    }
}

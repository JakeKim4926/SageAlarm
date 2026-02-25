package com.sagealarm.presentation.alarm.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagealarm.domain.model.Alarm
import com.sagealarm.domain.repository.AlarmRepository
import com.sagealarm.domain.usecase.DeleteAlarmUseCase
import com.sagealarm.domain.usecase.SaveAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class AlarmEditUiState(
    val hour: Int = 8,
    val minute: Int = 0,
    val label: String = "",
    val ttsMessage: String = "",
    val repeatDays: Set<Int> = emptySet(),
    val musicUri: String? = null,
    val isLoading: Boolean = false,
    val isNavigateBack: Boolean = false,
    val isDuplicateTime: Boolean = false,
    val isDataLoaded: Boolean = false,
)

@HiltViewModel
class AlarmEditViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val saveAlarmUseCase: SaveAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmEditUiState())
    val uiState: StateFlow<AlarmEditUiState> = _uiState.asStateFlow()

    private var editingAlarm: Alarm? = null

    fun loadAlarm(alarmId: Long) {
        if (alarmId == -1L) {
            val now = Calendar.getInstance()
            _uiState.update {
                it.copy(
                    hour = now.get(Calendar.HOUR_OF_DAY),
                    minute = now.get(Calendar.MINUTE),
                    isDataLoaded = true,
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val alarm = alarmRepository.getAlarmById(alarmId)
            if (alarm != null) {
                editingAlarm = alarm
                _uiState.update {
                    it.copy(
                        hour = alarm.hour,
                        minute = alarm.minute,
                        label = alarm.label,
                        ttsMessage = alarm.ttsMessage,
                        repeatDays = alarm.repeatDays,
                        musicUri = alarm.musicUri,
                        isLoading = false,
                        isDataLoaded = true,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, isDataLoaded = true) }
            }
        }
    }

    fun updateLabel(label: String) {
        if (label.length <= MAX_LABEL_LENGTH) _uiState.update { it.copy(label = label) }
    }

    fun updateTtsMessage(msg: String) {
        if (msg.length <= MAX_TTS_LENGTH) _uiState.update { it.copy(ttsMessage = msg) }
    }

    fun toggleRepeatDay(day: Int) {
        _uiState.update { state ->
            val updated = if (day in state.repeatDays) state.repeatDays - day
            else state.repeatDays + day
            state.copy(repeatDays = updated)
        }
    }

    fun updateMusicUri(uri: String?) = _uiState.update { it.copy(musicUri = uri) }

    fun clearDuplicateError() = _uiState.update { it.copy(isDuplicateTime = false) }

    fun saveAlarm(hour: Int, minute: Int) {
        viewModelScope.launch {
            val existing = alarmRepository.getAlarmByTime(hour, minute)
            val isSelf = existing?.id == editingAlarm?.id
            if (existing != null && !isSelf) {
                _uiState.update { it.copy(isDuplicateTime = true) }
                return@launch
            }
            val state = _uiState.value
            val alarm = (editingAlarm ?: Alarm(hour = hour, minute = minute)).copy(
                hour = hour,
                minute = minute,
                label = state.label,
                ttsMessage = state.ttsMessage,
                repeatDays = state.repeatDays,
                musicUri = state.musicUri,
                isEnabled = true,
            )
            saveAlarmUseCase(alarm)
            _uiState.update { it.copy(isNavigateBack = true) }
        }
    }

    fun deleteAlarm() {
        val alarm = editingAlarm ?: return
        viewModelScope.launch {
            deleteAlarmUseCase(alarm)
            _uiState.update { it.copy(isNavigateBack = true) }
        }
    }

    companion object {
        const val MAX_LABEL_LENGTH = 50
        const val MAX_TTS_LENGTH = 50
    }
}

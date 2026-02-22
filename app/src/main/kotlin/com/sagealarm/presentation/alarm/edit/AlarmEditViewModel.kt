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
            _uiState.update { it.copy(hour = now.get(Calendar.HOUR_OF_DAY), minute = now.get(Calendar.MINUTE)) }
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
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateHour(hour: Int) = _uiState.update { it.copy(hour = hour) }
    fun updateMinute(minute: Int) = _uiState.update { it.copy(minute = minute) }
    fun updateLabel(label: String) = _uiState.update { it.copy(label = label) }
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

    fun saveAlarm() {
        viewModelScope.launch {
            val state = _uiState.value
            val alarm = (editingAlarm ?: Alarm(hour = state.hour, minute = state.minute)).copy(
                hour = state.hour,
                minute = state.minute,
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
        const val MAX_TTS_LENGTH = 100
    }
}

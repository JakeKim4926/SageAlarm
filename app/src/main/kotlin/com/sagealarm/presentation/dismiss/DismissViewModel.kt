package com.sagealarm.presentation.dismiss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagealarm.domain.repository.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PUZZLE_COUNT = 5
private const val NUMBER_MIN = 1
private const val NUMBER_MAX = 99

data class NumberItem(
    val value: Int,
    val xFraction: Float,
    val yFraction: Float,
)

data class DismissUiState(
    val isPuzzleEnabled: Boolean = true,
    val numberItems: List<NumberItem> = emptyList(),
    val sortedTarget: List<Int> = emptyList(),
    val nextIndex: Int = 0,
    val isDismissed: Boolean = false,
)

@HiltViewModel
class DismissViewModel @Inject constructor(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DismissUiState())
    val uiState: StateFlow<DismissUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _uiState.update { it.copy(isPuzzleEnabled = settings.isDismissPuzzleEnabled) }
            }
        }
        generatePuzzle()
    }

    fun dismiss() {
        _uiState.update { it.copy(isDismissed = true) }
    }

    fun onNumberClicked(value: Int) {
        val state = _uiState.value
        if (state.isDismissed) return

        val expected = state.sortedTarget.getOrNull(state.nextIndex) ?: return

        if (value == expected) {
            val nextIndex = state.nextIndex + 1
            if (nextIndex >= state.sortedTarget.size) {
                _uiState.update { it.copy(isDismissed = true) }
            } else {
                _uiState.update { it.copy(nextIndex = nextIndex) }
            }
        } else {
            generatePuzzle()
        }
    }

    private fun generatePuzzle() {
        val numbers = (NUMBER_MIN..NUMBER_MAX).shuffled().take(PUZZLE_COUNT)
        val sorted = numbers.sorted()
        val items = numbers.map { value ->
            NumberItem(
                value = value,
                xFraction = (0.05f..0.75f).random(),
                yFraction = (0.05f..0.75f).random(),
            )
        }
        _uiState.update {
            it.copy(
                numberItems = items,
                sortedTarget = sorted,
                nextIndex = 0,
                isDismissed = false,
            )
        }
    }

    private fun ClosedFloatingPointRange<Float>.random(): Float =
        start + (endInclusive - start) * Random.nextFloat()
}

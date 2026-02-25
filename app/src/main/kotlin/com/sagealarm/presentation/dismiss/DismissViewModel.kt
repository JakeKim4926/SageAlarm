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
    val isDismissed: Boolean = false,
)

@HiltViewModel
class DismissViewModel @Inject constructor(
    private val settingsRepository: AppSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DismissUiState())
    val uiState: StateFlow<DismissUiState> = _uiState.asStateFlow()

    private var sortedTarget: List<Int> = emptyList()
    private var nextIndex: Int = 0

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
        if (_uiState.value.isDismissed) return

        val expected = sortedTarget.getOrNull(nextIndex) ?: return

        if (value == expected) {
            nextIndex++
            if (nextIndex >= sortedTarget.size) {
                _uiState.update { it.copy(isDismissed = true) }
            }
        } else {
            generatePuzzle()
        }
    }

    private fun generatePuzzle() {
        val numbers = (NUMBER_MIN..NUMBER_MAX).shuffled().take(PUZZLE_COUNT)
        sortedTarget = numbers.sorted()
        nextIndex = 0
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
                isDismissed = false,
            )
        }
    }

    private fun ClosedFloatingPointRange<Float>.random(): Float =
        start + (endInclusive - start) * Random.nextFloat()
}

package com.sagealarm.presentation.dismiss

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sagealarm.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random
import javax.inject.Inject

private const val PUZZLE_COUNT = 5
private const val NUMBER_MIN = 1
private const val NUMBER_MAX = 99
private const val MIN_FRACTION_DISTANCE = 0.22f
private const val MAX_POSITION_ATTEMPTS = 100

internal const val PATTERN_GRID_SIZE = 3
private const val PATTERN_TILE_COUNT = PATTERN_GRID_SIZE * PATTERN_GRID_SIZE
internal const val PATTERN_SEQUENCE_LENGTH = 4
private const val PATTERN_FLASH_DURATION_MS = 600L
private const val PATTERN_FLASH_GAP_MS = 200L
private const val PATTERN_INITIAL_DELAY_MS = 800L

enum class PuzzleType { NUMBER_ORDER, PATTERN_FOLLOW }

data class NumberItem(
    val value: Int,
    val xFraction: Float,
    val yFraction: Float,
)

data class TileState(
    val index: Int,
    val isHighlighted: Boolean = false,
)

data class DismissUiState(
    val isPuzzleEnabled: Boolean = false,
    val puzzleType: PuzzleType = PuzzleType.NUMBER_ORDER,
    val isDismissed: Boolean = false,
    // Number puzzle
    val numberItems: List<NumberItem> = emptyList(),
    // Pattern puzzle
    val patternTiles: List<TileState> = emptyList(),
    val isShowingPattern: Boolean = false,
    val patternInputProgress: Int = 0,
)

@HiltViewModel
class DismissViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DismissUiState())
    val uiState: StateFlow<DismissUiState> = _uiState.asStateFlow()

    private var sortedTarget: List<Int> = emptyList()
    private var nextIndex: Int = 0

    private var patternSequence: List<Int> = emptyList()
    private var patternInputIndex: Int = 0
    private var patternPlaybackJob: Job? = null

    init {
        val alarmId = savedStateHandle.get<Long>("alarmId") ?: -1L
        viewModelScope.launch {
            val alarm = alarmRepository.getAlarmById(alarmId)
            _uiState.update { it.copy(isPuzzleEnabled = alarm?.isPuzzleEnabled ?: false) }
        }
        val selectedType = if (Random.nextBoolean()) PuzzleType.NUMBER_ORDER else PuzzleType.PATTERN_FOLLOW
        _uiState.update { it.copy(puzzleType = selectedType) }
        when (selectedType) {
            PuzzleType.NUMBER_ORDER -> generateNumberPuzzle()
            PuzzleType.PATTERN_FOLLOW -> generatePatternPuzzle()
        }
    }

    fun dismiss() {
        _uiState.update { it.copy(isDismissed = true) }
    }

    fun onNumberClicked(value: Int) {
        if (_uiState.value.isDismissed) return
        val expected = sortedTarget.getOrNull(nextIndex) ?: return
        if (value == expected) {
            nextIndex++
            val remaining = _uiState.value.numberItems.filter { it.value != value }
            if (nextIndex >= sortedTarget.size) {
                _uiState.update { it.copy(isDismissed = true) }
            } else {
                _uiState.update { it.copy(numberItems = remaining) }
            }
        } else {
            generateNumberPuzzle()
        }
    }

    fun onPatternTileTapped(tileIndex: Int) {
        if (_uiState.value.isShowingPattern) return
        if (_uiState.value.isDismissed) return
        val expected = patternSequence.getOrNull(patternInputIndex) ?: return
        if (tileIndex == expected) {
            patternInputIndex++
            if (patternInputIndex >= patternSequence.size) {
                _uiState.update { it.copy(isDismissed = true) }
            } else {
                _uiState.update { it.copy(patternInputProgress = patternInputIndex) }
            }
        } else {
            generatePatternPuzzle()
        }
    }

    private fun generateNumberPuzzle() {
        val numbers = (NUMBER_MIN..NUMBER_MAX).shuffled().take(PUZZLE_COUNT)
        sortedTarget = numbers.sortedDescending()
        nextIndex = 0
        val positions = generateNonOverlappingPositions(PUZZLE_COUNT)
        val items = numbers.mapIndexed { index, value ->
            NumberItem(
                value = value,
                xFraction = positions[index].first,
                yFraction = positions[index].second,
            )
        }
        _uiState.update { it.copy(numberItems = items, isDismissed = false) }
    }

    private fun generatePatternPuzzle() {
        val sequence = (0 until PATTERN_TILE_COUNT).toList().shuffled().take(PATTERN_SEQUENCE_LENGTH)
        patternSequence = sequence
        patternInputIndex = 0
        val tiles = (0 until PATTERN_TILE_COUNT).map { TileState(index = it) }
        _uiState.update {
            it.copy(
                patternTiles = tiles,
                isShowingPattern = true,
                patternInputProgress = 0,
                isDismissed = false,
            )
        }
        playPatternSequence()
    }

    private fun playPatternSequence() {
        patternPlaybackJob?.cancel()
        patternPlaybackJob = viewModelScope.launch {
            delay(PATTERN_INITIAL_DELAY_MS)
            patternSequence.forEach { tileIndex ->
                _uiState.update { state ->
                    state.copy(
                        patternTiles = state.patternTiles.map {
                            it.copy(isHighlighted = it.index == tileIndex)
                        },
                    )
                }
                delay(PATTERN_FLASH_DURATION_MS)
                _uiState.update { state ->
                    state.copy(patternTiles = state.patternTiles.map { it.copy(isHighlighted = false) })
                }
                delay(PATTERN_FLASH_GAP_MS)
            }
            _uiState.update { it.copy(isShowingPattern = false) }
        }
    }

    private fun generateNonOverlappingPositions(count: Int): List<Pair<Float, Float>> {
        val result = mutableListOf<Pair<Float, Float>>()
        repeat(count) {
            var attempts = 0
            var pos: Pair<Float, Float>
            do {
                pos = Pair(
                    (0.0f..1.0f).random(),
                    (0.15f..1.0f).random(),
                )
                attempts++
            } while (
                attempts < MAX_POSITION_ATTEMPTS &&
                result.any { other ->
                    (pos.first - other.first).pow(2) + (pos.second - other.second).pow(2) <
                    MIN_FRACTION_DISTANCE * MIN_FRACTION_DISTANCE
                }
            )
            result.add(pos)
        }
        return result
    }

    private fun ClosedFloatingPointRange<Float>.random(): Float =
        start + (endInclusive - start) * Random.nextFloat()
}

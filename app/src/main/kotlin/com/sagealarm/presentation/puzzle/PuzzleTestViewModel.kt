package com.sagealarm.presentation.puzzle

import android.graphics.Color as AndroidColor
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
private const val CAPTCHA_LENGTH = 6
private const val CAPTCHA_NOISE_COUNT = 6
private const val CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789"
private const val COLOR_WORD_ROUNDS = 3
private const val COLOR_OPTION_COUNT = 4
private const val PATTERN_TILE_COUNT = 9
internal const val PATTERN_SEQUENCE_LENGTH = 4
private const val PATTERN_FLASH_DURATION_MS = 600L
private const val PATTERN_FLASH_GAP_MS = 200L
private const val PATTERN_INITIAL_DELAY_MS = 800L

data class PuzzleNumberItem(
    val value: Int,
    val xFraction: Float,
    val yFraction: Float,
)

data class CaptchaCharItem(
    val char: Char,
    val rotationDeg: Float,
    val yOffsetFraction: Float,
    val scaleFactor: Float,
    val skewX: Float,
    val colorArgb: Int,
)

data class CaptchaNoiseItem(
    val startXFraction: Float,
    val startYFraction: Float,
    val endXFraction: Float,
    val endYFraction: Float,
    val colorArgb: Int,
)

data class ColorWordOption(
    val name: String,
    val colorArgb: Long,
)

data class ColorWordRound(
    val word: String,
    val inkColorArgb: Long,
    val correctName: String,
    val options: List<ColorWordOption>,
)

data class PuzzleTestUiState(
    val puzzleType: PuzzleType = PuzzleType.NUMBER_ORDER,
    val numberItems: List<PuzzleNumberItem> = emptyList(),
    val captchaTarget: String = "",
    val captchaInput: String = "",
    val captchaChars: List<CaptchaCharItem> = emptyList(),
    val captchaNoiseLines: List<CaptchaNoiseItem> = emptyList(),
    val colorWordRound: ColorWordRound? = null,
    val colorWordProgress: Int = 0,
    val patternHighlightedIndex: Int? = null,
    val isShowingPattern: Boolean = false,
    val patternInputProgress: Int = 0,
    val isComplete: Boolean = false,
)

@HiltViewModel
class PuzzleTestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val puzzleType: PuzzleType = PuzzleType.valueOf(
        savedStateHandle.get<String>("puzzleType") ?: PuzzleType.NUMBER_ORDER.name,
    )

    private val _uiState = MutableStateFlow(PuzzleTestUiState(puzzleType = puzzleType))
    val uiState: StateFlow<PuzzleTestUiState> = _uiState.asStateFlow()

    private var sortedTarget: List<Int> = emptyList()
    private var nextIndex: Int = 0

    private var patternSequence: List<Int> = emptyList()
    private var patternInputIndex: Int = 0
    private var patternPlaybackJob: Job? = null

    init {
        generatePuzzle()
    }

    fun restart() {
        generatePuzzle()
    }

    fun onNumberClicked(value: Int) {
        if (_uiState.value.isComplete) return
        val expected = sortedTarget.getOrNull(nextIndex) ?: return
        if (value == expected) {
            nextIndex++
            val remaining = _uiState.value.numberItems.filter { it.value != value }
            if (nextIndex >= sortedTarget.size) {
                _uiState.update { it.copy(isComplete = true, numberItems = emptyList()) }
            } else {
                _uiState.update { it.copy(numberItems = remaining) }
            }
        } else {
            generatePuzzle()
        }
    }

    fun onColorWordOptionSelected(name: String) {
        if (_uiState.value.isComplete) return
        val round = _uiState.value.colorWordRound ?: return
        val newProgress = if (name == round.correctName) {
            _uiState.value.colorWordProgress + 1
        } else {
            0
        }
        if (newProgress >= COLOR_WORD_ROUNDS) {
            _uiState.update { it.copy(isComplete = true, colorWordProgress = newProgress) }
        } else {
            _uiState.update { it.copy(colorWordProgress = newProgress) }
            generateColorWord()
        }
    }

    fun onCaptchaInputChanged(input: String) {
        if (_uiState.value.isComplete) return
        val filtered = input.filter { it.isLetterOrDigit() }.take(CAPTCHA_LENGTH)
        _uiState.update { it.copy(captchaInput = filtered) }
        if (filtered.length == CAPTCHA_LENGTH) {
            if (filtered == _uiState.value.captchaTarget) {
                _uiState.update { it.copy(isComplete = true) }
            } else {
                generateCaptcha()
            }
        }
    }

    fun onPatternTileTapped(tileIndex: Int) {
        if (_uiState.value.isShowingPattern) return
        if (_uiState.value.isComplete) return
        val expected = patternSequence.getOrNull(patternInputIndex) ?: return
        if (tileIndex == expected) {
            patternInputIndex++
            if (patternInputIndex >= patternSequence.size) {
                _uiState.update { it.copy(isComplete = true) }
            } else {
                _uiState.update { it.copy(patternInputProgress = patternInputIndex) }
            }
        } else {
            generatePatternFollow()
        }
    }

    private fun generatePuzzle() {
        _uiState.update { it.copy(isComplete = false, colorWordProgress = 0) }
        when (puzzleType) {
            PuzzleType.NUMBER_ORDER -> generateNumberOrder()
            PuzzleType.CAPTCHA -> generateCaptcha()
            PuzzleType.COLOR_WORD -> generateColorWord()
            PuzzleType.PATTERN_FOLLOW -> generatePatternFollow()
        }
    }

    private fun generatePatternFollow() {
        val sequence = (0 until PATTERN_TILE_COUNT).toList().shuffled().take(PATTERN_SEQUENCE_LENGTH)
        patternSequence = sequence
        patternInputIndex = 0
        _uiState.update {
            it.copy(
                patternHighlightedIndex = null,
                isShowingPattern = true,
                patternInputProgress = 0,
                isComplete = false,
            )
        }
        patternPlaybackJob?.cancel()
        patternPlaybackJob = viewModelScope.launch {
            delay(PATTERN_INITIAL_DELAY_MS)
            patternSequence.forEach { tileIndex ->
                _uiState.update { it.copy(patternHighlightedIndex = tileIndex) }
                delay(PATTERN_FLASH_DURATION_MS)
                _uiState.update { it.copy(patternHighlightedIndex = null) }
                delay(PATTERN_FLASH_GAP_MS)
            }
            _uiState.update { it.copy(isShowingPattern = false) }
        }
    }

    private fun generateNumberOrder() {
        val numbers = (NUMBER_MIN..NUMBER_MAX).shuffled().take(PUZZLE_COUNT)
        sortedTarget = numbers.sortedDescending()
        nextIndex = 0
        val positions = generateNonOverlappingPositions(PUZZLE_COUNT)
        val items = numbers.mapIndexed { index, value ->
            PuzzleNumberItem(
                value = value,
                xFraction = positions[index].first,
                yFraction = positions[index].second,
            )
        }
        _uiState.update { it.copy(numberItems = items, isComplete = false) }
    }

    private fun generateColorWord() {
        val inkColor = COLOR_PALETTE.random()
        val wordColor = COLOR_PALETTE.filter { it.first != inkColor.first }.random()
        val distractors = COLOR_PALETTE
            .filter { it.first != inkColor.first }
            .shuffled()
            .take(COLOR_OPTION_COUNT - 1)
        val options = (listOf(ColorWordOption(inkColor.first, inkColor.second)) +
            distractors.map { ColorWordOption(it.first, it.second) }).shuffled()
        _uiState.update {
            it.copy(
                colorWordRound = ColorWordRound(
                    word = wordColor.first,
                    inkColorArgb = inkColor.second,
                    correctName = inkColor.first,
                    options = options,
                ),
            )
        }
    }

    private fun generateCaptcha() {
        val target = (1..CAPTCHA_LENGTH).map {
            CAPTCHA_CHARS[Random.nextInt(CAPTCHA_CHARS.length)]
        }.joinToString("")
        val chars = target.map { char ->
            CaptchaCharItem(
                char = char,
                rotationDeg = Random.nextFloat() * 50f - 25f,
                yOffsetFraction = Random.nextFloat() * 0.3f - 0.15f,
                scaleFactor = 0.8f + Random.nextFloat() * 0.5f,
                skewX = Random.nextFloat() * 0.4f - 0.2f,
                colorArgb = CHAR_COLORS[Random.nextInt(CHAR_COLORS.size)],
            )
        }
        val noiseLines = (1..CAPTCHA_NOISE_COUNT).map {
            CaptchaNoiseItem(
                startXFraction = Random.nextFloat(),
                startYFraction = Random.nextFloat(),
                endXFraction = Random.nextFloat(),
                endYFraction = Random.nextFloat(),
                colorArgb = NOISE_COLORS[Random.nextInt(NOISE_COLORS.size)],
            )
        }
        _uiState.update {
            it.copy(
                captchaTarget = target,
                captchaInput = "",
                captchaChars = chars,
                captchaNoiseLines = noiseLines,
                isComplete = false,
            )
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

    companion object {
        private val COLOR_PALETTE = listOf<Pair<String, Long>>(
            Pair("빨강", 0xFFD32F2FL),
            Pair("파랑", 0xFF1565C0L),
            Pair("초록", 0xFF2E7D32L),
            Pair("노랑", 0xFFF9A825L),
            Pair("보라", 0xFF6A1B9AL),
            Pair("주황", 0xFFE65100L),
        )
        private val CHAR_COLORS = listOf(
            AndroidColor.rgb(0x3A, 0x2F, 0x1E),
            AndroidColor.rgb(0x55, 0x44, 0x30),
            AndroidColor.rgb(0x7A, 0x6A, 0x56),
            AndroidColor.rgb(0x8B, 0x5E, 0x3C),
            AndroidColor.rgb(0x6B, 0x50, 0x42),
        )
        private val NOISE_COLORS = listOf(
            AndroidColor.argb(80, 0xA8, 0x90, 0x70),
            AndroidColor.argb(80, 0xCE, 0xC4, 0xB4),
            AndroidColor.argb(60, 0x7A, 0x6A, 0x56),
        )
    }
}

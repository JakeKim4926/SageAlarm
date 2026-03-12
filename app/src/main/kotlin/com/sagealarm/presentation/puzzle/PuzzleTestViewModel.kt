package com.sagealarm.presentation.puzzle

import android.graphics.Color as AndroidColor
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

data class PuzzleTestUiState(
    val puzzleType: PuzzleType = PuzzleType.NUMBER_ORDER,
    val numberItems: List<PuzzleNumberItem> = emptyList(),
    val captchaTarget: String = "",
    val captchaInput: String = "",
    val captchaChars: List<CaptchaCharItem> = emptyList(),
    val captchaNoiseLines: List<CaptchaNoiseItem> = emptyList(),
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

    private fun generatePuzzle() {
        _uiState.update { it.copy(isComplete = false) }
        when (puzzleType) {
            PuzzleType.NUMBER_ORDER -> generateNumberOrder()
            PuzzleType.CAPTCHA -> generateCaptcha()
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

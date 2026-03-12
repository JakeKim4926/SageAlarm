package com.sagealarm.presentation.puzzle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.presentation.theme.Beige
import com.sagealarm.presentation.theme.BeigeMuted
import com.sagealarm.presentation.theme.Taupe
import com.sagealarm.presentation.theme.WarmBrown
import com.sagealarm.presentation.theme.WarmBrownMuted
import com.sagealarm.presentation.theme.WarmWhite

private val BUTTON_SIZE = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleTestScreen(
    onBack: () -> Unit,
    viewModel: PuzzleTestViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = uiState.puzzleType.displayName,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmBrownMuted,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로",
                                tint = WarmBrownMuted,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                HorizontalDivider(color = Beige, thickness = 1.dp)
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        if (uiState.isComplete) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "해제 성공!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    color = WarmBrown,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = viewModel::restart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Taupe,
                        contentColor = WarmWhite,
                    ),
                ) {
                    Text(text = "다시 시작")
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            ) {
                Text(
                    text = uiState.puzzleType.instruction,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmBrownMuted,
                )

                when (uiState.puzzleType) {
                    PuzzleType.NUMBER_ORDER -> NumberOrderPuzzle(
                        numberItems = uiState.numberItems,
                        onNumberClicked = viewModel::onNumberClicked,
                    )
                    PuzzleType.CAPTCHA -> CaptchaPuzzle(
                        captchaChars = uiState.captchaChars,
                        captchaNoiseLines = uiState.captchaNoiseLines,
                        input = uiState.captchaInput,
                        onInputChanged = viewModel::onCaptchaInputChanged,
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptchaPuzzle(
    captchaChars: List<CaptchaCharItem>,
    captchaNoiseLines: List<CaptchaNoiseItem>,
    input: String,
    onInputChanged: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(captchaChars) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Beige)
                .border(1.dp, BeigeMuted, RoundedCornerShape(12.dp)),
        ) {
            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas

                val noisePaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    strokeWidth = 2.5f
                    style = android.graphics.Paint.Style.STROKE
                }
                captchaNoiseLines.forEach { line ->
                    noisePaint.color = line.colorArgb
                    nativeCanvas.drawLine(
                        line.startXFraction * size.width,
                        line.startYFraction * size.height,
                        line.endXFraction * size.width,
                        line.endYFraction * size.height,
                        noisePaint,
                    )
                }

                val charPaint = android.graphics.Paint().apply {
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.SERIF,
                        android.graphics.Typeface.BOLD_ITALIC,
                    )
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val charWidth = size.width / (captchaChars.size + 1)
                captchaChars.forEachIndexed { index, item ->
                    val x = charWidth * (index + 1)
                    charPaint.textSize = item.scaleFactor * size.height * 0.55f
                    charPaint.color = item.colorArgb
                    charPaint.textSkewX = item.skewX
                    val y = size.height / 2f +
                        item.yOffsetFraction * size.height +
                        charPaint.textSize * 0.35f
                    nativeCanvas.save()
                    nativeCanvas.rotate(item.rotationDeg, x, y)
                    nativeCanvas.drawText(item.char.toString(), x, y, charPaint)
                    nativeCanvas.restore()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = input,
            onValueChange = onInputChanged,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                capitalization = KeyboardCapitalization.None,
                imeAction = ImeAction.None,
            ),
            keyboardActions = KeyboardActions.Default,
            textStyle = TextStyle(
                textAlign = TextAlign.Center,
                letterSpacing = 6.sp,
                fontSize = 22.sp,
                fontFamily = FontFamily.Monospace,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = WarmBrown,
                unfocusedTextColor = WarmBrown,
                focusedBorderColor = Taupe,
                unfocusedBorderColor = BeigeMuted,
                cursorColor = Taupe,
            ),
            modifier = Modifier
                .width(240.dp)
                .focusRequester(focusRequester),
        )
    }
}

@Composable
private fun NumberOrderPuzzle(
    numberItems: List<PuzzleNumberItem>,
    onNumberClicked: (Int) -> Unit,
) {
    val density = LocalDensity.current
    Layout(
        content = {
            numberItems.forEach { item ->
                key(item.value) {
                    Button(
                        onClick = { onNumberClicked(item.value) },
                        modifier = Modifier.size(BUTTON_SIZE),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = WarmBrown,
                        ),
                    ) {
                        Text(
                            text = item.value.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { measurables, constraints ->
        val buttonSizePx = with(density) { BUTTON_SIZE.roundToPx() }
        val placeables = measurables.map { it.measure(Constraints.fixed(buttonSizePx, buttonSizePx)) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { index, placeable ->
                val item = numberItems[index]
                val x = ((constraints.maxWidth - buttonSizePx) * item.xFraction).toInt()
                val y = ((constraints.maxHeight - buttonSizePx) * item.yFraction).toInt()
                placeable.placeRelative(x, y)
            }
        }
    }
}

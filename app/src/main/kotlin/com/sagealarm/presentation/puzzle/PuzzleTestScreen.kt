package com.sagealarm.presentation.puzzle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.presentation.theme.Beige
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
                }
            }
        }
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

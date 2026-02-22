package com.sagealarm.presentation.dismiss

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.R
import com.sagealarm.service.AlarmService

@Composable
fun DismissScreen(
    onDismissed: () -> Unit,
    viewModel: DismissViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isDismissed) {
        if (uiState.isDismissed) {
            context.stopService(Intent(context, AlarmService::class.java))
            onDismissed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.dismiss_instruction),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )

        NumberPuzzle(
            numberItems = uiState.numberItems,
            sortedTarget = uiState.sortedTarget,
            nextIndex = uiState.nextIndex,
            onNumberClicked = viewModel::onNumberClicked,
        )

        ProgressIndicator(
            current = uiState.nextIndex,
            total = uiState.sortedTarget.size,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        )
    }
}

@Composable
private fun NumberPuzzle(
    numberItems: List<NumberItem>,
    sortedTarget: List<Int>,
    nextIndex: Int,
    onNumberClicked: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val buttonSizeDp = 64.dp
    val buttonSizePx = with(density) { buttonSizeDp.toPx() }
    val screenWidthFraction = 0.85f
    val screenHeightFraction = 0.75f

    Box(modifier = Modifier.fillMaxSize()) {
        numberItems.forEach { item ->
            val isNextTarget = sortedTarget.getOrNull(nextIndex) == item.value
            val isAlreadyTapped = sortedTarget.indexOf(item.value) < nextIndex

            if (!isAlreadyTapped) {
                NumberButton(
                    value = item.value,
                    isHighlighted = isNextTarget,
                    xFraction = item.xFraction * screenWidthFraction,
                    yFraction = item.yFraction * screenHeightFraction,
                    buttonSizeDp = buttonSizeDp,
                    onClick = { onNumberClicked(item.value) },
                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    value: Int,
    isHighlighted: Boolean,
    xFraction: Float,
    yFraction: Float,
    buttonSizeDp: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .size(buttonSizeDp)
                .align(Alignment.TopStart)
                .offset(
                    x = (xFraction * 100).dp,
                    y = (yFraction * 100).dp,
                ),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isHighlighted) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.surface,
            ),
        ) {
            Text(
                text = value.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ProgressIndicator(
    current: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "$current / $total",
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

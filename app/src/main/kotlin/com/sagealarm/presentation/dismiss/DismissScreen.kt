package com.sagealarm.presentation.dismiss

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sagealarm.R
import com.sagealarm.service.AlarmService

private val BUTTON_SIZE = 64.dp

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

    if (uiState.isPuzzleEnabled) {
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
                onNumberClicked = viewModel::onNumberClicked,
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Button(onClick = viewModel::dismiss) {
                Text(text = stringResource(R.string.notification_dismiss))
            }
        }
    }
}

@Composable
private fun NumberPuzzle(
    numberItems: List<NumberItem>,
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
                            contentColor = MaterialTheme.colorScheme.onSurface,
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

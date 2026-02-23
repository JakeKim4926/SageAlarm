package com.sagealarm.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Taupe,
    onPrimary = WarmWhite,
    surface = Beige,
    onSurface = WarmBrown,
    background = Ivory,
    onBackground = WarmBrown,
    secondary = TaupeLight,
    onSecondary = WarmBrown,
    surfaceVariant = Beige,
    onSurfaceVariant = WarmBrownMuted,
    outline = BeigeMuted,
    outlineVariant = BeigeMuted,
)

@Composable
fun SageAlarmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}

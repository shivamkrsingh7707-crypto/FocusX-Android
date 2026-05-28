package com.focusx.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.focusx.app.data.FocusXState

private val DarkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = TextDark,
    primaryContainer = AccentMuted,
    background = AmoledBlack,
    surface = SurfaceDark,
    surfaceVariant = Surface2Dark,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = Text2Dark,
    outline = BorderDark,
    outlineVariant = BorderDarkLight,
    secondary = AccentMuted,
    error = Danger,
)

private val LightScheme = lightColorScheme(
    primary = AccentLight,
    onPrimary = SurfaceLight,
    primaryContainer = Accent.copy(alpha = 0.1f),
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = Surface2Light,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = Text2Light,
    outline = BorderLight,
    outlineVariant = BorderLightLight,
    secondary = Accent.copy(alpha = 0.1f),
    error = Danger,
)

@Composable
fun FocusXTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}

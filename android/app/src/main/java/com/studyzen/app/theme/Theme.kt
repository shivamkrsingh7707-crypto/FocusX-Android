package com.studyzen.app.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FocusXColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleDim,
    onPrimaryContainer = Color.White,
    secondary = StreakAccent,
    onSecondary = Color.White,
    secondaryContainer = PrimaryPurpleDim.copy(alpha = 0.3f),
    onSecondaryContainer = Color.White,
    background = AmoledBlack,
    onBackground = TextPrimary,
    surface = CardDark,
    onSurface = TextPrimary,
    surfaceVariant = CardElevated,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = BorderLow,
    outlineVariant = BorderMid,
    inverseSurface = Color.White,
    inverseOnSurface = AmoledBlack,
    inversePrimary = PrimaryPurple
)

@Composable
fun FocusXTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FocusXColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AmoledBlack.toArgb()
            window.navigationBarColor = AmoledBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FocusXTypography,
        content = content
    )
}

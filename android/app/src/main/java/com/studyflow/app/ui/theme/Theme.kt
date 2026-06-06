package com.studyflow.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Immutable
data class StudyFlowColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceDim: Color,
    val onBackground: Color,
    val onSurface: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val borderMid: Color
)

private val DarkStudyFlowColors = StudyFlowColors(
    background = AmoledBlack,
    surface = CardDark,
    surfaceElevated = CardElevated,
    surfaceDim = SurfaceDim,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    border = BorderLow,
    borderMid = BorderMid
)

private val LightStudyFlowColors = StudyFlowColors(
    background = LightBackground,
    surface = LightSurface,
    surfaceElevated = LightSurfaceElevated,
    surfaceDim = LightSurfaceDim,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    border = LightBorderLow,
    borderMid = LightBorderMid
)

/**
 * Tracking local — when the theme changes, every consumer recomposes
 * exactly once and picks up the new tokens. We deliberately do NOT animate
 * the individual tokens (no `animateColorAsState` per field) because that
 * would force a recomposition on every frame for 360ms. The visual
 * transition is owned entirely by [ThemeRevealOverlay], which animates a
 * single graphics layer instead of 10 color fields.
 */
val LocalStudyFlowColors = compositionLocalOf { DarkStudyFlowColors }

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDim,
    onPrimaryContainer = Color.White,
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = AccentTealDim.copy(alpha = 0.3f),
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
    inversePrimary = PrimaryBlue
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = PrimaryBlueDim,
    secondary = AccentTeal,
    onSecondary = Color.White,
    secondaryContainer = AccentTeal.copy(alpha = 0.15f),
    onSecondaryContainer = AccentTealDim,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = LightTextSecondary,
    error = ErrorRed,
    onError = Color.White,
    outline = LightBorderLow,
    outlineVariant = LightBorderMid,
    inverseSurface = Color(0xFF1F2937),
    inverseOnSurface = Color.White,
    inversePrimary = PrimaryBlueLight
)

@Composable
fun StudyFlowTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val targetDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val tokens = if (targetDark) DarkStudyFlowColors else LightStudyFlowColors
    val scheme = if (targetDark) DarkColorScheme else LightColorScheme

    // System bars: update ONCE per theme change, not per frame. The reveal
    // overlay covers the screen during the transition, so we don't need to
    // animate the bars — flipping them at the same instant as the theme
    // tokens is invisible to the user.
    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(targetDark) {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = tokens.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = tokens.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !targetDark
            controller.isAppearanceLightNavigationBars = !targetDark
        }
    }

    CompositionLocalProvider(LocalStudyFlowColors provides tokens) {
        MaterialTheme(
            colorScheme = scheme,
            typography = StudyFlowTypography,
            content = content
        )
    }
}

object StudyFlowTheme {
    val colors: StudyFlowColors
        @Composable
        @ReadOnlyComposable
        get() = LocalStudyFlowColors.current
}

package com.studyflow.app.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { SYSTEM, LIGHT, DARK }

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

private const val ThemeAnimDurationMs = 350

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

    val targetColors = if (targetDark) DarkStudyFlowColors else LightStudyFlowColors
    val targetScheme = if (targetDark) DarkColorScheme else LightColorScheme

    // Animate each token so the whole UI smoothly crossfades between themes.
    val animSpec = tween<Color>(ThemeAnimDurationMs)
    val background by animateColorAsState(targetColors.background, animSpec, label = "bg")
    val surface by animateColorAsState(targetColors.surface, animSpec, label = "surface")
    val surfaceElevated by animateColorAsState(targetColors.surfaceElevated, animSpec, label = "surfaceElevated")
    val surfaceDim by animateColorAsState(targetColors.surfaceDim, animSpec, label = "surfaceDim")
    val onBackground by animateColorAsState(targetColors.onBackground, animSpec, label = "onBg")
    val onSurface by animateColorAsState(targetColors.onSurface, animSpec, label = "onSurface")
    val textSecondary by animateColorAsState(targetColors.textSecondary, animSpec, label = "textSec")
    val textMuted by animateColorAsState(targetColors.textMuted, animSpec, label = "textMuted")
    val border by animateColorAsState(targetColors.border, animSpec, label = "border")
    val borderMid by animateColorAsState(targetColors.borderMid, animSpec, label = "borderMid")

    val animatedTokens = StudyFlowColors(
        background = background,
        surface = surface,
        surfaceElevated = surfaceElevated,
        surfaceDim = surfaceDim,
        onBackground = onBackground,
        onSurface = onSurface,
        textSecondary = textSecondary,
        textMuted = textMuted,
        border = border,
        borderMid = borderMid
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = animatedTokens.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = animatedTokens.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !targetDark
            controller.isAppearanceLightNavigationBars = !targetDark
        }
    }

    CompositionLocalProvider(LocalStudyFlowColors provides animatedTokens) {
        MaterialTheme(
            colorScheme = targetScheme,
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

package com.studyflow.app.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
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

/**
 * Duration of the theme crossfade. 360 ms is long enough to feel deliberate
 * on a 120Hz panel (≈ 43 frames) but short enough that the user doesn't wait.
 */
private val ThemeCrossfadeMs = 360

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
    val targetTokens = if (targetDark) DarkStudyFlowColors else LightStudyFlowColors
    val targetScheme = if (targetDark) DarkColorScheme else LightColorScheme

    val spec: AnimationSpec<Color> = tween(
        durationMillis = ThemeCrossfadeMs,
        easing = FastOutSlowInEasing
    )

    // Animate every color token so dark<->light flows instead of snapping.
    // `animateColorAsState` is driven by the Choreographer, so it stays
    // perfectly in sync with the 120Hz frame budget.
    val animatedTokens = StudyFlowColors(
        background = animateColorAsState(targetTokens.background, spec, label = "bg").value,
        surface = animateColorAsState(targetTokens.surface, spec, label = "sf").value,
        surfaceElevated = animateColorAsState(targetTokens.surfaceElevated, spec, label = "se").value,
        surfaceDim = animateColorAsState(targetTokens.surfaceDim, spec, label = "sd").value,
        onBackground = animateColorAsState(targetTokens.onBackground, spec, label = "ob").value,
        onSurface = animateColorAsState(targetTokens.onSurface, spec, label = "os").value,
        textSecondary = animateColorAsState(targetTokens.textSecondary, spec, label = "ts").value,
        textMuted = animateColorAsState(targetTokens.textMuted, spec, label = "tm").value,
        border = animateColorAsState(targetTokens.border, spec, label = "bd").value,
        borderMid = animateColorAsState(targetTokens.borderMid, spec, label = "bm").value
    )
    val animatedScheme: ColorScheme = targetScheme.copy(
        background = animateColorAsState(targetScheme.background, spec, label = "mbg").value,
        onBackground = animateColorAsState(targetScheme.onBackground, spec, label = "mob").value,
        surface = animateColorAsState(targetScheme.surface, spec, label = "msf").value,
        onSurface = animateColorAsState(targetScheme.onSurface, spec, label = "mos").value,
        surfaceVariant = animateColorAsState(targetScheme.surfaceVariant, spec, label = "msv").value,
        onSurfaceVariant = animateColorAsState(targetScheme.onSurfaceVariant, spec, label = "mosv").value,
        outline = animateColorAsState(targetScheme.outline, spec, label = "mol").value,
        outlineVariant = animateColorAsState(targetScheme.outlineVariant, spec, label = "mov").value
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val argb = animatedTokens.background.toArgb()
            @Suppress("DEPRECATION")
            window.statusBarColor = argb
            @Suppress("DEPRECATION")
            window.navigationBarColor = argb
        }
        val controller = remember(view) {
            WindowCompat.getInsetsController((view.context as Activity).window, view)
        }
        LaunchedEffect(targetDark) {
            controller.isAppearanceLightStatusBars = !targetDark
            controller.isAppearanceLightNavigationBars = !targetDark
        }
    }

    CompositionLocalProvider(LocalStudyFlowColors provides animatedTokens) {
        MaterialTheme(
            colorScheme = animatedScheme,
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

package dev.focusx.app.ui.theme

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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.focusx.app.domain.ThemeMode

/**
 * A flat colour-token table for the current theme. We keep this an
 * immutable data class with no animated children. Composables that read
 * [LocalFocusXColors] only recompose when the theme *itself* changes —
 * never on a per-frame basis. The visual transition between themes is
 * owned by [ThemeRevealOverlay], which animates a single draw layer.
 */
@Immutable
data class FocusXColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceMuted: Color,
    val onBackground: Color,
    val onSurface: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textMuted: Color,
    val hairline: Color,
    val hairlineHi: Color,
    val primary: Color,
    val primarySoft: Color,
    val onPrimary: Color,
    val success: Color,
    val successSoft: Color,
    val warn: Color,
    val danger: Color,
    val break_: Color
)

private val Dark = FocusXColors(
    background = AmoledBlack,
    surface = Surface1,
    surfaceElevated = Surface2,
    surfaceMuted = Surface3,
    onBackground = TextHi,
    onSurface = TextHi,
    textSecondary = TextMid,
    textTertiary = TextLo,
    textMuted = TextMuted,
    hairline = Hairline,
    hairlineHi = HairlineHi,
    primary = AccentIndigo,
    primarySoft = AccentViolet,
    onPrimary = Color.White,
    success = Success,
    successSoft = SuccessDim,
    warn = Warn,
    danger = Danger,
    break_ = Break
)

private val Light = FocusXColors(
    background = LightBackground,
    surface = LightSurface1,
    surfaceElevated = LightSurface2,
    surfaceMuted = LightSurface3,
    onBackground = LightTextHi,
    onSurface = LightTextHi,
    textSecondary = LightTextMid,
    textTertiary = LightTextLo,
    textMuted = LightTextMuted,
    hairline = LightHairline,
    hairlineHi = LightHairlineHi,
    primary = LightAccent,
    primarySoft = LightAccentSoft,
    onPrimary = Color.White,
    success = LightSuccess,
    successSoft = LightSuccess,
    warn = Warn,
    danger = Danger,
    break_ = Break
)

/**
 * Tracking composition local — consumers will recompose exactly once
 * when the theme flips. We deliberately don't make the tokens themselves
 * animatable (that would force a 350ms recomposition cascade for every
 * card on the screen on every theme change).
 */
val LocalFocusXColors = compositionLocalOf { Dark }
val LocalIsDark = compositionLocalOf { true }

/**
 * Static local for things that are guaranteed never to change at runtime
 * (typographic families, motion durations). `staticCompositionLocalOf`
 * skips the per-read tracking overhead, which adds up when many screens
 * read these on every recomposition.
 */
val LocalMotion = staticCompositionLocalOf {
    Motion(
        fast = 140,
        normal = 240,
        slow = 360,
        springResponse = 0.45f,
        springDamping = 0.78f
    )
}

@Immutable
data class Motion(
    val fast: Int,
    val normal: Int,
    val slow: Int,
    val springResponse: Float,
    val springDamping: Float
)

private val DarkScheme = darkColorScheme(
    primary = AccentIndigo,
    onPrimary = Color.White,
    primaryContainer = Surface3,
    onPrimaryContainer = AccentLilac,
    secondary = AccentViolet,
    onSecondary = Color.White,
    background = AmoledBlack,
    onBackground = TextHi,
    surface = Surface1,
    onSurface = TextHi,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMid,
    error = Danger,
    onError = Color.White,
    outline = Hairline,
    outlineVariant = HairlineHi
)

private val LightScheme = lightColorScheme(
    primary = LightAccent,
    onPrimary = Color.White,
    primaryContainer = LightSurface2,
    onPrimaryContainer = LightAccent,
    secondary = LightAccentSoft,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextHi,
    surface = LightSurface1,
    onSurface = LightTextHi,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightTextMid,
    error = Danger,
    onError = Color.White,
    outline = LightHairline,
    outlineVariant = LightHairlineHi
)

@Composable
fun FocusXTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val tokens = if (dark) Dark else Light
    val scheme = if (dark) DarkScheme else LightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(dark) {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = tokens.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = tokens.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }

    CompositionLocalProvider(
        LocalFocusXColors provides tokens,
        LocalIsDark provides dark,
        LocalMotion provides Motion(
            fast = 140,
            normal = 240,
            slow = 360,
            springResponse = 0.45f,
            springDamping = 0.78f
        )
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = FocusXTypography,
            content = content
        )
    }
}

object FocusXTheme {
    val colors: FocusXColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFocusXColors.current

    val motion: Motion
        @Composable
        @ReadOnlyComposable
        get() = LocalMotion.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDark.current
}

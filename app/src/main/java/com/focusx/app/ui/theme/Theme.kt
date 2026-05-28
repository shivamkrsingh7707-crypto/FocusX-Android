package com.focusx.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AMOLED Dark palette
val AmoledBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF09090B)
val Surface2Dark = Color(0xFF161616)
val Surface3Dark = Color(0xFF1E1E1E)
val TextDark = Color(0xFFFFFFFF)
val Text2Dark = Color(0xFFA1A1AA)
val Text3Dark = Color(0xFF52525B)
val Accent = Color(0xFF8B5CF6)
val AccentMuted = Color(0x268B5CF6)
val BorderDark = Color(0x0FFFFFFF)
val BorderDarkLight = Color(0x1AFFFFFF)
val Success = Color(0xFF4ADE80)
val Danger = Color(0xFFF87171)

// Light palette
val SurfaceLight = Color(0xFFFFFFFF)
val Surface2Light = Color(0xFFF0F0F2)
val Surface3Light = Color(0xFFE8E8EC)
val TextLight = Color(0xFF1C1C1E)
val Text2Light = Color(0xFF6C6C70)
val Text3Light = Color(0xFFAEAEB2)
val AccentLight = Color(0xFF7C3AED)
val BorderLight = Color(0x0F000000)
val BorderLightLight = Color(0x1A000000)

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
    primaryContainer = AccentLight.copy(alpha = 0.1f),
    background = SurfaceLight,
    surface = SurfaceLight,
    surfaceVariant = Surface2Light,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = Text2Light,
    outline = BorderLight,
    outlineVariant = BorderLightLight,
    secondary = AccentLight.copy(alpha = 0.1f),
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

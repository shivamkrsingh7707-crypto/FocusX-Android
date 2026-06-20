package dev.focusx.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── Surfaces ──────────────────────────────────────────────────────────────
// Pure-black background saves the OLED panel real power and is the
// foundation of the visual language. The lighter elevations step *up* in
// luminance by ~3% per layer, which is enough to read on cheap displays
// without making the background feel grey.
internal val AmoledBlack = Color(0xFF000000)
internal val Surface0 = Color(0xFF070708)
internal val Surface1 = Color(0xFF0D0D10)
internal val Surface2 = Color(0xFF131318)
internal val Surface3 = Color(0xFF1B1B22)
internal val Hairline = Color(0x14FFFFFF)
internal val HairlineHi = Color(0x1FFFFFFF)

// ── Text ─────────────────────────────────────────────────────────────────
internal val TextHi = Color(0xFFF5F5F7)
internal val TextMid = Color(0xFFB4B4BE)
internal val TextLo = Color(0xFF7E7E8A)
internal val TextMuted = Color(0xFF55555F)

// ── Accent (Indigo → Violet) ─────────────────────────────────────────────
// Two-stop sweep is what the timer ring uses; the loop colour is the same
// pair so the user always knows what blue means.
internal val AccentIndigo = Color(0xFF6366F1)
internal val AccentViolet = Color(0xFF8B5CF6)
internal val AccentLilac = Color(0xFFA78BFA)

// ── Status ───────────────────────────────────────────────────────────────
internal val Success = Color(0xFF34D399)
internal val SuccessDim = Color(0xFF059669)
internal val Warn = Color(0xFFFBBF24)
internal val Danger = Color(0xFFF87171)
internal val Break = Color(0xFF22D3EE) // cyan for break sessions

// ── Light scheme (kept for system-mode fallback) ─────────────────────────
internal val LightBackground = Color(0xFFF7F7F8)
internal val LightSurface1 = Color(0xFFFFFFFF)
internal val LightSurface2 = Color(0xFFEFEFF1)
internal val LightSurface3 = Color(0xFFE5E5EA)
internal val LightHairline = Color(0x14000000)
internal val LightHairlineHi = Color(0x24000000)
internal val LightTextHi = Color(0xFF0F172A)
internal val LightTextMid = Color(0xFF475569)
internal val LightTextLo = Color(0xFF94A3B8)
internal val LightTextMuted = Color(0xFFCBD5E1)
internal val LightAccent = Color(0xFF4F46E5)
internal val LightAccentSoft = Color(0xFF6366F1)
internal val LightSuccess = Color(0xFF059669)

// ── Subject colour wheel ─────────────────────────────────────────────────
// Used as the deterministic subject colour; chosen so every one of the
// ten is distinguishable against the dark background and has decent text
// contrast for labels.
val SubjectPalette: List<Color> = listOf(
    Color(0xFF6366F1), // indigo
    Color(0xFF14B8A6), // teal
    Color(0xFFF59E0B), // amber
    Color(0xFFEF4444), // red
    Color(0xFF8B5CF6), // violet
    Color(0xFFEC4899), // pink
    Color(0xFF3B82F6), // blue
    Color(0xFF10B981), // emerald
    Color(0xFFF97316), // orange
    Color(0xFF06B6D4)  // cyan
)

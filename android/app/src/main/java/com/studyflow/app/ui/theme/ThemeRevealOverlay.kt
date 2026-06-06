package com.studyflow.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

/**
 * Animation length for the "freeze frame" reveal. 720 ms is a hair longer than
 * the theme crossfade (360 ms) so the user actually sees the wipe happen —
 * the new theme has already finished crossfading by the time the hole is
 * fully open, which is when the overlay disappears.
 */
private const val RevealDurationMs = 720
private const val HoldDurationMs = 120L
private const val GlowStrokeDp = 26f

/**
 * The reveal "punch" — a soft corner-to-full theme transition.
 *
 * The new theme renders behind this overlay. We draw a solid rectangle in the
 * OLD color over the entire screen, but punch an expanding circular hole in
 * it (via PathFillType.EvenOdd) so the new theme "wipes" out from the user's
 * tap. Two refinements on top of the original:
 *
 *  1. **Spring physics** for the radius — gives the wipe a natural, slightly
 *     bouncy finish that matches the rest of the app's motion language.
 *  2. **Soft outer glow ring** — drawn as a wide stroke around the hole edge
 *     with a radial fade, so the boundary between old and new looks like a
 *     warm light instead of a hard line.
 *
 * `drawWithCache` re-uses the [Path] across frames so the render thread
 * doesn't allocate at 120 Hz.
 */
@Composable
fun ThemeRevealOverlay(
    active: Boolean,
    origin: Offset,
    isTargetDark: Boolean,
    onFinished: () -> Unit
) {
    val radiusAnim = remember { Animatable(0f) }
    val glowAnim = remember { Animatable(0f) }

    LaunchedEffect(active) {
        if (active) {
            radiusAnim.snapTo(0f)
            glowAnim.snapTo(0f)
            // Run the wipe and the glow tail in parallel. The spring is
            // tuned to feel "snappy" (medium-low stiffness, low bounce).
            radiusAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                    visibilityThreshold = 0.001f
                )
            )
            glowAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = RevealDurationMs,
                    easing = FastOutSlowInEasing
                )
            )
            kotlinx.coroutines.delay(HoldDurationMs)
            onFinished()
        } else {
            radiusAnim.snapTo(0f)
            glowAnim.snapTo(0f)
        }
    }

    if (!active) return

    val overlayColor = if (isTargetDark) LightBackground else AmoledBlack

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val maxRadius = hypot(w, h) * 0.62f
                val strokePx = GlowStrokeDp.dp.toPx()
                onDrawWithContent {
                    val progress = radiusAnim.value
                    val glow = glowAnim.value
                    if (progress <= 0f) {
                        // Frame 0: the overlay hasn't expanded at all, so we
                        // paint the old theme to avoid flashing the new one
                        // for a single frame.
                        drawRect(color = overlayColor)
                        return@onDrawWithContent
                    }
                    val radius = maxRadius * progress

                    // Glow ring first (so the punch sits on top of it).
                    if (glow > 0f && progress < 1f) {
                        val ringAlpha = (1f - progress).coerceIn(0f, 1f) * 0.35f
                        val ringColor = if (isTargetDark) {
                            lerp(LightBackground, Color.Black, 0.6f).copy(alpha = ringAlpha)
                        } else {
                            lerp(AmoledBlack, Color.White, 0.6f).copy(alpha = ringAlpha)
                        }
                        drawCircle(
                            color = ringColor,
                            radius = radius + strokePx / 2f,
                            center = origin,
                            style = Stroke(width = strokePx),
                            blendMode = BlendMode.Plus
                        )
                    }

                    val path = Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRect(Rect(0f, 0f, w, h))
                        addOval(
                            Rect(
                                left = origin.x - radius,
                                top = origin.y - radius,
                                right = origin.x + radius,
                                bottom = origin.y + radius
                            )
                        )
                    }
                    drawPath(path = path, color = overlayColor, style = Fill)
                }
            }
    )
}

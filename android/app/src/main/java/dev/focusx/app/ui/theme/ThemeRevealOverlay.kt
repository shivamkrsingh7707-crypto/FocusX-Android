package dev.focusx.app.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.hypot

private const val RevealDurationMs = 620
private const val HoldDurationMs = 90L
private const val GlowStrokeDp = 18f

/**
 * The reveal "punch" — a soft corner-to-full theme transition.
 *
 * The new theme renders behind this overlay. We draw a solid rectangle
 * in the OLD color over the entire screen, but punch an expanding
 * circular hole in it (via PathFillType.EvenOdd) so the new theme
 * "wipes" out from the user's tap. Three performance-friendly
 * refinements:
 *
 *  1. **Spring physics** for the radius — gives the wipe a natural feel.
 *  2. **Single soft ring** — drawn with `SrcOver` (no `BlendMode.Plus`)
 *     so the GPU doesn't have to read-back the framebuffer on every frame.
 *  3. **`drawWithCache`** re-uses the [Path] so the render thread
 *     doesn't allocate at 120 Hz.
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
            delay(HoldDurationMs)
            onFinished()
        } else {
            radiusAnim.snapTo(0f)
            glowAnim.snapTo(0f)
        }
    }

    if (!active) return

    val overlayColor = if (isTargetDark) LightBackground else AmoledBlack
    val glowColor = if (isTargetDark) Color.Black else Color.White

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
                        drawRect(color = overlayColor)
                        return@onDrawWithContent
                    }
                    val radius = maxRadius * progress

                    if (glow > 0f && progress < 1f) {
                        val ringAlpha = (1f - progress).coerceIn(0f, 1f) * 0.30f
                        drawCircle(
                            color = glowColor.copy(alpha = ringAlpha),
                            radius = radius + strokePx / 2f,
                            center = origin,
                            style = Stroke(width = strokePx)
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

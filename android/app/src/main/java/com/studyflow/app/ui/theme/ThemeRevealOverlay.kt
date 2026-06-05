package com.studyflow.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import kotlin.math.hypot

private const val RevealDurationMs = 650
private const val HoldDurationMs = 160L
private const val InactiveProgress = 0f

/**
 * Cute corner-to-full theme transition.
 *
 * The new theme renders behind this overlay. We draw a solid rectangle in the
 * OLD color over the entire screen, but punch an expanding circular hole in it
 * (via PathFillType.EvenOdd) so the new theme "wipes" out from the user's tap.
 *
 * `drawWithCache` re-uses the [Path] across frames instead of allocating a
 * new one every recomposition, which matters at 90 Hz.
 */
@Composable
fun ThemeRevealOverlay(
    active: Boolean,
    origin: Offset,
    isTargetDark: Boolean,
    onFinished: () -> Unit
) {
    val anim = remember { Animatable(InactiveProgress) }

    LaunchedEffect(active) {
        if (active) {
            anim.snapTo(InactiveProgress)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = RevealDurationMs,
                    easing = FastOutSlowInEasing
                )
            )
            kotlinx.coroutines.delay(HoldDurationMs)
            onFinished()
        } else {
            anim.snapTo(InactiveProgress)
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
                val maxRadius = hypot(w, h) * 0.6f
                onDrawWithContent {
                    val radius = maxRadius * anim.value
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

package com.studyflow.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.hypot

private const val RevealDurationMs = 700
private const val HoldDurationMs = 180

/**
 * Cute corner-to-full theme transition.
 *
 * The new theme renders behind this overlay. We draw a solid rectangle in the
 * OLD color over the entire screen, but punch an expanding circular hole in it
 * (via PathFillType.EvenOdd) so the new theme "wipes" out from the user's tap.
 */
@Composable
fun ThemeRevealOverlay(
    active: Boolean,
    origin: Offset,
    isTargetDark: Boolean,
    onFinished: () -> Unit
) {
    val anim = remember { Animatable(0f) }

    LaunchedEffect(active) {
        if (active) {
            anim.snapTo(0f)
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
            anim.snapTo(0f)
        }
    }

    if (!active) return

    // The overlay must match the OLD theme so the new theme is the only thing
    // visible through the expanding hole.
    val overlayColor = if (isTargetDark) LightBackground else AmoledBlack

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawReveal(anim.value, origin, overlayColor)
    }
}

private fun DrawScope.drawReveal(progress: Float, origin: Offset, overlay: Color) {
    val w = size.width
    val h = size.height
    val maxRadius = hypot(w, h) * 0.6f
    val radius = maxRadius * progress

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
    drawPath(path = path, color = overlay)
}

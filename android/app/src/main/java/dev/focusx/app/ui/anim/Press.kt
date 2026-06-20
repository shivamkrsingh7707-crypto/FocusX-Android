package dev.focusx.app.ui.anim

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

/**
 * Press-scale modifier backed by a single `animateFloatAsState` keyed on
 * the press interaction. 110 ms in, 180 ms out — snappy enough that it
 * doesn't lag the user's finger, but with a soft land that doesn't feel
 * like the screen is snatching the touch.
 */
fun Modifier.pressScale(
    scaleTo: Float = 0.97f,
    interactionSource: InteractionSource
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleTo else 1f,
        animationSpec = tween(
            durationMillis = if (pressed) 110 else 180,
            easing = FastOutSlowInEasing
        ),
        label = "press-scale"
    )
    scale(scale)
}

/**
 * Slow ambient pulse for the "live" status dot. The asymmetric keyframe
 * (fast rise, long fall) reads as a heartbeat, not a sine wave.
 */
@Composable
fun rememberPulse(
    minScale: Float = 1f,
    maxScale: Float = 1.55f,
    periodMs: Int = 1200
): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(periodMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse-phase"
    )
    return when {
        phase < 0.4f -> minScale + (maxScale - minScale) * (phase / 0.4f)
        else -> maxScale - (maxScale - minScale) * ((phase - 0.4f) / 0.6f)
    }
}

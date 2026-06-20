package dev.focusx.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Live status dot — a soft halo that breathes outward. The draw is a
 * single concentric `drawCircle` so it's free at 120 Hz.
 */
@Composable
fun PulsingDot(
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
    color: Color
) {
    val transition = rememberInfiniteTransition(label = "dot")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val r = (this.size.minDimension / 2f) * (1f + t * 0.9f)
            val alpha = (1f - t) * 0.55f
            drawCircle(color = color.copy(alpha = alpha), radius = r, center = center)
            drawCircle(color = color, radius = this.size.minDimension / 2.6f, center = center)
        }
    }
}

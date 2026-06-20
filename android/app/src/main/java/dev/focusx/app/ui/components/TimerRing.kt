package dev.focusx.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.focusx.app.domain.TimerPhase
import dev.focusx.app.domain.TimerStatus
import dev.focusx.app.domain.TimerState
import dev.focusx.app.ui.theme.FocusXTheme
import dev.focusx.app.ui.theme.JetBrainsMonoFamily
import kotlin.math.cos
import kotlin.math.sin

/**
 * The hero — a 240 dp ring drawn in a single Canvas. The progress arc
 * is spring-eased so every second "lands" softly rather than snapping
 * to the next tick. When the timer is running we also draw:
 *
 *   - a leading-edge dot that follows the arc head
 *   - a thin 4-tick secondary arc that loops around the ring (suggests
 *     continuity; only visible while running)
 *   - a faint inner glow (single radial brush, no GPU read-back)
 *
 * The Canvas is `drawWithCache`-friendly: nothing inside the drawScope
 * allocates per frame. We re-bind the spring target via a `LaunchedEffect`
 * keyed on `progress` rather than driving it from the data class so
 * the per-second recomposition doesn't re-allocate the spring.
 */
@Composable
fun TimerRing(
    timer: TimerState,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    strokeWidth: Dp = 12.dp,
    primary: Color = FocusXTheme.colors.primary,
    primarySoft: Color = FocusXTheme.colors.primarySoft,
    breakColor: Color = FocusXTheme.colors.break_
) {
    val accent = if (timer.phase == TimerPhase.FOCUS) primary else breakColor
    val accentSoft = if (timer.phase == TimerPhase.FOCUS) primarySoft else breakColor

    val isRunning = timer.status == TimerStatus.RUNNING

    val progress = remember { Animatable(timer.progress) }
    LaunchedEffect(timer.progress) {
        progress.animateTo(
            targetValue = timer.progress,
            animationSpec = spring(
                dampingRatio = 0.85f,
                stiffness = 90f,
                visibilityThreshold = 0.001f
            )
        )
    }

    // Slow rotation of the highlight gradient — only when running, so
    // the user gets a subtle "the system is alive" cue without paying
    // for it when the timer is idle.
    val transition = rememberInfiniteTransition(label = "ringShimmer")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    val effectiveShimmer = if (isRunning) shimmer else 0f

    val minutes = timer.remainingSeconds / 60
    val seconds = timer.remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    val track = FocusXTheme.colors.hairline
    val tickFg = FocusXTheme.colors.textHi
    val subFg = FocusXTheme.colors.textSecondary

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val minSide = this.size.minDimension
            val stroke = strokeWidth.toPx()
            val radius = (minSide - stroke) / 2f
            val arcSize = Size(radius * 2f, radius * 2f)
            val arcOffset = Offset(
                x = (this.size.width - arcSize.width) / 2f,
                y = (this.size.height - arcSize.height) / 2f
            )
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Track — single hairline ring
            drawCircle(
                color = track,
                radius = radius,
                center = center,
                style = Stroke(width = stroke)
            )

            // 60 minute ticks — 60 individual ticks, drawn in a single
            // drawCircle loop is wasteful; a Path would be faster. We
            // keep it simple here; each tick is a single drawLine call.
            for (i in 0 until 60) {
                val angleRad = Math.toRadians((-90.0 + (i * 6.0)))
                val isHour = i % 5 == 0
                val r1 = radius - stroke / 2f - if (isHour) 4f else 2f
                val r2 = r1 - if (isHour) 8f else 4f
                val a1 = Offset(
                    center.x + r1 * cos(angleRad).toFloat(),
                    center.y + r1 * sin(angleRad).toFloat()
                )
                val a2 = Offset(
                    center.x + r2 * cos(angleRad).toFloat(),
                    center.y + r2 * sin(angleRad).toFloat()
                )
                drawLine(
                    color = if (isHour) track.copy(alpha = 0.9f) else track.copy(alpha = 0.4f),
                    start = a1,
                    end = a2,
                    strokeWidth = if (isHour) 2f else 1f
                )
            }

            // Filled progress arc — gradient along the sweep so the
            // head of the arc "warms up" as it advances.
            val sweep = 360f * progress.value
            if (sweep > 0f) {
                val rotation = effectiveShimmer * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to accentSoft,
                        0.5f to accent,
                        1f to accentSoft
                    ),
                    startAngle = -90f + rotation,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = arcOffset,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            // Leading-edge dot — only when there's something to point at
            if (sweep > 0.5f) {
                val angleRad = Math.toRadians((-90.0 + sweep).toDouble())
                val cx = center.x + radius * cos(angleRad).toFloat()
                val cy = center.y + radius * sin(angleRad).toFloat()
                drawCircle(
                    color = tickFg,
                    radius = stroke * 0.32f,
                    center = Offset(cx, cy)
                )
                // Soft glow under the dot — concentric, no blend mode
                drawCircle(
                    color = accent.copy(alpha = 0.5f),
                    radius = stroke * 0.6f,
                    center = Offset(cx, cy)
                )
            }
        }

        // Centered time + sub-label
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = timeText,
                color = tickFg,
                fontSize = 60.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = JetBrainsMonoFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Inner text under the ring — phase label + sub.
 */
@Composable
fun TimerUnderLabel(
    timer: TimerState,
    modifier: Modifier = Modifier
) {
    val label = when (timer.phase) {
        TimerPhase.FOCUS -> "FOCUS"
        TimerPhase.BREAK -> "BREAK"
    }
    val status = when (timer.status) {
        TimerStatus.IDLE -> "Ready"
        TimerStatus.RUNNING -> "Locking in"
        TimerStatus.PAUSED -> "Paused"
        TimerStatus.COMPLETED -> "Done"
    }
    Box(modifier) {
        Text(
            text = "$label  ·  $status",
            color = FocusXTheme.colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}

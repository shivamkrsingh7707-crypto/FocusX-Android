package com.studyflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.StudyFlowTheme

@Composable
fun TimerRing(
    progress: Float,
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    size: Dp = 256.dp,
    strokeWidth: Dp = 10.dp,
    isActive: Boolean = false
) {
    // Spring the progress for a smoother countdown feel — when the underlying
    // value drops each second we ease between frames rather than snap.
    val progressAnim = remember { Animatable(progress) }
    LaunchedEffect(progress) {
        progressAnim.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
    }

    val trackColor by animateColorAsState(
        targetValue = Color.White.copy(alpha = 0.08f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "track"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    // Subtle rotating shimmer for the gradient stops while the timer runs.
    // Pure visual candy — it sits inside a sweepGradient so it scales with
    // the ring's colour rather than introducing an extra draw.
    val shimmerAnim = remember { Animatable(0f) }
    LaunchedEffect(isActive) {
        if (isActive) {
            shimmerAnim.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable<Float>(
                    animation = tween(2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            shimmerAnim.snapTo(0f)
        }
    }
    val shimmer = shimmerAnim.value

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val stroke = strokeWidth.toPx()
            val radius = (canvasSize - stroke) / 2
            val arcSize = Size(radius * 2, radius * 2)
            val arcOffset = Offset(
                (this.size.width - arcSize.width) / 2f,
                (this.size.height - arcSize.height) / 2f
            )

            // Track
            drawCircle(
                color = trackColor,
                radius = radius,
                center = Offset(this.size.width / 2f, this.size.height / 2f),
                style = Stroke(width = stroke)
            )

            // Soft glow halo behind the active arc, just on the leading edge.
            // It's a slightly thicker, very low-alpha stroke that gives the
            // ring a "lit" feel without burning extra draws.
            if (isActive) {
                val glowStroke = stroke * 1.8f
                drawArc(
                    color = PrimaryBlue.copy(alpha = 0.18f),
                    startAngle = -90f,
                    sweepAngle = 360f * progressAnim.value,
                    useCenter = false,
                    topLeft = Offset(
                        arcOffset.x - (glowStroke - stroke) / 2f,
                        arcOffset.y - (glowStroke - stroke) / 2f
                    ),
                    size = Size(arcSize.width + (glowStroke - stroke), arcSize.height + (glowStroke - stroke)),
                    style = Stroke(width = glowStroke, cap = StrokeCap.Round)
                )
            }

            // Main progress arc with rotating gradient stops. The third stop
            // sweeps around the ring while the timer is active so the colour
            // doesn't feel static.
            val sweep = 360f * progressAnim.value
            val activeColors = if (isActive) {
                val stop = shimmer
                listOf(
                    PrimaryBlue,
                    PrimaryBlue.copy(alpha = 0.85f),
                    PrimaryBlue.copy(alpha = 0.55f + 0.45f * (1f - stop)),
                    PrimaryBlue
                )
            } else {
                listOf(
                    PrimaryBlue,
                    PrimaryBlue.copy(alpha = 0.7f),
                    PrimaryBlue
                )
            }
            drawArc(
                brush = Brush.sweepGradient(activeColors),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Tiny leading-edge "head" dot. Pure decoration but very iOS-like.
            if (isActive && progressAnim.value > 0f) {
                val angleRad = Math.toRadians((-90f + sweep).toDouble())
                val cx = (this.size.width / 2f) + radius * kotlin.math.cos(angleRad).toFloat()
                val cy = (this.size.height / 2f) + radius * kotlin.math.sin(angleRad).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = stroke * 0.55f,
                    center = Offset(cx, cy)
                )
            }
        }

        Text(
            text = timeText,
            color = StudyFlowTheme.colors.onSurface,
            fontSize = 48.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-1).sp
        )
    }
}

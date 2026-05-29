package com.studyzen.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyzen.app.theme.StreakOrange
import com.studyzen.app.theme.StreakYellow
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary

@Composable
fun StreakFlame(
    streak: Int,
    modifier: Modifier = Modifier,
    flameSize: Dp = 48.dp
) {
    val transition = rememberInfiniteTransition(label = "flame")
    val flamePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flamePhase"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(flameSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(flameSize)) {
                drawFlame(flamePhase)
            }
        }

        Text(
            text = "$streak",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp
        )

        Text(
            text = "day streak",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

private fun DrawScope.drawFlame(phase: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2
    val cy = h * 0.6f

    val sway = (phase - 0.5f) * w * 0.08f

    val flamePath = Path().apply {
        moveTo(cx + sway, h * 0.15f)
        cubicTo(
            cx + w * 0.3f + sway, h * 0.25f,
            cx + w * 0.25f + sway, h * 0.5f,
            cx + w * 0.15f + sway, h * 0.7f
        )
        cubicTo(
            cx + w * 0.05f + sway, h * 0.85f,
            cx - w * 0.05f + sway, h * 0.85f,
            cx - w * 0.15f + sway, h * 0.7f
        )
        cubicTo(
            cx - w * 0.25f + sway, h * 0.5f,
            cx - w * 0.3f + sway, h * 0.25f,
            cx + sway, h * 0.15f
        )
        close()
    }

    drawPath(
        path = flamePath,
        brush = Brush.verticalGradient(
            colors = listOf(
                StreakYellow,
                StreakOrange
            ),
            startY = 0f,
            endY = h
        )
    )

    val innerPath = Path().apply {
        val innerSway = (phase - 0.5f) * w * 0.04f
        moveTo(cx + innerSway, h * 0.2f)
        cubicTo(
            cx + w * 0.18f + innerSway, h * 0.35f,
            cx + w * 0.12f + innerSway, h * 0.55f,
            cx + w * 0.05f + innerSway, h * 0.68f
        )
        cubicTo(
            cx - w * 0.05f + innerSway, h * 0.7f,
            cx - w * 0.12f + innerSway, h * 0.55f,
            cx + innerSway, h * 0.2f
        )
        close()
    }

    drawPath(
        path = innerPath,
        color = Color.White.copy(alpha = 0.5f)
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                StreakYellow.copy(alpha = 0.3f),
                Color.Transparent
            )
        ),
        radius = w * 0.4f,
        center = Offset(cx, cy)
    )
}

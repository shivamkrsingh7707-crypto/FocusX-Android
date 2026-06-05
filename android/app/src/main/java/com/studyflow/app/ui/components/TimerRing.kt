package com.studyflow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.theme.AmoledBlack
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.TextPrimary

@Composable
fun TimerRing(
    progress: Float,
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    strokeWidth: Dp = 8.dp,
    isActive: Boolean = false
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "timerProgress"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val stroke = strokeWidth.toPx()
            val radius = (canvasSize - stroke) / 2

            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = radius,
                center = Offset(size.width.toPx() / 2, size.height.toPx() / 2),
                style = Stroke(width = stroke)
            )

            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        PrimaryBlue,
                        PrimaryBlue.copy(alpha = 0.6f),
                        PrimaryBlue
                    )
                ),
                radius = radius,
                center = Offset(size.width.toPx() / 2, size.height.toPx() / 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress
            )
        }

        Text(
            text = timeText,
            color = TextPrimary,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp
        )
    }
}

package com.studyzen.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyzen.app.theme.PrimaryPurple
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary

@Composable
fun AnimatedTimerRing(
    progress: Float,
    timeText: String,
    modeText: String,
    modifier: Modifier = Modifier,
    ringSize: Dp = 220.dp,
    strokeWidth: Dp = 4.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "timerProgress"
    )

    Box(
        modifier = modifier
            .size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val stroke = strokeWidth.toPx()
            val arcSize = canvasSize - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)

            drawRoundRect(
                color = Color.White.copy(alpha = 0.05f),
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                cornerRadius = CornerRadius(arcSize / 2),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            drawArc(
                color = PrimaryPurple,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = Size(arcSize, arcSize),
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = timeText,
                color = TextPrimary,
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = modeText,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PremiumDivider(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth().height(1.dp).padding(horizontal = 4.dp)) {
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset.Zero,
            end = Offset(size.width, 0f),
            strokeWidth = 1f
        )
    }
}

package com.studyzen.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyzen.app.theme.GlowPurple
import com.studyzen.app.theme.Primary
import com.studyzen.app.theme.Secondary
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary

@Composable
fun WeeklyBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)
    val barMaxHeight = 120f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (label, value) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatMinutes(value),
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .height(
                                (value.toFloat() / maxValue * barMaxHeight).dp.coerceAtLeast(4.dp)
                            )
                    ) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(
                            (value.toFloat() / maxValue * barMaxHeight).dp.coerceAtLeast(4.dp)
                        )) {
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Primary, Secondary)
                                ),
                                cornerRadius = CornerRadius(6.dp.toPx()),
                                size = size
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = label,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MonthlyLineChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    val maxValue = data.maxOf { it.second }.coerceAtLeast(1)
    val padding = 20f

    Canvas(modifier = modifier.fillMaxWidth().height(150.dp)) {
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)

        val path = Path().apply {
            var first = true
            data.forEachIndexed { index, (_, value) ->
                val x = padding + index * stepX
                val y = padding + chartHeight - (value.toFloat() / maxValue * chartHeight)
                if (first) {
                    moveTo(x, y)
                    first = false
                } else {
                    lineTo(x, y)
                }
            }
        }

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(Primary, Secondary)
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        val fillPath = Path().apply {
            addPath(path)
            val lastX = padding + (data.size - 1) * stepX
            val firstX = padding
            lineTo(lastX, padding + chartHeight)
            lineTo(firstX, padding + chartHeight)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Primary.copy(alpha = 0.2f),
                    Color.Transparent
                )
            )
        )

        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = padding + chartHeight - (value.second.toFloat() / maxValue * chartHeight)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Primary, Secondary)
                ),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    return if (minutes >= 60) {
        "${minutes / 60}h"
    } else {
        "${minutes}m"
    }
}

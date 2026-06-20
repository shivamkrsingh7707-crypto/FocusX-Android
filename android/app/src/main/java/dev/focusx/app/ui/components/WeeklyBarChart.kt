package dev.focusx.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.focusx.app.ui.theme.FocusXTheme
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class DayPoint(
    val date: LocalDate,
    val minutes: Int,
    val isToday: Boolean
)

@Composable
fun WeeklyBarChart(
    data: List<DayPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    goal: Int? = null
) {
    val theme = FocusXTheme.colors
    val max = (data.maxOfOrNull { it.minutes } ?: 0).coerceAtLeast(60)
    val ratio = max.coerceAtLeast(1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height + 32.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            val animated by animateFloatAsState(
                targetValue = day.minutes.toFloat() / ratio,
                animationSpec = tween(600, easing = FastOutSlowInEasing),
                label = "bar"
            )
            val isToday = day.isToday
            val barColor = if (day.minutes == 0) {
                theme.hairline
            } else if (isToday) {
                theme.primary
            } else {
                theme.primarySoft.copy(alpha = 0.55f)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = if (day.minutes > 0) "${day.minutes}" else "",
                    color = if (isToday) theme.onSurface else theme.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .clip(RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
                        val w = size.width
                        val h = size.height
                        val barH = (h * animated).coerceAtLeast(2f)
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(barColor, barColor.copy(alpha = 0.6f))
                            ),
                            topLeft = Offset(0f, h - barH),
                            size = Size(w, barH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                    color = if (isToday) theme.primary else theme.textTertiary,
                    fontSize = 11.sp,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    if (goal != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Goal $goal min/day",
            color = theme.textTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

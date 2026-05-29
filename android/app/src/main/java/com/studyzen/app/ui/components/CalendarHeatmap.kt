package com.studyzen.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyzen.app.data.database.StreakEntity
import com.studyzen.app.theme.Card
import com.studyzen.app.theme.GlassBorder
import com.studyzen.app.theme.Primary
import com.studyzen.app.theme.Success
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CalendarHeatmap(
    streakData: List<StreakEntity>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val daysInMonth = today.lengthOfMonth()
    val firstDayOfMonth = today.withDayOfMonth(1).dayOfWeek.value % 7

    val days = mutableListOf<Pair<String, Int?>>()

    for (i in 0 until firstDayOfMonth) {
        days.add(Pair("", null))
    }

    for (day in 1..daysInMonth) {
        val date = today.withDayOfMonth(day).format(formatter)
        val data = streakData.find { it.date == date }
        days.add(Pair(date, data?.totalMinutes ?: 0))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = today.month.name + " " + today.year,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        val numRows = (days.size + 6) / 7
        for (row in 0 until numRows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    if (index < days.size) {
                        val (date, minutes) = days[index]
                        if (date.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            HeatmapCell(minutes = minutes ?: 0)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                color = TextSecondary,
                fontSize = 10.sp
            )
            val levels = listOf(0, 1, 25, 50, 75)
            levels.forEach { level ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(heatmapColor(level))
                )
            }
            Text(
                text = "More",
                color = TextSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun HeatmapCell(minutes: Int) {
    val color = heatmapColor(minutes)

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(1.5.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

private fun heatmapColor(minutes: Int): Color {
    return when {
        minutes == 0 -> Card
        minutes < 15 -> Primary.copy(alpha = 0.2f)
        minutes < 25 -> Primary.copy(alpha = 0.4f)
        minutes < 50 -> Primary.copy(alpha = 0.6f)
        else -> Success.copy(alpha = 0.7f)
    }
}

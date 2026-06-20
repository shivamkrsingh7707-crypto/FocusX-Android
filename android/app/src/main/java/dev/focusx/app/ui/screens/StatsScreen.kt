package dev.focusx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.focusx.app.data.AppViewModel
import dev.focusx.app.domain.Session
import dev.focusx.app.domain.TimerPhase
import dev.focusx.app.ui.components.DayPoint
import dev.focusx.app.ui.components.Pill
import dev.focusx.app.ui.components.ScreenHeader
import dev.focusx.app.ui.components.SectionLabel
import dev.focusx.app.ui.components.SurfaceCard
import dev.focusx.app.ui.components.WeeklyBarChart
import dev.focusx.app.ui.theme.FocusXTheme
import dev.focusx.app.ui.theme.SubjectPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun StatsScreen(
    ui: AppViewModel.UiState,
    modifier: Modifier = Modifier
) {
    val theme = FocusXTheme.colors
    val snap = ui.snapshot
    val totalMin = snap.totalMinutes
    val totalSess = snap.totalSessions
    val avg = if (snap.sessions.isNotEmpty()) totalMin / snap.sessions.size else 0
    val best = snap.sessions.maxOfOrNull { it.minutes } ?: 0

    val week = (6 downTo 0).map { offset ->
        val date = LocalDate.now().minusDays(offset.toLong())
        val minutes = snap.sessions.filter { it.date == date }.sumOf { it.minutes }
        DayPoint(date = date, minutes = minutes, isToday = date == LocalDate.now())
    }
    val weekTotal = week.sumOf { it.minutes }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Statistics",
                    subtitle = "Your focus at a glance"
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigStat("Total", formatMinutes(totalMin), theme.primary, Modifier.weight(1f))
                    BigStat("Streak", "${snap.streak}d", theme.warn, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigStat("Sessions", "$totalSess", theme.success, Modifier.weight(1f))
                    BigStat("Average", "${avg}m", theme.primarySoft, Modifier.weight(1f))
                }
            }
            item {
                SectionLabel(text = "This week")
            }
            item {
                SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$weekTotal min",
                                color = theme.onSurface,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Total focus this week",
                                color = theme.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Pill(
                            label = "${snap.sessions.count { it.phase == TimerPhase.FOCUS && it.date >= LocalDate.now().minusDays(7) }} sessions",
                            color = theme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    WeeklyBarChart(
                        data = week,
                        goal = snap.settings.dailyGoalMinutes,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            item {
                SectionLabel(text = "Sessions")
            }
            if (snap.sessions.isEmpty()) {
                item {
                    SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Complete a focus session to see it here.",
                            color = theme.textTertiary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(items = snap.sessions, key = { it.id }) { session ->
                    SessionRow(session)
                }
                item { Spacer(modifier = Modifier.height(110.dp)) }
            }
        }
    }
}

@Composable
private fun BigStat(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val theme = FocusXTheme.colors
    SurfaceCard(modifier = modifier, contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0.16f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label.uppercase(),
                    color = theme.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = value,
                    color = theme.onSurface,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SessionRow(session: Session) {
    val theme = FocusXTheme.colors
    val subjectColor = session.subjectId
        ?.let { id -> theme.primary }
        ?: theme.textTertiary
    SurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = 14.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(subjectColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(subjectColor)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${session.minutes} min · ${session.phase.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    color = theme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = session.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                    color = theme.textTertiary,
                    fontSize = 12.sp
                )
            }
            Pill(
                label = "Done",
                color = theme.success
            )
        }
    }
}

private fun formatMinutes(min: Int): String {
    val h = min / 60
    val m = min % 60
    return when {
        h <= 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

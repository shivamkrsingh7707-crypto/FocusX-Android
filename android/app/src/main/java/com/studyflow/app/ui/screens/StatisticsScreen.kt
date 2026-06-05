package com.studyflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.components.StatCard
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.StudyFlowTheme
import com.studyflow.app.ui.theme.SuccessGreen
import com.studyflow.app.ui.theme.WarningAmber
import com.studyflow.app.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel
) {
    val state by statisticsViewModel.state.collectAsStateWithLifecycle()
    val theme = StudyFlowTheme.colors

    LaunchedEffect(Unit) {
        statisticsViewModel.refresh()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Statistics",
            color = theme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Filled.AccessTime,
                iconTint = PrimaryBlue,
                label = "Total Time",
                value = formatMinutes(state.totalMinutes),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.Timer,
                iconTint = WarningAmber,
                label = "Sessions",
                value = "${state.totalSessions}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.ShowChart,
                iconTint = SuccessGreen,
                label = "Daily Avg",
                value = "${state.averageDailyMinutes.toInt()}m",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "THIS WEEK",
            color = theme.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "${state.weeklyTotal} min",
                    color = theme.onSurface,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total this week",
                    color = theme.textSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    state.weeklyData.forEach { day ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(
                                        when {
                                            day.minutes >= 60 -> 36.dp
                                            day.minutes >= 30 -> 28.dp
                                            day.minutes > 0 -> 20.dp
                                            else -> 8.dp
                                        }
                                    )
                                    .background(
                                        if (day.minutes > 0) PrimaryBlue.copy(alpha = 0.6f)
                                        else theme.border,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day.dayLabel,
                                color = if (day.isToday) PrimaryBlue else theme.textMuted,
                                fontSize = 11.sp,
                                fontWeight = if (day.isToday) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ACHIEVEMENTS",
            color = theme.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                icon = Icons.Filled.EmojiEvents,
                iconTint = WarningAmber,
                label = "Best Streak",
                value = "${state.currentStreak} days",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.BarChart,
                iconTint = PrimaryBlue,
                label = "Streak",
                value = "${state.currentStreak} days",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

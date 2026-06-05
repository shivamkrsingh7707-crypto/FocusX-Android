package com.studyflow.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.components.StatCard
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.StudyFlowTheme
import com.studyflow.app.ui.theme.SuccessGreen
import com.studyflow.app.ui.theme.WarningAmber
import com.studyflow.app.viewmodel.StatisticsViewModel
import com.studyflow.app.viewmodel.SubjectViewModel
import com.studyflow.app.viewmodel.TimerViewModel

@Composable
fun DashboardScreen(
    timerViewModel: TimerViewModel,
    subjectViewModel: SubjectViewModel,
    statisticsViewModel: StatisticsViewModel,
    onStartTimer: () -> Unit
) {
    val timerState by timerViewModel.state.collectAsState()
    val subjectState by subjectViewModel.state.collectAsState()
    val statsState by statisticsViewModel.state.collectAsState()
    val theme = StudyFlowTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "StudyFlow",
                    color = theme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Stay in the flow",
                    color = theme.textSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "TODAY'S FOCUS",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${timerState.sessionsCompleted}",
                        color = PrimaryBlue,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "sessions",
                        color = theme.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                if (timerState.totalFocusMinutes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${timerState.totalFocusMinutes} total minutes focused",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                StartTimerCard(
                    focusMinutes = timerState.focusMinutes,
                    breakMinutes = timerState.breakMinutes,
                    onClick = onStartTimer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "OVERVIEW",
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
                icon = Icons.Filled.Timer,
                iconTint = PrimaryBlue,
                label = "Total Time",
                value = formatTime(statsState.totalMinutes),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.Bolt,
                iconTint = WarningAmber,
                label = "Streak",
                value = "${statsState.currentStreak} days",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.ShowChart,
                iconTint = SuccessGreen,
                label = "Avg/Day",
                value = "${statsState.averageDailyMinutes.toInt()}m",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (subjectState.subjects.isNotEmpty()) {
            Text(
                text = "YOUR SUBJECTS",
                color = theme.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            subjectState.subjects.take(3).forEach { subject ->
                com.studyflow.app.ui.components.SubjectCard(
                    subject = subject,
                    onClick = { },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun StartTimerCard(
    focusMinutes: Int,
    breakMinutes: Int,
    onClick: () -> Unit
) {
    val theme = StudyFlowTheme.colors
    val pressedOffsetY by animateDpAsState(
        targetValue = 0.dp,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.6f),
        label = "press"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PrimaryBlue.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Start focus session",
                    color = theme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$focusMinutes min focus / $breakMinutes min break",
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Start",
                color = PrimaryBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlue.copy(alpha = 0.25f))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

private fun formatTime(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

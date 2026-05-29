package com.studyzen.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyzen.app.theme.Background
import com.studyzen.app.theme.Card
import com.studyzen.app.theme.GlassBorder
import com.studyzen.app.theme.GlowPurple
import com.studyzen.app.theme.Primary
import com.studyzen.app.theme.Secondary
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary
import com.studyzen.app.theme.TextTertiary
import com.studyzen.app.ui.components.GlassCard
import com.studyzen.app.ui.components.GlassCardMinimal
import com.studyzen.app.ui.components.MonthlyLineChart
import com.studyzen.app.ui.components.PremiumDivider
import com.studyzen.app.ui.components.WeeklyBarChart
import com.studyzen.app.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel
) {
    val state by statisticsViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        statisticsViewModel.loadStatistics()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Statistics",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Your productivity insights",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCardMinimal(modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Focus",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = formatTotalHours(state.totalMinutes),
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                GlassCardMinimal(modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sessions",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${state.totalSessions}",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                GlassCardMinimal(modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Filled.ShowChart,
                        contentDescription = null,
                        tint = GlowPurple,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Daily Avg",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${state.averageDailyMinutes.toInt()}m",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Weekly Chart
            GlassCard {
                Text(
                    text = "WEEKLY OVERVIEW",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                WeeklyBarChart(
                    data = state.weeklyStats
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Chart
            GlassCard {
                Text(
                    text = "MONTHLY TREND",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (state.monthlyStats.isNotEmpty()) {
                    MonthlyLineChart(
                        data = state.monthlyStats
                    )
                } else {
                    Text(
                        text = "No data yet. Start focusing!",
                        color = TextTertiary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 40.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Productivity Score
            GlassCardMinimal {
                Text(
                    text = "PRODUCTIVITY SCORE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .drawBehind {
                                drawRoundRect(
                                    color = Card,
                                    cornerRadius = CornerRadius(12.dp.toPx()),
                                    size = size
                                )
                                val score = calculateProductivityScore(
                                    state.totalMinutes,
                                    state.totalSessions,
                                    state.averageDailyMinutes
                                )
                                drawRoundRect(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Primary, Secondary)
                                    ),
                                    cornerRadius = CornerRadius(12.dp.toPx()),
                                    size = Size(
                                        size.width * (score / 100f),
                                        size.height
                                    )
                                )
                            }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${calculateProductivityScore(state.totalMinutes, state.totalSessions, state.averageDailyMinutes)}%",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

private fun formatTotalHours(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

private fun calculateProductivityScore(
    totalMinutes: Int,
    totalSessions: Int,
    avgDailyMinutes: Double
): Int {
    if (totalMinutes == 0) return 0
    val sessionScore = minOf(totalSessions * 5, 30)
    val timeScore = minOf((totalMinutes / 60) * 2, 40)
    val avgScore = minOf(avgDailyMinutes.toInt(), 30)
    return minOf(sessionScore + timeScore + avgScore, 100)
}

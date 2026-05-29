package com.studyzen.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.studyzen.app.theme.StreakOrange
import com.studyzen.app.theme.StreakYellow
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary
import com.studyzen.app.theme.TextTertiary
import com.studyzen.app.ui.components.CalendarHeatmap
import com.studyzen.app.ui.components.GlassCard
import com.studyzen.app.ui.components.GlassCardMinimal
import com.studyzen.app.viewmodel.StreakViewModel

@Composable
fun StreakScreen(
    streakViewModel: StreakViewModel
) {
    val state by streakViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        streakViewModel.refresh()
    }

    val transition = rememberInfiniteTransition(label = "streakGlow")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streakGlowAlpha"
    )

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
                text = "Your Streak",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Consistency is key",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main streak display
            GlassCard(
                glowColor = StreakOrange
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Animated flame icon
                        Box(
                            modifier = Modifier
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                StreakOrange.copy(alpha = glowAlpha * 0.4f),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = size.minDimension * 0.6f
                                    )
                                }
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = StreakOrange,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${state.currentStreak}",
                            color = TextPrimary,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Day Streak",
                            color = StreakOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 3.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Best & Today stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCardMinimal(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = StreakYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Best Streak",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${state.bestStreak} days",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                GlassCardMinimal(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Today",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${state.todayMinutes}m",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Achievements
            GlassCard {
                Text(
                    text = "ACHIEVEMENTS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AchievementBadge(
                        achieved = state.currentStreak >= 3,
                        icon = Icons.Filled.AutoAwesome,
                        label = "3 Day\nStreak"
                    )
                    AchievementBadge(
                        achieved = state.currentStreak >= 7,
                        icon = Icons.Filled.AutoAwesome,
                        label = "7 Day\nStreak"
                    )
                    AchievementBadge(
                        achieved = state.currentStreak >= 15,
                        icon = Icons.Filled.AutoAwesome,
                        label = "15 Day\nStreak"
                    )
                    AchievementBadge(
                        achieved = state.currentStreak >= 30,
                        icon = Icons.Filled.AutoAwesome,
                        label = "30 Day\nStreak"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Calendar Heatmap
            GlassCard {
                Text(
                    text = "CALENDAR",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                CalendarHeatmap(
                    streakData = state.streakData
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun AchievementBadge(
    achieved: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .drawBehind {
                    drawRoundRect(
                        brush = if (achieved) {
                            Brush.linearGradient(
                                colors = listOf(
                                    StreakOrange.copy(alpha = 0.3f),
                                    StreakYellow.copy(alpha = 0.2f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.05f),
                                    Color.White.copy(alpha = 0.02f)
                                )
                            )
                        },
                        cornerRadius = CornerRadius(14.dp.toPx())
                    )
                    if (achieved) {
                        drawRoundRect(
                            color = StreakOrange.copy(alpha = 0.5f),
                            style = Stroke(width = 1.dp.toPx()),
                            cornerRadius = CornerRadius(14.dp.toPx())
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (achieved) StreakYellow else TextTertiary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = if (achieved) TextPrimary else TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

package com.studyzen.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
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
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary
import com.studyzen.app.theme.TextTertiary
import com.studyzen.app.ui.components.AnimatedTimerRing
import com.studyzen.app.ui.components.GlassCard
import com.studyzen.app.ui.components.GlassCardMinimal
import com.studyzen.app.ui.components.PremiumButton
import com.studyzen.app.ui.components.StreakFlame
import com.studyzen.app.viewmodel.PomodoroState
import com.studyzen.app.viewmodel.PomodoroViewModel
import com.studyzen.app.viewmodel.StreakViewModel
import com.studyzen.app.viewmodel.TimerState

@Composable
fun HomeScreen(
    pomodoroViewModel: PomodoroViewModel,
    streakViewModel: StreakViewModel
) {
    val pomodoroState by pomodoroViewModel.state.collectAsState()
    val streakState by streakViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        streakViewModel.refresh()
    }

    val quote = "\"The secret of getting ahead is getting started.\""
    val quoteAuthor = "— Mark Twain"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Greeting
            Text(
                text = "Welcome back,",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "Focus Master",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Streak + Timer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StreakFlame(
                    streak = streakState.currentStreak,
                    flameSize = 40.dp
                )
                GlassCardMinimal(
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = "${streakState.totalSessions}",
                            label = "Sessions"
                        )
                        StatItem(
                            value = "${streakState.totalMinutes / 60}h",
                            label = "Total"
                        )
                        StatItem(
                            value = "${streakState.bestStreak}",
                            label = "Best"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Timer Ring
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedTimerRing(
                        progress = pomodoroState.progress,
                        timeText = formatTime(pomodoroState.remainingSeconds),
                        modeText = if (pomodoroState.timerMode.name == "FOCUS") "FOCUS" else "BREAK",
                        ringSize = 240.dp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Controls
                    when (pomodoroState.timerState) {
                        TimerState.IDLE -> {
                            PremiumButton(
                                text = "Start Focus",
                                onClick = {
                                    pomodoroViewModel.startTimer()
                                },
                                icon = {
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 40.dp)
                            )
                        }
                        TimerState.RUNNING -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PremiumButton(
                                    text = "Pause",
                                    onClick = { pomodoroViewModel.pauseTimer() },
                                    gradient = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF37474F),
                                            Color(0xFF263238)
                                        )
                                    ),
                                    modifier = Modifier.weight(1f),
                                    height = 48.dp
                                )
                                PremiumButton(
                                    text = "Reset",
                                    onClick = { pomodoroViewModel.resetTimer() },
                                    gradient = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF4A0000),
                                            Color(0xFF6D0000)
                                        )
                                    ),
                                    modifier = Modifier.weight(1f),
                                    height = 48.dp
                                )
                            }
                        }
                        TimerState.PAUSED -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                PremiumButton(
                                    text = "Resume",
                                    onClick = { pomodoroViewModel.resumeTimer() },
                                    icon = {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    height = 48.dp
                                )
                                PremiumButton(
                                    text = "Reset",
                                    onClick = { pomodoroViewModel.resetTimer() },
                                    gradient = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF4A0000),
                                            Color(0xFF6D0000)
                                        )
                                    ),
                                    modifier = Modifier.weight(1f),
                                    height = 48.dp
                                )
                            }
                        }
                        TimerState.COMPLETED -> {
                            PremiumButton(
                                text = "Great Focus! Continue",
                                onClick = { pomodoroViewModel.startTimer() },
                                icon = {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 40.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Quote
            GlassCard {
                Text(
                    text = "Daily Inspiration",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = quote,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = quoteAuthor,
                    color = TextTertiary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timer adjustment
            GlassCardMinimal {
                Text(
                    text = "Timer Settings",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimerPresetButton(
                        text = "25 min",
                        isSelected = pomodoroState.focusMinutes == 25,
                        onClick = { pomodoroViewModel.setFocusMinutes(25) }
                    )
                    TimerPresetButton(
                        text = "30 min",
                        isSelected = pomodoroState.focusMinutes == 30,
                        onClick = { pomodoroViewModel.setFocusMinutes(30) }
                    )
                    TimerPresetButton(
                        text = "45 min",
                        isSelected = pomodoroState.focusMinutes == 45,
                        onClick = { pomodoroViewModel.setFocusMinutes(45) }
                    )
                    TimerPresetButton(
                        text = "60 min",
                        isSelected = pomodoroState.focusMinutes == 60,
                        onClick = { pomodoroViewModel.setFocusMinutes(60) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun TimerPresetButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Primary.copy(alpha = 0.2f)
                else Card
            )
            .drawBehind {
                if (isSelected) {
                    drawRoundRect(
                        color = Primary.copy(alpha = 0.5f),
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Primary else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

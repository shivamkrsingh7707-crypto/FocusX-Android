package com.studyzen.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyzen.app.theme.AmoledBlack
import com.studyzen.app.theme.BorderLow
import com.studyzen.app.theme.CardDark
import com.studyzen.app.theme.PrimaryPurple
import com.studyzen.app.theme.TextMuted
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary
import com.studyzen.app.ui.components.AnimatedTimerRing
import com.studyzen.app.ui.components.MiniChip
import com.studyzen.app.ui.components.PremiumButton
import com.studyzen.app.viewmodel.PomodoroViewModel
import com.studyzen.app.viewmodel.StreakViewModel
import com.studyzen.app.viewmodel.TimerState

@Composable
fun HomeScreen(
    pomodoroViewModel: PomodoroViewModel,
    streakViewModel: StreakViewModel,
    onOpenSettings: () -> Unit
) {
    val pomodoroState by pomodoroViewModel.state.collectAsState()
    val streakState by streakViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        streakViewModel.refresh()
    }

    var selectedSubject by remember { mutableStateOf("Math") }
    var showSubjectPicker by remember { mutableStateOf(false) }
    val subjects = listOf("Math", "Physics", "Chemistry", "Biology", "CS")

    val streakCount = streakState.currentStreak

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FX",
                color = PrimaryPurple,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .drawBehind {
                            drawRoundRect(
                                color = BorderLow,
                                style = Stroke(width = 1.dp.toPx()),
                                cornerRadius = CornerRadius(12.dp.toPx())
                            )
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = PrimaryPurple,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$streakCount",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = { /* theme toggle */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DarkMode,
                        contentDescription = "Theme",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedTimerRing(
                    progress = pomodoroState.progress,
                    timeText = formatTime(pomodoroState.remainingSeconds),
                    modeText = if (pomodoroState.timerMode.name == "FOCUS") "FOCUS" else "BREAK",
                    ringSize = 200.dp,
                    strokeWidth = 3.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box {
                    MiniChip(
                        text = "Studying: $selectedSubject",
                        onClick = { showSubjectPicker = !showSubjectPicker }
                    )

                    AnimatedVisibility(
                        visible = showSubjectPicker,
                        enter = fadeIn() + scaleIn(animationSpec = spring()),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(top = 40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardDark)
                                .drawBehind {
                                    drawRoundRect(
                                        color = BorderLow,
                                        style = Stroke(width = 1.dp.toPx()),
                                        cornerRadius = CornerRadius(12.dp.toPx())
                                    )
                                }
                                .padding(4.dp)
                        ) {
                            subjects.forEach { subject ->
                                Text(
                                    text = subject,
                                    color = if (subject == selectedSubject) PrimaryPurple else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (subject == selectedSubject) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            selectedSubject = subject
                                            showSubjectPicker = false
                                        }
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                when (pomodoroState.timerState) {
                    TimerState.IDLE -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn(animationSpec = spring()),
                            exit = fadeOut() + scaleOut()
                        ) {
                            PremiumButton(
                                text = "Start Focus",
                                onClick = { pomodoroViewModel.startTimer() },
                                icon = {
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 60.dp)
                            )
                        }
                    }
                    TimerState.RUNNING -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn(animationSpec = spring()),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PremiumButton(
                                    text = "Pause",
                                    onClick = { pomodoroViewModel.pauseTimer() },
                                    filled = false,
                                    icon = {
                                        Icon(
                                            Icons.Filled.Pause,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 80.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Reset",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            pomodoroViewModel.resetTimer()
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    TimerState.PAUSED -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn(animationSpec = spring()),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PremiumButton(
                                    text = "Resume",
                                    onClick = { pomodoroViewModel.resumeTimer() },
                                    icon = {
                                        Icon(
                                            Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier.padding(horizontal = 80.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Reset",
                                    color = TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            pomodoroViewModel.resetTimer()
                                        }
                                        .padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    TimerState.COMPLETED -> {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn(animationSpec = spring()),
                            exit = fadeOut() + scaleOut()
                        ) {
                            PremiumButton(
                                text = "Continue",
                                onClick = { pomodoroViewModel.startTimer() },
                                icon = {
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 60.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

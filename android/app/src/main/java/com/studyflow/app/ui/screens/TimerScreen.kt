package com.studyflow.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.studyflow.app.model.TimerMode
import com.studyflow.app.model.TimerState
import com.studyflow.app.model.defaultPresets
import com.studyflow.app.ui.components.TimerRing
import com.studyflow.app.ui.theme.AccentTeal
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.StudyFlowTheme
import com.studyflow.app.ui.theme.SuccessGreen
import com.studyflow.app.ui.theme.WarningAmber
import com.studyflow.app.viewmodel.TimerViewModel

@Composable
fun TimerScreen(
    timerViewModel: TimerViewModel
) {
    val state by timerViewModel.state.collectAsState()
    val theme = StudyFlowTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Timer",
                color = theme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(24.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.timerMode == TimerMode.FOCUS) "FOCUS" else "BREAK",
                        color = if (state.timerMode == TimerMode.FOCUS) PrimaryBlue else AccentTeal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    )
                    if (state.timerMode == TimerMode.BREAK) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TIME",
                            color = AccentTeal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TimerRing(
                    progress = state.progress,
                    remainingSeconds = state.remainingSeconds,
                    isActive = state.timerState == TimerState.RUNNING
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (state.timerState) {
                        TimerState.IDLE -> {
                            Button(
                                onClick = { timerViewModel.startTimer() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(160.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Start Focus",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        TimerState.RUNNING -> {
                            Button(
                                onClick = { timerViewModel.pauseTimer() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WarningAmber
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(160.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Pause,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pause",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        TimerState.PAUSED -> {
                            Button(
                                onClick = { timerViewModel.resumeTimer() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SuccessGreen
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(160.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Resume",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        TimerState.COMPLETED -> {
                            Button(
                                onClick = { timerViewModel.resetTimer() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryBlue
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .width(160.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = state.timerState != TimerState.IDLE
                    ) {
                        IconButton(
                            onClick = { timerViewModel.resetTimer() }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset",
                                tint = theme.textMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (state.sessionsCompleted > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${state.sessionsCompleted} session(s) completed today",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (state.timerState == TimerState.IDLE) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "QUICK PRESETS",
                color = theme.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                defaultPresets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surfaceElevated)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${preset.focusMinutes}m",
                                color = theme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = preset.label,
                                color = theme.textMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CUSTOMIZE",
                color = theme.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(theme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Focus",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = state.focusMinutes
                                if (current > 5) timerViewModel.setFocusMinutes(current - 5)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-", color = theme.onSurface, fontSize = 18.sp)
                        }
                        Text(
                            text = "${state.focusMinutes}",
                            color = theme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = {
                                val current = state.focusMinutes
                                if (current < 120) timerViewModel.setFocusMinutes(current + 5)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+", color = theme.onSurface, fontSize = 18.sp)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Break",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = state.breakMinutes
                                if (current > 1) timerViewModel.setBreakMinutes(current - 1)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-", color = theme.onSurface, fontSize = 18.sp)
                        }
                        Text(
                            text = "${state.breakMinutes}",
                            color = theme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        IconButton(
                            onClick = {
                                val current = state.breakMinutes
                                if (current < 30) timerViewModel.setBreakMinutes(current + 1)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+", color = theme.onSurface, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

package com.focusx.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusx.app.audio.AudioEngine
import com.focusx.app.data.*
import com.focusx.app.ui.components.AmbientBar
import com.focusx.app.ui.components.CircularTimer
import com.focusx.app.ui.components.TimerStats
import com.focusx.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TimerScreen(
    state: FocusXState,
    ambientMode: String,
    onStateUpdate: (FocusXState) -> Unit,
    onAmbientChange: (String) -> Unit,
    audioEngine: AudioEngine,
) {
    var timerRunning by remember { mutableStateOf(false) }
    var timerPaused by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var startTime by remember { mutableLongStateOf(0L) }
    var currentSubject by remember { mutableStateOf(state.subjects.firstOrNull() ?: "") }

    val scope = rememberCoroutineScope()
    val todayMinutes = getMinutesToday(state.sessions)
    val focusScore = if (state.dailyGoal > 0) (todayMinutes * 100 / state.dailyGoal).coerceIn(0, 100) else 0

    // Timer interval
    LaunchedEffect(timerRunning, timerPaused) {
        if (timerRunning && !timerPaused) {
            while (true) {
                elapsedMs = System.currentTimeMillis() - startTime
                delay(50)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Timer card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 24.dp)) {
                // Glow effect
                if (timerRunning && !timerPaused && state.prefs.glow) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(Accent.copy(alpha = 0.15f), Color.Transparent),
                                ),
                                shape = RoundedCornerShape(120),
                            ),
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    CircularTimer(
                        elapsedMs = elapsedMs,
                        dailyGoal = state.dailyGoal,
                    )

                    // Category pill
                    var expandedCategory by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100))
                                .clickable(enabled = !timerRunning) { expandedCategory = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = currentSubject,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (!timerRunning) {
                                Text("\u25BC", fontSize = 8.sp, color = Text3Dark)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false },
                        ) {
                            state.subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject, fontSize = 13.sp) },
                                    onClick = {
                                        currentSubject = subject
                                        expandedCategory = false
                                    },
                                )
                            }
                        }
                    }

                    // Status text
                    Text(
                        text = when {
                            timerRunning && timerPaused -> "Paused"
                            timerRunning -> "Focusing"
                            else -> "Ready"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (timerRunning && !timerPaused) Accent else Text3Dark,
                        letterSpacing = 0.5.sp,
                    )

                    // Controls
                    Box(
                        modifier = Modifier.height(52.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedContent(
                            targetState = timerRunning,
                            transitionSpec = {
                                fadeIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)) togetherWith
                                    fadeOut(animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f))
                            },
                            label = "timerControl",
                        ) { running ->
                            if (!running) {
                                Button(
                                    onClick = {
                                        audioEngine.playChime("start", state.prefs.chimes)
                                        audioEngine.haptic("start", state.prefs.haptics)
                                        startTime = System.currentTimeMillis()
                                        timerRunning = true
                                        timerPaused = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                    shape = RoundedCornerShape(100),
                                    contentPadding = PaddingValues(horizontal = 36.dp, vertical = 14.dp),
                                ) {
                                    Text("\u25B6  Start", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Pause/Resume
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                                            .clickable {
                                                if (timerPaused) {
                                                    audioEngine.haptic("start", state.prefs.haptics)
                                                    startTime = System.currentTimeMillis() - elapsedMs
                                                    timerPaused = false
                                                } else {
                                                    audioEngine.playChime("pause", state.prefs.chimes)
                                                    audioEngine.haptic("pause", state.prefs.haptics)
                                                    timerPaused = true
                                                }
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            if (timerPaused) "\u25B6" else "\u23F8\uFE0F",
                                            fontSize = 20.sp,
                                        )
                                    }

                                    // Reset
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                                            .clickable {
                                                val wasRunning = timerRunning && elapsedMs > 0
                                                val savedDuration = (elapsedMs / 60000).toInt()

                                                if (wasRunning && savedDuration >= 1) {
                                                    val newSessions = state.sessions + Session(
                                                        subject = currentSubject,
                                                        date = getToday(),
                                                        duration = savedDuration,
                                                        timestamp = System.currentTimeMillis(),
                                                    )
                                                    onStateUpdate(state.copy(sessions = newSessions))
                                                    audioEngine.playChime("complete", state.prefs.chimes)
                                                    audioEngine.haptic("complete", state.prefs.haptics)
                                                } else if (wasRunning) {
                                                    audioEngine.haptic("pause", state.prefs.haptics)
                                                }

                                                timerRunning = false
                                                timerPaused = false
                                                elapsedMs = 0L
                                            },
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("\u21BA", fontSize = 20.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Stats
                    TimerStats(
                        todayMinutes = todayMinutes,
                        focusScore = focusScore,
                    )
                }
            }
        }

        // Ambient bar
        AmbientBar(
            ambientMode = ambientMode,
            enabled = state.prefs.ambient,
            onAmbientChange = onAmbientChange,
        )
    }
}

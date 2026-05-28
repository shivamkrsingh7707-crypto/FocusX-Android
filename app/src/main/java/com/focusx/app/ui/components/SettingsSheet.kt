package com.focusx.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusx.app.audio.AudioEngine
import com.focusx.app.data.*
import com.focusx.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsSheet(
    state: FocusXState,
    onDismiss: () -> Unit,
    onStateUpdate: (FocusXState) -> Unit,
    audioEngine: AudioEngine,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var newSubject by remember { mutableStateOf("") }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .clickable(enabled = false) {}
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface2Dark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BorderDark),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Handle + Header
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .width(32.dp).height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Text3Dark.copy(alpha = 0.4f))
                                .margin(top = 10.dp),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Preferences", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Surface3Dark)
                                .border(1.dp, BorderDark, RoundedCornerShape(50))
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("\u2715", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Body
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Feedback section
                        SectionHeader("Notifications & Feedback")
                        PrefToggle(
                            label = "Haptic Feedback",
                            desc = "Vibration on timer actions",
                            checked = state.prefs.haptics,
                            onChecked = { val p = state.prefs.copy(haptics = it); onStateUpdate(state.copy(prefs = p)) },
                        )
                        PrefToggle(
                            label = "Audio Chimes",
                            desc = "Procedural tones on state changes",
                            checked = state.prefs.chimes,
                            onChecked = { val p = state.prefs.copy(chimes = it); onStateUpdate(state.copy(prefs = p)) },
                        )
                        PrefToggle(
                            label = "Ambient Engine",
                            desc = "Background soundscapes",
                            checked = state.prefs.ambient,
                            onChecked = {
                                val p = state.prefs.copy(ambient = it)
                                onStateUpdate(state.copy(prefs = p))
                                if (!it) audioEngine.stopAmbient()
                            },
                        )
                        PrefToggle(
                            label = "Glow Effect",
                            desc = "Visual glow around timer",
                            checked = state.prefs.glow,
                            onChecked = { val p = state.prefs.copy(glow = it); onStateUpdate(state.copy(prefs = p)) },
                        )

                        // Timer section
                        SectionHeader("Timer")
                        OutlinedTextField(
                            value = state.dailyGoal.toString(),
                            onValueChange = { v ->
                                val n = v.filter { it.isDigit() }.take(4)
                                if (n.isNotEmpty()) {
                                    val num = n.toIntOrNull() ?: return@OutlinedTextField
                                    onStateUpdate(state.copy(dailyGoal = num.coerceIn(1, 1440)))
                                }
                            },
                            label = { Text("Daily Goal (minutes)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Accent,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                cursorColor = Accent,
                            ),
                        )

                        // Subjects section
                        SectionHeader("Subjects")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = newSubject,
                                onValueChange = { newSubject = it },
                                placeholder = { Text("Add subject...", fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Accent,
                                    unfocusedBorderColor = BorderDark,
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    cursorColor = Accent,
                                ),
                            )
                            Button(
                                onClick = {
                                    val name = newSubject.trim()
                                    if (name.isNotEmpty() && !state.subjects.contains(name)) {
                                        onStateUpdate(state.copy(subjects = state.subjects + name))
                                        newSubject = ""
                                        audioEngine.haptic("tap", state.prefs.haptics)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            ) { Text("Add", fontSize = 13.sp) }
                        }

                        // Subject tags
                        if (state.subjects.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                state.subjects.forEach { subject ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(100))
                                            .background(AccentMuted)
                                            .border(1.dp, Accent.copy(alpha = 0.15f), RoundedCornerShape(100))
                                            .clickable {
                                                if (state.subjects.size > 1) {
                                                    onStateUpdate(state.copy(subjects = state.subjects - subject))
                                                }
                                            }
                                            .padding(horizontal = 12.dp, vertical = 5.dp),
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Text(subject, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Accent)
                                            if (state.subjects.size > 1) {
                                                Text("\u2715", fontSize = 10.sp, color = Accent.copy(alpha = 0.7f))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Data section
                        SectionHeader("Data")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedButton(
                                onClick = { /* export */ },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(BorderDark),
                                ),
                            ) { Text("\u2B07 Export", fontSize = 12.sp) }

                            Button(
                                onClick = {
                                    scope.launch {
                                        onStateUpdate(state.copy(sessions = emptyList(), tests = emptyList()))
                                        audioEngine.haptic("tap", state.prefs.haptics)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Danger.copy(alpha = 0.1f)),
                            ) {
                                Text("\uD83D\uDDD1 Clear", fontSize = 12.sp, color = Danger)
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text(
                            "FocusX v2.0 \u2014 Premium Edition",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 11.sp,
                            color = Text3Dark,
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = Text3Dark,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun PrefToggle(
    label: String,
    desc: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onBackground)
            Text(desc, fontSize = 11.sp, color = Text3Dark)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Accent,
                uncheckedThumbColor = Text2Dark,
                uncheckedTrackColor = Surface3Dark,
                uncheckedBorderColor = BorderDark,
            ),
        )
    }
}

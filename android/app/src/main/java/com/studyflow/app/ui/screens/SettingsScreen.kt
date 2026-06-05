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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.theme.AmoledBlack
import com.studyflow.app.ui.theme.BorderLow
import com.studyflow.app.ui.theme.CardDark
import com.studyflow.app.ui.theme.CardElevated
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.TextMuted
import com.studyflow.app.ui.theme.TextPrimary
import com.studyflow.app.ui.theme.TextSecondary
import com.studyflow.app.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel
) {
    val state by settingsViewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "TIMER PREFERENCES") {
            SettingsSwitch(
                icon = Icons.Filled.Vibration,
                label = "Vibrations",
                description = "Vibrate on timer events",
                checked = state.vibrationsEnabled,
                onCheckedChange = { settingsViewModel.setVibrations(it) }
            )
            SettingsSwitch(
                icon = Icons.Filled.VolumeUp,
                label = "Sound Signals",
                description = "Play sound on timer events",
                checked = state.soundEnabled,
                onCheckedChange = { settingsViewModel.setSound(it) }
            )
            SettingsSwitch(
                icon = Icons.Filled.Timer,
                label = "Auto-start Breaks",
                description = "Automatically start break after focus",
                checked = state.autoStartBreaks,
                onCheckedChange = { settingsViewModel.setAutoStartBreaks(it) }
            )
            SettingsSwitch(
                icon = Icons.Filled.Schedule,
                label = "Auto-start Focus",
                description = "Automatically start next focus session",
                checked = state.autoStartFocus,
                onCheckedChange = { settingsViewModel.setAutoStartFocus(it) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "DAILY GOAL") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Focus Goal",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${state.dailyGoalMinutes} min",
                        color = PrimaryBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Slider(
                    value = state.dailyGoalMinutes.toFloat(),
                    onValueChange = { settingsViewModel.setDailyGoal(it.toInt()) },
                    valueRange = 5f..120f,
                    steps = 22,
                    colors = SliderDefaults.colors(
                        thumbColor = PrimaryBlue,
                        activeTrackColor = PrimaryBlue,
                        inactiveTrackColor = CardElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "REMINDERS") {
            SettingsSwitch(
                icon = Icons.Filled.Notifications,
                label = "Daily Reminder",
                description = "Get reminded to study every day",
                checked = state.reminderEnabled,
                onCheckedChange = { enabled ->
                    settingsViewModel.setReminder(enabled)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "StudyFlow v1.0.0",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryBlue,
                checkedTrackColor = PrimaryBlue.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CardElevated
            )
        )
    }
}

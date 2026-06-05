package com.studyflow.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.theme.CardElevated
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.PrimaryBlueDim
import com.studyflow.app.ui.theme.StudyFlowTheme
import com.studyflow.app.ui.theme.ThemeMode
import com.studyflow.app.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onThemeToggle: (Offset) -> Unit = {}
) {
    val state by settingsViewModel.state.collectAsState()
    val theme = StudyFlowTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Settings",
            color = theme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "APPEARANCE") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Theme",
                    color = theme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
                Text(
                    text = "Switches the whole app",
                    color = theme.textMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                ThemeModeRow(
                    current = state.themeMode,
                    onSelect = { mode, origin -> onThemeToggle(origin) },
                    onSelectAfter = { mode -> settingsViewModel.setThemeMode(mode) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(title = "TIMER PREFERENCES") {
            SettingsSwitch(
                icon = Icons.Filled.Vibration,
                label = "Vibrations",
                description = "Vibrate on timer events",
                checked = state.vibrationsEnabled,
                onCheckedChange = { settingsViewModel.setVibrations(it) }
            )
            SettingsDivider()
            SettingsSwitch(
                icon = Icons.Filled.VolumeUp,
                label = "Sound Signals",
                description = "Play sound on timer events",
                checked = state.soundEnabled,
                onCheckedChange = { settingsViewModel.setSound(it) }
            )
            SettingsDivider()
            SettingsSwitch(
                icon = Icons.Filled.Timer,
                label = "Auto-start Breaks",
                description = "Automatically start break after focus",
                checked = state.autoStartBreaks,
                onCheckedChange = { settingsViewModel.setAutoStartBreaks(it) }
            )
            SettingsDivider()
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
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Focus Goal",
                        color = theme.textSecondary,
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
                        inactiveTrackColor = theme.surfaceElevated
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
                onCheckedChange = { settingsViewModel.setReminder(it) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "StudyFlow v1.0.0",
                color = theme.textMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val theme = StudyFlowTheme.colors
    Column {
        Text(
            text = title,
            color = theme.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surface, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsDivider() {
    val theme = StudyFlowTheme.colors
    val color by animateColorAsState(
        targetValue = theme.border,
        animationSpec = tween(250),
        label = "divider"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 44.dp, end = 10.dp)
            .height(1.dp)
            .background(color)
    )
}

@Composable
private fun SettingsSwitch(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val theme = StudyFlowTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.textSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = theme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = theme.textMuted,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryBlue,
                checkedTrackColor = PrimaryBlue.copy(alpha = 0.3f),
                uncheckedThumbColor = theme.textMuted,
                uncheckedTrackColor = theme.surfaceElevated
            )
        )
    }
}

@Composable
private fun ThemeModeRow(
    current: ThemeMode,
    onSelect: (ThemeMode, Offset) -> Unit,
    onSelectAfter: (ThemeMode) -> Unit
) {
    val theme = StudyFlowTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .background(theme.surfaceElevated, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            ThemeModeChip(
                mode = mode,
                selected = current == mode,
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { coords ->
                        // capture position so the reveal can start from this chip
                    },
                onClick = { origin ->
                    onSelect(mode, origin)
                    onSelectAfter(mode)
                }
            )
        }
    }
}

@Composable
private fun ThemeModeChip(
    mode: ThemeMode,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Offset) -> Unit
) {
    val theme = StudyFlowTheme.colors
    val targetBg by animateColorAsState(
        targetValue = if (selected) PrimaryBlue else Color.Transparent,
        animationSpec = tween(220),
        label = "chipBg"
    )
    val targetFg by animateColorAsState(
        targetValue = if (selected) Color.White else theme.textSecondary,
        animationSpec = tween(220),
        label = "chipFg"
    )
    val (icon, label) = when (mode) {
        ThemeMode.SYSTEM -> Icons.Filled.Brightness6 to "System"
        ThemeMode.LIGHT -> Icons.Filled.LightMode to "Light"
        ThemeMode.DARK -> Icons.Filled.DarkMode to "Dark"
    }
    var center by remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(targetBg)
            .clickable {
                onClick(center)
            }
            .onGloballyPositioned { coords ->
                val bounds = coords.boundsInWindow()
                center = Offset(
                    x = bounds.left + bounds.width / 2f,
                    y = bounds.top + bounds.height / 2f
                )
            }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = targetFg,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = targetFg,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

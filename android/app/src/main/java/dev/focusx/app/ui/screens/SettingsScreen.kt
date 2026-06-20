package dev.focusx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.focusx.app.data.AppViewModel
import dev.focusx.app.domain.Settings
import dev.focusx.app.domain.ThemeMode
import dev.focusx.app.ui.components.ScreenHeader
import dev.focusx.app.ui.components.SectionLabel
import dev.focusx.app.ui.components.SurfaceCard
import dev.focusx.app.ui.theme.FocusXTheme
import dev.focusx.app.ui.theme.SubjectPalette

@Composable
fun SettingsScreen(
    ui: AppViewModel.UiState,
    onThemeMode: (ThemeMode) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onAutoStartBreak: (Boolean) -> Unit,
    onAutoStartFocus: (Boolean) -> Unit,
    onDailyGoal: (Int) -> Unit,
    onReminder: (Boolean) -> Unit,
    onThemeOrigin: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = FocusXTheme.colors
    val settings = ui.snapshot.settings

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Settings",
                    subtitle = "Tune your focus"
                )
            }
            item {
                SectionLabel(text = "Appearance")
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Theme",
                        color = theme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ThemeModeRow(
                        current = settings.themeMode,
                        onSelect = onThemeMode,
                        onOrigin = onThemeOrigin
                    )
                }
            }
            item {
                SectionLabel(text = "Timer")
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    SettingsSwitch(
                        icon = Icons.Filled.Vibration,
                        label = "Haptics",
                        description = "Vibrate on key moments",
                        checked = settings.hapticsEnabled,
                        onCheckedChange = onHaptics
                    )
                    DividerLine()
                    SettingsSwitch(
                        icon = Icons.Filled.VolumeUp,
                        label = "Sound",
                        description = "Audio cues on session events",
                        checked = settings.soundEnabled,
                        onCheckedChange = onSound
                    )
                    DividerLine()
                    SettingsSwitch(
                        icon = Icons.Filled.Timer,
                        label = "Auto-start break",
                        description = "Begin break right after focus",
                        checked = settings.autoStartBreak,
                        onCheckedChange = onAutoStartBreak
                    )
                    DividerLine()
                    SettingsSwitch(
                        icon = Icons.Filled.Schedule,
                        label = "Auto-start focus",
                        description = "Begin next focus after break",
                        checked = settings.autoStartFocus,
                        onCheckedChange = onAutoStartFocus
                    )
                }
            }
            item {
                SectionLabel(text = "Daily goal")
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Focus minutes / day",
                                color = theme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Long-press the slider for fine control",
                                color = theme.textTertiary,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "${settings.dailyGoalMinutes}m",
                            color = theme.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = settings.dailyGoalMinutes.toFloat(),
                        onValueChange = { onDailyGoal(it.toInt()) },
                        valueRange = 5f..180f,
                        steps = 34,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.primary,
                            activeTrackColor = theme.primary,
                            inactiveTrackColor = theme.surfaceElevated
                        )
                    )
                }
            }
            item {
                SectionLabel(text = "Reminders")
                SurfaceCard(modifier = Modifier.fillMaxWidth()) {
                    SettingsSwitch(
                        icon = Icons.Filled.Notifications,
                        label = "Daily reminder",
                        description = "9:00 AM each day",
                        checked = settings.reminderEnabled,
                        onCheckedChange = onReminder
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(110.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FocusX v2.0",
                        color = theme.textTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 46.dp, end = 0.dp)
            .height(1.dp)
            .background(FocusXTheme.colors.hairline)
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
    val theme = FocusXTheme.colors
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(theme.surfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = theme.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = theme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = theme.textTertiary,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = theme.primary,
                uncheckedThumbColor = theme.textTertiary,
                uncheckedTrackColor = theme.surfaceElevated,
                uncheckedBorderColor = theme.hairline,
                checkedBorderColor = theme.primary
            )
        )
    }
}

@Composable
private fun ThemeModeRow(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onOrigin: (Offset) -> Unit
) {
    val theme = FocusXTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceElevated)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            ThemeChip(
                mode = mode,
                selected = current == mode,
                modifier = Modifier.weight(1f),
                onClick = { origin ->
                    onOrigin(origin)
                    onSelect(mode)
                }
            )
        }
    }
}

@Composable
private fun ThemeChip(
    mode: ThemeMode,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Offset) -> Unit
) {
    val theme = FocusXTheme.colors
    val bg = if (selected) theme.primary else Color.Transparent
    val fg = if (selected) Color.White else theme.textSecondary
    val source = remember { MutableInteractionSource() }
    val (icon, label) = when (mode) {
        ThemeMode.SYSTEM -> Icons.Filled.Brightness6 to "System"
        ThemeMode.LIGHT -> Icons.Filled.LightMode to "Light"
        ThemeMode.DARK -> Icons.Filled.DarkMode to "Dark"
    }
    var center by remember { mutableStateOf(Offset.Zero) }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = { onClick(center) }
            )
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                center = Offset(
                    x = b.left + b.width / 2f,
                    y = b.top + b.height / 2f
                )
            }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            color = fg,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

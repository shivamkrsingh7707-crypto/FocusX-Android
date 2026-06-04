package com.studyzen.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyzen.app.theme.AmoledBlack
import com.studyzen.app.theme.BorderLow
import com.studyzen.app.theme.CardDark
import com.studyzen.app.theme.PrimaryPurple
import com.studyzen.app.theme.TextMuted
import com.studyzen.app.theme.TextPrimary
import com.studyzen.app.theme.TextSecondary

@Composable
fun SettingsSheetContent(
    vibrationsEnabled: Boolean,
    onVibrationsChange: (Boolean) -> Unit,
    acousticSignalsEnabled: Boolean,
    onAcousticSignalsChange: (Boolean) -> Unit,
    glowingAuraEnabled: Boolean,
    onGlowingAuraChange: (Boolean) -> Unit,
    deepFocusModeEnabled: Boolean,
    onDeepFocusModeChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AmoledBlack)
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(TextMuted.copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Preferences",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        SettingsToggle(
            icon = null,
            title = "Micro-Vibrations",
            subtitle = "Haptic feedback on timer events",
            checked = vibrationsEnabled,
            onCheckedChange = onVibrationsChange
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsToggle(
            icon = null,
            title = "Acoustic Signals",
            subtitle = "Audio tones for start, pause, completion",
            checked = acousticSignalsEnabled,
            onCheckedChange = onAcousticSignalsChange
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsToggle(
            icon = null,
            title = "Active Glowing Aura",
            subtitle = "Animated glow backdrop behind the timer",
            checked = glowingAuraEnabled,
            onCheckedChange = onGlowingAuraChange
        )

        Spacer(modifier = Modifier.height(4.dp))

        SettingsToggle(
            icon = null,
            title = "Strict Deep Focus Mode",
            subtitle = "Block distractions during sessions",
            checked = deepFocusModeEnabled,
            onCheckedChange = onDeepFocusModeChange
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "FocusX v1.0.0",
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .drawBehind {
                        drawCircle(
                            color = PrimaryPurple.copy(alpha = 0.15f)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (checked) PrimaryPurple else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryPurple,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = CardDark
            )
        )
    }
}

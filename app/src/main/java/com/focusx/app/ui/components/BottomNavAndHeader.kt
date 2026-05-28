package com.focusx.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusx.app.MainActivity
import com.focusx.app.Tab
import com.focusx.app.ui.theme.*

@Composable
fun FocusXHeader(
    streak: Int,
    isDark: Boolean,
    onThemeToggle: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Accent),
                contentAlignment = Alignment.Center,
            ) {
                Text("X", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text("FocusX", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AccentMuted)
                    .border(1.dp, Accent.copy(alpha = 0.15f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("\uD83D\uDD25", fontSize = 10.sp)
                Text("$streak", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
            }

            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            ) {
                Text(if (isDark) "\u2600\uFE0F" else "\uD83C\uDF19", fontSize = 14.sp)
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FocusXBottomNav(
    activeTab: Tab,
    onTabSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface2Dark),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(BorderDark),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            for (tab in Tab.values()) {
                val isActive = tab == activeTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) AccentMuted else Color.Transparent)
                        .clickable { onTabSelect(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val icon = when (tab) {
                        Tab.Timer -> "\u23F1\uFE0F"
                        Tab.Progress -> "\uD83D\uDCCA"
                        Tab.Tests -> "\uD83D\uDCDD"
                    }
                    Text(icon, fontSize = 16.sp)

                    val label = when (tab) {
                        Tab.Timer -> "Timer"
                        Tab.Progress -> "Progress"
                        Tab.Tests -> "Tests"
                    }
                    Text(
                        label,
                        fontSize = 9.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isActive) Accent else Text3Dark,
                        letterSpacing = 0.2.sp,
                    )
                }
            }
        }
    }
}

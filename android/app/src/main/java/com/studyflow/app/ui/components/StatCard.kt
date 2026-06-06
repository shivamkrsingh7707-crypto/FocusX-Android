package com.studyflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.ui.theme.StudyFlowTheme

@Composable
fun StatCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val theme = StudyFlowTheme.colors
    // A subtle gradient gives the card a "lit" feel without changing the
    // design language. Both colors track the theme so it crossfades cleanly.
    val topColor by animateColorAsState(
        targetValue = theme.surfaceElevated,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "statTop"
    )
    val bottomColor by animateColorAsState(
        targetValue = theme.surface,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "statBottom"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = label,
            color = theme.textSecondary,
            fontSize = 11.sp,
            letterSpacing = 0.3.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = theme.onSurface,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun StatRow(
    items: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { item ->
            item()
        }
    }
}

package com.studyflow.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.model.AppScreen
import com.studyflow.app.ui.theme.PrimaryBlue
import com.studyflow.app.ui.theme.StudyFlowTheme

data class BottomNavItem(
    val screen: AppScreen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

private val BottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(AppScreen.DASHBOARD, Icons.Filled.Home, Icons.Outlined.Home, "Home"),
    BottomNavItem(AppScreen.SUBJECTS, Icons.Filled.Home, Icons.Outlined.School, "Subjects"),
    BottomNavItem(AppScreen.TIMER, Icons.Filled.Timer, Icons.Outlined.Timer, "Timer"),
    BottomNavItem(AppScreen.STATISTICS, Icons.Filled.BarChart, Icons.Outlined.BarChart, "Stats"),
    BottomNavItem(AppScreen.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings, "Settings")
)

@Composable
fun BottomNavBar(
    selectedScreen: AppScreen,
    onScreenSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = StudyFlowTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = theme.border,
                    spotColor = theme.border,
                    clip = false
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            theme.surfaceElevated,
                            theme.surface
                        )
                    )
                )
                .border(1.dp, theme.border, RoundedCornerShape(32.dp))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            BottomNavItems.forEach { item ->
                FloatingNavItem(
                    item = item,
                    selected = item.screen == selectedScreen,
                    onClick = { onScreenSelected(item.screen) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = StudyFlowTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Spring-driven press scale so taps feel "alive" at 120Hz.
    val pressAnim = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        pressAnim.animateTo(
            targetValue = if (pressed) 0.92f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )
    }

    val iconColor by animateColorAsState(
        targetValue = if (selected) PrimaryBlue else theme.textMuted,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "icon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) PrimaryBlue else theme.textMuted,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "label"
    )
    val pillBg by animateColorAsState(
        targetValue = if (selected) PrimaryBlue.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "pillBg"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = pressAnim.value
                scaleY = pressAnim.value
            }
            .clip(RoundedCornerShape(24.dp))
            .background(pillBg)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.label,
                color = labelColor,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                letterSpacing = 0.2.sp
            )
        }
    }
}

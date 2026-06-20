package dev.focusx.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.focusx.app.domain.Subject
import dev.focusx.app.ui.theme.FocusXTheme
import dev.focusx.app.ui.theme.SubjectPalette

private val SubjectIcons: List<ImageVector> = listOf(
    Icons.Filled.Book,
    Icons.Filled.Code,
    Icons.Filled.Science,
    Icons.Filled.Calculate,
    Icons.Filled.Language
)

@Composable
fun SubjectRow(
    subject: Subject,
    isSelected: Boolean,
    onSelect: () -> Unit,
    totalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val theme = FocusXTheme.colors
    val color = SubjectPalette[subject.colorIndex.coerceIn(0, SubjectPalette.lastIndex)]
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(if (pressed) 100 else 160, easing = FastOutSlowInEasing),
        label = "row"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.6f) else theme.hairline,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "rowBorder"
    )
    val progress = (totalMinutes.toFloat() / (subject.targetHoursPerWeek * 60f).coerceAtLeast(1f))
        .coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isSelected) listOf(color.copy(alpha = 0.18f), color.copy(alpha = 0.04f))
                    else listOf(theme.surfaceElevated, theme.surface)
                )
            )
            .clickable(
                interactionSource = source,
                indication = null,
                onClick = onSelect
            )
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SubjectIcons[subject.colorIndex.coerceIn(0, SubjectIcons.lastIndex)],
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    color = theme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${totalMinutes / 60}h ${totalMinutes % 60}m logged · goal ${subject.targetHoursPerWeek}h/wk",
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(2.dp, borderColor, CircleShape)
                    )
                }
            }
        }

        // Underline progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .padding(start = 54.dp, end = 48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color.copy(alpha = 0.15f))
                .align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}



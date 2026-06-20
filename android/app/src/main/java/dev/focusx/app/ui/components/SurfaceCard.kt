package dev.focusx.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.focusx.app.ui.theme.FocusXTheme

/**
 * Glass-style surface card: subtle vertical gradient + hairline border.
 * Sits in front of the AMOLED background and reads as "elevated
 * surface" without the noisy outer drop-shadow that Material default
 * cards ship with.
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    contentPadding: Dp = 18.dp,
    content: @Composable () -> Unit
) {
    val theme = FocusXTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(theme.surfaceElevated, theme.surface)
                )
            )
            .border(1.dp, theme.hairline, RoundedCornerShape(cornerRadius))
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun Pill(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = FocusXTheme.colors.primary,
    textColor: Color = FocusXTheme.colors.onSurface
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
        )
    }
}

package com.studyzen.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.studyzen.app.theme.Card
import com.studyzen.app.theme.GlassBorder
import com.studyzen.app.theme.GlowPurple
import com.studyzen.app.theme.TextSecondary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = GlowPurple,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (onClick != null) 0.98f else 1f,
        label = "cardAlpha"
    )

    Card(
        onClick = { onClick?.invoke() },
        modifier = modifier
            .fillMaxWidth()
            .alpha(animatedAlpha),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Card.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        ),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.03f),
                                Color.Transparent
                            )
                        ),
                        size = size,
                        topLeft = Offset.Zero
                    )
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.1f),
                                Color.Transparent,
                                glowColor.copy(alpha = 0.05f)
                            )
                        ),
                        topLeft = Offset(-size.width * 0.1f, size.height * 0.3f),
                        size = Size(size.width * 1.2f, size.height * 0.4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                        style = Stroke(width = 60f)
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                content = content
            )
        }
    }
}

@Composable
fun GlassCardMinimal(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Card,
                        Card.copy(alpha = 0.7f)
                    )
                )
            )
            .drawBehind {
                drawRoundRect(
                    color = GlassBorder,
                    style = Stroke(width = 1.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(20.dp.toPx())
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    ),
                    size = size,
                    topLeft = Offset.Zero
                )
            }
            .padding(16.dp)
    ) {
        Column(content = content)
    }
}

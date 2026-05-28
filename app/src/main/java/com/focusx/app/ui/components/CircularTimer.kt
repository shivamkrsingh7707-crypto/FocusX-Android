package com.focusx.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusx.app.audio.AudioEngine
import com.focusx.app.data.*
import com.focusx.app.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularTimer(
    elapsedMs: Long,
    dailyGoal: Int,
    modifier: Modifier = Modifier,
) {
    val size = 200.dp
    val strokeWidth = 3.dp
    val elapsedMin = elapsedMs / 60000f
    val progress = if (dailyGoal > 0) (elapsedMin / dailyGoal).coerceIn(0f, 1f) else 0f
    val mins = (elapsedMs / 60000).toInt()
    val secs = ((elapsedMs % 60000) / 1000).toInt()

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = (size.toPx() - stroke) / 2
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(size.toPx() - stroke, size.toPx() - stroke)

            // Track
            drawArc(
                color = Surface2Dark,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            // Progress
            drawArc(
                color = Accent,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "${String.format("%02d", mins)}:${String.format("%02d", secs)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
fun AmbientBar(
    ambientMode: String,
    enabled: Boolean,
    onAmbientChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(100))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("\uD83D\uDD0A", fontSize = 12.sp)
                    Text(
                        if (ambientMode == "none") "Ambient" else ambientMode.replaceFirstChar { it.uppercase() },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AnimatedVisibility(visible = expanded, enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("none", "rain", "whitenoise", "cafe").forEach { mode ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100))
                                .background(if (mode == ambientMode) AccentMuted else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onAmbientChange(mode) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Text(
                                when (mode) {
                                    "none" -> "Off"
                                    "rain" -> "Rain"
                                    "whitenoise" -> "Noise"
                                    "cafe" -> "Cafe"
                                    else -> mode
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (mode == ambientMode) Accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimerStats(
    todayMinutes: Int,
    focusScore: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (todayMinutes >= 60) "${todayMinutes / 60}h ${todayMinutes % 60}m" else "${todayMinutes} min",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Today's Study",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Text3Dark,
                    letterSpacing = 0.6.sp,
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$focusScore%",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = "Focus Score",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Text3Dark,
                    letterSpacing = 0.6.sp,
                )
            }
        }
    }
}

package com.studyflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyflow.app.model.SubjectWithStats
import com.studyflow.app.ui.theme.StudyFlowTheme
import com.studyflow.app.ui.theme.SubjectColors

private val subjectIcons: Map<String, ImageVector> = mapOf(
    "book" to Icons.Filled.Book,
    "code" to Icons.Filled.Code,
    "science" to Icons.Filled.Science,
    "language" to Icons.Filled.Language,
    "calculate" to Icons.Filled.Calculate
)

@Composable
fun SubjectCard(
    subject: SubjectWithStats,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = SubjectColors.getOrElse(subject.colorIndex) { SubjectColors[0] }
    val theme = StudyFlowTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) subjectColor.copy(alpha = 0.15f) else theme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(subjectColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = subjectIcons[subject.iconName] ?: Icons.Filled.Book,
                    contentDescription = null,
                    tint = subjectColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    color = theme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${subject.totalMinutes}m studied",
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
            }

            if (subject.targetHoursPerWeek > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${subject.totalMinutes / 60}h / ${subject.targetHoursPerWeek}h",
                        color = theme.textMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(subjectColor.copy(alpha = 0.2f))
                    ) {
                        val progress = if (subject.targetHoursPerWeek > 0)
                            (subject.totalMinutes / 60f) / subject.targetHoursPerWeek else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(subjectColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubjectCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = StudyFlowTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(theme.surfaceElevated)
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Book,
                contentDescription = null,
                tint = theme.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Subject",
                color = theme.textMuted,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

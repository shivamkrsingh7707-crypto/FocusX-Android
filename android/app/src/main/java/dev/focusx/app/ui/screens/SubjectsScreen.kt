package dev.focusx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.focusx.app.data.AppViewModel
import dev.focusx.app.domain.Subject
import dev.focusx.app.ui.components.GhostButton
import dev.focusx.app.ui.components.PrimaryButton
import dev.focusx.app.ui.components.ScreenHeader
import dev.focusx.app.ui.components.SectionLabel
import dev.focusx.app.ui.components.SubjectRow
import dev.focusx.app.ui.theme.FocusXTheme
import dev.focusx.app.ui.theme.SubjectPalette

@Composable
fun SubjectsScreen(
    ui: AppViewModel.UiState,
    onAdd: (String, Int, Int) -> Unit,
    onDelete: (String) -> Unit,
    onSelect: (String?) -> Unit,
    activeSubjectId: String?,
    modifier: Modifier = Modifier
) {
    val theme = FocusXTheme.colors
    var showAdd by remember { mutableStateOf(false) }
    val totalBySubject = ui.snapshot.sessions.groupingBy { it.subjectId }.fold(0) { acc, s -> acc + s.minutes }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
            Spacer(modifier = Modifier.height(6.dp))
            ScreenHeader(
                title = "Subjects",
                subtitle = "Group your focus by topic",
                trailing = {
                    IconButton(onClick = { showAdd = true }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(theme.primary.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add subject",
                                tint = theme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (ui.snapshot.subjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No subjects yet. Tap + to add one.",
                        color = theme.textTertiary,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 110.dp)
                ) {
                    item {
                        SectionLabel(text = "Active for next session")
                    }
                    items(items = ui.snapshot.subjects, key = { it.id }) { subject ->
                        SubjectRow(
                            subject = subject,
                            isSelected = activeSubjectId == subject.id,
                            onSelect = { onSelect(if (activeSubjectId == subject.id) null else subject.id) },
                            totalMinutes = totalBySubject[subject.id] ?: 0
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddSubjectDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, color, hours ->
                onAdd(name, color, hours)
                showAdd = false
            }
        )
    }
}

@Composable
private fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int) -> Unit
) {
    val theme = FocusXTheme.colors
    var name by remember { mutableStateOf("") }
    var color by remember { mutableIntStateOf(0) }
    var target by remember { mutableFloatStateOf(8f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.surface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(
                text = "New subject",
                color = theme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Linear Algebra", color = theme.textTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = theme.primary,
                        unfocusedBorderColor = theme.hairline,
                        focusedTextColor = theme.onSurface,
                        unfocusedTextColor = theme.onSurface,
                        cursorColor = theme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Colour",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SubjectPalette.forEachIndexed { idx, c ->
                        val sel = color == idx
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { color = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            if (sel) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Weekly goal: ${target.toInt()}h",
                    color = theme.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                Slider(
                    value = target,
                    onValueChange = { target = it },
                    valueRange = 1f..40f,
                    steps = 38,
                    colors = SliderDefaults.colors(
                        thumbColor = theme.primary,
                        activeTrackColor = theme.primary,
                        inactiveTrackColor = theme.surfaceElevated
                    )
                )
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "Add",
                onClick = {
                    if (name.isNotBlank()) onConfirm(name.trim(), color, target.toInt())
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = theme.textTertiary)
            }
        }
    )
}

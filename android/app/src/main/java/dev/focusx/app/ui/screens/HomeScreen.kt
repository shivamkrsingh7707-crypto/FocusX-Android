package dev.focusx.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import dev.focusx.app.domain.TimerPhase
import dev.focusx.app.domain.TimerState
import dev.focusx.app.domain.TimerStatus
import dev.focusx.app.ui.components.GhostButton
import dev.focusx.app.ui.components.IconCircle
import dev.focusx.app.ui.components.Pill
import dev.focusx.app.ui.components.PrimaryButton
import dev.focusx.app.ui.components.PulsingDot
import dev.focusx.app.ui.components.ScreenHeader
import dev.focusx.app.ui.components.SectionLabel
import dev.focusx.app.ui.components.SurfaceCard
import dev.focusx.app.ui.components.TimerRing
import dev.focusx.app.ui.components.TimerUnderLabel
import dev.focusx.app.ui.components.WeeklyBarChart
import dev.focusx.app.ui.components.DayPoint
import dev.focusx.app.ui.theme.FocusXTheme
import dev.focusx.app.ui.theme.SubjectPalette
import java.time.LocalDate

@Composable
fun HomeScreen(
    ui: AppViewModel.UiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onSelectSubject: (String?) -> Unit,
    onOpenSubjects: () -> Unit,
    onOpenStats: () -> Unit,
    onAddSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = FocusXTheme.colors
    val scroll = rememberScrollState()

    val snap = ui.snapshot
    val timer = ui.timer

    val goalPct = (snap.todayMinutes.toFloat() / snap.settings.dailyGoalMinutes.coerceAtLeast(1))
        .coerceIn(0f, 1f)
    val goalAnim by animateFloatAsState(
        targetValue = goalPct,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "goal"
    )

    val week = buildWeek(snap)
    val greeting = remember { greetingForNow() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .verticalScroll(scroll)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        ScreenHeader(
            title = "FocusX",
            subtitle = "$greeting, ${(snap.streak)} day streak",
            trailing = {
                Pill(
                    label = "Lv ${(snap.totalSessions / 5) + 1}",
                    color = theme.primary
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── Hero card ──────────────────────────────────────────────
        SurfaceCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 26.dp,
            contentPadding = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PulsingDot(
                        color = when (timer.status) {
                            TimerStatus.RUNNING -> theme.success
                            TimerStatus.PAUSED -> theme.warn
                            TimerStatus.COMPLETED -> theme.primary
                            TimerStatus.IDLE -> theme.textTertiary
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TimerUnderLabel(timer = timer)
                }
                Spacer(modifier = Modifier.height(10.dp))
                TimerRing(timer = timer)
                Spacer(modifier = Modifier.height(20.dp))

                // ── Controls ───────────────────────────────────────
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconCircle(
                        icon = Icons.Filled.Refresh,
                        onClick = onReset
                    )
                    PrimaryButton(
                        text = primaryLabel(timer),
                        leading = primaryIcon(timer),
                        onClick = {
                            when (timer.status) {
                                TimerStatus.RUNNING -> onPause()
                                else -> onStart()
                            }
                        }
                    )
                    IconCircle(
                        icon = Icons.Filled.SkipNext,
                        onClick = onSkip
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Today / streak row ────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MiniStat(
                label = "Today",
                value = "${snap.todayMinutes}",
                unit = "min",
                accent = theme.primary,
                modifier = Modifier.weight(1f)
            )
            MiniStat(
                label = "Streak",
                value = "${snap.streak}",
                unit = "days",
                accent = theme.warn,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── Daily goal progress ───────────────────────────────────
        SurfaceCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily goal",
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${snap.todayMinutes} / ${snap.settings.dailyGoalMinutes} min",
                        color = theme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${(goalPct * 100).toInt()}%",
                    color = theme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.hairline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goalAnim)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(theme.primarySoft, theme.primary)
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        SectionLabel(text = "This week", right = {
            GhostButton(text = "Open stats", onClick = onOpenStats)
        })
        SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            WeeklyBarChart(
                data = week,
                goal = snap.settings.dailyGoalMinutes,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        SectionLabel(text = "Active subject", right = {
            GhostButton(text = "Manage", onClick = onOpenSubjects)
        })
        if (snap.subjects.isEmpty()) {
            EmptySubjectCard(onAddSubject)
        } else {
            ActiveSubjectSelector(
                subjects = snap.subjects,
                selected = timer.activeSubjectId,
                totalBySubject = snap.sessions.groupingBy { it.subjectId }.fold(0) { acc, s -> acc + s.minutes },
                onSelect = onSelectSubject
            )
        }

        Spacer(modifier = Modifier.height(110.dp))
    }
}

@Composable
private fun MiniStat(
    label: String,
    value: String,
    unit: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val theme = FocusXTheme.colors
    SurfaceCard(modifier = modifier, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = label.uppercase(),
                    color = theme.textTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.4.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        color = theme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSubjectSelector(
    subjects: List<Subject>,
    selected: String?,
    totalBySubject: Map<String?, Int>,
    onSelect: (String?) -> Unit
) {
    val theme = FocusXTheme.colors
    SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 10.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.take(4).forEach { subj ->
                val color = SubjectPalette[subj.colorIndex.coerceIn(0, SubjectPalette.lastIndex)]
                val isSel = selected == subj.id
                val source = remember { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSel) color.copy(alpha = 0.22f)
                            else theme.surfaceElevated
                        )
                        .clickable(
                            interactionSource = source,
                            indication = null,
                            onClick = { onSelect(subj.id) }
                        )
                        .padding(vertical = 10.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = subj.name,
                            color = if (isSel) color else theme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = "${(totalBySubject[subj.id] ?: 0)}m",
                            color = theme.textTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySubjectCard(onAdd: () -> Unit) {
    val theme = FocusXTheme.colors
    SurfaceCard(modifier = Modifier.fillMaxWidth(), contentPadding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.primary.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = theme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Add a subject",
                    color = theme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Group your focus by topic — Math, Code, Reading…",
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )
            }
            GhostButton(text = "Add", onClick = onAdd)
        }
    }
}

private fun buildWeek(state: dev.focusx.app.domain.AppState): List<DayPoint> {
    val today = LocalDate.now()
    val byDate = state.sessions.groupBy { it.date }.mapValues { (_, v) -> v.sumOf { it.minutes } }
    return (6 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        DayPoint(
            date = date,
            minutes = byDate[date] ?: 0,
            isToday = date == today
        )
    }
}

private fun greetingForNow(): String {
    val h = java.time.LocalTime.now().hour
    return when {
        h < 5 -> "Late night"
        h < 12 -> "Morning"
        h < 17 -> "Afternoon"
        h < 21 -> "Evening"
        else -> "Late night"
    }
}

private fun primaryLabel(timer: TimerState): String = when (timer.status) {
    TimerStatus.RUNNING -> "Pause"
    TimerStatus.PAUSED -> "Resume"
    TimerStatus.COMPLETED -> "Start new"
    TimerStatus.IDLE -> if (timer.phase == TimerPhase.BREAK) "Start break" else "Start focus"
}

private fun primaryIcon(timer: TimerState) = when (timer.status) {
    TimerStatus.RUNNING -> Icons.Filled.Pause
    else -> Icons.Filled.PlayArrow
}

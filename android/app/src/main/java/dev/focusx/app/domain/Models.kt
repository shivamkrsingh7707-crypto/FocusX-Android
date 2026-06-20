package dev.focusx.app.domain

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Single state value object. Marked [@Immutable] so the Compose runtime
 * can skip recomposition when an equivalent value flows through the
 * graph (data class equality short-circuits). Setters on the holder
 * always copy.
 */
@Immutable
data class AppState(
    val timer: TimerState = TimerState.Idle,
    val subjects: List<Subject> = defaultSubjects,
    val sessions: List<Session> = emptyList(),
    val streak: Int = 0,
    val settings: Settings = Settings(),
    val todayMinutes: Int = 0
) {
    val totalMinutes: Int get() = sessions.sumOf { it.minutes }
    val totalSessions: Int get() = sessions.size
    val sessionsForActiveSubject: Int
        get() = sessions.count { it.subjectId == timer.activeSubjectId }
}

enum class TimerPhase { FOCUS, BREAK }
enum class TimerStatus { IDLE, RUNNING, PAUSED, COMPLETED }

@Immutable
data class TimerState(
    val phase: TimerPhase = TimerPhase.FOCUS,
    val status: TimerStatus = TimerStatus.IDLE,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val remainingSeconds: Int = 25 * 60,
    val totalFocusSeconds: Int = 25 * 60,
    val activeSubjectId: String? = null,
    val sessionsToday: Int = 0,
    val totalFocusMinutesToday: Int = 0
) {
    val progress: Float
        get() = if (totalFocusSeconds == 0) 0f
        else (remainingSeconds.toFloat() / totalFocusSeconds).coerceIn(0f, 1f)
}

@Immutable
data class Subject(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val targetHoursPerWeek: Int = 5,
    val archived: Boolean = false
)

@Immutable
data class Session(
    val id: String,
    val subjectId: String?,
    val minutes: Int,
    val date: LocalDate,
    val phase: TimerPhase,
    val startedAt: Long
) {
    companion object {
        val IsoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}

@Immutable
data class Settings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val autoStartBreak: Boolean = false,
    val autoStartFocus: Boolean = false,
    val dailyGoalMinutes: Int = 25,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0
)

internal val defaultSubjects: List<Subject> = listOf(
    Subject(id = "math", name = "Math", colorIndex = 0, targetHoursPerWeek = 8),
    Subject(id = "code", name = "Code", colorIndex = 1, targetHoursPerWeek = 6),
    Subject(id = "read", name = "Reading", colorIndex = 4, targetHoursPerWeek = 4)
)

@Immutable
data class Grade(
    val id: String,
    val subjectId: String,
    val score: Double,
    val maxScore: Double,
    val weight: Double,
    val title: String,
    val date: LocalDate,
    val createdAt: Long = System.currentTimeMillis()
)

internal fun isoToday(date: LocalDate = LocalDate.now()): String =
    date.format(Session.IsoDate)

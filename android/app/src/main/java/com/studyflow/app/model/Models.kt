package com.studyflow.app.model

data class SubjectWithStats(
    val id: Long,
    val name: String,
    val colorIndex: Int,
    val iconName: String,
    val totalMinutes: Int,
    val sessionCount: Int,
    val targetHoursPerWeek: Int
)

data class WeeklyDay(
    val dayLabel: String,
    val date: String,
    val minutes: Int,
    val isToday: Boolean = false
)

data class TimerPreset(
    val label: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val isDefault: Boolean = false
)

val defaultPresets = listOf(
    TimerPreset("Deep Focus", 50, 10),
    TimerPreset("Standard", 25, 5, isDefault = true),
    TimerPreset("Short Sprint", 15, 3),
    TimerPreset("Extended", 90, 20)
)

enum class TimerState { IDLE, RUNNING, PAUSED, COMPLETED }
enum class TimerMode { FOCUS, BREAK }

data class PomodoroState(
    val timerState: TimerState = TimerState.IDLE,
    val timerMode: TimerMode = TimerMode.FOCUS,
    val remainingSeconds: Int = 25 * 60,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val totalFocusMinutes: Int = 0,
    val sessionsCompleted: Int = 0,
    val progress: Float = 1f,
    val activeSubjectId: Long? = null
)

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    SUBJECTS("Subjects"),
    TIMER("Timer"),
    STATISTICS("Statistics"),
    SETTINGS("Settings")
}

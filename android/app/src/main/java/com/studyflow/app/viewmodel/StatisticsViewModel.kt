package com.studyflow.app.viewmodel

import android.app.Application
import androidx.compose.runtime.Immutable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.database.StudyFlowDatabase
import com.studyflow.app.data.repository.StudyRepository
import com.studyflow.app.model.WeeklyDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Immutable
data class StatisticsState(
    val weeklyData: List<WeeklyDay> = emptyList(),
    val totalMinutes: Int = 0,
    val totalSessions: Int = 0,
    val weeklyTotal: Int = 0,
    val averageDailyMinutes: Double = 0.0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(
        StudyFlowDatabase.getInstance(application).subjectDao(),
        StudyFlowDatabase.getInstance(application).sessionDao(),
        StudyFlowDatabase.getInstance(application).streakDao()
    )

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { computeStatistics() }
                .onSuccess { fresh -> _state.value = fresh }
                .onFailure { _state.update { it.copy(isLoading = false) } }
        }
    }

    fun refresh() = loadStatistics()

    private suspend fun computeStatistics(): StatisticsState {
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = monday.plusDays(WEEK_LENGTH - 1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val locale = Locale.getDefault()

        val dailyMinutes = repository.getDailyMinutesInRange(
            monday.format(formatter),
            sunday.format(formatter)
        )
        val dayMinutesMap = dailyMinutes.associate { it.date to it.totalMinutes }

        val weeklyData = (0 until WEEK_LENGTH).map { offset ->
            val date = monday.plusDays(offset.toLong())
            val dateStr = date.format(formatter)
            WeeklyDay(
                dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale).take(DAY_LABEL_LEN),
                date = dateStr,
                minutes = dayMinutesMap[dateStr] ?: 0,
                isToday = date == today
            )
        }
        val weekTotal = weeklyData.sumOf { it.minutes }
        val totalMin = repository.totalMinutes.first() ?: 0
        val totalSess = repository.totalSessions.first() ?: 0

        return StatisticsState(
            weeklyData = weeklyData,
            totalMinutes = totalMin,
            totalSessions = totalSess,
            weeklyTotal = weekTotal,
            averageDailyMinutes = if (weeklyData.isNotEmpty()) weekTotal / WEEK_LENGTH_DOUBLE else 0.0,
            currentStreak = 0,
            isLoading = false
        )
    }

    private companion object {
        const val WEEK_LENGTH = 7
        const val WEEK_LENGTH_DOUBLE = 7.0
        const val DAY_LABEL_LEN = 3
    }
}

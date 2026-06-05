package com.studyflow.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.database.StudyFlowDatabase
import com.studyflow.app.data.repository.StudyRepository
import com.studyflow.app.model.WeeklyDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

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

    private val database = StudyFlowDatabase.getInstance(application)
    private val repository = StudyRepository(
        database.subjectDao(),
        database.sessionDao(),
        database.streakDao()
    )

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val today = LocalDate.now()
                val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val sunday = monday.plusDays(6)

                val formatter = DateTimeFormatter.ISO_LOCAL_DATE
                val dailyMinutes = repository.getDailyMinutesInRange(
                    monday.format(formatter),
                    sunday.format(formatter)
                )

                val dayMinutesMap = dailyMinutes.associate { it.date to it.totalMinutes }
                var weekTotal = 0

                val weeklyData = (0..6).map { offset ->
                    val date = monday.plusDays(offset.toLong())
                    val dateStr = date.format(formatter)
                    val minutes = dayMinutesMap[dateStr] ?: 0
                    weekTotal += minutes
                    WeeklyDay(
                        dayLabel = date.dayOfWeek.getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            java.util.Locale.getDefault()
                        ).take(3),
                        date = dateStr,
                        minutes = minutes,
                        isToday = date == today
                    )
                }

                val totalMin = repository.totalMinutes.first() ?: 0
                val totalSess = repository.totalSessions.first() ?: 0

                _state.value = StatisticsState(
                    weeklyData = weeklyData,
                    totalMinutes = totalMin,
                    totalSessions = totalSess,
                    weeklyTotal = weekTotal,
                    averageDailyMinutes = if (weeklyData.isNotEmpty()) weekTotal / 7.0 else 0.0,
                    currentStreak = 0,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun refresh() {
        loadStatistics()
    }
}

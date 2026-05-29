package com.studyzen.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyzen.app.data.database.StreakEntity
import com.studyzen.app.data.database.StudyDatabase
import com.studyzen.app.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StreakState(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val todayMinutes: Int = 0,
    val totalMinutes: Int = 0,
    val totalSessions: Int = 0,
    val isTodayGoalMet: Boolean = false,
    val streakData: List<StreakEntity> = emptyList(),
    val isLoading: Boolean = true
)

class StreakViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(
        StudyDatabase.getInstance(application).sessionDao()
    )

    private val _state = MutableStateFlow(StreakState())
    val state: StateFlow<StreakState> = _state.asStateFlow()

    init {
        loadStreakData()
    }

    fun loadStreakData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val currentStreak = repository.getCurrentStreak()
                val bestStreak = repository.getBestStreak()
                val totalMinutes = repository.getTotalMinutes()
                val totalSessions = repository.getTotalSessions()
                val streaks = repository.getAllStreaks()

                val today = java.time.LocalDate.now()
                    .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val todayData = streaks.find { it.date == today }
                val todayMinutes = todayData?.totalMinutes ?: 0
                val isTodayGoalMet = todayData?.isGoalMet ?: false

                _state.value = StreakState(
                    currentStreak = currentStreak,
                    bestStreak = bestStreak,
                    todayMinutes = todayMinutes,
                    totalMinutes = totalMinutes,
                    totalSessions = totalSessions,
                    isTodayGoalMet = isTodayGoalMet,
                    streakData = streaks,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun refresh() {
        loadStreakData()
    }
}

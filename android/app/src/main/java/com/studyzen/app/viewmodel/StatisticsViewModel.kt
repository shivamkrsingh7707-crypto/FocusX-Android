package com.studyzen.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyzen.app.data.database.StudyDatabase
import com.studyzen.app.data.repository.StudyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatisticsState(
    val weeklyStats: List<Pair<String, Int>> = emptyList(),
    val monthlyStats: List<Pair<String, Int>> = emptyList(),
    val totalMinutes: Int = 0,
    val totalSessions: Int = 0,
    val averageDailyMinutes: Double = 0.0,
    val isLoading: Boolean = true
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(
        StudyDatabase.getInstance(application).sessionDao()
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
                val weekly = repository.getWeeklyStats()
                val monthly = repository.getMonthlyStats()
                val totalMin = repository.getTotalMinutes()
                val totalSess = repository.getTotalSessions()

                val avgDaily = if (weekly.isNotEmpty()) {
                    weekly.sumOf { it.second } / 7.0
                } else 0.0

                _state.value = StatisticsState(
                    weeklyStats = weekly,
                    monthlyStats = monthly,
                    totalMinutes = totalMin,
                    totalSessions = totalSess,
                    averageDailyMinutes = avgDaily,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

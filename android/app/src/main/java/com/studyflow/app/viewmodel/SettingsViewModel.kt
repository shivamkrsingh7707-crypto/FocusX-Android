package com.studyflow.app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val vibrationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val dailyGoalMinutes: Int = 25,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0
)

class SettingsViewModel : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun setVibrations(enabled: Boolean) {
        _state.value = _state.value.copy(vibrationsEnabled = enabled)
    }

    fun setSound(enabled: Boolean) {
        _state.value = _state.value.copy(soundEnabled = enabled)
    }

    fun setAutoStartBreaks(enabled: Boolean) {
        _state.value = _state.value.copy(autoStartBreaks = enabled)
    }

    fun setAutoStartFocus(enabled: Boolean) {
        _state.value = _state.value.copy(autoStartFocus = enabled)
    }

    fun setDailyGoal(minutes: Int) {
        _state.value = _state.value.copy(dailyGoalMinutes = minutes)
    }

    fun setReminder(enabled: Boolean, hour: Int = 9, minute: Int = 0) {
        _state.value = _state.value.copy(
            reminderEnabled = enabled,
            reminderHour = hour,
            reminderMinute = minute
        )
    }
}

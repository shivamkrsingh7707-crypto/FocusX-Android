package com.studyflow.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.studyflow.app.ui.theme.ThemeMode

data class SettingsState(
    val vibrationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val autoStartBreaks: Boolean = false,
    val autoStartFocus: Boolean = false,
    val dailyGoalMinutes: Int = 25,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

private const val PREFS_NAME = "studyflow_settings"
private const val KEY_THEME_MODE = "theme_mode"

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(
        SettingsState(
            themeMode = loadThemeMode()
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private fun loadThemeMode(): ThemeMode {
        val ordinal = prefs.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM.ordinal)
        return ThemeMode.entries.getOrNull(ordinal) ?: ThemeMode.SYSTEM
    }

    private fun saveThemeMode(mode: ThemeMode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode.ordinal).apply()
    }

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

    fun setThemeMode(mode: ThemeMode) {
        if (_state.value.themeMode == mode) return
        _state.value = _state.value.copy(themeMode = mode)
        saveThemeMode(mode)
    }
}

package dev.focusx.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.focusx.app.domain.AppState
import dev.focusx.app.domain.Settings
import dev.focusx.app.domain.Subject
import dev.focusx.app.domain.TimerPhase
import dev.focusx.app.domain.TimerState
import dev.focusx.app.domain.TimerStatus
import dev.focusx.app.sys.Haptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * One [ViewModel] to rule them all. The state graph is small enough that
 * a single MutableStateFlow feeding every screen is faster than
 * per-screen VMs (no per-screen data-store subscription overhead, no
 * dependency graph to wire up).
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = StateRepository(application)
    private val haptics = Haptics(application)

    private val _timer = MutableStateFlow(TimerState())
    val timer: StateFlow<TimerState> = _timer.asStateFlow()

    private var tickJob: Job? = null
    /** Wall-clock instant (ms) when the running timer should hit zero. */
    private var endsAt: Long = 0L

    val state: StateFlow<UiState> = combine(
        repo.state,
        _timer
    ) { snapshot, timer ->
        UiState(snapshot, timer)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UiState()
    )

    fun onTimerStart() {
        val current = _timer.value
        when (current.status) {
            TimerStatus.IDLE, TimerStatus.COMPLETED -> {
                endsAt = System.currentTimeMillis() + current.focusMinutes * 60_000L
                _timer.value = current.copy(
                    status = TimerStatus.RUNNING,
                    phase = TimerPhase.FOCUS,
                    remainingSeconds = current.focusMinutes * 60,
                    totalFocusSeconds = current.focusMinutes * 60
                )
                haptics.medium()
                startTicking()
            }
            TimerStatus.PAUSED -> {
                endsAt = System.currentTimeMillis() + current.remainingSeconds * 1000L
                _timer.value = current.copy(status = TimerStatus.RUNNING)
                haptics.medium()
                startTicking()
            }
            TimerStatus.RUNNING -> Unit
        }
    }

    fun onTimerPause() {
        if (_timer.value.status != TimerStatus.RUNNING) return
        tickJob?.cancel()
        _timer.value = _timer.value.copy(status = TimerStatus.PAUSED)
        haptics.light()
    }

    fun onTimerReset() {
        tickJob?.cancel()
        val focus = _timer.value.focusMinutes
        _timer.value = TimerState(
            focusMinutes = focus,
            breakMinutes = _timer.value.breakMinutes,
            totalFocusSeconds = focus * 60,
            remainingSeconds = focus * 60,
            activeSubjectId = _timer.value.activeSubjectId,
            sessionsToday = _timer.value.sessionsToday,
            totalFocusMinutesToday = _timer.value.totalFocusMinutesToday
        )
        haptics.light()
    }

    fun onSetFocusMinutes(minutes: Int) {
        if (_timer.value.status != TimerStatus.IDLE) return
        val m = minutes.coerceIn(5, 120)
        _timer.value = _timer.value.copy(
            focusMinutes = m,
            totalFocusSeconds = m * 60,
            remainingSeconds = m * 60
        )
    }

    fun onSetBreakMinutes(minutes: Int) {
        if (_timer.value.status != TimerStatus.IDLE) return
        _timer.value = _timer.value.copy(breakMinutes = minutes.coerceIn(1, 30))
    }

    fun onSelectSubject(id: String?) {
        if (_timer.value.status != TimerStatus.IDLE) return
        _timer.value = _timer.value.copy(activeSubjectId = id)
    }

    fun onAddSubject(name: String, colorIndex: Int, targetHours: Int) {
        if (name.isBlank()) return
        val id = java.util.UUID.randomUUID().toString()
        viewModelScope.launch {
            repo.upsertSubject(
                Subject(
                    id = id,
                    name = name.trim(),
                    colorIndex = colorIndex.coerceIn(0, 9),
                    targetHoursPerWeek = targetHours.coerceIn(1, 40)
                )
            )
        }
    }

    fun onDeleteSubject(id: String) {
        viewModelScope.launch { repo.deleteSubject(id) }
    }

    fun onSetThemeMode(mode: dev.focusx.app.domain.ThemeMode) {
        viewModelScope.launch { repo.setThemeMode(mode) }
    }

    fun onSetHaptics(enabled: Boolean) {
        viewModelScope.launch { repo.setHaptics(enabled) }
    }

    fun onSetSound(enabled: Boolean) {
        viewModelScope.launch { repo.setSound(enabled) }
    }

    fun onSetAutoStartBreak(enabled: Boolean) {
        viewModelScope.launch { repo.setAutoStartBreak(enabled) }
    }

    fun onSetAutoStartFocus(enabled: Boolean) {
        viewModelScope.launch { repo.setAutoStartFocus(enabled) }
    }

    fun onSetDailyGoal(minutes: Int) {
        viewModelScope.launch { repo.setDailyGoal(minutes) }
    }

    fun onSetReminder(enabled: Boolean, hour: Int = 9, minute: Int = 0) {
        viewModelScope.launch { repo.setReminder(enabled, hour, minute) }
    }

    /**
     * One coroutine, driven off the wall clock. Every iteration we
     * compute remaining = (endsAt - now) and update the state. The
     * coroutine is cancelled on pause/reset/complete; we re-spawn it
     * with a fresh `endsAt` on each start/resume.
     */
    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (_timer.value.status == TimerStatus.RUNNING) {
                val now = System.currentTimeMillis()
                val remainingMs = endsAt - now
                val remaining = (remainingMs / 1000L).toInt().coerceAtLeast(0)
                _timer.value = _timer.value.copy(remainingSeconds = remaining)
                if (remaining <= 0) {
                    handleComplete()
                    break
                }
                delay(250)
            }
        }
    }

    private suspend fun handleComplete() {
        val snapshot = _timer.value
        val phase = snapshot.phase
        val minutes = when (phase) {
            TimerPhase.FOCUS -> snapshot.focusMinutes
            TimerPhase.BREAK -> snapshot.breakMinutes
        }
        haptics.complete()

        viewModelScope.launch {
            repo.appendSession(
                subjectId = snapshot.activeSubjectId,
                minutes = minutes,
                phase = phase,
                startedAt = System.currentTimeMillis() - minutes * 60_000L
            )
        }

        val newTodayMin = if (phase == TimerPhase.FOCUS)
            snapshot.totalFocusMinutesToday + minutes
        else snapshot.totalFocusMinutesToday
        val newSess = if (phase == TimerPhase.FOCUS)
            snapshot.sessionsToday + 1
        else snapshot.sessionsToday

        _timer.value = snapshot.copy(
            status = TimerStatus.COMPLETED,
            remainingSeconds = 0,
            sessionsToday = newSess,
            totalFocusMinutesToday = newTodayMin
        )
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }

    /** Pair of (persisted snapshot, live timer) — what every screen reads. */
    data class UiState(
        val snapshot: AppState = AppState(),
        val timer: TimerState = TimerState()
    )

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AppViewModel(app) as T
            }
    }
}

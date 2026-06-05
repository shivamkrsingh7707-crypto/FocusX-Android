package com.studyflow.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyflow.app.data.database.StudyFlowDatabase
import com.studyflow.app.data.repository.StudyRepository
import com.studyflow.app.model.PomodoroState
import com.studyflow.app.model.TimerMode
import com.studyflow.app.model.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(
        StudyFlowDatabase.getInstance(application).subjectDao(),
        StudyFlowDatabase.getInstance(application).sessionDao(),
        StudyFlowDatabase.getInstance(application).streakDao()
    )

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var timerJob: Job? = null

    var onTimerStart: (() -> Unit)? = null
    var onTimerPause: (() -> Unit)? = null
    var onTimerComplete: (() -> Unit)? = null

    fun startTimer() {
        if (_state.value.timerState == TimerState.IDLE) {
            _state.value = _state.value.copy(
                remainingSeconds = _state.value.focusMinutes * 60,
                timerState = TimerState.RUNNING,
                timerMode = TimerMode.FOCUS,
                progress = 1f
            )
            onTimerStart?.invoke()
        }
        startTicking()
    }

    fun pauseTimer() {
        _state.value = _state.value.copy(timerState = TimerState.PAUSED)
        timerJob?.cancel()
        onTimerPause?.invoke()
    }

    fun resumeTimer() {
        _state.value = _state.value.copy(timerState = TimerState.RUNNING)
        startTicking()
    }

    fun resetTimer() {
        timerJob?.cancel()
        val focusMin = _state.value.focusMinutes
        _state.value = _state.value.copy(
            timerState = TimerState.IDLE,
            timerMode = TimerMode.FOCUS,
            remainingSeconds = focusMin * 60,
            progress = 1f,
            activeSubjectId = null
        )
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (_state.value.remainingSeconds > 0 && _state.value.timerState == TimerState.RUNNING) {
                delay(1000L)
                val current = _state.value
                val newRemaining = current.remainingSeconds - 1
                val totalSeconds = if (current.timerMode == TimerMode.FOCUS)
                    current.focusMinutes * 60 else current.breakMinutes * 60
                val newProgress = newRemaining.toFloat() / totalSeconds

                _state.value = current.copy(
                    remainingSeconds = newRemaining,
                    progress = newProgress
                )
            }

            if (_state.value.remainingSeconds <= 0) {
                handleTimerComplete()
            }
        }
    }

    private suspend fun handleTimerComplete() {
        val current = _state.value
        onTimerComplete?.invoke()

        if (current.timerMode == TimerMode.FOCUS) {
            val minutes = current.focusMinutes
            repository.recordSession(minutes, current.activeSubjectId)

            val newSessions = current.sessionsCompleted + 1
            val newTotal = current.totalFocusMinutes + minutes

            _state.value = current.copy(
                timerState = TimerState.COMPLETED,
                timerMode = TimerMode.BREAK,
                remainingSeconds = current.breakMinutes * 60,
                totalFocusMinutes = newTotal,
                sessionsCompleted = newSessions,
                progress = 1f
            )
        } else {
            _state.value = current.copy(
                timerState = TimerState.COMPLETED,
                timerMode = TimerMode.FOCUS,
                remainingSeconds = current.focusMinutes * 60,
                progress = 1f
            )
        }
    }

    fun setFocusMinutes(minutes: Int) {
        if (_state.value.timerState == TimerState.IDLE) {
            _state.value = _state.value.copy(
                focusMinutes = minutes,
                remainingSeconds = minutes * 60,
                progress = 1f
            )
        }
    }

    fun setBreakMinutes(minutes: Int) {
        if (_state.value.timerState == TimerState.IDLE) {
            _state.value = _state.value.copy(breakMinutes = minutes)
        }
    }

    fun setActiveSubject(subjectId: Long?) {
        if (_state.value.timerState == TimerState.IDLE) {
            _state.value = _state.value.copy(activeSubjectId = subjectId)
        }
    }

    fun applyPreset(focus: Int, breakDuration: Int) {
        if (_state.value.timerState == TimerState.IDLE) {
            _state.value = _state.value.copy(
                focusMinutes = focus,
                breakMinutes = breakDuration,
                remainingSeconds = focus * 60,
                progress = 1f
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

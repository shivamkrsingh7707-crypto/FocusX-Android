package com.studyzen.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studyzen.app.data.database.StudyDatabase
import com.studyzen.app.data.repository.StudyRepository
import com.studyzen.app.utils.showSessionCompleteNotification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerState {
    IDLE, RUNNING, PAUSED, COMPLETED
}

enum class TimerMode {
    FOCUS, BREAK
}

data class PomodoroState(
    val timerState: TimerState = TimerState.IDLE,
    val timerMode: TimerMode = TimerMode.FOCUS,
    val remainingSeconds: Int = 25 * 60,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val totalFocusMinutes: Int = 0,
    val sessionsCompleted: Int = 0,
    val progress: Float = 1f
)

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudyRepository(
        StudyDatabase.getInstance(application).sessionDao()
    )

    private val _state = MutableStateFlow(PomodoroState())
    val state: StateFlow<PomodoroState> = _state.asStateFlow()

    private var timerJob: Job? = null

    private var onTimerStart: (() -> Unit)? = null
    private var onTimerPause: (() -> Unit)? = null
    private var onTimerComplete: (() -> Unit)? = null

    fun setOnStartCallback(cb: () -> Unit) { onTimerStart = cb }
    fun setOnPauseCallback(cb: () -> Unit) { onTimerPause = cb }
    fun setOnCompleteCallback(cb: () -> Unit) { onTimerComplete = cb }

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
            progress = 1f
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
                onTimerCompleteInternal()
            }
        }
    }

    private suspend fun onTimerCompleteInternal() {
        val current = _state.value
        onTimerComplete?.invoke()

        if (current.timerMode == TimerMode.FOCUS) {
            val minutes = current.focusMinutes
            repository.recordSession(minutes)
            showSessionCompleteNotification(getApplication(), minutes)

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

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

package dev.focusx.app.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service that tracks study session duration using absolute
 * wall-clock timestamps ([System.currentTimeMillis]).  Because the
 * elapsed time is derived from real-time epoch timestamps rather than
 * counting delay-loop iterations, Android's Doze mode cannot cause
 * drift: even if the tick coroutine is deferred by the system, the
 * computed elapsed time remains exactly correct.
 *
 * Commands are accepted via [Intent] actions:
 *   [ACTION_START]   – begin or resume a session
 *   [ACTION_PAUSE]   – pause an active session
 *   [ACTION_RESET]   – cancel the current session
 *
 * The UI observes [elapsedMs] and [serviceState] via the returned
 * [TimerBinder] after binding with [bindService].
 */
class StudyTimerService : Service() {

    // ── Intent actions ────────────────────────────────────────────────────

    companion object {
        const val ACTION_START = "dev.focusx.app.action.START_TIMER"
        const val ACTION_PAUSE = "dev.focusx.app.action.PAUSE_TIMER"
        const val ACTION_RESUME = "dev.focusx.app.action.RESUME_TIMER"
        const val ACTION_RESET = "dev.focusx.app.action.RESET_TIMER"

        const val EXTRA_DURATION_MS = "extra_duration_ms"
        const val EXTRA_SUBJECT_ID = "extra_subject_id"

        fun startIntent(
            durationMs: Long,
            subjectId: String? = null
        ): Intent = Intent(ACTION_START).apply {
            putExtra(EXTRA_DURATION_MS, durationMs)
            subjectId?.let { putExtra(EXTRA_SUBJECT_ID, it) }
        }

        fun pauseIntent() = Intent(ACTION_PAUSE)

        fun resumeIntent() = Intent(ACTION_RESUME)

        fun resetIntent() = Intent(ACTION_RESET)
    }

    enum class ServiceState { IDLE, RUNNING, PAUSED, COMPLETED }

    // ── Binder ────────────────────────────────────────────────────────────

    inner class TimerBinder : Binder() {
        fun getService(): StudyTimerService = this@StudyTimerService
    }

    private val binder = TimerBinder()
    private lateinit var notificationHelper: TimerNotificationHelper

    // ── Observable state ──────────────────────────────────────────────────

    private val _elapsedMs = MutableStateFlow(0L)
    val elapsedMs: StateFlow<Long> = _elapsedMs.asStateFlow()

    private val _serviceState = MutableStateFlow(ServiceState.IDLE)
    val serviceState: StateFlow<ServiceState> = _serviceState.asStateFlow()

    private val _activeSubjectId = MutableStateFlow<String?>(null)
    val activeSubjectId: StateFlow<String?> = _activeSubjectId.asStateFlow()

    // ── Internal timer bookkeeping ────────────────────────────────────────

    private var startWallClockMs: Long = 0L
    private var accumulatedMs: Long = 0L
    private var focusDurationMs: Long = 0L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        notificationHelper = TimerNotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_RESET -> handleReset()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    // ── Command handlers ──────────────────────────────────────────────────

    private fun handleStart(intent: Intent) {
        focusDurationMs = intent.getLongExtra(EXTRA_DURATION_MS, 25 * 60 * 1000L)
        _activeSubjectId.value = intent.getStringExtra(EXTRA_SUBJECT_ID)
        startWallClockMs = System.currentTimeMillis()
        accumulatedMs = 0L
        _elapsedMs.value = 0L
        _serviceState.value = ServiceState.RUNNING

        val notification = notificationHelper.buildNotification(
            elapsedMs = 0L,
            focusDurationMs = focusDurationMs,
            subjectName = null,
            isRunning = true
        )
        startForeground(TimerNotificationHelper.NOTIFICATION_ID, notification)
        startTicking()
    }

    private fun handlePause() {
        if (_serviceState.value != ServiceState.RUNNING) return
        accumulatedMs += System.currentTimeMillis() - startWallClockMs
        _serviceState.value = ServiceState.PAUSED
        tickJob?.cancel()
        pushStateAndNotification()
    }

    private fun handleResume() {
        if (_serviceState.value != ServiceState.PAUSED) return
        startWallClockMs = System.currentTimeMillis()
        _serviceState.value = ServiceState.RUNNING
        startTicking()
    }

    private fun handleReset() {
        tickJob?.cancel()
        accumulatedMs = 0L
        startWallClockMs = 0L
        focusDurationMs = 0L
        _elapsedMs.value = 0L
        _serviceState.value = ServiceState.IDLE
        _activeSubjectId.value = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Tick loop ─────────────────────────────────────────────────────────

    /**
     * Launches a coroutine that reads the wall clock every ~250 ms,
     * computes the true elapsed time, and pushes updates to the
     * [elapsedMs] StateFlow and the foreground notification.
     *
     * Because elapsed = [accumulatedMs] + (now – [startWallClockMs]),
     * the value is always anchored to absolute time irrespective of how
     * often the coroutine actually runs.
     */
    private fun startTicking() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive && _serviceState.value == ServiceState.RUNNING) {
                val now = System.currentTimeMillis()
                val elapsed = accumulatedMs + (now - startWallClockMs)
                val clamped = elapsed.coerceAtMost(focusDurationMs)
                _elapsedMs.value = clamped

                if (clamped >= focusDurationMs) {
                    onTimerCompleted()
                    break
                }

                updateNotification()
                delay(250L)
            }
        }
    }

    private fun onTimerCompleted() {
        _elapsedMs.value = focusDurationMs
        _serviceState.value = ServiceState.COMPLETED
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Notification ──────────────────────────────────────────────────────

    private fun pushStateAndNotification() {
        updateNotification()
    }

    private fun updateNotification() {
        val notification = notificationHelper.buildNotification(
            elapsedMs = _elapsedMs.value,
            focusDurationMs = focusDurationMs,
            subjectName = null,
            isRunning = _serviceState.value == ServiceState.RUNNING
        )
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(TimerNotificationHelper.NOTIFICATION_ID, notification)
    }
}

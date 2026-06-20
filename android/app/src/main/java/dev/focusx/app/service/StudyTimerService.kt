package dev.focusx.app.service

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import dev.focusx.app.FocusXApplication
import dev.focusx.app.data.local.entity.StudySessionEntity
import dev.focusx.app.data.repository.StudyRepository
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
import kotlinx.coroutines.withContext

class StudyTimerService : Service() {

    // ── Intent actions ────────────────────────────────────────────────────

    companion object {
        const val ACTION_START = "dev.focusx.app.action.START_TIMER"
        const val ACTION_PAUSE = "dev.focusx.app.action.PAUSE_TIMER"
        const val ACTION_RESUME = "dev.focusx.app.action.RESUME_TIMER"
        const val ACTION_RESET = "dev.focusx.app.action.RESET_TIMER"

        const val EXTRA_DURATION_MS = "extra_duration_ms"
        const val EXTRA_SUBJECT_ID = "extra_subject_id"
        const val EXTRA_SUBJECT_NAME = "extra_subject_name"

        fun startIntent(
            durationMs: Long,
            subjectId: String? = null,
            subjectName: String? = null
        ): Intent = Intent(ACTION_START).apply {
            putExtra(EXTRA_DURATION_MS, durationMs)
            subjectId?.let { putExtra(EXTRA_SUBJECT_ID, it) }
            subjectName?.let { putExtra(EXTRA_SUBJECT_NAME, it) }
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
    private lateinit var roomRepo: StudyRepository

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
    private var subjectName: String? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null

    // ── Lifecycle ─────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        notificationHelper = TimerNotificationHelper(this)
        val db = (applicationContext as FocusXApplication).database
        roomRepo = StudyRepository(
            db.subjectDao(), db.sessionDao(), db.gradeDao(), db.studySessionDao()
        )
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
        subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME)
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
        subjectName = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ── Tick loop ─────────────────────────────────────────────────────────

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

        scope.launch {
            try {
                val name = subjectName ?: "General"
                withContext(Dispatchers.IO) {
                    roomRepo.insertStudySession(
                        StudySessionEntity(
                            subjectName = name,
                            durationMinutes = focusDurationMs / 60_000L,
                            timestamp = startWallClockMs
                        )
                    )
                }
                scheduleAlarm()
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun scheduleAlarm() {
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val showIntent = Intent(this, dev.focusx.app.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        alarmMgr.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                System.currentTimeMillis(),
                PendingIntent.getActivity(
                    this, 0, showIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            ),
            pendingIntent
        )
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

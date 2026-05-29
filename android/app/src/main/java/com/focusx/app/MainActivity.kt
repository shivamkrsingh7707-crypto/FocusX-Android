package com.focusx.app

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.focusx.app.databinding.ActivityMainBinding
import com.focusx.app.ui.settings.SettingsBottomSheet

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private enum class TimerState { IDLE, RUNNING, PAUSED, COMPLETED }
    private var timerState = TimerState.IDLE
    private var countDownTimer: CountDownTimer? = null
    private var remainingMillis: Long = 25 * 60 * 1000L
    private val totalMillis: Long = 25 * 60 * 1000L
    private var totalFocusSeconds = 0L
    private var completedSessions = 0

    private var vibrator: Vibrator? = null
    private var hapticEnabled = true
    private var soundEnabled = true
    private var auraEnabled = true
    private var strictModeEnabled = false

    private val categories = arrayOf(
        "Studying: Math", "Studying: Physics", "Studying: Coding",
        "Working: Design", "Working: Writing", "Reading: General"
    )
    private var selectedCategoryIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHardware()
        setupBottomNavigation()
        setupClickAnimations()
        updateTimerDisplay()
        updateProgress()
        updateStats()
        updateCategoryBadge()
    }

    private fun setupHardware() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_timer -> true
                R.id.navigation_progress -> {
                    Toast.makeText(this, "Progress analytics coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.navigation_tests -> {
                    Toast.makeText(this, "Tests module coming soon", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.navigation_timer
    }

    private fun setupClickAnimations() {
        val views = listOfNotNull(
            binding.startButton, binding.pauseButton, binding.resetButton,
            binding.settingsButton, binding.themeToggle, binding.categoryBadge
        )
        for (v in views) {
            v.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start()
                        triggerMicroHaptic()
                        false
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                        view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                        false
                    }
                    else -> false
                }
            }
        }
    }

    fun onStartClick(view: View) {
        triggerMicroHaptic()
        when (timerState) {
            TimerState.IDLE, TimerState.PAUSED, TimerState.COMPLETED -> startTimer()
            TimerState.RUNNING -> {
                if (strictModeEnabled) {
                    Toast.makeText(this, "Session already active", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onPauseClick(view: View) {
        triggerMicroHaptic()
        if (timerState == TimerState.RUNNING) pauseTimer()
    }

    fun onResetClick(view: View) {
        triggerMicroHaptic()
        if (timerState == TimerState.RUNNING || timerState == TimerState.PAUSED) {
            if (strictModeEnabled && timerState == TimerState.RUNNING) {
                Toast.makeText(this, "Strict Deep Focus active — cannot reset", Toast.LENGTH_SHORT).show()
                return
            }
            resetTimer()
        }
    }

    fun onSettingsClick(view: View) {
        triggerMicroHaptic()
        val sheet = SettingsBottomSheet.newInstance(hapticEnabled, soundEnabled, auraEnabled, strictModeEnabled)
        sheet.onSettingsChanged = { h, s, a, st ->
            hapticEnabled = h; soundEnabled = s; auraEnabled = a; strictModeEnabled = st
        }
        sheet.show(supportFragmentManager, "SettingsBottomSheet")
    }

    fun onThemeToggleClick(view: View) {
        triggerMicroHaptic()
        Toast.makeText(this, "Theme toggle (build variants)", Toast.LENGTH_SHORT).show()
    }

    fun onCategoryClick(view: View) {
        triggerMicroHaptic()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setAdapter(adapter) { _, which ->
                selectedCategoryIndex = which
                updateCategoryBadge()
            }
            .show()
    }

    private fun updateCategoryBadge() {
        binding.categoryBadge.text = categories[selectedCategoryIndex]
    }

    private fun startTimer() {
        timerState = TimerState.RUNNING
        morphToActiveState()

        val millisToRun = if (remainingMillis > 0) remainingMillis else totalMillis
        countDownTimer = object : CountDownTimer(millisToRun, 100L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingMillis = millisUntilFinished
                updateTimerDisplay()
                updateProgress()
            }

            override fun onFinish() {
                remainingMillis = 0L
                timerState = TimerState.COMPLETED
                updateTimerDisplay()
                updateProgress()
                onTimerComplete()
            }
        }.start()

        playStartChime()
        triggerStartHaptic()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerState = TimerState.PAUSED
        morphToIdleState()
        playPauseChime()
        triggerPauseHaptic()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timerState = TimerState.IDLE
        remainingMillis = totalMillis
        morphToIdleState()
        updateTimerDisplay()
        updateProgress()
    }

    private fun onTimerComplete() {
        totalFocusSeconds += totalMillis / 1000L
        completedSessions++
        updateStats()
        morphToIdleState()
        playCompleteChime()
        triggerCompleteHaptic()
        Toast.makeText(this, "Session complete! Great focus.", Toast.LENGTH_LONG).show()
    }

    private fun updateTimerDisplay() {
        val totalSecs = (remainingMillis / 1000L).toInt()
        binding.timerDisplay.text = String.format("%02d:%02d", totalSecs / 60, totalSecs % 60)
        binding.timerStateLabel.apply {
            when (timerState) {
                TimerState.IDLE -> { text = "Ready"; visibility = View.GONE }
                TimerState.RUNNING -> { text = "Focusing"; visibility = View.VISIBLE }
                TimerState.PAUSED -> { text = "Paused"; visibility = View.VISIBLE }
                TimerState.COMPLETED -> { text = "Complete!"; visibility = View.VISIBLE }
            }
        }
    }

    private fun updateProgress() {
        val progress = ((totalMillis - remainingMillis) * 100 / totalMillis).toInt()
        binding.timerProgress.progress = progress
    }

    private fun updateStats() {
        val mins = totalFocusSeconds / 60
        binding.todaysStudyValue.text = "$mins min"
        val score = if (completedSessions > 0) (completedSessions * 25).coerceAtMost(100) else 0
        binding.focusScoreValue.text = "$score%"
    }

    private fun morphToActiveState() {
        binding.startButton.visibility = View.GONE
        binding.activeControls.visibility = View.VISIBLE
        binding.activeControls.apply {
            alpha = 0f
            scaleX = 0.85f
            scaleY = 0.85f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(250)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    private fun morphToIdleState() {
        if (binding.activeControls.visibility != View.VISIBLE) {
            binding.startButton.visibility = View.VISIBLE
            return
        }
        binding.activeControls.animate()
            .alpha(0f).scaleX(0.85f).scaleY(0.85f)
            .setDuration(180)
            .withEndAction {
                binding.activeControls.visibility = View.GONE
                binding.startButton.apply {
                    visibility = View.VISIBLE
                    alpha = 0f
                    scaleX = 0.85f
                    scaleY = 0.85f
                    animate()
                        .alpha(1f).scaleX(1f).scaleY(1f)
                        .setDuration(250)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
            }
            .start()
    }

    // ─── Synthesized Audio Chimes (ToneGenerator API) ────────────────────────

    private fun playStartChime() {
        if (!soundEnabled) return
        Thread {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
            try {
                tg.startTone(ToneGenerator.TONE_DTMF_1, 140)
                sleep(150)
                tg.startTone(ToneGenerator.TONE_DTMF_3, 200)
                sleep(210)
            } finally {
                tg.release()
            }
        }.apply { isDaemon = true; start() }
    }

    private fun playPauseChime() {
        if (!soundEnabled) return
        Thread {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 35)
            try {
                tg.startTone(ToneGenerator.TONE_PROP_NACK, 200)
                sleep(210)
            } finally {
                tg.release()
            }
        }.apply { isDaemon = true; start() }
    }

    private fun playCompleteChime() {
        if (!soundEnabled) return
        Thread {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
            try {
                tg.startTone(ToneGenerator.TONE_DTMF_1, 160)
                sleep(170)
                tg.startTone(ToneGenerator.TONE_DTMF_3, 160)
                sleep(170)
                tg.startTone(ToneGenerator.TONE_DTMF_5, 160)
                sleep(170)
                tg.startTone(ToneGenerator.TONE_DTMF_8, 500)
                sleep(510)
            } finally {
                tg.release()
            }
        }.apply { isDaemon = true; start() }
    }

    @Suppress("SameParameterValue")
    private fun sleep(ms: Long) {
        try { Thread.sleep(ms) } catch (_: InterruptedException) { }
    }

    // ─── Haptic Feedback ──────────────────────────────────────────────────────

    private fun triggerMicroHaptic() {
        if (!hapticEnabled || vibrator == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator!!.vibrate(VibrationEffect.createOneShot(6, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun triggerStartHaptic() {
        if (!hapticEnabled || vibrator == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator!!.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 25, 30, 35),
                    intArrayOf(0, 220, 0, 200),
                    -1
                )
            )
        }
    }

    private fun triggerPauseHaptic() {
        if (!hapticEnabled || vibrator == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator!!.vibrate(VibrationEffect.createOneShot(15, 100))
        }
    }

    private fun triggerCompleteHaptic() {
        if (!hapticEnabled || vibrator == null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator!!.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 40, 50, 40, 70, 60, 90),
                    intArrayOf(0, 255, 0, 200, 0, 255, 0, 255),
                    -1
                )
            )
        }
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}

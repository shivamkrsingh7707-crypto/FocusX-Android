package com.studyflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyflow.app.ui.navigation.StudyFlowNavigation
import com.studyflow.app.ui.theme.StudyFlowTheme
import com.studyflow.app.ui.theme.ThemeMode
import com.studyflow.app.ui.theme.ThemeRevealOverlay
import com.studyflow.app.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyHighRefreshRate()

        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }

        requestNotificationPermission()

        setContent {
            StudyFlowApp()
        }
    }

    /**
     * Asks the system for the smoothest display mode available (90/120 Hz on
     * supported devices). Falls back to a 90Hz hint which Android maps to the
     * closest supported mode.
     */
    private fun applyHighRefreshRate() {
        try {
            val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                window.windowManager.defaultDisplay
            }
            val bestMode = display?.supportedModes?.maxByOrNull { it.refreshRate }
            if (bestMode != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val params = window.attributes
                params.preferredDisplayModeId = bestMode.modeId
                window.attributes = params
            } else {
                @Suppress("DEPRECATION")
                val params = window.attributes
                params.preferredRefreshRate = 90f
                window.attributes = params
            }
        } catch (_: Throwable) {
            // best-effort; if the device won't honor it, the default rate is used
            @Suppress("DEPRECATION")
            window.attributes = window.attributes.apply {
                preferredRefreshRate = 90f
            }
        }
        // Hint the framework for a high refresh rate even when a specific mode
        // can't be picked (older devices).
        runCatching {
            window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
    }
}

@androidx.compose.runtime.Composable
private fun StudyFlowApp() {
    val settingsViewModel: SettingsViewModel = viewModel()
    val state by settingsViewModel.state.collectAsState()

    val isDark = when (state.themeMode) {
        ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Track the tap origin so the reveal animation can grow from where the
    // user actually pressed.
    var revealActive by remember { mutableStateOf(false) }
    var revealOrigin by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxSize()) {
        StudyFlowTheme(themeMode = state.themeMode) {
            StudyFlowNavigation(
                settingsViewModel = settingsViewModel,
                onThemeToggle = { origin ->
                    revealOrigin = origin
                    revealActive = true
                }
            )
        }

        ThemeRevealOverlay(
            active = revealActive,
            origin = revealOrigin,
            isTargetDark = isDark,
            onFinished = { revealActive = false }
        )
    }
}

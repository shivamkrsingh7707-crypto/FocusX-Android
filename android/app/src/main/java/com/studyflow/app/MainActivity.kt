package com.studyflow.app

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Display
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        installSplashScreen()
        enableEdgeToEdge()
        applyHighRefreshRate()
        hintSustainedPerformance()

        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 60)
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }

        requestNotificationPermission()

        setContent { StudyFlowApp() }
    }

    /**
     * Asks the system for the smoothest display mode available. We try three
     * strategies in order, all targeting >= 120 Hz:
     *
     *  1. Pick the [android.view.Display.Mode] with the highest refresh rate
     *     the activity's display reports (works since API 23, honours per-mode
     *     resolution combos like 1080p@120 on phones that need it).
     *  2. Fall back to a 120 Hz hint on older devices. The system maps the
     *     hint to the closest supported mode, so it still picks 90/120 if
     *     available.
     *  3. Mark the window hardware-accelerated and ask for sustained
     *     performance mode so the SoC stays in the high-clock rail.
     */
    private fun applyHighRefreshRate() {
        val display: Display? = currentDisplay()

        val params = window.attributes
        val picked = pickBestMode(display)
        if (picked != null) {
            params.preferredDisplayModeId = picked
        } else {
            @Suppress("DEPRECATION")
            params.preferredRefreshRate = TARGET_REFRESH_HZ
        }
        window.attributes = params

        runCatching { window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) }
    }

    private fun currentDisplay(): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            @Suppress("DEPRECATION")
            window.windowManager.defaultDisplay
        }
    }

    private fun pickBestMode(display: Display?): Int? {
        if (display == null) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        val modes = display.supportedModes
        val best = modes.maxByOrNull { it.refreshRate } ?: return null
        // Only honour the request if it would actually give us >= 90Hz. The
        // system can still down-clock to 60Hz in low-battery / always-on
        // scenarios, which is fine.
        return if (best.refreshRate >= 90f) best.modeId else null
    }

    private fun hintSustainedPerformance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            runCatching { window.setSustainedPerformanceMode(true) }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) return
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIF_PERMISSION_REQUEST)
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
    }

    private companion object {
        const val TARGET_REFRESH_HZ = 120f
        const val NOTIF_PERMISSION_REQUEST = 1001
    }
}

@Composable
private fun StudyFlowApp() {
    val settingsViewModel: SettingsViewModel = viewModel()
    val state by settingsViewModel.state.collectAsStateWithLifecycle()

    val isDark = when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

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

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
        enableEdgeToEdge()
        applyHighRefreshRate()

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
     * Asks the system for the smoothest display mode available (90/120 Hz on
     * supported devices). Falls back to a 90Hz hint which Android maps to the
     * closest supported mode.
     */
    private fun applyHighRefreshRate() {
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            @Suppress("DEPRECATION")
            window.windowManager.defaultDisplay
        }

        val params = window.attributes
        display?.supportedModes
            ?.maxByOrNull { it.refreshRate }
            ?.takeIf { Build.VERSION.SDK_INT >= Build.VERSION_CODES.M }
            ?.let { params.preferredDisplayModeId = it.modeId }
            ?: run { @Suppress("DEPRECATION") params.preferredRefreshRate = TARGET_REFRESH_HZ }
        window.attributes = params

        runCatching { window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) }
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
        const val TARGET_REFRESH_HZ = 90f
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

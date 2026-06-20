package dev.focusx.app

import android.os.Build
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.focusx.app.data.AppViewModel
import dev.focusx.app.sys.FrameTiming
import dev.focusx.app.ui.AppNavigation
import dev.focusx.app.ui.theme.FocusXTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        applyHighRefreshRate()
        FrameTiming.attach(window)

        setContent {
            val appVm: AppViewModel = viewModel(factory = AppViewModel.factory(application))
            val ui by appVm.state.collectAsStateWithLifecycle()

            FocusXTheme(themeMode = ui.snapshot.settings.themeMode) {
                AppNavigation(
                    ui = ui,
                    onStart = appVm::onTimerStart,
                    onPause = appVm::onTimerPause,
                    onReset = appVm::onTimerReset,
                    onSkip = { appVm.onTimerReset() },
                    onSelectSubject = appVm::onSelectSubject,
                    onAddSubject = appVm::onAddSubject,
                    onDeleteSubject = appVm::onDeleteSubject,
                    onThemeMode = appVm::onSetThemeMode,
                    onHaptics = appVm::onSetHaptics,
                    onSound = appVm::onSetSound,
                    onAutoStartBreak = appVm::onSetAutoStartBreak,
                    onAutoStartFocus = appVm::onSetAutoStartFocus,
                    onDailyGoal = appVm::onSetDailyGoal,
                    onReminder = appVm::onSetReminder
                )
            }
        }
    }

    /**
     * Try the [Display.Mode] with the highest refresh rate the panel
     * actually reports; otherwise hint a target of 120 Hz. We only do
     * this once — the system may throttle later for thermal reasons
     * and that's fine; we just don't want to *force* 60 Hz by asking
     * for a mode we never picked.
     */
    private fun applyHighRefreshRate() {
        val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display
        } else {
            @Suppress("DEPRECATION")
            window.windowManager.defaultDisplay
        }

        val params = window.attributes
        val picked = pickBestMode(display)
        if (picked != null) {
            params.preferredDisplayModeId = picked
        } else {
            @Suppress("DEPRECATION")
            params.preferredRefreshRate = TARGET_REFRESH_HZ
        }
        window.attributes = params
    }

    private fun pickBestMode(display: Display?): Int? {
        if (display == null) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        val best = display.supportedModes.maxByOrNull { it.refreshRate } ?: return null
        return if (best.refreshRate >= 90f) best.modeId else null
    }

    private companion object {
        const val TARGET_REFRESH_HZ = 120f
    }
}

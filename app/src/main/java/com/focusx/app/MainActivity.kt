package com.focusx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focusx.app.audio.AudioEngine
import com.focusx.app.data.*
import com.focusx.app.ui.components.FocusXBottomNav
import com.focusx.app.ui.components.FocusXHeader
import com.focusx.app.ui.components.SettingsSheet
import com.focusx.app.ui.screens.ProgressScreen
import com.focusx.app.ui.screens.TestsScreen
import com.focusx.app.ui.screens.TimerScreen
import com.focusx.app.ui.theme.FocusXTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var audioEngine: AudioEngine
    private lateinit var repository: StateRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioEngine = AudioEngine(applicationContext)
        repository = StateRepository(applicationContext)

        setContent {
            FocusXApp(repository, audioEngine)
        }
    }

    override fun onDestroy() {
        audioEngine.release()
        super.onDestroy()
    }
}

enum class Tab { Timer, Progress, Tests }

@Composable
fun FocusXApp(repository: StateRepository, audioEngine: AudioEngine) {
    var state by remember { mutableStateOf(repository.load()) }
    var activeTab by remember { mutableStateOf(Tab.Timer) }
    var showSettings by remember { mutableStateOf(false) }
    var ambientMode by remember { mutableStateOf("none") }

    val scope = rememberCoroutineScope()

    FocusXTheme(darkTheme = state.darkMode) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 72.dp),
            ) {
                FocusXHeader(
                    streak = calculateStreak(state.sessions),
                    isDark = state.darkMode,
                    onThemeToggle = {
                        state = state.copy(darkMode = !state.darkMode)
                        scope.launch(Dispatchers.IO) { repository.save(state) }
                        audioEngine.haptic("tap", state.prefs.haptics)
                    },
                    onSettingsClick = { showSettings = true },
                )

                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(200))
                        },
                        label = "tabContent",
                    ) { tab ->
                        when (tab) {
                            Tab.Timer -> TimerScreen(
                                state = state,
                                ambientMode = ambientMode,
                                onStateUpdate = { newState ->
                                    state = newState
                                    scope.launch(Dispatchers.IO) { repository.save(newState) }
                                },
                                onAmbientChange = { mode ->
                                    ambientMode = mode
                                    audioEngine.startAmbient(mode, state.prefs.ambient)
                                },
                                audioEngine = audioEngine,
                            )
                            Tab.Progress -> ProgressScreen(state = state)
                            Tab.Tests -> TestsScreen(
                                state = state,
                                onStateUpdate = { newState ->
                                    state = newState
                                    scope.launch(Dispatchers.IO) { repository.save(newState) }
                                },
                            )
                        }
                    }
                }
            }

            FocusXBottomNav(
                activeTab = activeTab,
                onTabSelect = {
                    activeTab = it
                    audioEngine.haptic("tap", state.prefs.haptics)
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            if (showSettings) {
                SettingsSheet(
                    state = state,
                    onDismiss = { showSettings = false },
                    onStateUpdate = { newState ->
                        state = newState
                        scope.launch(Dispatchers.IO) { repository.save(newState) }
                    },
                    audioEngine = audioEngine,
                )
            }
        }
    }
}

package dev.focusx.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import dev.focusx.app.data.AppViewModel
import dev.focusx.app.domain.ThemeMode
import dev.focusx.app.ui.components.FloatingBottomNav
import dev.focusx.app.ui.screens.HomeScreen
import dev.focusx.app.ui.screens.SettingsScreen
import dev.focusx.app.ui.screens.StatsScreen
import dev.focusx.app.ui.screens.SubjectsScreen
import dev.focusx.app.ui.theme.FocusXTheme
import dev.focusx.app.ui.theme.ThemeRevealOverlay

/**
 * Screen-to-screen transition: short cross-fade + a hair of scale, so
 * the screen "lands" rather than just blinking in. The two transforms
 * are symmetric so going back feels just as smooth as going forward.
 */
private val ScreenTransition: ContentTransform = ContentTransform(
    targetContentEnter = fadeIn(tween(220)) + scaleIn(
        initialScale = 0.985f,
        animationSpec = tween(260)
    ),
    initialContentExit = fadeOut(tween(160)) + scaleOut(
        targetScale = 0.998f,
        animationSpec = tween(160)
    )
)

private const val ROUTE_HOME = "home"
private const val ROUTE_SUBJECTS = "subjects"
private const val ROUTE_STATS = "stats"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun AppNavigation(
    ui: AppViewModel.UiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onSelectSubject: (String?) -> Unit,
    onAddSubject: (String, Int, Int) -> Unit,
    onDeleteSubject: (String) -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onAutoStartBreak: (Boolean) -> Unit,
    onAutoStartFocus: (Boolean) -> Unit,
    onDailyGoal: (Int) -> Unit,
    onReminder: (Boolean) -> Unit
) {
    val theme = FocusXTheme.colors
    val systemDark = isSystemInDarkTheme()
    var current by rememberSaveable { mutableStateOf(ROUTE_HOME) }
    var revealActive by remember { mutableStateOf(false) }
    var revealOrigin by remember { mutableStateOf(Offset.Zero) }
    var revealTargetDark by remember { mutableStateOf(true) }

    val isTargetDark = when (ui.snapshot.settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    Scaffold(
        containerColor = theme.background,
        contentColor = theme.onBackground,
        bottomBar = {
            FloatingBottomNav(
                currentRoute = current,
                onSelect = { current = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(theme.background)
        ) {
            AnimatedContent(
                targetState = current,
                transitionSpec = { ScreenTransition },
                label = "screenSwap"
            ) { route ->
                when (route) {
                    ROUTE_HOME -> HomeScreen(
                        ui = ui,
                        onStart = onStart,
                        onPause = onPause,
                        onReset = onReset,
                        onSkip = onSkip,
                        onSelectSubject = onSelectSubject,
                        onOpenSubjects = { current = ROUTE_SUBJECTS },
                        onOpenStats = { current = ROUTE_STATS },
                        onAddSubject = { current = ROUTE_SUBJECTS },
                        modifier = Modifier.fillMaxSize()
                    )
                    ROUTE_SUBJECTS -> SubjectsScreen(
                        ui = ui,
                        onAdd = onAddSubject,
                        onDelete = onDeleteSubject,
                        onSelect = onSelectSubject,
                        activeSubjectId = ui.timer.activeSubjectId,
                        modifier = Modifier.fillMaxSize()
                    )
                    ROUTE_STATS -> StatsScreen(
                        ui = ui,
                        modifier = Modifier.fillMaxSize()
                    )
                    ROUTE_SETTINGS -> SettingsScreen(
                        ui = ui,
                        onThemeMode = { mode ->
                            // Start the reveal BEFORE the theme flips,
                            // so the new theme is already on screen
                            // when the hole expands over the old one.
                            revealTargetDark = when (mode) {
                                ThemeMode.SYSTEM -> systemDark
                                ThemeMode.LIGHT -> false
                                ThemeMode.DARK -> true
                            }
                            revealActive = true
                            onThemeMode(mode)
                        },
                        onHaptics = onHaptics,
                        onSound = onSound,
                        onAutoStartBreak = onAutoStartBreak,
                        onAutoStartFocus = onAutoStartFocus,
                        onDailyGoal = onDailyGoal,
                        onReminder = onReminder,
                        onThemeOrigin = { o -> revealOrigin = o },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            ThemeRevealOverlay(
                active = revealActive,
                origin = revealOrigin,
                isTargetDark = revealTargetDark,
                onFinished = { revealActive = false }
            )
        }
    }
}

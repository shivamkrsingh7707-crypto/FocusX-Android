package com.studyflow.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.studyflow.app.model.AppScreen
import com.studyflow.app.ui.components.BottomNavBar
import com.studyflow.app.ui.screens.DashboardScreen
import com.studyflow.app.ui.screens.SettingsScreen
import com.studyflow.app.ui.screens.StatisticsScreen
import com.studyflow.app.ui.screens.SubjectsScreen
import com.studyflow.app.ui.screens.TimerScreen
import com.studyflow.app.viewmodel.SettingsViewModel
import com.studyflow.app.viewmodel.StatisticsViewModel
import com.studyflow.app.viewmodel.SubjectViewModel
import com.studyflow.app.viewmodel.TimerViewModel

@Composable
fun StudyFlowNavigation() {
    val timerViewModel: TimerViewModel = viewModel()
    val subjectViewModel: SubjectViewModel = viewModel()
    val statisticsViewModel: StatisticsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    var currentScreen by remember { mutableStateOf(AppScreen.TIMER) }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
            AppScreen.DASHBOARD -> {
                DashboardScreen(
                    timerViewModel = timerViewModel,
                    subjectViewModel = subjectViewModel,
                    statisticsViewModel = statisticsViewModel,
                    onStartTimer = { currentScreen = AppScreen.TIMER }
                )
            }
            AppScreen.SUBJECTS -> {
                SubjectsScreen(
                    subjectViewModel = subjectViewModel
                )
            }
            AppScreen.TIMER -> {
                TimerScreen(
                    timerViewModel = timerViewModel
                )
            }
            AppScreen.STATISTICS -> {
                StatisticsScreen(
                    statisticsViewModel = statisticsViewModel
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    settingsViewModel = settingsViewModel
                )
            }
            }
        }
    }
}

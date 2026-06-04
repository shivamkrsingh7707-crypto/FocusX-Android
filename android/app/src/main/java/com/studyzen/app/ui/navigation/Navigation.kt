package com.studyzen.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.studyzen.app.ui.components.BottomNavBar
import com.studyzen.app.ui.components.BottomNavItem
import com.studyzen.app.ui.screens.HomeScreen
import com.studyzen.app.ui.screens.ProgressScreen
import com.studyzen.app.ui.screens.SplashScreen
import com.studyzen.app.ui.screens.TestsScreen
import com.studyzen.app.viewmodel.PomodoroViewModel
import com.studyzen.app.viewmodel.StatisticsViewModel
import com.studyzen.app.viewmodel.StreakViewModel

@Composable
fun FocusXNavigation(
    onOpenSettings: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val pomodoroViewModel: PomodoroViewModel = viewModel()
    val streakViewModel: StreakViewModel = viewModel()
    val statisticsViewModel: StatisticsViewModel = viewModel()

    val bottomNavRoutes = BottomNavItem.entries.map { it.route }
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                val selectedItem = BottomNavItem.entries.find { it.route == currentRoute }
                BottomNavBar(
                    selectedItem = selectedItem ?: BottomNavItem.TIMER,
                    onItemSelected = { item ->
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("splash") {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate("timer") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("timer") {
                HomeScreen(
                    pomodoroViewModel = pomodoroViewModel,
                    streakViewModel = streakViewModel,
                    onOpenSettings = onOpenSettings
                )
            }
            composable("progress") {
                ProgressScreen(statisticsViewModel = statisticsViewModel)
            }
            composable("tests") {
                TestsScreen(streakViewModel = streakViewModel)
            }
        }
    }
}

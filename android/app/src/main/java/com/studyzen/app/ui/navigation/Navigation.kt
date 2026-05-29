package com.studyzen.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.studyzen.app.ui.screens.SettingsScreen
import com.studyzen.app.ui.screens.SplashScreen
import com.studyzen.app.ui.screens.StatisticsScreen
import com.studyzen.app.ui.screens.StreakScreen
import com.studyzen.app.viewmodel.PomodoroViewModel
import com.studyzen.app.viewmodel.StatisticsViewModel
import com.studyzen.app.viewmodel.StreakViewModel

@Composable
fun StudyZenNavigation() {
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
                    selectedItem = selectedItem ?: BottomNavItem.HOME,
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
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 4 },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) + fadeOut(animationSpec = tween(200))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 4 },
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            composable("splash") {
                SplashScreen(
                    onSplashComplete = {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    pomodoroViewModel = pomodoroViewModel,
                    streakViewModel = streakViewModel
                )
            }
            composable("streak") {
                StreakScreen(streakViewModel = streakViewModel)
            }
            composable("statistics") {
                StatisticsScreen(statisticsViewModel = statisticsViewModel)
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}



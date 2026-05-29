package com.studyzen.app.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.studyzen.app.ui.screens.HomeScreen
import com.studyzen.app.ui.screens.SettingsScreen
import com.studyzen.app.ui.screens.SplashScreen
import com.studyzen.app.ui.screens.StatisticsScreen
import com.studyzen.app.ui.screens.StreakScreen

fun NavHostController.navigateWithAnimation(route: String) {
    navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}

val navEnterTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { it / 4 },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    ) + fadeIn(animationSpec = tween(300))
}

val navExitTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    ) + fadeOut(animationSpec = tween(200))
}

val navPopEnterTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.EnterTransition = {
    slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    ) + fadeIn(animationSpec = tween(300))
}

val navPopExitTransition: AnimatedContentTransitionScope<*>.() -> androidx.compose.animation.ExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { it / 4 },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
    ) + fadeOut(animationSpec = tween(200))
}

@Composable
fun rememberPulseAnimation(): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    return pulse
}

@Composable
fun rememberGlowAnimation(): Float {
    val transition = rememberInfiniteTransition(label = "glow")
    val glow by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    return glow
}

package com.studyzen.app

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.ToneGenerator
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.studyzen.app.theme.AmoledBlack
import com.studyzen.app.theme.CardDark
import com.studyzen.app.theme.FocusXTheme
import com.studyzen.app.ui.navigation.FocusXNavigation
import com.studyzen.app.ui.screens.SettingsSheetContent

class MainActivity : ComponentActivity() {

    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            FocusXTheme {
                FocusXApp(
                    toneGenerator = toneGenerator,
                    vibrator = vibrator
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusXApp(
    toneGenerator: ToneGenerator?,
    vibrator: Vibrator?
) {
    var showSettingsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var vibrationsEnabled by remember { mutableStateOf(true) }
    var acousticSignalsEnabled by remember { mutableStateOf(true) }
    var glowingAuraEnabled by remember { mutableStateOf(true) }
    var deepFocusModeEnabled by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        FocusXNavigation(
            onOpenSettings = { showSettingsSheet = true }
        )
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            sheetState = sheetState,
            containerColor = AmoledBlack,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = CardDark) }
        ) {
            SettingsSheetContent(
                vibrationsEnabled = vibrationsEnabled,
                onVibrationsChange = { enabled ->
                    vibrationsEnabled = enabled
                    if (enabled) playHaptic(vibrator, HapticPattern.TAP)
                },
                acousticSignalsEnabled = acousticSignalsEnabled,
                onAcousticSignalsChange = { enabled ->
                    acousticSignalsEnabled = enabled
                    if (enabled) playTone(toneGenerator, ToneType.START)
                },
                glowingAuraEnabled = glowingAuraEnabled,
                onGlowingAuraChange = { glowingAuraEnabled = it },
                deepFocusModeEnabled = deepFocusModeEnabled,
                onDeepFocusModeChange = { deepFocusModeEnabled = it }
            )
        }
    }
}

enum class ToneType { START, PAUSE, COMPLETE }

fun playTone(toneGenerator: ToneGenerator?, type: ToneType) {
    if (toneGenerator == null) return
    when (type) {
        ToneType.START -> {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
        }
        ToneType.PAUSE -> {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 100)
        }
        ToneType.COMPLETE -> {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            Thread.sleep(100)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
            Thread.sleep(100)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
        }
    }
}

enum class HapticPattern { START, TAP, COMPLETE }

fun playHaptic(vibrator: Vibrator?, pattern: HapticPattern) {
    if (vibrator == null) return
    when (pattern) {
        HapticPattern.START -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 30, 20, 50),
                        intArrayOf(0, 200, 0, 150),
                        -1
                    )
                )
            }
        }
        HapticPattern.TAP -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        }
        HapticPattern.COMPLETE -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 80, 30, 60, 30, 100),
                        intArrayOf(0, 255, 0, 200, 0, 255),
                        -1
                    )
                )
            }
        }
    }
}

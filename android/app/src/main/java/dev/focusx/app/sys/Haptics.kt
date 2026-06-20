package dev.focusx.app.sys

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Thin wrapper around the vibrator service. We never call into the
 * service unless the user has haptics enabled in settings, so toggling
 * the preference takes effect immediately.
 */
class Haptics(context: Context) {

    private val app = context.applicationContext
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun light() = vibrate(8, 80)
    fun medium() = vibrate(16, 140)
    fun heavy() = vibrate(28, 200)
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            vibrate(40, 80)
        }
    }

    fun complete() {
        vibrate(40, 80)
        android.os.Handler(app.mainLooper).postDelayed({
            vibrate(80, 140)
        }, 120)
    }

    private fun vibrate(ms: Long, amp: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(ms, amp.coerceIn(1, 255))
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(ms)
        }
    }
}

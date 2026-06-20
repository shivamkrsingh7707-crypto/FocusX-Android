package dev.focusx.app.sys

import android.os.Build
import android.view.Window
import androidx.metrics.performance.JankStats
import android.util.Log

/**
 * JankStats helper. We start tracking after the first frame is laid out
 * (no point in measuring the splash) and forward long frames to a
 * simple logcat tag for now — production apps would forward to a
 * crash/analytics service, but for this app the goal is to *detect*
 * jank, not to send it anywhere.
 */
object FrameTiming {

    private const val TAG = "FocusX/Frame"

    fun attach(window: Window) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val stats = JankStats.createAndTrack(window) { frame ->
            val durationMs = frame.frameDurationUiNanos / 1_000_000.0
            if (durationMs > 16.7) {
                Log.d(TAG, "jank ${"%.2f".format(durationMs)}ms state=${frame.states}")
            }
        }
        stats.isTrackingEnabled = true
    }
}

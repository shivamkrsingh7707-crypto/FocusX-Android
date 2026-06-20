package dev.focusx.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import dev.focusx.app.R

class TimerNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "study_timer_channel"
        const val NOTIFICATION_ID = 1001
    }

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Study Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows the active study session timer and controls"
            setShowBadge(false)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun buildNotification(
        elapsedMs: Long,
        focusDurationMs: Long,
        subjectName: String?,
        isRunning: Boolean
    ): Notification {
        val elapsedSecs = elapsedMs / 1000
        val totalSecs = focusDurationMs / 1000
        val timeText = formatTime(elapsedSecs, totalSecs)
        val title = if (isRunning) "Studying" else "Paused"
        val content = if (subjectName != null) "$subjectName  ·  $timeText" else timeText

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setShowWhen(false)
            .build()
    }

    private fun formatTime(elapsedSeconds: Long, totalSeconds: Long): String {
        val eMin = elapsedSeconds / 60
        val eSec = elapsedSeconds % 60
        val tMin = totalSeconds / 60
        val tSec = totalSeconds % 60
        return "%02d:%02d / %02d:%02d".format(eMin, eSec, tMin, tSec)
    }
}

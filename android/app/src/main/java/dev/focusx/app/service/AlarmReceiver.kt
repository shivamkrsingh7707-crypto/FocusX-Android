package dev.focusx.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ALARM -> {
                val svc = Intent(context, AlarmService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(svc)
                } else {
                    context.startService(svc)
                }
            }
            ACTION_DISMISS -> {
                val svc = Intent(context, AlarmService::class.java)
                context.stopService(svc)
            }
        }
    }

    companion object {
        const val ACTION_ALARM = "dev.focusx.app.action.ALARM"
        const val ACTION_DISMISS = "dev.focusx.app.action.DISMISS_ALARM"
    }
}

package dev.focusx.app

import android.app.Application
import dev.focusx.app.data.local.FocusXDatabase
import dev.focusx.app.service.TimerNotificationHelper

class FocusXApplication : Application() {

    lateinit var database: FocusXDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = FocusXDatabase.create(this)
        TimerNotificationHelper(this).createChannel()
    }
}

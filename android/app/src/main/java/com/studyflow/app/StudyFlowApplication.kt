package com.studyflow.app

import android.app.Application
import com.studyflow.app.utils.createNotificationChannel

class StudyFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
}

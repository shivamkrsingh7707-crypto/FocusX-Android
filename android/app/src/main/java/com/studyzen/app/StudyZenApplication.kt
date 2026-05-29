package com.studyzen.app

import android.app.Application
import com.studyzen.app.utils.createNotificationChannel

class StudyZenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }
}

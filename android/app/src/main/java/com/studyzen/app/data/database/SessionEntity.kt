package com.studyzen.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val type: String = "focus"
)

package com.studyzen.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_data")
data class StreakEntity(
    @PrimaryKey
    val date: String,
    val totalMinutes: Int,
    val sessionsCompleted: Int,
    val isGoalMet: Boolean = false
)

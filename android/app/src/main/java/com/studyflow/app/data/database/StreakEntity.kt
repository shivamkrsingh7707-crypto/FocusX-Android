package com.studyflow.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey
    val date: String,
    val totalMinutes: Int = 0,
    val sessionsCount: Int = 0,
    val isGoalMet: Boolean = false
)

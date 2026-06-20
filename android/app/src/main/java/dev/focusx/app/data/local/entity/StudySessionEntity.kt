package dev.focusx.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "subject_name") val subjectName: String,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Long,
    val timestamp: Long
)

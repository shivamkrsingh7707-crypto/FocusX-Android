package com.studyflow.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val colorIndex: Int = 0,
    val iconResName: String = "book",
    val createdAt: Long = System.currentTimeMillis(),
    val targetHoursPerWeek: Int = 10,
    val isArchived: Boolean = false
)

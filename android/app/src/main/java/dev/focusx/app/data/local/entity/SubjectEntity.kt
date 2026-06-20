package dev.focusx.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.focusx.app.domain.Subject

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "color_index") val colorIndex: Int,
    @ColumnInfo(name = "target_hours_per_week") val targetHoursPerWeek: Int = 5,
    val archived: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

fun SubjectEntity.toDomain(): Subject = Subject(
    id = id,
    name = name,
    colorIndex = colorIndex,
    targetHoursPerWeek = targetHoursPerWeek,
    archived = archived
)

fun Subject.toEntity(): SubjectEntity = SubjectEntity(
    id = id,
    name = name,
    colorIndex = colorIndex,
    targetHoursPerWeek = targetHoursPerWeek,
    archived = archived
)

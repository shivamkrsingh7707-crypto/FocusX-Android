package dev.focusx.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.focusx.app.domain.Grade
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subject_id")]
)
data class GradeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "subject_id") val subjectId: String,
    val score: Double,
    @ColumnInfo(name = "max_score") val maxScore: Double,
    val weight: Double,
    val title: String,
    val date: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

fun GradeEntity.toDomain(): Grade = Grade(
    id = id,
    subjectId = subjectId,
    score = score,
    maxScore = maxScore,
    weight = weight,
    title = title,
    date = LocalDate.parse(date),
    createdAt = createdAt
)

fun Grade.toEntity(): GradeEntity = GradeEntity(
    id = id,
    subjectId = subjectId,
    score = score,
    maxScore = maxScore,
    weight = weight,
    title = title,
    date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
    createdAt = createdAt
)

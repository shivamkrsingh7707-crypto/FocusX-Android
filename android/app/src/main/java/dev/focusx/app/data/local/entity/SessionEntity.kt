package dev.focusx.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.focusx.app.domain.Session
import dev.focusx.app.domain.TimerPhase
import java.time.LocalDate

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("subject_id"),
        Index("date")
    ]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "subject_id") val subjectId: String?,
    val minutes: Int,
    val date: String,
    val phase: String,
    @ColumnInfo(name = "started_at") val startedAt: Long
)

fun SessionEntity.toDomain(): Session = Session(
    id = id,
    subjectId = subjectId,
    minutes = minutes,
    date = LocalDate.parse(date),
    phase = TimerPhase.valueOf(phase),
    startedAt = startedAt
)

fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    subjectId = subjectId,
    minutes = minutes,
    date = date.format(Session.IsoDate),
    phase = phase.name,
    startedAt = startedAt
)

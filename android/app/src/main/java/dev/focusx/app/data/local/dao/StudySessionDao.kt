package dev.focusx.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.focusx.app.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    fun observeForDate(startOfDay: Long, endOfDay: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM study_sessions")
    fun observeTotalMinutes(): Flow<Long>

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM study_sessions WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    suspend fun getMinutesForDate(startOfDay: Long, endOfDay: Long): Long

    @Insert
    suspend fun insert(session: StudySessionEntity)
}

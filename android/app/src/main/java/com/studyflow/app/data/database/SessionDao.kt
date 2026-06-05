package com.studyflow.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getSessionsBySubject(subjectId: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsByDate(date: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date >= :startDate AND date <= :endDate ORDER BY timestamp DESC")
    suspend fun getSessionsInRange(startDate: String, endDate: String): List<SessionEntity>

    @Query("SELECT SUM(durationMinutes) FROM sessions")
    fun getTotalMinutes(): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM sessions WHERE date = :date")
    suspend fun getMinutesByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM sessions")
    fun getTotalSessions(): Flow<Int?>

    @Query("SELECT SUM(durationMinutes) FROM sessions WHERE subjectId = :subjectId")
    fun getSubjectTotalMinutes(subjectId: Long): Flow<Int?>

    @Query("""
        SELECT date, SUM(durationMinutes) as totalMinutes
        FROM sessions WHERE date >= :startDate AND date <= :endDate
        GROUP BY date ORDER BY date ASC
    """)
    suspend fun getDailyMinutesInRange(startDate: String, endDate: String): List<DateMinutes>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT COUNT(*) FROM sessions WHERE date = :date")
    suspend fun getSessionCountByDate(date: String): Int
}

data class DateMinutes(
    val date: String,
    val totalMinutes: Int
)

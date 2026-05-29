package com.studyzen.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startTime DESC")
    suspend fun getAllSessions(): List<SessionEntity>

    @Query("SELECT * FROM focus_sessions WHERE date = :date ORDER BY startTime ASC")
    suspend fun getSessionsByDate(date: String): List<SessionEntity>

    @Query("SELECT * FROM streak_data ORDER BY date DESC")
    suspend fun getAllStreaks(): List<StreakEntity>

    @Query("SELECT * FROM streak_data WHERE date = :date")
    suspend fun getStreakByDate(date: String): StreakEntity?

    @Query("""
        SELECT SUM(durationMinutes) FROM focus_sessions 
        WHERE date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalMinutesInRange(startDate: String, endDate: String): Int?

    @Query("""
        SELECT SUM(durationMinutes) FROM focus_sessions
    """)
    suspend fun getTotalMinutes(): Int?

    @Query("""
        SELECT COUNT(*) FROM focus_sessions
    """)
    suspend fun getTotalSessions(): Int

    @Query("""
        SELECT COUNT(DISTINCT date) FROM streak_data WHERE isGoalMet = 1
    """)
    suspend fun getDaysGoalMet(): Int
}

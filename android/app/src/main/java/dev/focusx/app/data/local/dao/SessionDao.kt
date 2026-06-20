package dev.focusx.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.focusx.app.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY started_at DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM sessions WHERE subject_id = :subjectId ORDER BY started_at DESC")
    suspend fun getBySubjectId(subjectId: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE date = :date ORDER BY started_at DESC")
    suspend fun getByDate(date: String): List<SessionEntity>

    @Query("SELECT DISTINCT date FROM sessions ORDER BY date DESC")
    suspend fun getAllDates(): List<String>

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM sessions WHERE date = :date")
    suspend fun getMinutesForDate(date: String): Int

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM sessions")
    suspend fun getTotalMinutes(): Int

    @Query("SELECT * FROM sessions WHERE date >= :startDate AND date <= :endDate ORDER BY started_at DESC")
    suspend fun getInDateRange(startDate: String, endDate: String): List<SessionEntity>

    @Query("SELECT COUNT(*) FROM sessions WHERE date = :date")
    suspend fun countForDate(date: String): Int
}

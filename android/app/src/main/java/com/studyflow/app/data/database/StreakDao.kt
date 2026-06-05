package com.studyflow.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks ORDER BY date DESC")
    fun getAllStreaks(): Flow<List<StreakEntity>>

    @Query("SELECT * FROM streaks ORDER BY date DESC LIMIT 1")
    suspend fun getLatestStreak(): StreakEntity?

    @Query("SELECT * FROM streaks WHERE date = :date")
    suspend fun getStreakByDate(date: String): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStreak(streak: StreakEntity)

    @Query("SELECT COUNT(*) FROM streaks WHERE isGoalMet = 1")
    fun getGoalMetDaysCount(): Flow<Int>

    @Query("SELECT MAX(totalMinutes) FROM streaks")
    fun getBestDayMinutes(): Flow<Int?>

    @Query("""
        SELECT COUNT(*) FROM streaks
        WHERE isGoalMet = 1
        ORDER BY date DESC
    """)
    suspend fun getStreakCount(): Int
}

package dev.focusx.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.focusx.app.data.local.entity.GradeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {

    @Query("SELECT * FROM grades ORDER BY created_at DESC")
    fun observeAll(): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE subject_id = :subjectId ORDER BY created_at DESC")
    fun observeBySubject(subjectId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades WHERE id = :id")
    suspend fun getById(id: String): GradeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(grade: GradeEntity)

    @Delete
    suspend fun delete(grade: GradeEntity)

    @Query("DELETE FROM grades WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM grades WHERE subject_id = :subjectId")
    suspend fun deleteBySubject(subjectId: String)

    @Query("SELECT * FROM grades WHERE subject_id = :subjectId ORDER BY created_at DESC")
    suspend fun getBySubjectId(subjectId: String): List<GradeEntity>

    @Query("SELECT * FROM grades ORDER BY created_at DESC")
    suspend fun getAll(): List<GradeEntity>
}

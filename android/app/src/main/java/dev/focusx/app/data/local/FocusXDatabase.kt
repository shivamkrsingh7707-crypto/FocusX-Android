package dev.focusx.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.focusx.app.data.local.dao.GradeDao
import dev.focusx.app.data.local.dao.SessionDao
import dev.focusx.app.data.local.dao.SubjectDao
import dev.focusx.app.data.local.entity.GradeEntity
import dev.focusx.app.data.local.entity.SessionEntity
import dev.focusx.app.data.local.entity.SubjectEntity

@Database(
    entities = [
        SubjectEntity::class,
        SessionEntity::class,
        GradeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusXDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun sessionDao(): SessionDao
    abstract fun gradeDao(): GradeDao

    companion object {
        @Volatile
        private var INSTANCE: FocusXDatabase? = null

        fun create(context: Context): FocusXDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FocusXDatabase::class.java,
                    "focusx.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

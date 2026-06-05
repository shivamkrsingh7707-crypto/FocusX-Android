package com.studyflow.app.data.repository

import com.studyflow.app.data.database.DateMinutes
import com.studyflow.app.data.database.SessionDao
import com.studyflow.app.data.database.SessionEntity
import com.studyflow.app.data.database.StreakDao
import com.studyflow.app.data.database.StreakEntity
import com.studyflow.app.data.database.SubjectDao
import com.studyflow.app.data.database.SubjectEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudyRepository(
    private val subjectDao: SubjectDao,
    private val sessionDao: SessionDao,
    private val streakDao: StreakDao
) {
    val allSubjects: Flow<List<SubjectEntity>> = subjectDao.getAllSubjects()
    val subjectCount: Flow<Int> = subjectDao.getSubjectCount()
    val allSessions: Flow<List<SessionEntity>> = sessionDao.getAllSessions()
    val totalMinutes: Flow<Int?> = sessionDao.getTotalMinutes()
    val totalSessions: Flow<Int?> = sessionDao.getTotalSessions()
    val allStreaks: Flow<List<StreakEntity>> = streakDao.getAllStreaks()
    val goalMetDaysCount: Flow<Int> = streakDao.getGoalMetDaysCount()
    val bestDayMinutes: Flow<Int?> = streakDao.getBestDayMinutes()

    suspend fun addSubject(subject: SubjectEntity): Long = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = subjectDao.deleteSubject(subject)
    suspend fun getSubjectById(id: Long): SubjectEntity? = subjectDao.getSubjectById(id)
    suspend fun getAllSubjectsList(): List<SubjectEntity> = subjectDao.getAllSubjectsList()

    suspend fun recordSession(minutes: Int, subjectId: Long? = null, note: String = "") {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val session = SessionEntity(
            subjectId = subjectId,
            durationMinutes = minutes,
            date = today,
            note = note
        )
        sessionDao.insertSession(session)
        updateStreak(today, minutes)
    }

    private suspend fun updateStreak(date: String, minutes: Int) {
        val existing = streakDao.getStreakByDate(date)
        val newTotal = (existing?.totalMinutes ?: 0) + minutes
        val newCount = (existing?.sessionsCount ?: 0) + 1
        streakDao.upsertStreak(
            StreakEntity(
                date = date,
                totalMinutes = newTotal,
                sessionsCount = newCount,
                isGoalMet = newTotal >= 25
            )
        )
    }

    suspend fun getCurrentStreak(): Int {
        val streaks = streakDao.getStreakCount()
        return streaks
    }

    fun getSubjectMinutes(subjectId: Long): Flow<Int?> = sessionDao.getSubjectTotalMinutes(subjectId)

    suspend fun getDailyMinutesInRange(startDate: String, endDate: String): List<DateMinutes> {
        return sessionDao.getDailyMinutesInRange(startDate, endDate)
    }

    suspend fun getSessionsInRange(startDate: String, endDate: String): List<SessionEntity> {
        return sessionDao.getSessionsInRange(startDate, endDate)
    }

    fun getSessionsBySubject(subjectId: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsBySubject(subjectId)
}

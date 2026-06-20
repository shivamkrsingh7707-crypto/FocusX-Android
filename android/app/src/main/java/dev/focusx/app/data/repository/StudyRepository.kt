package dev.focusx.app.data.repository

import dev.focusx.app.data.local.dao.GradeDao
import dev.focusx.app.data.local.dao.SessionDao
import dev.focusx.app.data.local.dao.StudySessionDao
import dev.focusx.app.data.local.dao.SubjectDao
import dev.focusx.app.data.local.entity.StudySessionEntity
import dev.focusx.app.data.local.entity.toDomain
import dev.focusx.app.data.local.entity.toEntity
import dev.focusx.app.domain.Grade
import dev.focusx.app.domain.Session
import dev.focusx.app.domain.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StudyRepository(
    private val subjectDao: SubjectDao,
    private val sessionDao: SessionDao,
    private val gradeDao: GradeDao,
    private val studySessionDao: StudySessionDao
) {

    // ── Subjects ──────────────────────────────────────────────────────────

    fun observeSubjects(): Flow<List<Subject>> =
        subjectDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeActiveSubjects(): Flow<List<Subject>> =
        subjectDao.observeActive().map { entities -> entities.map { it.toDomain() } }

    suspend fun upsertSubject(subject: Subject) = withContext(Dispatchers.IO) {
        subjectDao.upsert(subject.toEntity())
    }

    suspend fun deleteSubject(id: String) = withContext(Dispatchers.IO) {
        subjectDao.deleteById(id)
    }

    suspend fun getSubject(id: String): Subject? = withContext(Dispatchers.IO) {
        subjectDao.getById(id)?.toDomain()
    }

    // ── Sessions ──────────────────────────────────────────────────────────

    fun observeSessions(): Flow<List<Session>> =
        sessionDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun insertSession(session: Session) = withContext(Dispatchers.IO) {
        sessionDao.insert(session.toEntity())
    }

    suspend fun getTodayMinutes(): Int = withContext(Dispatchers.IO) {
        sessionDao.getMinutesForDate(LocalDate.now().format(Session.IsoDate))
    }

    suspend fun getTotalMinutes(): Int = withContext(Dispatchers.IO) {
        sessionDao.getTotalMinutes()
    }

    suspend fun getSessionsForSubject(subjectId: String): List<Session> =
        withContext(Dispatchers.IO) {
            sessionDao.getBySubjectId(subjectId).map { it.toDomain() }
        }

    suspend fun getSessionsInRange(start: LocalDate, end: LocalDate): List<Session> =
        withContext(Dispatchers.IO) {
            sessionDao.getInDateRange(
                start.format(Session.IsoDate),
                end.format(Session.IsoDate)
            ).map { it.toDomain() }
        }

    // ── Streak ────────────────────────────────────────────────────────────

    suspend fun computeStreak(): Int = withContext(Dispatchers.IO) {
        val dateStrings = sessionDao.getAllDates()
        if (dateStrings.isEmpty()) return@withContext 0

        val dates = dateStrings.map { LocalDate.parse(it) }.toHashSet()
        var streak = 0
        var cursor = LocalDate.now()

        while (dates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }

        streak
    }

    // ── Grades ────────────────────────────────────────────────────────────

    fun observeGrades(): Flow<List<Grade>> =
        gradeDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observeGradesForSubject(subjectId: String): Flow<List<Grade>> =
        gradeDao.observeBySubject(subjectId).map { entities -> entities.map { it.toDomain() } }

    suspend fun insertGrade(grade: Grade) = withContext(Dispatchers.IO) {
        gradeDao.insert(grade.toEntity())
    }

    suspend fun deleteGrade(id: String) = withContext(Dispatchers.IO) {
        gradeDao.deleteById(id)
    }

    suspend fun getWeightedAverageForSubject(subjectId: String): Double =
        withContext(Dispatchers.IO) {
            val grades = gradeDao.getBySubjectId(subjectId)
            computeWeightedAverage(grades.map { it.toDomain() })
        }

    suspend fun getOverallWeightedAverage(): Double = withContext(Dispatchers.IO) {
        val grades = gradeDao.getAll()
        computeWeightedAverage(grades.map { it.toDomain() })
    }

    // ── Study Sessions ────────────────────────────────────────────────────

    fun observeStudySessions(): Flow<List<StudySessionEntity>> =
        studySessionDao.observeAll()

    suspend fun insertStudySession(session: StudySessionEntity) = withContext(Dispatchers.IO) {
        studySessionDao.insert(session)
    }

    private fun computeWeightedAverage(grades: List<Grade>): Double {
        if (grades.isEmpty()) return 0.0
        val weightedSum = grades.sumOf { it.score * it.weight }
        val totalWeight = grades.sumOf { it.maxScore * it.weight }
        if (totalWeight == 0.0) return 0.0
        return (weightedSum / totalWeight) * 100.0
    }
}

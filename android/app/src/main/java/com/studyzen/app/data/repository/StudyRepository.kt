package com.studyzen.app.data.repository

import com.studyzen.app.data.database.SessionDao
import com.studyzen.app.data.database.SessionEntity
import com.studyzen.app.data.database.StreakEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StudyRepository(private val sessionDao: SessionDao) {

    suspend fun recordSession(durationMinutes: Int): SessionEntity {
        val now = System.currentTimeMillis()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val session = SessionEntity(
            date = today,
            startTime = now - (durationMinutes * 60 * 1000L),
            endTime = now,
            durationMinutes = durationMinutes,
            type = "focus"
        )
        sessionDao.insertSession(session)
        updateStreak(today, durationMinutes)
        return session
    }

    private suspend fun updateStreak(date: String, minutes: Int) {
        val existing = sessionDao.getStreakByDate(date)
        if (existing != null) {
            val total = sessionDao.getTotalMinutesInRange("$date 00:00:00", "$date 23:59:59") ?: 0
            sessionDao.upsertStreak(
                existing.copy(
                    totalMinutes = total,
                    sessionsCompleted = existing.sessionsCompleted + 1,
                    isGoalMet = total >= 25
                )
            )
        } else {
            val total = sessionDao.getTotalMinutesInRange("$date 00:00:00", "$date 23:59:59") ?: minutes
            sessionDao.upsertStreak(
                StreakEntity(
                    date = date,
                    totalMinutes = total,
                    sessionsCompleted = 1,
                    isGoalMet = total >= 25
                )
            )
        }
    }

    suspend fun getCurrentStreak(): Int {
        val streaks = sessionDao.getAllStreaks().sortedByDescending { it.date }
        if (streaks.isEmpty()) return 0

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        var streak = 0
        var currentDate = LocalDate.now()

        while (true) {
            val dateStr = currentDate.format(formatter)
            val dayData = streaks.find { it.date == dateStr }
            if (dayData != null && dayData.isGoalMet) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (dayData == null && streak == 0) {
                currentDate = currentDate.minusDays(1)
                continue
            } else {
                break
            }
        }
        return streak
    }

    suspend fun getBestStreak(): Int {
        val streaks = sessionDao.getAllStreaks().filter { it.isGoalMet }
            .sortedBy { it.date }
        if (streaks.isEmpty()) return 0

        var best = 0
        var current = 0
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        var prevDate: LocalDate? = null

        for (streak in streaks) {
            val date = LocalDate.parse(streak.date, formatter)
            if (prevDate == null || date == prevDate.plusDays(1)) {
                current++
            } else {
                current = 1
            }
            best = maxOf(best, current)
            prevDate = date
        }
        return best
    }

    suspend fun getSessionsForDate(date: String): List<SessionEntity> {
        return sessionDao.getSessionsByDate(date)
    }

    suspend fun getWeeklyStats(): List<Pair<String, Int>> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val stats = mutableListOf<Pair<String, Int>>()

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val minutes = sessionDao.getTotalMinutesInRange(
                date.format(formatter),
                date.format(formatter)
            ) ?: 0
            stats.add(Pair(date.dayOfWeek.name.take(3), minutes))
        }
        return stats
    }

    suspend fun getMonthlyStats(): List<Pair<String, Int>> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val stats = mutableListOf<Pair<String, Int>>()

        for (i in 29 downTo 0) {
            val date = today.minusDays(i.toLong())
            val minutes = sessionDao.getTotalMinutesInRange(
                date.format(formatter),
                date.format(formatter)
            ) ?: 0
            if (minutes > 0) {
                stats.add(Pair(date.dayOfMonth.toString(), minutes))
            }
        }
        return stats
    }

    suspend fun getTotalMinutes(): Int {
        return sessionDao.getTotalMinutes() ?: 0
    }

    suspend fun getTotalSessions(): Int {
        return sessionDao.getTotalSessions()
    }

    suspend fun getAllStreaks(): List<StreakEntity> {
        return sessionDao.getAllStreaks()
    }
}

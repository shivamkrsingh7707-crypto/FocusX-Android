package dev.focusx.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.focusx.app.domain.AppState
import dev.focusx.app.domain.Settings
import dev.focusx.app.domain.Subject
import dev.focusx.app.domain.ThemeMode
import dev.focusx.app.domain.TimerPhase
import dev.focusx.app.domain.defaultSubjects
import dev.focusx.app.domain.isoToday
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focusx_state")

/**
 * Lightweight preferences-backed repository. We store:
 *   - scalar settings (theme, haptics, daily goal, …) as typed keys
 *   - the user's subjects as a `stringSet` of encoded records
 *   - sessions as a `stringSet` of encoded records
 *
 * For the volumes an individual user will hit (hundreds, not millions of
 * rows) a stringSet is more than fast enough and avoids pulling Room
 * into the build.
 */
class StateRepository(private val context: Context) {

    val state: Flow<AppState> = context.dataStore.data.map { prefs ->
        val subjects = decodeSubjects(prefs[KEY_SUBJECTS]).ifEmpty { defaultSubjects }
        val sessions = decodeSessions(prefs[KEY_SESSIONS])
        val today = LocalDate.now()
        val todayKey = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val todayMinutes = sessions
            .filter { it.date == today }
            .sumOf { it.minutes }
        val streak = computeStreak(sessions)
        AppState(
            subjects = subjects,
            sessions = sessions,
            settings = decodeSettings(prefs),
            streak = streak,
            todayMinutes = todayMinutes
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) =
        context.dataStore.edit { it[KEY_THEME_MODE] = mode.ordinal }

    suspend fun setHaptics(enabled: Boolean) =
        context.dataStore.edit { it[KEY_HAPTICS] = enabled }

    suspend fun setSound(enabled: Boolean) =
        context.dataStore.edit { it[KEY_SOUND] = enabled }

    suspend fun setAutoStartBreak(enabled: Boolean) =
        context.dataStore.edit { it[KEY_AUTOBREAK] = enabled }

    suspend fun setAutoStartFocus(enabled: Boolean) =
        context.dataStore.edit { it[KEY_AUTOFOCUS] = enabled }

    suspend fun setDailyGoal(minutes: Int) =
        context.dataStore.edit { it[KEY_GOAL] = minutes.coerceIn(5, 600) }

    suspend fun setReminder(enabled: Boolean, hour: Int, minute: Int) =
        context.dataStore.edit {
            it[KEY_REMINDER] = enabled
            it[KEY_REMINDER_HOUR] = hour
            it[KEY_REMINDER_MIN] = minute
        }

    suspend fun upsertSubject(subject: Subject) {
        context.dataStore.edit { prefs ->
            val current = decodeSubjects(prefs[KEY_SUBJECTS]).toMutableList()
            val idx = current.indexOfFirst { it.id == subject.id }
            if (idx >= 0) current[idx] = subject else current.add(0, subject)
            prefs[KEY_SUBJECTS] = current.map(::encodeSubject).toSet()
        }
    }

    suspend fun deleteSubject(id: String) {
        context.dataStore.edit { prefs ->
            val current = decodeSubjects(prefs[KEY_SUBJECTS])
                .filterNot { it.id == id }
            prefs[KEY_SUBJECTS] = current.map(::encodeSubject).toSet()
        }
    }

    suspend fun appendSession(
        subjectId: String?,
        minutes: Int,
        phase: TimerPhase,
        startedAt: Long
    ) {
        context.dataStore.edit { prefs ->
            val current = decodeSessions(prefs[KEY_SESSIONS]).toMutableList()
            current.add(
                0,
                SessionRecord(
                    id = java.util.UUID.randomUUID().toString(),
                    subjectId = subjectId,
                    minutes = minutes,
                    date = isoToday(),
                    phase = phase.name,
                    startedAt = startedAt
                )
            )
            prefs[KEY_SESSIONS] = current.map(::encodeSession).toSet()
        }
    }

    private fun decodeSettings(prefs: Preferences): Settings {
        val mode = ThemeMode.entries
            .getOrNull(prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.ordinal)
            ?: ThemeMode.SYSTEM
        return Settings(
            themeMode = mode,
            hapticsEnabled = prefs[KEY_HAPTICS] ?: true,
            soundEnabled = prefs[KEY_SOUND] ?: true,
            autoStartBreak = prefs[KEY_AUTOBREAK] ?: false,
            autoStartFocus = prefs[KEY_AUTOFOCUS] ?: false,
            dailyGoalMinutes = prefs[KEY_GOAL] ?: 25,
            reminderEnabled = prefs[KEY_REMINDER] ?: false,
            reminderHour = prefs[KEY_REMINDER_HOUR] ?: 9,
            reminderMinute = prefs[KEY_REMINDER_MIN] ?: 0
        )
    }

    private fun encodeSubject(s: Subject): String =
        "${s.id}|${s.name}|${s.colorIndex}|${s.targetHoursPerWeek}|${s.archived}"

    private fun decodeSubjects(raw: Set<String>?): List<Subject> =
        raw.orEmpty().mapNotNull(::tryDecodeSubject).sortedBy { it.name.lowercase() }

    private fun tryDecodeSubject(s: String): Subject? {
        val parts = s.split('|')
        if (parts.size < 4) return null
        return runCatching {
            Subject(
                id = parts[0],
                name = parts[1],
                colorIndex = parts[2].toInt(),
                targetHoursPerWeek = parts[3].toInt(),
                archived = parts.getOrNull(4)?.toBoolean() ?: false
            )
        }.getOrNull()
    }

    private fun encodeSession(r: SessionRecord): String =
        "${r.id}|${r.subjectId ?: ""}|${r.minutes}|${r.date}|${r.phase}|${r.startedAt}"

    private fun decodeSessions(raw: Set<String>?): List<dev.focusx.app.domain.Session> =
        raw.orEmpty()
            .mapNotNull(::tryDecodeSession)
            .sortedByDescending { it.startedAt }

    private fun tryDecodeSession(s: String): dev.focusx.app.domain.Session? {
        val parts = s.split('|')
        if (parts.size < 6) return null
        return runCatching {
            dev.focusx.app.domain.Session(
                id = parts[0],
                subjectId = parts[1].ifEmpty { null },
                minutes = parts[2].toInt(),
                date = LocalDate.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE),
                phase = TimerPhase.valueOf(parts[4]),
                startedAt = parts[5].toLong()
            )
        }.getOrNull()
    }

    private data class SessionRecord(
        val id: String,
        val subjectId: String?,
        val minutes: Int,
        val date: String,
        val phase: String,
        val startedAt: Long
    )

    /**
     * Walk backwards from today counting consecutive days with at least
     * one session. Stops at the first gap.
     */
    private fun computeStreak(sessions: List<dev.focusx.app.domain.Session>): Int {
        if (sessions.isEmpty()) return 0
        val days = sessions.map { it.date }.toHashSet()
        var streak = 0
        var cursor = LocalDate.now()
        while (days.contains(cursor)) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    private companion object {
        val KEY_THEME_MODE = intPreferencesKey("theme_mode")
        val KEY_HAPTICS = booleanPreferencesKey("haptics")
        val KEY_SOUND = booleanPreferencesKey("sound")
        val KEY_AUTOBREAK = booleanPreferencesKey("autobreak")
        val KEY_AUTOFOCUS = booleanPreferencesKey("autofocus")
        val KEY_GOAL = intPreferencesKey("goal")
        val KEY_REMINDER = booleanPreferencesKey("reminder")
        val KEY_REMINDER_HOUR = intPreferencesKey("reminder_h")
        val KEY_REMINDER_MIN = intPreferencesKey("reminder_m")
        val KEY_SUBJECTS = stringSetPreferencesKey("subjects")
        val KEY_SESSIONS = stringSetPreferencesKey("sessions")
    }
}

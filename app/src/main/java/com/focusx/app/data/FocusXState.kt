package com.focusx.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Session(
    val subject: String,
    val date: String,
    val duration: Int,
    val timestamp: Long,
)

data class TestRecord(
    val subject: String,
    val score: Int,
    val total: Int,
    val date: String,
    val timestamp: Long,
)

data class Prefs(
    val haptics: Boolean = true,
    val chimes: Boolean = true,
    val ambient: Boolean = true,
    val glow: Boolean = true,
)

data class FocusXState(
    val subjects: List<String> = listOf("Math", "Science", "English", "SST", "Hindi"),
    val sessions: List<Session> = emptyList(),
    val tests: List<TestRecord> = emptyList(),
    val dailyGoal: Int = 120,
    val darkMode: Boolean = true,
    val prefs: Prefs = Prefs(),
)

class StateRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences("focusx_prefs", Context.MODE_PRIVATE)

    fun load(): FocusXState {
        val raw = prefs.getString("state", null) ?: return FocusXState()
        return try {
            val obj = JSONObject(raw)
            FocusXState(
                subjects = jsonArrayToList(obj.optJSONArray("subjects")) { it },
                sessions = parseSessions(obj.optJSONArray("sessions")),
                tests = parseTests(obj.optJSONArray("tests")),
                dailyGoal = obj.optInt("dailyGoal", 120),
                darkMode = obj.optBoolean("darkMode", true),
                prefs = parsePrefs(obj.optJSONObject("prefs")),
            )
        } catch (_: Exception) { FocusXState() }
    }

    fun save(state: FocusXState) {
        val obj = JSONObject().apply {
            put("subjects", JSONArray(state.subjects))
            put("sessions", JSONArray(state.sessions.map { sessionToJson(it) }))
            put("tests", JSONArray(state.tests.map { testToJson(it) }))
            put("dailyGoal", state.dailyGoal)
            put("darkMode", state.darkMode)
            put("prefs", prefsToJson(state.prefs))
        }
        prefs.edit().putString("state", obj.toString()).apply()
    }

    private fun sessionToJson(s: Session) = JSONObject().apply {
        put("subject", s.subject)
        put("date", s.date)
        put("duration", s.duration)
        put("timestamp", s.timestamp)
    }

    private fun testToJson(t: TestRecord) = JSONObject().apply {
        put("subject", t.subject)
        put("score", t.score)
        put("total", t.total)
        put("date", t.date)
        put("timestamp", t.timestamp)
    }

    private fun prefsToJson(p: Prefs) = JSONObject().apply {
        put("haptics", p.haptics)
        put("chimes", p.chimes)
        put("ambient", p.ambient)
        put("glow", p.glow)
    }

    private fun parseSessions(arr: JSONArray?): List<Session> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Session(o.getString("subject"), o.getString("date"), o.getInt("duration"), o.getLong("timestamp"))
        }
    }

    private fun parseTests(arr: JSONArray?): List<TestRecord> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            TestRecord(o.getString("subject"), o.getInt("score"), o.getInt("total"), o.getString("date"), o.getLong("timestamp"))
        }
    }

    private fun parsePrefs(o: JSONObject?): Prefs {
        if (o == null) return Prefs()
        return Prefs(
            haptics = o.optBoolean("haptics", true),
            chimes = o.optBoolean("chimes", true),
            ambient = o.optBoolean("ambient", true),
            glow = o.optBoolean("glow", true),
        )
    }

    private fun <T> jsonArrayToList(arr: JSONArray?, transform: (String) -> T): List<T> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { transform(arr.getString(it)) }
    }
}

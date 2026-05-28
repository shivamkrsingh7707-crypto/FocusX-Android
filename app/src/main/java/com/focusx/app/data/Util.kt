package com.focusx.app.data

import java.text.SimpleDateFormat
import java.util.*

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

fun getToday(): String = dateFormat.format(Date())

fun getMinutesToday(sessions: List<Session>): Int {
    val today = getToday()
    return sessions.filter { it.date == today }.sumOf { it.duration }
}

fun calculateStreak(sessions: List<Session>): Int {
    val dates = sessions.map { it.date }.distinct().sorted()
    if (dates.isEmpty()) return 0
    var streak = 0
    val cal = Calendar.getInstance()
    val today = dateFormat.format(cal.time)
    for (i in 0 until 365) {
        cal.timeInMillis = System.currentTimeMillis()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val date = dateFormat.format(cal.time)
        if (dates.contains(date)) streak++
        else if (i > 0 || !dates.contains(today)) break
    }
    return streak
}

fun getDayLabel(dateStr: String): String {
    val cal = Calendar.getInstance()
    val parts = dateStr.split("-")
    if (parts.size == 3) {
        cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
    }
    return SimpleDateFormat("E", Locale.US).format(cal.time)
}

fun getDateLabel(dateStr: String): String {
    val parts = dateStr.split("-")
    return if (parts.size == 3) "${parts[2]}/${parts[1]}" else dateStr
}

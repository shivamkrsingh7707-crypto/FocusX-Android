package com.focusx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusx.app.data.*
import com.focusx.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(state: FocusXState) {
    var period by remember { mutableStateOf("week") }
    val streak = calculateStreak(state.sessions)
    val todayMins = getMinutesToday(state.sessions)
    val focus = if (state.dailyGoal > 0) (todayMins * 100 / state.dailyGoal).coerceIn(0, 100) else 0

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("$streak", "Day Streak", Modifier.weight(1f))
            StatCard(
                if (todayMins >= 60) "${todayMins / 60}h ${todayMins % 60}m" else "${todayMins}m",
                "Today", Modifier.weight(1f),
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("$focus%", "Focus", Modifier.weight(1f))
            StatCard("${state.subjects.size}", "Subjects", Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Weekly Progress", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        listOf("week" to "Week", "month" to "Month").forEach { (key, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (period == key) Accent else Color.Transparent)
                                    .clickable { period = key }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                                    color = if (period == key) Color.White else Text3Dark)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                val chartData = buildChartData(state.sessions, period)
                val maxMins = chartData.maxOfOrNull { it.second } ?: 1

                Row(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    chartData.forEach { (label, mins) ->
                        val heightFraction = (mins.toFloat() / maxMins).coerceIn(0.01f, 1f)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            if (mins > 0) {
                                Text(
                                    if (mins >= 60) "${mins / 60}h" else "${mins}m",
                                    fontSize = 8.sp, color = Text3Dark, fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((heightFraction * 100).dp.coerceAtLeast(3.dp))
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(Accent.copy(alpha = 0.7f)),
                            )
                            Text(label, fontSize = 8.sp, color = Text3Dark, textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Subject Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))

                val bySubject = mutableMapOf<String, Int>()
                state.sessions.filter { it.date == getToday() }.forEach { s ->
                    bySubject[s.subject] = (bySubject[s.subject] ?: 0) + s.duration
                }

                if (bySubject.isEmpty()) {
                    Text("No study sessions today.", fontSize = 12.sp, color = Text3Dark,
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center)
                } else {
                    val total = bySubject.values.sum()
                    bySubject.toList().sortedByDescending { it.second }.forEach { (subject, mins) ->
                        val pct = if (total > 0) mins.toFloat() / total else 0f
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(subject, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.width(72.dp))
                            Box(
                                modifier = Modifier.weight(1f).height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            ) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(pct)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Accent.copy(alpha = 0.8f)))
                            }
                            Text("${mins}m", fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(40.dp), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground, letterSpacing = -0.5.sp)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                color = Text3Dark, letterSpacing = 0.6.sp)
        }
    }
}

private fun buildChartData(sessions: List<Session>, period: String): List<Pair<String, Int>> {
    val days = if (period == "week") 7 else 30
    val cal = Calendar.getInstance()
    val todayStr = getToday()
    val todayParts = todayStr.split("-")
    cal.set(todayParts[0].toInt(), todayParts[1].toInt() - 1, todayParts[2].toInt())
    val dayFormat = SimpleDateFormat("E", Locale.US)
    val dateFormat = SimpleDateFormat("d/M", Locale.US)

    return (0 until days).map { i ->
        val d = Calendar.getInstance()
        d.timeInMillis = cal.timeInMillis
        d.add(Calendar.DAY_OF_YEAR, -(days - 1 - i))
        val dateStr = String.format("%04d-%02d-%02d", d.get(Calendar.YEAR),
            d.get(Calendar.MONTH) + 1, d.get(Calendar.DAY_OF_MONTH))
        val mins = sessions.filter { it.date == dateStr }.sumOf { it.duration }
        val label = if (period == "week") dayFormat.format(d.time) else dateFormat.format(d.time)
        label to mins
    }
}

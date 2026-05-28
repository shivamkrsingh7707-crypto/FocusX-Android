package com.focusx.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusx.app.data.*
import com.focusx.app.ui.theme.*

@Composable
fun TestsScreen(
    state: FocusXState,
    onStateUpdate: (FocusXState) -> Unit,
) {
    var selectedSubject by remember { mutableStateOf(state.subjects.firstOrNull() ?: "") }
    var score by remember { mutableStateOf("") }
    var total by remember { mutableStateOf("100") }
    var filter by remember { mutableStateOf("all") }
    val focusManager = LocalFocusManager.current

    val stats = computeTestStats(state.tests)
    val filteredTests = if (filter == "all") state.tests else state.tests.filter { it.subject == filter }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Form card
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
                Text("Log Test Score", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var expandedSubject by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .clickable { expandedSubject = true }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(selectedSubject, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                            Text("\u25BC", fontSize = 8.sp, color = Text3Dark)
                        }
                        DropdownMenu(
                            expanded = expandedSubject,
                            onDismissRequest = { expandedSubject = false },
                        ) {
                            state.subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject, fontSize = 13.sp) },
                                    onClick = {
                                        selectedSubject = subject
                                        expandedSubject = false
                                    },
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = score,
                        onValueChange = { score = it.filter { c -> c.isDigit() }.take(4) },
                        placeholder = { Text("Score", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = Accent,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                    )

                    OutlinedTextField(
                        value = total,
                        onValueChange = { total = it.filter { c -> c.isDigit() }.take(4) },
                        placeholder = { Text("Total", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = Accent,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                    )
                }

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        val s = score.toIntOrNull() ?: return@Button
                        val t = total.toIntOrNull() ?: return@Button
                        if (t <= 0 || s < 0 || s > t) return@Button
                        val newTest = TestRecord(
                            subject = selectedSubject,
                            score = s,
                            total = t,
                            date = getToday(),
                            timestamp = System.currentTimeMillis(),
                        )
                        onStateUpdate(state.copy(tests = state.tests + newTest))
                        score = ""
                        focusManager.clearFocus()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                ) { Text("Add Test", fontSize = 13.sp) }
            }
        }

        // Stats row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TestStatCard(stats.avg, "Average", Modifier.weight(1f))
            TestStatCard("${state.tests.size}", "Tests", Modifier.weight(1f))
            TestStatCard(stats.best, "Best", Modifier.weight(1f))
        }

        // History
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
                    Text("History", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        listOf("all" to "All") + state.subjects.map { it to it }.forEach { (key, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (filter == key) Accent else Color.Transparent)
                                    .clickable { filter = key }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium,
                                    color = if (filter == key) Color.White else Text3Dark)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (filteredTests.isEmpty()) {
                    Text("No test scores logged yet.", fontSize = 12.sp, color = Text3Dark,
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center)
                } else {
                    filteredTests.reversed().forEach { test ->
                        val pct = (test.score * 100 / test.total)
                        val isPass = pct >= 60
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(test.subject, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground)
                                Text(test.date, fontSize = 10.sp, color = Text3Dark)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("${test.score}", fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                        color = if (isPass) Success else Danger)
                                    Text("/ ${test.total}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("($pct%)", fontSize = 10.sp, color = Text3Dark)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            onStateUpdate(state.copy(tests = state.tests - test))
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("\u2715", fontSize = 10.sp, color = Text3Dark)
                                }
                            }
                        }
                        if (test != filteredTests.reversed().last()) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outline))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestStatCard(value: String, label: String, modifier: Modifier = Modifier) {
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold,
                color = Text3Dark, letterSpacing = 0.5.sp)
        }
    }
}

data class TestStats(val avg: String, val best: String)

private fun computeTestStats(tests: List<TestRecord>): TestStats {
    if (tests.isEmpty()) return TestStats("--", "--")
    val pcts = tests.map { it.score * 100 / it.total }
    return TestStats(
        avg = "${pcts.average().toInt()}%",
        best = "${pcts.max()}%",
    )
}

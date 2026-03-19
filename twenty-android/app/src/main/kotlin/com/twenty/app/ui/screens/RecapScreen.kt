package com.twenty.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.twenty.app.data.Session
import com.twenty.app.domain.BestDay
import com.twenty.app.domain.CalendarDay
import com.twenty.app.domain.computeStats
import com.twenty.app.domain.formatDurationLong
import com.twenty.app.domain.getBestDayOfWeek
import com.twenty.app.domain.getStreakCalendar
import com.twenty.app.domain.getWeekStart
import com.twenty.app.domain.getMonthStart
import com.twenty.app.ui.components.GlassCard
import com.twenty.app.ui.theme.AccentPrimary
import com.twenty.app.ui.theme.GlassBackground
import com.twenty.app.ui.theme.Success
import com.twenty.app.ui.theme.TextMuted
import com.twenty.app.ui.theme.TextPrimary
import com.twenty.app.ui.theme.TextSecondary
import com.twenty.app.ui.theme.Warning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecapScreen(
    sessions: List<Session>
) {
    var selectedTab by remember { mutableStateOf("week") }

    val filteredSessions = remember(sessions, selectedTab) {
        val start = when (selectedTab) {
            "week" -> getWeekStart()
            "month" -> getMonthStart()
            else -> Date(0)
        }
        sessions.filter { s ->
            Date(s.startTime) >= start
        }
    }

    val stats = remember(filteredSessions) { computeStats(filteredSessions) }
    val streakCalendar = remember(sessions) {
        getStreakCalendar(sessions)
    }
    val bestDay = remember(sessions) {
        getBestDayOfWeek(sessions)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Your Recap", style = MaterialTheme.typography.headlineLarge)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassBackground, RoundedCornerShape(14.dp))
                    .padding(4.dp)
            ) {
                listOf("week", "month", "year").forEach { tab ->
                    Button(
                        onClick = { selectedTab = tab },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (selectedTab == tab) AccentPrimary else Color.Transparent,
                            contentColor = if (selectedTab == tab) Color.White else TextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(tab.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }

        if (filteredSessions.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📊", style = MaterialTheme.typography.displayLarge)
                        Spacer(Modifier.height(16.dp))
                        Text("No sessions yet", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Start your first digital session to see your wellness recap here.",
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return@LazyColumn
        }

        item {
            StreakCalendarView(days = streakCalendar)
        }

        item {
            Text("Overview", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }

        item {
            stats?.let { s ->
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    StatCard(s.count.toString(), "Sessions", modifier = Modifier.weight(1f))
                    StatCard(formatDurationLong(s.totalTime), "Total", modifier = Modifier.weight(1f))
                    StatCard(formatDurationLong(s.longestSession), "Longest", modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            stats?.let { s ->
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) {
                    StatCard(formatDurationLong(s.avgDuration), "Avg", modifier = Modifier.weight(1f))
                    StatCard(s.totalBreaksTaken.toString(), "Breaks", modifier = Modifier.weight(1f))
                    StatCard(
                        if (s.streakDays > 0) "${s.streakDays}d" else "—",
                        "Streak",
                        if (s.streakDays > 0) Warning else null,
                        Modifier.weight(1f)
                    )
                }
            }
        }

        bestDay?.let { day ->
            item {
                Text("Insights", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            item {
                BestDayCard(day)
            }
        }

        stats?.let { s ->
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Compliance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${(s.complianceRate * 100).toInt()}%", color = AccentPrimary)
                        }
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { s.complianceRate },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentPrimary,
                            trackColor = Color(0x1Affffff),
                        )
                    }
                }
            }
        }

        item {
            Text("Recent Sessions", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }

        items(filteredSessions.take(15)) { session ->
            SessionItem(session)
        }
    }
}

@Composable
fun StatCard(
    value: String,
    label: String,
    accent: Color? = null,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent ?: TextPrimary
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun StreakCalendarView(days: List<CalendarDay>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Activity", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(Modifier.height(12.dp))

            val weeks = days.chunked(7)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                weeks.forEach { week ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        week.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            day.hasSession -> AccentPrimary
                                            else -> Color(0x0Dffffff)
                                        }
                                    )
                                    .then(
                                        if (day.isToday) Modifier
                                            .background(Color.Transparent)
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(AccentPrimary)
                                        else Modifier
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(Modifier.width(4.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0x0Dffffff), RoundedCornerShape(2.dp)))
                Box(modifier = Modifier.size(8.dp).background(AccentPrimary, RoundedCornerShape(2.dp)))
                Text("More", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun BestDayCard(day: BestDay) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Best Day", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(day.day, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.weight(1f))
                Text("${day.count} sessions", color = Warning)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val maxCount = day.allDays.maxOfOrNull { it.count } ?: 1
                day.allDays.forEach { dc ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((dc.count.toFloat() / maxCount) * 40).dp.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (dc.count == maxCount && maxCount > 0) Warning
                                    else Color(0x14ffffff)
                                )
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            dc.shortName.first().toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: Session) {
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(dateFormat.format(Date(session.startTime)), style = MaterialTheme.typography.bodyMedium)
                Text(timeFormat.format(Date(session.startTime)), color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatDurationLong(session.durationMs), style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${session.breaksTaken} / ${session.breaksTriggered}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

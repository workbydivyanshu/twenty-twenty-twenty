package com.twenty.app.domain

import java.util.Calendar
import java.util.Date
import com.twenty.app.data.Session

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "$hours:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
    } else {
        "$minutes:${String.format("%02d", seconds)}"
    }
}

fun formatDurationLong(ms: Long): String {
    val minutes = ms / 60000
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}h ${mins}m"
        hours > 0 -> "${hours}h"
        else -> "${mins}m"
    }
}

fun formatNextBreak(seconds: Int): String {
    return if (seconds >= 60) {
        "${seconds / 60}m ${seconds % 60}s"
    } else {
        "${seconds}s"
    }
}

fun getWeekStart(date: Date = Date()): Date {
    val cal = Calendar.getInstance().apply { time = date }
    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun getMonthStart(date: Date = Date()): Date {
    val cal = Calendar.getInstance().apply { time = date }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

data class SessionStats(
    val count: Int,
    val totalTime: Long,
    val totalBreaksTaken: Int,
    val totalBreaksSkipped: Int,
    val totalBreaksTriggered: Int,
    val avgDuration: Long,
    val longestSession: Long,
    val complianceRate: Float,
    val streakDays: Int
)

data class CalendarDay(
    val date: Date,
    val hasSession: Boolean,
    val isToday: Boolean,
    val isWeekStart: Boolean
)

data class BestDay(
    val day: String,
    val shortDay: String,
    val count: Int,
    val dayIndex: Int,
    val allDays: List<DayCount>
)

data class DayCount(
    val name: String,
    val shortName: String,
    val count: Int,
    val dayIndex: Int
)

fun computeStats(sessions: List<Session>): SessionStats? {
    if (sessions.isEmpty()) return null
    val totalTime = sessions.sumOf { it.durationMs }
    val totalBreaksTaken = sessions.sumOf { it.breaksTaken }
    val totalBreaksSkipped = sessions.sumOf { it.breaksSkipped }
    val totalBreaksTriggered = sessions.sumOf { it.breaksTriggered }
    val avgDuration = totalTime / sessions.size
    val longestSession = sessions.maxOfOrNull { it.durationMs } ?: 0L
    val complianceRate = if (totalBreaksTriggered > 0) {
        totalBreaksTaken.toFloat() / totalBreaksTriggered
    } else 0f

    return SessionStats(
        count = sessions.size,
        totalTime = totalTime,
        totalBreaksTaken = totalBreaksTaken,
        totalBreaksSkipped = totalBreaksSkipped,
        totalBreaksTriggered = totalBreaksTriggered,
        avgDuration = avgDuration,
        longestSession = longestSession,
        complianceRate = complianceRate,
        streakDays = computeStreak(sessions)
    )
}

fun computeStreak(sessions: List<Session>): Int {
    if (sessions.isEmpty()) return 0
    val sessionDays = sessions.map {
        Calendar.getInstance().apply { time = Date(it.startTime) }
            .apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            .timeInMillis
    }.toSet()

    var streak = 0
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    for (i in 0 until 365) {
        if (sessionDays.contains(today.timeInMillis)) {
            streak++
            today.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            break
        }
    }
    return streak
}

fun getStreakCalendar(sessions: List<Session>, weeks: Int = 12): List<CalendarDay> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val startCal = today.clone() as Calendar
    startCal.add(Calendar.DAY_OF_MONTH, -(weeks * 7) + 1)

    val sessionDates = sessions.map {
        Calendar.getInstance().apply { time = Date(it.startTime) }
            .apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            .timeInMillis
    }.toSet()

    val days = mutableListOf<CalendarDay>()
    val todayMillis = today.timeInMillis

    for (i in 0 until weeks * 7) {
        val d = startCal.clone() as Calendar
        d.add(Calendar.DAY_OF_MONTH, i)
        val dateMillis = d.timeInMillis
        days.add(CalendarDay(
            date = d.time,
            hasSession = sessionDates.contains(dateMillis),
            isToday = dateMillis == todayMillis,
            isWeekStart = d.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        ))
    }
    return days
}

fun getBestDayOfWeek(sessions: List<Session>): BestDay? {
    val dayCounts = IntArray(7)
    val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    sessions.forEach { s ->
        val day = Calendar.getInstance().apply { time = Date(s.startTime) }.get(Calendar.DAY_OF_WEEK) - 1
        dayCounts[day]++
    }

    val maxCount = dayCounts.maxOrNull() ?: 0
    if (maxCount == 0) return null

    val bestDayIndex = dayCounts.indexOf(maxCount)
    return BestDay(
        day = dayNames[bestDayIndex],
        shortDay = dayNames[bestDayIndex].slice(0..2),
        count = maxCount,
        dayIndex = bestDayIndex,
        allDays = dayNames.mapIndexed { i, name ->
            DayCount(name, name.slice(0..2), dayCounts[i], i)
        }
    )
}

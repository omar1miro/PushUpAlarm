package com.pushup.alarm.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    fun formatTime(hour: Int, minute: Int, use24Hour: Boolean = true): String {
        return if (use24Hour) {
            String.format("%02d:%02d", hour, minute)
        } else {
            val period = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            String.format("%d:%02d %s", displayHour, minute, period)
        }
    }

    fun formatRepeatDays(days: List<Int>): String {
        if (days.isEmpty()) return "Once"
        if (days.size == 7) return "Every day"
        if (days == listOf(1, 2, 3, 4, 5)) return "Weekdays"
        if (days == listOf(6, 7)) return "Weekends"

        val dayNames = mapOf(
            1 to "Mon", 2 to "Tue", 3 to "Wed",
            4 to "Thu", 5 to "Fri", 6 to "Sat", 7 to "Sun"
        )
        return days.sorted().mapNotNull { dayNames[it] }.joinToString(", ")
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}

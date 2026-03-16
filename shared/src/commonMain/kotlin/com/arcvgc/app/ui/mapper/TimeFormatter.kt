package com.arcvgc.app.ui.mapper

import com.arcvgc.app.data.getUtcOffsetSeconds

private val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

private val monthDays = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

private fun isLeapYear(year: Int): Boolean =
    (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

private fun daysInMonth(month: Int, year: Int): Int =
    if (month == 2 && isLeapYear(year)) 29 else monthDays[month - 1]

/**
 * Formats ISO 8601 UTC timestamp (e.g., "2026-02-08T17:03:32") to a friendly local time format.
 * Output: "Feb 8, 5:03 PM" (adjusted to user's time zone)
 */
internal fun formatUploadTime(isoTime: String): String {
    return try {
        val parts = isoTime.split("T")
        if (parts.size != 2) return isoTime

        val dateParts = parts[0].split("-")
        // Strip timezone suffix (Z, +HH:MM, -HH:MM) from time part for parsing
        val timeString = parts[1].replace("Z", "").split("+")[0].let { s ->
            // Handle negative offset in time part (e.g., "17:03:32-05:00")
            // Only split on '-' if it appears after the seconds
            val lastDash = s.lastIndexOf('-')
            if (lastDash > 0) s.substring(0, lastDash) else s
        }
        val timeParts = timeString.split(":")
        if (dateParts.size != 3 || timeParts.size < 2) return isoTime

        var year = dateParts[0].toIntOrNull() ?: return isoTime
        var month = dateParts[1].toIntOrNull() ?: return isoTime
        var day = dateParts[2].toIntOrNull() ?: return isoTime
        var hour = timeParts[0].toIntOrNull() ?: return isoTime
        var minute = timeParts[1].toIntOrNull() ?: return isoTime

        // Apply local UTC offset
        val offsetSeconds = getUtcOffsetSeconds()
        val offsetMinutes = offsetSeconds / 60
        minute += offsetMinutes % 60
        hour += offsetMinutes / 60

        // Normalize minutes overflow/underflow
        if (minute >= 60) {
            minute -= 60
            hour += 1
        } else if (minute < 0) {
            minute += 60
            hour -= 1
        }

        // Normalize hours overflow/underflow
        if (hour >= 24) {
            hour -= 24
            day += 1
            if (day > daysInMonth(month, year)) {
                day = 1
                month += 1
                if (month > 12) {
                    month = 1
                    year += 1
                }
            }
        } else if (hour < 0) {
            hour += 24
            day -= 1
            if (day < 1) {
                month -= 1
                if (month < 1) {
                    month = 12
                    year -= 1
                }
                day = daysInMonth(month, year)
            }
        }

        val monthName = monthNames.getOrNull(month - 1) ?: return isoTime

        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (hour < 12) "AM" else "PM"

        "$monthName $day, $hour12:${minute.toString().padStart(2, '0')} $amPm"
    } catch (_: Exception) {
        isoTime
    }
}

package com.example.showdown26.ui.mapper

/**
 * Formats ISO 8601 timestamp (e.g., "2026-02-08T17:03:32") to a friendly format.
 * Output: "Feb 8, 5:03 PM"
 */
internal fun formatUploadTime(isoTime: String): String {
    return try {
        val parts = isoTime.split("T")
        if (parts.size != 2) return isoTime

        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        if (dateParts.size != 3 || timeParts.size < 2) return isoTime

        val month = dateParts[1].toIntOrNull() ?: return isoTime
        val day = dateParts[2].toIntOrNull() ?: return isoTime
        val hour = timeParts[0].toIntOrNull() ?: return isoTime
        val minute = timeParts[1].toIntOrNull() ?: return isoTime

        val monthName = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        ).getOrNull(month - 1) ?: return isoTime

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

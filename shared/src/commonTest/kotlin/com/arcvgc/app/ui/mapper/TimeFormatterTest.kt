package com.arcvgc.app.ui.mapper

import com.arcvgc.app.data.getUtcOffsetSeconds
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatterTest {

    /**
     * Computes the expected formatted string by manually applying the UTC offset,
     * mirroring the production code logic.
     */
    private fun expectedLocal(isoUtc: String): String {
        // Parse the UTC timestamp the same way production code does
        val parts = isoUtc.split("T")
        val dateParts = parts[0].split("-")
        val timeString = parts[1].replace("Z", "").split("+")[0].let { s ->
            val lastDash = s.lastIndexOf('-')
            if (lastDash > 0) s.substring(0, lastDash) else s
        }
        val timeParts = timeString.split(":")

        var year = dateParts[0].toInt()
        var month = dateParts[1].toInt()
        var day = dateParts[2].toInt()
        var hour = timeParts[0].toInt()
        var minute = timeParts[1].toInt()

        val offsetSeconds = getUtcOffsetSeconds()
        val offsetMinutes = offsetSeconds / 60
        minute += offsetMinutes % 60
        hour += offsetMinutes / 60

        if (minute >= 60) { minute -= 60; hour += 1 }
        else if (minute < 0) { minute += 60; hour -= 1 }

        val monthDays = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        fun isLeapYear(y: Int) = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)
        fun daysInMonth(m: Int, y: Int) = if (m == 2 && isLeapYear(y)) 29 else monthDays[m - 1]

        if (hour >= 24) {
            hour -= 24; day += 1
            if (day > daysInMonth(month, year)) { day = 1; month += 1; if (month > 12) { month = 1; year += 1 } }
        } else if (hour < 0) {
            hour += 24; day -= 1
            if (day < 1) { month -= 1; if (month < 1) { month = 12; year -= 1 }; day = daysInMonth(month, year) }
        }

        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthName = monthNames[month - 1]
        val hour12 = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
        val amPm = if (hour < 12) "AM" else "PM"
        return "$monthName $day, $hour12:${minute.toString().padStart(2, '0')} $amPm"
    }

    @Test
    fun standardAfternoonTime() {
        assertEquals(expectedLocal("2026-02-08T17:03:32"), formatUploadTime("2026-02-08T17:03:32"))
    }

    @Test
    fun midnightHour() {
        assertEquals(expectedLocal("2026-01-15T00:30:00"), formatUploadTime("2026-01-15T00:30:00"))
    }

    @Test
    fun noonHour() {
        assertEquals(expectedLocal("2026-06-01T12:00:00"), formatUploadTime("2026-06-01T12:00:00"))
    }

    @Test
    fun singleDigitMinutePadding() {
        assertEquals(expectedLocal("2026-03-10T09:05:00"), formatUploadTime("2026-03-10T09:05:00"))
    }

    @Test
    fun invalidInputReturnsRawString() {
        assertEquals("not-a-date", formatUploadTime("not-a-date"))
    }

    @Test
    fun missingTSeparatorReturnsRawString() {
        val input = "2026-02-08 17:03:32"
        assertEquals(input, formatUploadTime(input))
    }

    @Test
    fun timestampWithZSuffix() {
        assertEquals(expectedLocal("2026-01-01T00:00:00Z"), formatUploadTime("2026-01-01T00:00:00Z"))
    }

    @Test
    fun timestampWithoutZSuffixTreatedAsUtc() {
        assertEquals(
            formatUploadTime("2026-12-25T08:00:00Z"),
            formatUploadTime("2026-12-25T08:00:00")
        )
    }

    @Test
    fun hour11IsAM() {
        assertEquals(expectedLocal("2026-03-01T11:00:00"), formatUploadTime("2026-03-01T11:00:00"))
    }

    @Test
    fun hour12IsPM() {
        assertEquals(expectedLocal("2026-03-01T12:00:00"), formatUploadTime("2026-03-01T12:00:00"))
    }

    @Test
    fun hour13Is1PM() {
        assertEquals(expectedLocal("2026-03-01T13:00:00"), formatUploadTime("2026-03-01T13:00:00"))
    }
}

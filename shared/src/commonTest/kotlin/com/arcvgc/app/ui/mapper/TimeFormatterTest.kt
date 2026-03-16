package com.arcvgc.app.ui.mapper

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatterTest {

    @Test
    fun standardAfternoonTime() {
        assertEquals("Feb 8, 5:03 PM", formatUploadTime("2026-02-08T17:03:32"))
    }

    @Test
    fun midnightHour() {
        assertEquals("Jan 15, 12:30 AM", formatUploadTime("2026-01-15T00:30:00"))
    }

    @Test
    fun noonHour() {
        assertEquals("Jun 1, 12:00 PM", formatUploadTime("2026-06-01T12:00:00"))
    }

    @Test
    fun singleDigitMinutePadding() {
        assertEquals("Mar 10, 9:05 AM", formatUploadTime("2026-03-10T09:05:00"))
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
    fun januaryMonth() {
        assertEquals("Jan 1, 12:00 AM", formatUploadTime("2026-01-01T00:00:00"))
    }

    @Test
    fun decemberMonth() {
        assertEquals("Dec 25, 8:00 AM", formatUploadTime("2026-12-25T08:00:00"))
    }

    @Test
    fun hour11IsAM() {
        assertEquals("Mar 1, 11:00 AM", formatUploadTime("2026-03-01T11:00:00"))
    }

    @Test
    fun hour12IsPM() {
        assertEquals("Mar 1, 12:00 PM", formatUploadTime("2026-03-01T12:00:00"))
    }

    @Test
    fun hour13Is1PM() {
        assertEquals("Mar 1, 1:00 PM", formatUploadTime("2026-03-01T13:00:00"))
    }
}

package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LookbackWindowTest {

    @Test
    fun fromValue_all_returnsAll() {
        assertEquals(LookbackWindow.All, LookbackWindow.fromValue("all"))
    }

    @Test
    fun fromValue_week_returnsWeek() {
        assertEquals(LookbackWindow.Week, LookbackWindow.fromValue("week"))
    }

    @Test
    fun fromValue_day_returnsDay() {
        assertEquals(LookbackWindow.Day, LookbackWindow.fromValue("day"))
    }

    @Test
    fun fromValue_thirtyDays_returnsThirtyDays() {
        assertEquals(LookbackWindow.ThirtyDays, LookbackWindow.fromValue("30days"))
    }

    @Test
    fun fromValue_null_returnsNull() {
        assertNull(LookbackWindow.fromValue(null))
    }

    @Test
    fun fromValue_unknownString_returnsNull() {
        assertNull(LookbackWindow.fromValue("month"))
        assertNull(LookbackWindow.fromValue(""))
        assertNull(LookbackWindow.fromValue("30 days"))
    }

    @Test
    fun fromValue_isCaseSensitive() {
        assertNull(LookbackWindow.fromValue("All"))
        assertNull(LookbackWindow.fromValue("WEEK"))
    }

    @Test
    fun value_matchesSerializedName() {
        assertEquals("all", LookbackWindow.All.value)
        assertEquals("week", LookbackWindow.Week.value)
        assertEquals("day", LookbackWindow.Day.value)
        assertEquals("30days", LookbackWindow.ThirtyDays.value)
    }
}

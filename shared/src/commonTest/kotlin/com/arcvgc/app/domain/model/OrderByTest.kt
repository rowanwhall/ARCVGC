package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OrderByTest {

    @Test
    fun fromValue_time_returnsTime() {
        assertEquals(OrderBy.Time, OrderBy.fromValue("time"))
    }

    @Test
    fun fromValue_rating_returnsRating() {
        assertEquals(OrderBy.Rating, OrderBy.fromValue("rating"))
    }

    @Test
    fun fromValue_null_returnsNull() {
        assertNull(OrderBy.fromValue(null))
    }

    @Test
    fun fromValue_unknownString_returnsNull() {
        assertNull(OrderBy.fromValue("date"))
        assertNull(OrderBy.fromValue(""))
    }

    @Test
    fun fromValue_isCaseSensitive() {
        assertNull(OrderBy.fromValue("Rating"))
        assertNull(OrderBy.fromValue("TIME"))
    }

    @Test
    fun value_matchesSerializedName() {
        assertEquals("time", OrderBy.Time.value)
        assertEquals("rating", OrderBy.Rating.value)
    }
}

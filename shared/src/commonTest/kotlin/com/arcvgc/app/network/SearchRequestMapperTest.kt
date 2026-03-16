package com.arcvgc.app.network

import com.arcvgc.app.testutil.testSearchFilterSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchRequestMapperTest {

    @Test
    fun basicRequestWithOneFilterAndFormat() {
        val filter = testSearchFilterSlot(pokemonId = 25)
        val result = buildSearchRequest(
            filters = listOf(filter),
            formatId = 1,
            orderBy = "time",
            limit = 50,
            page = 1
        )

        assertEquals(1, result.formatId)
        assertEquals("time", result.orderBy)
        assertEquals(50, result.limit)
        assertEquals(1, result.page)
        assertEquals(1, result.pokemon.size)
        assertEquals(25, result.pokemon[0].id)
    }

    @Test
    fun noRatingFieldsProducesNullRating() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            orderBy = "time",
            limit = 50,
            page = 1
        )

        assertNull(result.rating)
    }

    @Test
    fun onlyMinimumRatingSet() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            minimumRating = 1500,
            orderBy = "time",
            limit = 50,
            page = 1
        )

        assertEquals(1500, result.rating?.min)
        assertNull(result.rating?.max)
        assertNull(result.rating?.unratedOnly)
    }

    @Test
    fun onlyMaximumRatingSet() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            maximumRating = 1800,
            orderBy = "time",
            limit = 50,
            page = 1
        )

        assertNull(result.rating?.min)
        assertEquals(1800, result.rating?.max)
        assertNull(result.rating?.unratedOnly)
    }

    @Test
    fun unratedOnlyTrue() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            unratedOnly = true,
            orderBy = "time",
            limit = 50,
            page = 1
        )

        assertTrue(result.rating?.unratedOnly == true)
    }

    @Test
    fun bothMinAndMaxRating() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            minimumRating = 1200,
            maximumRating = 1800,
            orderBy = "time",
            limit = 50,
            page = 1
        )

        assertEquals(1200, result.rating?.min)
        assertEquals(1800, result.rating?.max)
    }

    @Test
    fun noTimeRangeWhenBothNull() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            orderBy = "time",
            limit = 50,
            page = 1
        )

        assertNull(result.timeRange)
    }

    @Test
    fun bothStartAndEndCreatesTimeRange() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            orderBy = "time",
            limit = 50,
            page = 1,
            timeRangeStart = 1000L,
            timeRangeEnd = 2000L
        )

        assertEquals(1000L, result.timeRange?.start)
        assertEquals(2000L, result.timeRange?.end)
    }

    @Test
    fun onlyStartWithoutEndProducesNullTimeRange() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            orderBy = "time",
            limit = 50,
            page = 1,
            timeRangeStart = 1000L,
            timeRangeEnd = null
        )

        assertNull(result.timeRange)
    }

    @Test
    fun blankPlayerNameBecomesNull() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            orderBy = "time",
            limit = 50,
            page = 1,
            playerName = "   "
        )

        assertNull(result.playerName)
    }

    @Test
    fun nonBlankPlayerNamePassedThrough() {
        val result = buildSearchRequest(
            filters = emptyList(),
            formatId = 1,
            orderBy = "time",
            limit = 50,
            page = 1,
            playerName = "Wolfe"
        )

        assertEquals("Wolfe", result.playerName)
    }

    @Test
    fun multipleFiltersWithItemAndTeraType() {
        val filters = listOf(
            testSearchFilterSlot(pokemonId = 25, itemId = 10, teraTypeId = 3),
            testSearchFilterSlot(pokemonId = 150, itemId = null, teraTypeId = 5),
            testSearchFilterSlot(pokemonId = 6, itemId = 7, teraTypeId = null)
        )

        val result = buildSearchRequest(
            filters = filters,
            formatId = 2,
            orderBy = "rating",
            limit = 25,
            page = 3
        )

        assertEquals(3, result.pokemon.size)

        assertEquals(25, result.pokemon[0].id)
        assertEquals(10, result.pokemon[0].itemId)
        assertEquals(3, result.pokemon[0].teraTypeId)

        assertEquals(150, result.pokemon[1].id)
        assertNull(result.pokemon[1].itemId)
        assertEquals(5, result.pokemon[1].teraTypeId)

        assertEquals(6, result.pokemon[2].id)
        assertEquals(7, result.pokemon[2].itemId)
        assertNull(result.pokemon[2].teraTypeId)
    }
}

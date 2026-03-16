package com.arcvgc.app.domain.model

import com.arcvgc.app.testutil.testSearchFilterSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchParamsTest {

    private fun baseParams(
        filters: List<SearchFilterSlot> = emptyList(),
        minimumRating: Int? = null,
        maximumRating: Int? = null,
        unratedOnly: Boolean = false,
        playerName: String? = null,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null
    ) = SearchParams(
        filters = filters,
        formatId = 1,
        minimumRating = minimumRating,
        maximumRating = maximumRating,
        unratedOnly = unratedOnly,
        orderBy = "date",
        playerName = playerName,
        timeRangeStart = timeRangeStart,
        timeRangeEnd = timeRangeEnd
    )

    @Test
    fun canRemovePokemonAt_singleFilterNoOtherCriteria_false() {
        val params = baseParams(filters = listOf(testSearchFilterSlot()))
        assertFalse(params.canRemovePokemonAt(0))
    }

    @Test
    fun canRemovePokemonAt_singleFilterWithMinRating_true() {
        val params = baseParams(
            filters = listOf(testSearchFilterSlot()),
            minimumRating = 1500
        )
        assertTrue(params.canRemovePokemonAt(0))
    }

    @Test
    fun canRemovePokemonAt_twoFilters_true() {
        val params = baseParams(
            filters = listOf(
                testSearchFilterSlot(pokemonId = 1),
                testSearchFilterSlot(pokemonId = 2)
            )
        )
        assertTrue(params.canRemovePokemonAt(0))
    }

    @Test
    fun removePokemonAt_returnsCopyWithoutThatIndex() {
        val filter1 = testSearchFilterSlot(pokemonId = 1, pokemonName = "Pikachu")
        val filter2 = testSearchFilterSlot(pokemonId = 2, pokemonName = "Charizard")
        val params = baseParams(filters = listOf(filter1, filter2))

        val result = params.removePokemonAt(0)
        assertEquals(listOf(filter2), result.filters)
    }

    @Test
    fun canRemoveMinRating_onlyCriterion_false() {
        val params = baseParams(minimumRating = 1500)
        assertFalse(params.canRemoveMinRating())
    }

    @Test
    fun canRemoveMinRating_filtersExist_true() {
        val params = baseParams(
            filters = listOf(testSearchFilterSlot()),
            minimumRating = 1500
        )
        assertTrue(params.canRemoveMinRating())
    }

    @Test
    fun canRemoveMaxRating_onlyCriterion_false() {
        val params = baseParams(maximumRating = 1800)
        assertFalse(params.canRemoveMaxRating())
    }

    @Test
    fun canRemoveUnrated_onlyCriterion_false() {
        val params = baseParams(unratedOnly = true)
        assertFalse(params.canRemoveUnrated())
    }

    @Test
    fun canRemoveUnrated_filtersExist_true() {
        val params = baseParams(
            filters = listOf(testSearchFilterSlot()),
            unratedOnly = true
        )
        assertTrue(params.canRemoveUnrated())
    }

    @Test
    fun canRemovePlayerName_onlyCriterion_false() {
        val params = baseParams(playerName = "Ash")
        assertFalse(params.canRemovePlayerName())
    }

    @Test
    fun canRemovePlayerName_filtersExist_true() {
        val params = baseParams(
            filters = listOf(testSearchFilterSlot()),
            playerName = "Ash"
        )
        assertTrue(params.canRemovePlayerName())
    }

    @Test
    fun canRemoveTimeRange_onlyCriterion_false() {
        val params = baseParams(timeRangeStart = 1000L, timeRangeEnd = 2000L)
        assertFalse(params.canRemoveTimeRange())
    }

    @Test
    fun canRemoveTimeRange_filtersExist_true() {
        val params = baseParams(
            filters = listOf(testSearchFilterSlot()),
            timeRangeStart = 1000L,
            timeRangeEnd = 2000L
        )
        assertTrue(params.canRemoveTimeRange())
    }
}

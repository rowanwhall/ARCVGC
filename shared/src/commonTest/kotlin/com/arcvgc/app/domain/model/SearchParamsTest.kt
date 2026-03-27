package com.arcvgc.app.domain.model

import com.arcvgc.app.testutil.testSearchFilterSlot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchParamsTest {

    private fun baseParams(
        filters: List<SearchFilterSlot> = emptyList(),
        team2Filters: List<SearchFilterSlot> = emptyList(),
        minimumRating: Int? = null,
        maximumRating: Int? = null,
        unratedOnly: Boolean = false,
        playerName: String? = null,
        timeRangeStart: Long? = null,
        timeRangeEnd: Long? = null
    ) = SearchParams(
        filters = filters,
        team2Filters = team2Filters,
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

    // --- Team 2 ---

    @Test
    fun canRemovePokemonAt_singleFilterWithTeam2_true() {
        val params = baseParams(
            filters = listOf(testSearchFilterSlot(pokemonId = 1)),
            team2Filters = listOf(testSearchFilterSlot(pokemonId = 2))
        )
        assertTrue(params.canRemovePokemonAt(0))
    }

    @Test
    fun removePokemonAt_promotesTeam2WhenTeam1Empty() {
        val team2Filter = testSearchFilterSlot(pokemonId = 2, pokemonName = "Charizard")
        val params = baseParams(
            filters = listOf(testSearchFilterSlot(pokemonId = 1)),
            team2Filters = listOf(team2Filter)
        )

        val result = params.removePokemonAt(0)
        assertEquals(listOf(team2Filter), result.filters)
        assertTrue(result.team2Filters.isEmpty())
    }

    @Test
    fun canRemoveTeam2PokemonAt_singleTeam2WithTeam1_true() {
        val params = baseParams(
            filters = listOf(testSearchFilterSlot(pokemonId = 1)),
            team2Filters = listOf(testSearchFilterSlot(pokemonId = 2))
        )
        assertTrue(params.canRemoveTeam2PokemonAt(0))
    }

    @Test
    fun canRemoveTeam2PokemonAt_singleTeam2NoOtherCriteria_false() {
        val params = baseParams(
            team2Filters = listOf(testSearchFilterSlot(pokemonId = 2))
        )
        assertFalse(params.canRemoveTeam2PokemonAt(0))
    }

    @Test
    fun removeTeam2PokemonAt_removesFromTeam2() {
        val filter1 = testSearchFilterSlot(pokemonId = 1)
        val filter2 = testSearchFilterSlot(pokemonId = 2)
        val params = baseParams(
            filters = listOf(testSearchFilterSlot(pokemonId = 10)),
            team2Filters = listOf(filter1, filter2)
        )

        val result = params.removeTeam2PokemonAt(0)
        assertEquals(listOf(filter2), result.team2Filters)
    }
}

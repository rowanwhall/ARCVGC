package com.arcvgc.app.domain.model

data class SearchParams(
    val filters: List<SearchFilterSlot>,
    val team2Filters: List<SearchFilterSlot> = emptyList(),
    val formatId: Int,
    val minimumRating: Int? = null,
    val maximumRating: Int? = null,
    val unratedOnly: Boolean = false,
    val orderBy: String,
    val timeRangeStart: Long? = null,
    val timeRangeEnd: Long? = null,
    val playerName: String? = null,
    val formatName: String? = null,
    val winnerFilter: WinnerFilter = WinnerFilter.NONE
) {
    private val hasTimeRange: Boolean get() = timeRangeStart != null && timeRangeEnd != null

    private val isValid: Boolean
        get() = filters.isNotEmpty()
                || team2Filters.isNotEmpty()
                || minimumRating != null
                || maximumRating != null
                || unratedOnly
                || !playerName.isNullOrBlank()
                || hasTimeRange

    fun canRemovePokemonAt(index: Int): Boolean =
        copy(filters = filters.filterIndexed { i, _ -> i != index }).isValid

    fun removePokemonAt(index: Int): SearchParams {
        val newFilters = filters.filterIndexed { i, _ -> i != index }
        // Promote team2 to team1 if team1 is now empty
        return if (newFilters.isEmpty() && team2Filters.isNotEmpty()) {
            copy(
                filters = team2Filters,
                team2Filters = emptyList(),
                winnerFilter = if (winnerFilter == WinnerFilter.TEAM1) WinnerFilter.TEAM1
                    else WinnerFilter.NONE
            )
        } else {
            copy(filters = newFilters)
        }
    }

    fun canRemoveTeam2PokemonAt(index: Int): Boolean =
        copy(team2Filters = team2Filters.filterIndexed { i, _ -> i != index }).isValid

    fun removeTeam2PokemonAt(index: Int): SearchParams {
        val newFilters = team2Filters.filterIndexed { i, _ -> i != index }
        return copy(
            team2Filters = newFilters,
            winnerFilter = if (newFilters.isEmpty() && winnerFilter == WinnerFilter.TEAM2)
                WinnerFilter.NONE else winnerFilter
        )
    }

    fun canRemoveMinRating(): Boolean =
        copy(minimumRating = null).isValid

    fun removeMinRating(): SearchParams =
        copy(minimumRating = null)

    fun canRemoveMaxRating(): Boolean =
        copy(maximumRating = null).isValid

    fun removeMaxRating(): SearchParams =
        copy(maximumRating = null)

    fun canRemoveUnrated(): Boolean =
        copy(unratedOnly = false).isValid

    fun removeUnrated(): SearchParams =
        copy(unratedOnly = false)

    fun canRemovePlayerName(): Boolean =
        copy(playerName = null).isValid

    fun removePlayerName(): SearchParams =
        copy(playerName = null)

    fun canRemoveTimeRange(): Boolean =
        copy(timeRangeStart = null, timeRangeEnd = null).isValid

    fun removeTimeRange(): SearchParams =
        copy(timeRangeStart = null, timeRangeEnd = null)
}

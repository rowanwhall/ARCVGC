package com.arcvgc.app.network

import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.WinnerFilter
import com.arcvgc.app.network.model.RatingDto
import com.arcvgc.app.network.model.SearchPokemonDto
import com.arcvgc.app.network.model.SearchRequestDto
import com.arcvgc.app.network.model.SearchTeamDto
import com.arcvgc.app.network.model.TimeRangeDto

fun buildSearchRequest(
    filters: List<SearchFilterSlot>,
    formatId: Int,
    minimumRating: Int? = null,
    maximumRating: Int? = null,
    unratedOnly: Boolean = false,
    orderBy: String,
    limit: Int,
    page: Int,
    timeRangeStart: Long? = null,
    timeRangeEnd: Long? = null,
    playerName: String? = null,
    playerId: Int? = null,
    team2Filters: List<SearchFilterSlot> = emptyList(),
    winnerFilter: WinnerFilter = WinnerFilter.NONE
): SearchRequestDto {
    val ratingDto = if (minimumRating != null || maximumRating != null || unratedOnly) {
        RatingDto(
            min = minimumRating,
            max = maximumRating,
            unratedOnly = if (unratedOnly) true else null
        )
    } else null

    val timeRangeDto = if (timeRangeStart != null && timeRangeEnd != null) {
        TimeRangeDto(start = timeRangeStart, end = timeRangeEnd)
    } else null

    val resolvedPlayerName = playerName?.takeIf { it.isNotBlank() }

    val team1IsWinner = if (winnerFilter == WinnerFilter.TEAM1) true else null
    val team2IsWinner = if (winnerFilter == WinnerFilter.TEAM2) true else null

    val team1Dto = if (filters.isNotEmpty() || team1IsWinner != null) {
        SearchTeamDto(
            pokemon = filters.map { it.toSearchPokemonDto() }.ifEmpty { null },
            isWinner = team1IsWinner
        )
    } else null

    val team2Dto = if (team2Filters.isNotEmpty() || team2IsWinner != null) {
        SearchTeamDto(
            pokemon = team2Filters.map { it.toSearchPokemonDto() }.ifEmpty { null },
            isWinner = team2IsWinner
        )
    } else null

    return SearchRequestDto(
        limit = limit,
        page = page,
        formatId = formatId,
        rating = ratingDto,
        orderBy = orderBy,
        pokemon = emptyList(),
        timeRange = timeRangeDto,
        playerName = resolvedPlayerName,
        playerId = playerId,
        team1 = team1Dto,
        team2 = team2Dto
    )
}

private fun SearchFilterSlot.toSearchPokemonDto() = SearchPokemonDto(
    id = pokemonId,
    itemId = itemId,
    teraTypeId = teraTypeId,
    abilityId = abilityId
)

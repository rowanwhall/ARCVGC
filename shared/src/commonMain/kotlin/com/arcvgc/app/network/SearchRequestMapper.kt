package com.arcvgc.app.network

import com.arcvgc.app.domain.model.SearchFilterSlot
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
    team2Filters: List<SearchFilterSlot> = emptyList()
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

    val team1Dto = if (filters.isNotEmpty()) {
        SearchTeamDto(pokemon = filters.map { it.toSearchPokemonDto() })
    } else null

    val team2Dto = if (team2Filters.isNotEmpty()) {
        SearchTeamDto(pokemon = team2Filters.map { it.toSearchPokemonDto() })
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

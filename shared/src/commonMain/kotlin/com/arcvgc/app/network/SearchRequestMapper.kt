package com.arcvgc.app.network

import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.network.model.RatingDto
import com.arcvgc.app.network.model.SearchPokemonDto
import com.arcvgc.app.network.model.SearchRequestDto
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
    playerId: Int? = null
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

    return SearchRequestDto(
        limit = limit,
        page = page,
        formatId = formatId,
        rating = ratingDto,
        orderBy = orderBy,
        pokemon = filters.map {
            SearchPokemonDto(
                id = it.pokemonId,
                itemId = it.itemId,
                teraTypeId = it.teraTypeId,
                abilityId = it.abilityId
            )
        },
        timeRange = timeRangeDto,
        playerName = resolvedPlayerName,
        playerId = playerId
    )
}

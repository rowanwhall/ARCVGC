package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.MostUsedPokemon
import com.arcvgc.app.domain.model.PlayerProfile
import com.arcvgc.app.domain.model.RatedMatch
import com.arcvgc.app.network.model.MostUsedPokemonDto
import com.arcvgc.app.network.model.PlayerProfileDto
import com.arcvgc.app.network.model.RatedMatchDto
import com.arcvgc.app.network.normalizeImageUrl

fun RatedMatchDto.toDomain(): RatedMatch {
    return RatedMatch(id = id, rating = rating)
}

fun MostUsedPokemonDto.toDomain(): MostUsedPokemon {
    return MostUsedPokemon(
        id = id,
        name = name,
        usageCount = usageCount,
        imageUrl = normalizeImageUrl(imageUrl)
    )
}

fun PlayerProfileDto.toDomain(): PlayerProfile {
    return PlayerProfile(
        id = id,
        name = name,
        matchCount = matchCount,
        winCount = winCount,
        topRatedMatch = topRatedMatch?.toDomain(),
        mostRecentRatedMatch = mostRecentRatedMatch?.toDomain(),
        mostUsedPokemon = mostUsedPokemon.map { it.toDomain() }
    )
}

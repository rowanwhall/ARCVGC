package com.example.showdown26.network.mapper

import com.example.showdown26.domain.model.MostUsedPokemon
import com.example.showdown26.domain.model.PlayerProfile
import com.example.showdown26.domain.model.RatedMatch
import com.example.showdown26.network.model.MostUsedPokemonDto
import com.example.showdown26.network.model.PlayerProfileDto
import com.example.showdown26.network.model.RatedMatchDto
import com.example.showdown26.network.normalizeImageUrl

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

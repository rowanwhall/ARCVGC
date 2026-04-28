package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.FormatDetail
import com.arcvgc.app.domain.model.TopPokemon
import com.arcvgc.app.network.normalizeImageUrl
import com.arcvgc.app.network.model.FormatDetailDto
import com.arcvgc.app.network.model.TopPokemonDto

fun FormatDetailDto.toDomain(): FormatDetail {
    return FormatDetail(
        id = id,
        name = name,
        formattedName = formattedName,
        matchCount = matchCount,
        teamCount = teamCount,
        topPokemon = topPokemon?.map { it.toDomain() }.orEmpty(),
        isHistoric = isHistoric,
        isOpenTeamsheet = isOpenTeamsheet,
        isOfficial = isOfficial,
        hasSeries = hasSeries
    )
}

fun TopPokemonDto.toDomain(): TopPokemon {
    return TopPokemon(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        types = types.map { it.toDomain() },
        imageUrl = normalizeImageUrl(imageUrl),
        count = count
    )
}

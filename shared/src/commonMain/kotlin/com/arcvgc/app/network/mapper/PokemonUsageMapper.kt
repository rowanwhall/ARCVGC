package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.PokemonUsage
import com.arcvgc.app.domain.model.PokemonUsageStats
import com.arcvgc.app.network.normalizeImageUrl
import com.arcvgc.app.network.model.PokemonUsageDataDto
import com.arcvgc.app.network.model.PokemonUsageDto

fun PokemonUsageDataDto.toDomain(): PokemonUsageStats {
    return PokemonUsageStats(
        prevPeriodTotalTeams = prevPeriodTotalTeams,
        currentPeriodTotalTeams = currentPeriodTotalTeams,
        increased = increased?.map { it.toDomain() }.orEmpty(),
        decreased = decreased?.map { it.toDomain() }.orEmpty()
    )
}

fun PokemonUsageDto.toDomain(): PokemonUsage {
    return PokemonUsage(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        imageUrl = normalizeImageUrl(imageUrl),
        prevPeriodTeamCount = prevPeriodTeamCount,
        prevPeriodTeamPercent = prevPeriodTeamPercent,
        currentPeriodTeamCount = currentPeriodTeamCount,
        currentPeriodTeamPercent = currentPeriodTeamPercent,
        usageChangePercent = usageChangePercent
    )
}

package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.PokemonProfile
import com.arcvgc.app.domain.model.TopStatAbility
import com.arcvgc.app.domain.model.TopStatItem
import com.arcvgc.app.domain.model.TopStatMove
import com.arcvgc.app.domain.model.TopStatTeammate
import com.arcvgc.app.domain.model.TopStatTeraType
import com.arcvgc.app.network.model.PokemonProfileDto
import com.arcvgc.app.network.model.TopAbilityDto
import com.arcvgc.app.network.model.TopItemDto
import com.arcvgc.app.network.model.TopMoveDto
import com.arcvgc.app.network.model.TopTeammateDto
import com.arcvgc.app.network.model.TopTeraTypeDto
import com.arcvgc.app.network.normalizeImageUrl

fun PokemonProfileDto.toDomain(): PokemonProfile {
    return PokemonProfile(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        tier = tier,
        types = types.map { it.toDomain() },
        imageUrl = normalizeImageUrl(imageUrl),
        baseSpecies = baseSpecies?.toDomain(),
        matchCount = matchCount,
        teamCount = teamCount,
        matchPercent = matchPercent,
        topItems = topItems.orEmpty().map { it.toDomain() },
        topTeraTypes = topTeraTypes.orEmpty().map { it.toDomain() },
        topMoves = topMoves.orEmpty().map { it.toDomain() },
        topAbilities = topAbilities.orEmpty().map { it.toDomain() },
        topTeammates = topTeammates.orEmpty().map { it.toDomain() }
    )
}

fun TopItemDto.toDomain(): TopStatItem {
    return TopStatItem(count = count, id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
}

fun TopTeraTypeDto.toDomain(): TopStatTeraType {
    return TopStatTeraType(count = count, id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
}

fun TopMoveDto.toDomain(): TopStatMove {
    return TopStatMove(count = count, id = id, name = name)
}

fun TopAbilityDto.toDomain(): TopStatAbility {
    return TopStatAbility(count = count, id = id, name = name)
}

fun TopTeammateDto.toDomain(): TopStatTeammate {
    return TopStatTeammate(
        count = count,
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        imageUrl = normalizeImageUrl(imageUrl)
    )
}

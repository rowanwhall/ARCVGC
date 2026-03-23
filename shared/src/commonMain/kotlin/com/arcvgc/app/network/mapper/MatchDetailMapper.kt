package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.Ability
import com.arcvgc.app.domain.model.BaseSpecies
import com.arcvgc.app.domain.model.DomainItem
import com.arcvgc.app.domain.model.MatchDetail
import com.arcvgc.app.domain.model.Move
import com.arcvgc.app.domain.model.PlayerDetail
import com.arcvgc.app.domain.model.SetMatch
import com.arcvgc.app.domain.model.PokemonDetail
import com.arcvgc.app.domain.model.PokemonType
import com.arcvgc.app.domain.model.TeraType
import com.arcvgc.app.network.model.AbilityDto
import com.arcvgc.app.network.model.BaseSpeciesDto
import com.arcvgc.app.network.model.MatchDetailDto
import com.arcvgc.app.network.model.MoveDto
import com.arcvgc.app.network.model.SetMatchDto
import com.arcvgc.app.network.model.NetworkItemDto
import com.arcvgc.app.network.model.PlayerDetailDto
import com.arcvgc.app.network.model.PokemonDetailDto
import com.arcvgc.app.network.model.TeraTypeDto
import com.arcvgc.app.network.normalizeImageUrl
import com.arcvgc.app.network.model.TypeDto

fun AbilityDto.toDomain(): Ability {
    return Ability(id = id, name = name)
}

fun MoveDto.toDomain(): Move {
    return Move(id = id, name = name)
}

fun TypeDto.toDomain(): PokemonType {
    return PokemonType(id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
}

fun TeraTypeDto.toDomain(): TeraType? {
    val teraId = id ?: return null
    val teraName = name ?: return null
    return TeraType(id = teraId, name = teraName, imageUrl = normalizeImageUrl(imageUrl))
}

fun BaseSpeciesDto.toDomain(): BaseSpecies {
    return BaseSpecies(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber
    )
}

fun NetworkItemDto.toDomainItem(): DomainItem? {
    if (id == null || name == null) return null
    return DomainItem(id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
}

fun PokemonDetailDto.toDomain(): PokemonDetail {
    return PokemonDetail(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        tier = tier.orEmpty(),
        ability = ability.toDomain(),
        item = item?.toDomainItem(),
        moves = moves.map { it.toDomain() },
        types = types.map { it.toDomain() },
        baseSpecies = baseSpecies?.toDomain(),
        teraType = teraType?.toDomain(),
        imageUrl = normalizeImageUrl(imageUrl)
    )
}

fun PlayerDetailDto.toDomain(): PlayerDetail {
    return PlayerDetail(
        id = id,
        name = name,
        isWinner = winner,
        team = team.map { it.toDomain() }
    )
}

fun SetMatchDto.toDomain(): SetMatch {
    return SetMatch(
        id = id,
        showdownId = showdownId,
        positionInSet = positionInSet
    )
}

fun MatchDetailDto.toDomain(): MatchDetail {
    return MatchDetail(
        id = id,
        showdownId = showdownId,
        uploadTime = uploadTime,
        rating = rating,
        isPrivate = private,
        format = format.toDomain(),
        players = players.map { it.toDomain() },
        setId = setId,
        positionInSet = positionInSet,
        setMatches = setMatches?.map { it.toDomain() }.orEmpty()
    )
}

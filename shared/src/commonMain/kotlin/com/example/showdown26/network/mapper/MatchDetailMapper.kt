package com.example.showdown26.network.mapper

import com.example.showdown26.domain.model.Ability
import com.example.showdown26.domain.model.BaseSpecies
import com.example.showdown26.domain.model.DomainItem
import com.example.showdown26.domain.model.MatchDetail
import com.example.showdown26.domain.model.Move
import com.example.showdown26.domain.model.PlayerDetail
import com.example.showdown26.domain.model.PokemonDetail
import com.example.showdown26.domain.model.PokemonType
import com.example.showdown26.domain.model.TeraType
import com.example.showdown26.network.model.AbilityDto
import com.example.showdown26.network.model.BaseSpeciesDto
import com.example.showdown26.network.model.MatchDetailDto
import com.example.showdown26.network.model.MoveDto
import com.example.showdown26.network.model.NetworkItemDto
import com.example.showdown26.network.model.PlayerDetailDto
import com.example.showdown26.network.model.PokemonDetailDto
import com.example.showdown26.network.model.TeraTypeDto
import com.example.showdown26.network.normalizeImageUrl
import com.example.showdown26.network.model.TypeDto

fun AbilityDto.toDomain(): Ability {
    return Ability(id = id, name = name)
}

fun MoveDto.toDomain(): Move {
    return Move(id = id, name = name)
}

fun TypeDto.toDomain(): PokemonType {
    return PokemonType(id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
}

fun TeraTypeDto.toDomain(): TeraType {
    return TeraType(id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
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

fun MatchDetailDto.toDomain(): MatchDetail {
    return MatchDetail(
        id = id,
        showdownId = showdownId,
        uploadTime = uploadTime,
        rating = rating,
        isPrivate = private,
        format = format.toDomain(),
        players = players.map { it.toDomain() }
    )
}

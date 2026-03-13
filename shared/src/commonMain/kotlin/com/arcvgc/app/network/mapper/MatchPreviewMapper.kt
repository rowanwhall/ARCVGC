package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.DomainItem
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.domain.model.MatchPreview
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.PlayerPreview
import com.arcvgc.app.domain.model.PokemonPreview
import com.arcvgc.app.domain.model.TeraType
import com.arcvgc.app.network.normalizeImageUrl
import com.arcvgc.app.network.model.FormatDto
import com.arcvgc.app.network.model.MatchPreviewDto
import com.arcvgc.app.network.model.NetworkItemDto
import com.arcvgc.app.network.model.PaginationDto
import com.arcvgc.app.network.model.PlayerPreviewDto
import com.arcvgc.app.network.model.PokemonPreviewDto
import com.arcvgc.app.network.model.TeraTypeDto

fun NetworkItemDto.toDomain(): DomainItem? {
    if (id == null || name == null) return null
    return DomainItem(id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
}

fun TeraTypeDto.toTeraType(): TeraType {
    return TeraType(id = id, name = name, imageUrl = normalizeImageUrl(imageUrl))
}

fun PokemonPreviewDto.toDomain(): PokemonPreview {
    return PokemonPreview(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        item = item?.toDomain(),
        teraType = teraType?.toTeraType(),
        imageUrl = normalizeImageUrl(imageUrl)
    )
}

fun PlayerPreviewDto.toDomain(): PlayerPreview? {
    if (team.isEmpty()) return null
    return PlayerPreview(
        id = id,
        name = name,
        isWinner = winner,
        team = team.map { it.toDomain() }
    )
}

fun FormatDto.toDomain(): Format {
    return Format(
        id = id,
        name = name,
        formattedName = formattedName
    )
}

fun MatchPreviewDto.toDomain(): MatchPreview {
    return MatchPreview(
        id = id,
        showdownId = showdownId,
        uploadTime = uploadTime,
        rating = rating,
        isPrivate = private,
        format = format.toDomain(),
        players = players.mapNotNull { it.toDomain() }
    )
}

fun PaginationDto.toDomain(): Pagination {
    return Pagination(
        page = page,
        itemsPerPage = itemsPerPage,
        totalItems = totalItems,
        totalPages = totalPages
    )
}

fun List<MatchPreviewDto>.toDomain(): List<MatchPreview> = mapNotNull { dto ->
    val match = dto.toDomain()
    // Discard matches with fewer than 2 players/teams
    if (match.players.size >= 2) match else null
}

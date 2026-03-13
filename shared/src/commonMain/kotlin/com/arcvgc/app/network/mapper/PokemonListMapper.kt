package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.network.normalizeImageUrl
import com.arcvgc.app.network.model.PokemonListItemDto

fun PokemonListItemDto.toDomain(): PokemonListItem {
    return PokemonListItem(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        tier = tier,
        types = types.map { it.toDomain() },
        imageUrl = normalizeImageUrl(imageUrl)
    )
}

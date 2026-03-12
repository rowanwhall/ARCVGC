package com.example.showdown26.network.mapper

import com.example.showdown26.domain.model.PokemonListItem
import com.example.showdown26.network.normalizeImageUrl
import com.example.showdown26.network.model.PokemonListItemDto

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

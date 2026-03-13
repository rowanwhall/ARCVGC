package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponseDto(
    val success: Boolean,
    val data: List<PokemonListItemDto>,
    val pagination: PaginationDto
)

@Serializable
data class PokemonListItemDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int,
    val tier: String,
    val types: List<TypeDto>,
    @SerialName("image_url") val imageUrl: String? = null
)

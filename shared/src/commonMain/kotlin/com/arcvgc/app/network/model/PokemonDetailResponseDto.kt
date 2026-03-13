package com.arcvgc.app.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponseDto(
    val success: Boolean,
    val data: PokemonListItemDto
)

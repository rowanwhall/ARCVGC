package com.example.showdown26.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponseDto(
    val success: Boolean,
    val data: PokemonListItemDto
)

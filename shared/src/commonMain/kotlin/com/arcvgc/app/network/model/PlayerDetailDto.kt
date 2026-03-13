package com.arcvgc.app.network.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDetailDto(
    val id: Int,
    val name: String,
    val winner: Boolean?,
    val team: List<PokemonDetailDto>
)

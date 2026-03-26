package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerPreviewDto(
    val id: Int,
    val name: String,
    @SerialName("is_winner") val winner: Boolean?,
    val team: List<PokemonPreviewDto>
)

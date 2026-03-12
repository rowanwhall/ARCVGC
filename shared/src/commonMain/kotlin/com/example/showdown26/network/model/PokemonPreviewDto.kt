package com.example.showdown26.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonPreviewDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int,
    val item: NetworkItemDto?,
    @SerialName("tera_type") val teraType: TeraTypeDto? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseSpeciesDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int
)

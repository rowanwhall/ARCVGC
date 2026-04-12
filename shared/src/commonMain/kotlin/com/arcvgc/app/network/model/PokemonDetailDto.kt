package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int?,
    val tier: String?,
    val ability: AbilityDto? = null,
    val item: NetworkItemDto?,
    val moves: List<MoveDto> = emptyList(),
    val types: List<TypeDto>,
    @SerialName("base_species") val baseSpecies: BaseSpeciesDto?,
    @SerialName("tera_type") val teraType: TeraTypeDto? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

package com.example.showdown26.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int,
    val tier: String?,
    val ability: AbilityDto,
    val item: NetworkItemDto?,
    val moves: List<MoveDto>,
    val types: List<TypeDto>,
    @SerialName("base_species") val baseSpecies: BaseSpeciesDto?,
    @SerialName("tera_type") val teraType: TeraTypeDto? = null,
    @SerialName("image_url") val imageUrl: String? = null
)

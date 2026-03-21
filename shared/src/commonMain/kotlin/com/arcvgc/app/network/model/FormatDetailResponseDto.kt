package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FormatDetailResponseDto(
    val success: Boolean,
    val data: FormatDetailDto
)

@Serializable
data class FormatDetailDto(
    val id: Int,
    val name: String,
    @SerialName("formatted_name") val formattedName: String? = null,
    @SerialName("match_count") val matchCount: Int = 0,
    @SerialName("team_count") val teamCount: Int = 0,
    @SerialName("top_pokemon") val topPokemon: List<TopPokemonDto>? = null
)

@Serializable
data class TopPokemonDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int?,
    val tier: String?,
    @SerialName("is_nonstandard") val isNonstandard: String? = null,
    val types: List<TypeDto>,
    @SerialName("is_cosmetic_only") val isCosmeticOnly: Boolean = false,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("base_species") val baseSpecies: BaseSpeciesDto? = null,
    val count: Int = 0
)

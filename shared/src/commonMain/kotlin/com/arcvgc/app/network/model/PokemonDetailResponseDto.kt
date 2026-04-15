package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetailResponseDto(
    val success: Boolean,
    val data: PokemonProfileDto
)

@Serializable
data class PokemonProfileDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int?,
    val tier: String,
    val types: List<TypeDto>,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("base_species") val baseSpecies: BaseSpeciesDto? = null,
    val forms: List<PokemonFormDto>? = null,
    @SerialName("match_count") val matchCount: Int = 0,
    @SerialName("team_count") val teamCount: Int = 0,
    @SerialName("match_percent") val matchPercent: Double = 0.0,
    @SerialName("team_percent") val teamPercent: Double = 0.0,
    @SerialName("top_items") val topItems: List<TopItemDto>? = null,
    @SerialName("top_tera_types") val topTeraTypes: List<TopTeraTypeDto>? = null,
    @SerialName("top_moves") val topMoves: List<TopMoveDto>? = null,
    @SerialName("top_abilities") val topAbilities: List<TopAbilityDto>? = null,
    @SerialName("top_teammates") val topTeammates: List<TopTeammateDto>? = null
)

@Serializable
data class PokemonFormDto(
    val id: Int,
    @SerialName("pokedex_number") val pokedexNumber: Int?,
    val name: String,
    val tier: String?,
    val types: List<TypeDto>? = null,
    @SerialName("is_cosmetic_only") val isCosmeticOnly: Boolean,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class TopItemDto(
    val count: Int,
    val id: Int,
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class TopTeraTypeDto(
    val count: Int,
    val id: Int,
    val name: String,
    @SerialName("image_url") val imageUrl: String? = null
)

@Serializable
data class TopMoveDto(
    val count: Int,
    val id: Int,
    val name: String
)

@Serializable
data class TopAbilityDto(
    val count: Int,
    val id: Int,
    val name: String
)

@Serializable
data class TopTeammateDto(
    val count: Int,
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int?,
    @SerialName("image_url") val imageUrl: String? = null
)

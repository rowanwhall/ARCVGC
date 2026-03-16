package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfileResponseDto(
    val success: Boolean,
    val data: PlayerProfileDto
)

@Serializable
data class PlayerProfileDto(
    val id: Int,
    val name: String,
    @SerialName("match_count") val matchCount: Int,
    @SerialName("win_count") val winCount: Int,
    @SerialName("top_rated_match") val topRatedMatch: RatedMatchDto?,
    @SerialName("most_recent_rated_match") val mostRecentRatedMatch: RatedMatchDto?,
    @SerialName("most_used_pokemon") val mostUsedPokemon: List<MostUsedPokemonDto>
)

@Serializable
data class RatedMatchDto(val id: Int?, val rating: Int?)

@Serializable
data class MostUsedPokemonDto(
    val id: Int,
    val name: String,
    @SerialName("usage_count") val usageCount: Int,
    @SerialName("image_url") val imageUrl: String?
)

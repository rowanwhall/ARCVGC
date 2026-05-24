package com.arcvgc.app.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonUsageResponseDto(
    val success: Boolean,
    val data: PokemonUsageDataDto
)

@Serializable
data class PokemonUsageDataDto(
    @SerialName("prev_period_total_teams") val prevPeriodTotalTeams: Int = 0,
    @SerialName("current_period_total_teams") val currentPeriodTotalTeams: Int = 0,
    val increased: List<PokemonUsageDto>? = null,
    val decreased: List<PokemonUsageDto>? = null
)

@Serializable
data class PokemonUsageDto(
    val id: Int,
    val name: String,
    @SerialName("pokedex_number") val pokedexNumber: Int? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("prev_period_team_count") val prevPeriodTeamCount: Int = 0,
    @SerialName("prev_period_team_percent") val prevPeriodTeamPercent: Double = 0.0,
    @SerialName("current_period_team_count") val currentPeriodTeamCount: Int = 0,
    @SerialName("current_period_team_percent") val currentPeriodTeamPercent: Double = 0.0,
    @SerialName("usage_change_percent") val usageChangePercent: Double = 0.0
)

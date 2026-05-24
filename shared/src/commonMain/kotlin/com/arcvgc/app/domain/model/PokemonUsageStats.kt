package com.arcvgc.app.domain.model

data class PokemonUsageStats(
    val prevPeriodTotalTeams: Int,
    val currentPeriodTotalTeams: Int,
    val increased: List<PokemonUsage>,
    val decreased: List<PokemonUsage>
)

data class PokemonUsage(
    val id: Int,
    val name: String,
    val pokedexNumber: Int?,
    val imageUrl: String?,
    val prevPeriodTeamCount: Int,
    val prevPeriodTeamPercent: Double,
    val currentPeriodTeamCount: Int,
    val currentPeriodTeamPercent: Double,
    val usageChangePercent: Double
)

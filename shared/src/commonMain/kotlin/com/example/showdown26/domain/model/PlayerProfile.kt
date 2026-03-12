package com.example.showdown26.domain.model

data class PlayerProfile(
    val id: Int,
    val name: String,
    val matchCount: Int,
    val winCount: Int,
    val topRatedMatch: RatedMatch?,
    val mostRecentRatedMatch: RatedMatch?,
    val mostUsedPokemon: List<MostUsedPokemon>
)

data class RatedMatch(val id: Int, val rating: Int)

data class MostUsedPokemon(
    val id: Int,
    val name: String,
    val usageCount: Int,
    val imageUrl: String?
)

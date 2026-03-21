package com.arcvgc.app.domain.model

data class FormatDetail(
    val id: Int,
    val name: String,
    val formattedName: String?,
    val matchCount: Int,
    val teamCount: Int,
    val topPokemon: List<TopPokemon>
)

data class TopPokemon(
    val id: Int,
    val name: String,
    val pokedexNumber: Int?,
    val types: List<PokemonType>,
    val imageUrl: String?,
    val count: Int
)

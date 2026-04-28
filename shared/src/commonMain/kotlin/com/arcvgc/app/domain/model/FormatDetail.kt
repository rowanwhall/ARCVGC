package com.arcvgc.app.domain.model

data class FormatDetail(
    val id: Int,
    val name: String,
    val formattedName: String?,
    val matchCount: Int,
    val teamCount: Int,
    val topPokemon: List<TopPokemon>,
    val isHistoric: Boolean = false,
    val isOpenTeamsheet: Boolean = false,
    val isOfficial: Boolean = false,
    val hasSeries: Boolean = false
)

data class TopPokemon(
    val id: Int,
    val name: String,
    val pokedexNumber: Int?,
    val types: List<PokemonType>,
    val imageUrl: String?,
    val count: Int
)

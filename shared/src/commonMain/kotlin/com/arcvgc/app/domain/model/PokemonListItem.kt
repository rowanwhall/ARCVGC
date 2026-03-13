package com.arcvgc.app.domain.model

data class PokemonListItem(
    val id: Int,
    val name: String,
    val pokedexNumber: Int,
    val tier: String,
    val types: List<PokemonType>,
    val imageUrl: String?
)

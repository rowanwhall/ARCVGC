package com.example.showdown26.domain.model

data class PokemonPreview(
    val id: Int,
    val name: String,
    val pokedexNumber: Int,
    val item: DomainItem?,
    val teraType: TeraType? = null,
    val imageUrl: String? = null
)

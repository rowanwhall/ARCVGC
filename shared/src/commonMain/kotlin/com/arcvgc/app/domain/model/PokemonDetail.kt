package com.arcvgc.app.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val pokedexNumber: Int,
    val tier: String,
    val ability: Ability,
    val item: DomainItem?,
    val moves: List<Move>,
    val types: List<PokemonType>,
    val baseSpecies: BaseSpecies?,
    val teraType: TeraType? = null,
    val imageUrl: String? = null
)

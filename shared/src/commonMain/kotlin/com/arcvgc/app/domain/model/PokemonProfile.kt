package com.arcvgc.app.domain.model

data class PokemonProfile(
    val id: Int,
    val name: String,
    val pokedexNumber: Int?,
    val tier: String,
    val types: List<PokemonType>,
    val imageUrl: String?,
    val baseSpecies: BaseSpecies?,
    val matchCount: Int = 0,
    val teamCount: Int = 0,
    val matchPercent: Double = 0.0,
    val teamPercent: Double = 0.0,
    val topItems: List<TopStatItem> = emptyList(),
    val topTeraTypes: List<TopStatTeraType> = emptyList(),
    val topMoves: List<TopStatMove> = emptyList(),
    val topAbilities: List<TopStatAbility> = emptyList(),
    val topTeammates: List<TopStatTeammate> = emptyList()
) {
    fun toPokemonListItem(): PokemonListItem = PokemonListItem(
        id = id,
        name = name,
        pokedexNumber = pokedexNumber,
        tier = tier,
        types = types,
        imageUrl = imageUrl
    )
}

data class TopStatItem(
    val count: Int,
    val id: Int,
    val name: String,
    val imageUrl: String?
)

data class TopStatTeraType(
    val count: Int,
    val id: Int,
    val name: String,
    val imageUrl: String?
)

data class TopStatMove(
    val count: Int,
    val id: Int,
    val name: String
)

data class TopStatAbility(
    val count: Int,
    val id: Int,
    val name: String
)

data class TopStatTeammate(
    val count: Int,
    val id: Int,
    val name: String,
    val pokedexNumber: Int?,
    val imageUrl: String?
)

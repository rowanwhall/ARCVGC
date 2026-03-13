package com.arcvgc.app.domain.model

object SearchFilterRestrictions {
    private val fixedTeraTypePokemon = setOf(
        "ogerpon",
        "ogerpon-cornerstone",
        "ogerpon-hearthflame",
        "ogerpon-wellspring",
        "terapagos",
        "terapagos-stellar",
    )

    private val fixedItemPokemon = setOf(
        "ogerpon-cornerstone",
        "ogerpon-hearthflame",
        "ogerpon-wellspring",
        "zacian-crowned",
        "zamazenta-crowned",
        "giratina-origin",
        "palkia-origin",
        "dialga-origin",
    )

    fun canFilterByTeraType(pokemonName: String): Boolean =
        pokemonName.lowercase() !in fixedTeraTypePokemon

    fun canFilterByItem(pokemonName: String): Boolean =
        pokemonName.lowercase() !in fixedItemPokemon
}

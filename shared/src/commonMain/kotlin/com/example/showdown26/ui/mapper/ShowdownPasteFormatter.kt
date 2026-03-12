package com.example.showdown26.ui.mapper

import com.example.showdown26.ui.model.PokemonDetailUiModel

object ShowdownPasteFormatter {

    fun format(team: List<PokemonDetailUiModel>): String {
        return team.joinToString("\n\n") { pokemon -> formatPokemon(pokemon) }
    }

    private fun formatPokemon(pokemon: PokemonDetailUiModel): String {
        return buildString {
            // Name @ Item
            append(pokemon.name)
            if (pokemon.item != null) {
                append(" @ ")
                append(pokemon.item.name)
            }
            appendLine()

            // Ability
            appendLine("Ability: ${pokemon.abilityName}")

            // Level
            appendLine("Level: 50")

            // Tera Type
            if (pokemon.teraType != null) {
                appendLine("Tera Type: ${pokemon.teraType.name}")
            }

            // Moves
            for (move in pokemon.moves) {
                appendLine("- $move")
            }
        }.trimEnd()
    }
}

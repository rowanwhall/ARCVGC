package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TypeUiModel

object ContentListItemMapper {

    fun fromBattles(battles: List<BattleCardUiModel>): List<ContentListItem> {
        return battles.map { ContentListItem.Battle(it) }
    }

    fun fromPokemon(pokemon: List<PokemonListItem>): List<ContentListItem> {
        return pokemon.map { p ->
            ContentListItem.Pokemon(
                id = p.id,
                name = p.name,
                imageUrl = p.imageUrl,
                types = p.types.map { TypeUiModel(name = it.name, imageUrl = it.imageUrl) }
            )
        }
    }

    fun fromPlayers(players: List<PlayerListItem>): List<ContentListItem> {
        return players.map { ContentListItem.Player(id = it.id, name = it.name) }
    }

    fun fromPokemonCatalog(pokemonIds: List<Int>, catalog: List<PokemonPickerUiModel>): List<ContentListItem> {
        val catalogMap = catalog.associateBy { it.id }
        return pokemonIds.mapNotNull { id ->
            catalogMap[id]?.let { pokemon ->
                ContentListItem.Pokemon(
                    id = pokemon.id,
                    name = pokemon.name,
                    imageUrl = pokemon.imageUrl,
                    types = pokemon.types
                )
            }
        }
    }
}

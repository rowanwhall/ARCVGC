package com.arcvgc.app.ui.mapper

import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TypeUiModel

object PokemonPickerUiMapper {

    fun map(pokemon: PokemonListItem): PokemonPickerUiModel {
        return PokemonPickerUiModel(
            id = pokemon.id,
            name = pokemon.name,
            imageUrl = pokemon.imageUrl,
            types = pokemon.types.map {
                TypeUiModel(name = it.name, imageUrl = it.imageUrl)
            }
        )
    }

    fun mapList(pokemonList: List<PokemonListItem>): List<PokemonPickerUiModel> {
        return pokemonList.map { map(it) }
    }
}

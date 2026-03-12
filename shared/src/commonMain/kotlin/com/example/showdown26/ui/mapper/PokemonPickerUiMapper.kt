package com.example.showdown26.ui.mapper

import com.example.showdown26.domain.model.PokemonListItem
import com.example.showdown26.ui.model.PokemonPickerUiModel
import com.example.showdown26.ui.model.TypeUiModel

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

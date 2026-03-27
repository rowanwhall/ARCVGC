package com.arcvgc.app.ui.search

import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.SearchFilterSlotUiModel
import com.arcvgc.app.ui.model.SearchUiState
import com.arcvgc.app.ui.model.TeraTypeUiModel

object SearchStateReducer {

    fun initialState(): SearchUiState = SearchUiState()

    fun addPokemon(state: SearchUiState, pokemon: PokemonPickerUiModel): SearchUiState {
        if (!state.canAddMoreTeam1) return state
        return state.copy(
            filterSlots = state.filterSlots + SearchFilterSlotUiModel(
                pokemonId = pokemon.id,
                pokemonName = pokemon.name,
                pokemonImageUrl = pokemon.imageUrl,
                item = null,
                teraType = null
            )
        )
    }

    fun removePokemon(state: SearchUiState, index: Int): SearchUiState {
        val newSlots = state.filterSlots.toMutableList().apply { removeAt(index) }
        // If team1 is now empty, promote team2 into team1
        return if (newSlots.isEmpty() && state.team2FilterSlots.isNotEmpty()) {
            state.copy(filterSlots = state.team2FilterSlots, team2FilterSlots = emptyList())
        } else {
            state.copy(filterSlots = newSlots)
        }
    }

    fun setItem(state: SearchUiState, slotIndex: Int, item: ItemUiModel): SearchUiState {
        return state.copy(
            filterSlots = state.filterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(item = item)
            }
        )
    }

    fun setTeraType(state: SearchUiState, slotIndex: Int, teraType: TeraTypeUiModel): SearchUiState {
        return state.copy(
            filterSlots = state.filterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(teraType = teraType)
            }
        )
    }

    fun addTeam2Pokemon(state: SearchUiState, pokemon: PokemonPickerUiModel): SearchUiState {
        if (!state.canAddMoreTeam2) return state
        return state.copy(
            team2FilterSlots = state.team2FilterSlots + SearchFilterSlotUiModel(
                pokemonId = pokemon.id,
                pokemonName = pokemon.name,
                pokemonImageUrl = pokemon.imageUrl,
                item = null,
                teraType = null
            )
        )
    }

    fun removeTeam2Pokemon(state: SearchUiState, index: Int): SearchUiState {
        return state.copy(
            team2FilterSlots = state.team2FilterSlots.toMutableList().apply { removeAt(index) }
        )
    }

    fun setTeam2Item(state: SearchUiState, slotIndex: Int, item: ItemUiModel): SearchUiState {
        return state.copy(
            team2FilterSlots = state.team2FilterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(item = item)
            }
        )
    }

    fun setTeam2TeraType(state: SearchUiState, slotIndex: Int, teraType: TeraTypeUiModel): SearchUiState {
        return state.copy(
            team2FilterSlots = state.team2FilterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(teraType = teraType)
            }
        )
    }

    fun setFormat(state: SearchUiState, format: FormatUiModel): SearchUiState {
        return state.copy(selectedFormat = format)
    }

    fun setDefaultFormat(state: SearchUiState, format: FormatUiModel): SearchUiState {
        if (state.selectedFormat != null) return state
        return state.copy(selectedFormat = format)
    }

    fun setMinRating(state: SearchUiState, rating: Int?): SearchUiState {
        return state.copy(selectedMinRating = rating)
    }

    fun setMaxRating(state: SearchUiState, rating: Int?): SearchUiState {
        return state.copy(selectedMaxRating = rating)
    }

    fun setUnratedOnly(state: SearchUiState, value: Boolean): SearchUiState {
        return if (value) {
            state.copy(
                unratedOnly = true,
                selectedMinRating = null,
                selectedMaxRating = null,
                selectedOrderBy = if (state.selectedOrderBy == "rating") "time" else state.selectedOrderBy
            )
        } else {
            state.copy(unratedOnly = false)
        }
    }

    fun setTimeRange(state: SearchUiState, start: Long?, end: Long?): SearchUiState {
        return state.copy(timeRangeStart = start, timeRangeEnd = end)
    }

    fun setPlayerName(state: SearchUiState, name: String): SearchUiState {
        return state.copy(playerName = name)
    }

    fun setOrderBy(state: SearchUiState, orderBy: String): SearchUiState {
        return state.copy(selectedOrderBy = orderBy)
    }

    // Helper functions that duplicate SearchUiState computed properties.
    // Kotlin data class computed `get()` properties don't bridge reliably to Swift via SKIE,
    // so iOS calls these functions instead. Android/Web can use either.
    fun hasTeam2(state: SearchUiState): Boolean = state.team2FilterSlots.isNotEmpty()
    fun canAddMoreTeam1(state: SearchUiState): Boolean = state.filterSlots.size < 6
    fun canAddMoreTeam2(state: SearchUiState): Boolean = state.team2FilterSlots.size < 6
}

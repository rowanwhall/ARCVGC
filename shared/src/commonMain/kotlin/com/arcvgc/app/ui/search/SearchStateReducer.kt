package com.arcvgc.app.ui.search

import com.arcvgc.app.domain.model.OrderBy
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.domain.model.WinnerFilter
import com.arcvgc.app.ui.model.AbilityUiModel
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.SearchFilterSlotUiModel
import com.arcvgc.app.ui.model.SearchUiState
import com.arcvgc.app.ui.model.TeraTypeUiModel

object SearchStateReducer {

    fun initialState(): SearchUiState = SearchUiState()

    fun hydrateFromParams(
        params: SearchParams,
        resolvedFormat: FormatUiModel?
    ): SearchUiState {
        return SearchUiState(
            filterSlots = params.filters.map { it.toUiModel() },
            team2FilterSlots = params.team2Filters.map { it.toUiModel() },
            selectedFormat = resolvedFormat ?: FormatUiModel(
                id = params.formatId,
                displayName = params.formatName ?: ""
            ),
            userSelectedFormat = true,
            selectedMinRating = params.minimumRating,
            selectedMaxRating = params.maximumRating,
            unratedOnly = params.unratedOnly,
            selectedOrderBy = params.orderBy,
            timeRangeStart = params.timeRangeStart,
            timeRangeEnd = params.timeRangeEnd,
            playerName = params.playerName.orEmpty(),
            winnerFilter = params.winnerFilter
        )
    }

    private fun SearchFilterSlot.toUiModel(): SearchFilterSlotUiModel {
        val item = if (itemId != null) {
            ItemUiModel(id = itemId, name = itemName.orEmpty(), imageUrl = itemImageUrl)
        } else null
        val teraType = if (teraTypeId != null) {
            TeraTypeUiModel(id = teraTypeId, name = teraTypeName.orEmpty(), imageUrl = teraTypeImageUrl)
        } else null
        val ability = if (abilityId != null) {
            AbilityUiModel(id = abilityId, name = abilityName.orEmpty())
        } else null
        return SearchFilterSlotUiModel(
            pokemonId = pokemonId,
            pokemonName = pokemonName,
            pokemonImageUrl = pokemonImageUrl,
            item = item,
            teraType = teraType,
            ability = ability
        )
    }

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
            state.copy(
                filterSlots = state.team2FilterSlots,
                team2FilterSlots = emptyList(),
                // Team2 winner doesn't transfer on promotion; team1 winner transfers
                winnerFilter = if (state.winnerFilter == WinnerFilter.TEAM1) WinnerFilter.TEAM1
                else WinnerFilter.NONE
            )
        } else {
            state.copy(filterSlots = newSlots)
        }
    }

    fun setItem(state: SearchUiState, slotIndex: Int, item: ItemUiModel?): SearchUiState {
        return state.copy(
            filterSlots = state.filterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(item = item)
            }
        )
    }

    fun setTeraType(state: SearchUiState, slotIndex: Int, teraType: TeraTypeUiModel?): SearchUiState {
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
        val newSlots = state.team2FilterSlots.toMutableList().apply { removeAt(index) }
        return state.copy(
            team2FilterSlots = newSlots,
            // Clear team2 winner when team2 becomes empty
            winnerFilter = if (newSlots.isEmpty() && state.winnerFilter == WinnerFilter.TEAM2)
                WinnerFilter.NONE else state.winnerFilter
        )
    }

    fun setTeam2Item(state: SearchUiState, slotIndex: Int, item: ItemUiModel?): SearchUiState {
        return state.copy(
            team2FilterSlots = state.team2FilterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(item = item)
            }
        )
    }

    fun setTeam2TeraType(state: SearchUiState, slotIndex: Int, teraType: TeraTypeUiModel?): SearchUiState {
        return state.copy(
            team2FilterSlots = state.team2FilterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(teraType = teraType)
            }
        )
    }

    fun setAbility(state: SearchUiState, slotIndex: Int, ability: AbilityUiModel?): SearchUiState {
        return state.copy(
            filterSlots = state.filterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(ability = ability)
            }
        )
    }

    fun setTeam2Ability(state: SearchUiState, slotIndex: Int, ability: AbilityUiModel?): SearchUiState {
        return state.copy(
            team2FilterSlots = state.team2FilterSlots.toMutableList().apply {
                this[slotIndex] = this[slotIndex].copy(ability = ability)
            }
        )
    }

    fun setFormat(state: SearchUiState, format: FormatUiModel): SearchUiState {
        return state.copy(selectedFormat = format, userSelectedFormat = true)
    }

    fun setDefaultFormat(state: SearchUiState, format: FormatUiModel): SearchUiState {
        if (state.userSelectedFormat) return state
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
                selectedOrderBy = if (state.selectedOrderBy == OrderBy.Rating) OrderBy.Time else state.selectedOrderBy
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

    fun setOrderBy(state: SearchUiState, orderBy: OrderBy): SearchUiState {
        return state.copy(selectedOrderBy = orderBy)
    }

    fun setWinnerFilter(state: SearchUiState, filter: WinnerFilter): SearchUiState {
        return state.copy(
            winnerFilter = if (filter == state.winnerFilter) WinnerFilter.NONE else filter
        )
    }
}

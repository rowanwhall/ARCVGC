package com.example.showdown26.ui.search

import androidx.lifecycle.ViewModel
import com.example.showdown26.data.CatalogState
import com.example.showdown26.data.FormatCatalogRepository
import com.example.showdown26.data.ItemCatalogRepository
import com.example.showdown26.data.PokemonCatalogRepository
import com.example.showdown26.data.TeraTypeCatalogRepository
import com.example.showdown26.ui.model.FormatUiModel
import com.example.showdown26.ui.model.ItemUiModel
import com.example.showdown26.ui.model.PokemonPickerUiModel
import com.example.showdown26.ui.model.SearchFilterSlotUiModel
import com.example.showdown26.ui.model.TeraTypeUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel(
    pokemonCatalogRepository: PokemonCatalogRepository,
    itemCatalogRepository: ItemCatalogRepository,
    teraTypeCatalogRepository: TeraTypeCatalogRepository,
    formatCatalogRepository: FormatCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    val pokemonCatalogState: StateFlow<CatalogState<PokemonPickerUiModel>> =
        pokemonCatalogRepository.state

    val itemCatalogState: StateFlow<CatalogState<ItemUiModel>> =
        itemCatalogRepository.state

    val teraTypeCatalogState: StateFlow<CatalogState<TeraTypeUiModel>> =
        teraTypeCatalogRepository.state

    val formatCatalogState: StateFlow<CatalogState<FormatUiModel>> =
        formatCatalogRepository.state

    fun addPokemon(pokemon: PokemonPickerUiModel) {
        _uiState.update { state ->
            if (!state.canAddMore) return@update state
            state.copy(
                filterSlots = state.filterSlots + SearchFilterSlotUiModel(
                    pokemonId = pokemon.id,
                    pokemonName = pokemon.name,
                    pokemonImageUrl = pokemon.imageUrl,
                    item = null,
                    teraType = null
                )
            )
        }
    }

    fun removePokemon(index: Int) {
        _uiState.update { state ->
            state.copy(
                filterSlots = state.filterSlots.toMutableList().apply { removeAt(index) }
            )
        }
    }

    fun setItem(slotIndex: Int, item: ItemUiModel) {
        _uiState.update { state ->
            state.copy(
                filterSlots = state.filterSlots.toMutableList().apply {
                    this[slotIndex] = this[slotIndex].copy(item = item)
                }
            )
        }
    }

    fun setTeraType(slotIndex: Int, teraType: TeraTypeUiModel) {
        _uiState.update { state ->
            state.copy(
                filterSlots = state.filterSlots.toMutableList().apply {
                    this[slotIndex] = this[slotIndex].copy(teraType = teraType)
                }
            )
        }
    }

    fun setFormat(format: FormatUiModel) {
        _uiState.update { it.copy(selectedFormat = format) }
    }

    fun setMinRating(rating: Int?) {
        _uiState.update { it.copy(selectedMinRating = rating) }
    }

    fun setMaxRating(rating: Int?) {
        _uiState.update { it.copy(selectedMaxRating = rating) }
    }

    fun setUnratedOnly(value: Boolean) {
        _uiState.update {
            if (value) {
                it.copy(
                    unratedOnly = true,
                    selectedMinRating = null,
                    selectedMaxRating = null,
                    selectedOrderBy = if (it.selectedOrderBy == "rating") "time" else it.selectedOrderBy
                )
            } else {
                it.copy(unratedOnly = false)
            }
        }
    }

    fun setTimeRange(start: Long?, end: Long?) {
        _uiState.update { it.copy(timeRangeStart = start, timeRangeEnd = end) }
    }

    fun setPlayerName(name: String) {
        _uiState.update { it.copy(playerName = name) }
    }

    fun setOrderBy(orderBy: String) {
        _uiState.update { it.copy(selectedOrderBy = orderBy) }
    }
}

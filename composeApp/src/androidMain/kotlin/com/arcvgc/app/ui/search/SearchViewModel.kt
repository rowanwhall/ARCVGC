package com.arcvgc.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.SearchUiState
import com.arcvgc.app.ui.model.TeraTypeUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    pokemonCatalogRepository: PokemonCatalogRepository,
    itemCatalogRepository: ItemCatalogRepository,
    teraTypeCatalogRepository: TeraTypeCatalogRepository,
    formatCatalogRepository: FormatCatalogRepository,
    appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchStateReducer.initialState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appConfigRepository.config.filterNotNull().collect { config ->
                val format = config.defaultFormat
                _uiState.update {
                    SearchStateReducer.setDefaultFormat(
                        it,
                        FormatUiModel(
                            id = format.id,
                            displayName = format.formattedName ?: format.name
                        )
                    )
                }
            }
        }
    }

    val pokemonCatalogState: StateFlow<CatalogState<PokemonPickerUiModel>> =
        pokemonCatalogRepository.state

    val itemCatalogState: StateFlow<CatalogState<ItemUiModel>> =
        itemCatalogRepository.state

    val teraTypeCatalogState: StateFlow<CatalogState<TeraTypeUiModel>> =
        teraTypeCatalogRepository.state

    val formatCatalogState: StateFlow<CatalogState<FormatUiModel>> =
        formatCatalogRepository.state

    fun addPokemon(pokemon: PokemonPickerUiModel) {
        _uiState.update { SearchStateReducer.addPokemon(it, pokemon) }
    }

    fun removePokemon(index: Int) {
        _uiState.update { SearchStateReducer.removePokemon(it, index) }
    }

    fun setItem(slotIndex: Int, item: ItemUiModel) {
        _uiState.update { SearchStateReducer.setItem(it, slotIndex, item) }
    }

    fun setTeraType(slotIndex: Int, teraType: TeraTypeUiModel) {
        _uiState.update { SearchStateReducer.setTeraType(it, slotIndex, teraType) }
    }

    fun setFormat(format: FormatUiModel) {
        _uiState.update { SearchStateReducer.setFormat(it, format) }
    }

    fun setMinRating(rating: Int?) {
        _uiState.update { SearchStateReducer.setMinRating(it, rating) }
    }

    fun setMaxRating(rating: Int?) {
        _uiState.update { SearchStateReducer.setMaxRating(it, rating) }
    }

    fun setUnratedOnly(value: Boolean) {
        _uiState.update { SearchStateReducer.setUnratedOnly(it, value) }
    }

    fun setTimeRange(start: Long?, end: Long?) {
        _uiState.update { SearchStateReducer.setTimeRange(it, start, end) }
    }

    fun setPlayerName(name: String) {
        _uiState.update { SearchStateReducer.setPlayerName(it, name) }
    }

    fun setOrderBy(orderBy: String) {
        _uiState.update { SearchStateReducer.setOrderBy(it, orderBy) }
    }
}

package com.arcvgc.app.ui.search

import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.SearchUiState
import com.arcvgc.app.ui.model.TeraTypeUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchLogic(
    scope: CoroutineScope? = null,
    appConfigFlow: StateFlow<AppConfig?>? = null
) {
    private val _uiState = MutableStateFlow(SearchStateReducer.initialState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        if (scope != null && appConfigFlow != null) {
            scope.launch {
                appConfigFlow.filterNotNull().collect { config ->
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
    }

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

    fun addTeam2Pokemon(pokemon: PokemonPickerUiModel) {
        _uiState.update { SearchStateReducer.addTeam2Pokemon(it, pokemon) }
    }

    fun removeTeam2Pokemon(index: Int) {
        _uiState.update { SearchStateReducer.removeTeam2Pokemon(it, index) }
    }

    fun setTeam2Item(slotIndex: Int, item: ItemUiModel) {
        _uiState.update { SearchStateReducer.setTeam2Item(it, slotIndex, item) }
    }

    fun setTeam2TeraType(slotIndex: Int, teraType: TeraTypeUiModel) {
        _uiState.update { SearchStateReducer.setTeam2TeraType(it, slotIndex, teraType) }
    }

    fun setFormat(format: FormatUiModel) {
        _uiState.update { SearchStateReducer.setFormat(it, format) }
    }

    fun setDefaultFormat(format: FormatUiModel) {
        _uiState.update { SearchStateReducer.setDefaultFormat(it, format) }
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

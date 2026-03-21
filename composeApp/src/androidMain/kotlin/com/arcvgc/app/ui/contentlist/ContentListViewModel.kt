package com.arcvgc.app.ui.contentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.repository.AppConfigRepositoryImpl
import com.arcvgc.app.data.repository.BattleRepositoryImpl
import com.arcvgc.app.data.repository.FavoritesRepository
import com.arcvgc.app.data.repository.FavoritesRepositoryImpl
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.SettingsRepository
import com.arcvgc.app.ui.model.ContentListMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentListViewModel @Inject constructor(
    private val battleRepositoryImpl: BattleRepositoryImpl,
    private val favoritesRepositoryImpl: FavoritesRepositoryImpl,
    val settingsRepository: SettingsRepository,
    private val pokemonCatalogRepository: PokemonCatalogRepository,
    private val appConfigRepositoryImpl: AppConfigRepositoryImpl,
    val formatCatalogRepository: FormatCatalogRepository
) : ViewModel() {

    private var logic: ContentListLogic? = null

    val favoritesRepository: FavoritesRepository get() = favoritesRepositoryImpl

    private val _uiState = MutableStateFlow(ContentListUiState())
    val uiState: StateFlow<ContentListUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow("time")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _selectedFormatId = MutableStateFlow(0)
    val selectedFormatId: StateFlow<Int> = _selectedFormatId.asStateFlow()

    private var initialized = false

    fun initialize(mode: ContentListMode) {
        if (initialized) return
        initialized = true
        val l = ContentListLogic(
            scope = viewModelScope,
            repository = battleRepositoryImpl.shared,
            favoritesRepository = favoritesRepositoryImpl.shared,
            appConfigRepository = appConfigRepositoryImpl.shared,
            mode = mode,
            pokemonCatalogItems = pokemonCatalogRepository.state.value.items,
            pokemonCatalogState = pokemonCatalogRepository.state
        )
        logic = l

        viewModelScope.launch { l.uiState.collect { _uiState.value = it } }
        viewModelScope.launch { l.sortOrder.collect { _sortOrder.value = it } }
        viewModelScope.launch { l.selectedFormatId.collect { _selectedFormatId.value = it } }
        viewModelScope.launch { l.searchQuery.collect { _searchQuery.value = it } }

        l.initialize()
    }

    fun loadContent() { logic?.loadContent() }
    fun refresh() { logic?.refresh() }
    fun paginate() { logic?.paginate() }
    fun selectFormat(formatId: Int) { logic?.selectFormat(formatId) }
    fun toggleSortOrder() { logic?.toggleSortOrder() }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) { logic?.setSearchQuery(query) }
}

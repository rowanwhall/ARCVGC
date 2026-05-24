package com.arcvgc.app.ui.contentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.AppConfigRepositoryImpl
import com.arcvgc.app.data.repository.BattleRepositoryImpl
import com.arcvgc.app.data.repository.FavoritesRepository
import com.arcvgc.app.data.repository.FavoritesRepositoryImpl
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.SettingsRepository
import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.OrderBy
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
    val appConfigRepository: AppConfigRepository get() = appConfigRepositoryImpl

    private val _uiState = MutableStateFlow(ContentListUiState())
    val uiState: StateFlow<ContentListUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow(OrderBy.Time)
    val sortOrder: StateFlow<OrderBy> = _sortOrder.asStateFlow()

    private val _selectedFormatId = MutableStateFlow(0)
    val selectedFormatId: StateFlow<Int> = _selectedFormatId.asStateFlow()

    private val _selectedLookback = MutableStateFlow(LookbackWindow.All)
    val selectedLookback: StateFlow<LookbackWindow> = _selectedLookback.asStateFlow()

    // Apply-once tick pattern for deep-linked lookback: each deep link that
    // carries a lookback bumps a tick on the composition side; this counter
    // tracks the most recent tick already applied so re-entering composition
    // (tab switches, recomposition) with the same tick doesn't clobber a
    // lookback the user has since changed in the UI.
    var lastAppliedLookbackTick: Int = 0

    private var initialized = false

    fun initialize(mode: ContentListMode, initialLookback: LookbackWindow = LookbackWindow.All) {
        if (initialized) return
        initialized = true
        val l = ContentListLogic(
            scope = viewModelScope,
            repository = battleRepositoryImpl.shared,
            favoritesRepository = favoritesRepositoryImpl.shared,
            appConfigRepository = appConfigRepositoryImpl.shared,
            mode = mode,
            pokemonCatalogItems = pokemonCatalogRepository.state.value.items,
            pokemonCatalogState = pokemonCatalogRepository.state,
            initialLookback = initialLookback,
            settingsRepository = settingsRepository.shared,
            isFormatHistoric = { id ->
                formatCatalogRepository.state.value.items.find { it.id == id }?.isHistoric == true
            }
        )
        logic = l

        viewModelScope.launch { l.uiState.collect { _uiState.value = it } }
        viewModelScope.launch { l.sortOrder.collect { _sortOrder.value = it } }
        viewModelScope.launch { l.selectedFormatId.collect { _selectedFormatId.value = it } }
        viewModelScope.launch { l.selectedLookback.collect { _selectedLookback.value = it } }
        viewModelScope.launch { l.searchQuery.collect { _searchQuery.value = it } }

        l.initialize()
    }

    fun loadContent() { logic?.loadContent() }
    fun refresh() { logic?.refresh() }
    fun paginate() { logic?.paginate() }
    fun selectFormat(formatId: Int) { logic?.selectFormat(formatId) }
    fun selectLookback(lookback: LookbackWindow) { logic?.selectLookback(lookback) }
    fun toggleSortOrder() { logic?.toggleSortOrder() }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) { logic?.setSearchQuery(query) }
}

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
import kotlinx.coroutines.flow.StateFlow
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

    private lateinit var logic: ContentListLogic

    val favoritesRepository: FavoritesRepository get() = favoritesRepositoryImpl

    val uiState: StateFlow<ContentListUiState> get() = logic.uiState
    val sortOrder: StateFlow<String> get() = logic.sortOrder
    val selectedFormatId: StateFlow<Int> get() = logic.selectedFormatId

    private var initialized = false

    fun initialize(mode: ContentListMode) {
        if (initialized) return
        initialized = true
        logic = ContentListLogic(
            scope = viewModelScope,
            repository = battleRepositoryImpl.shared,
            favoritesRepository = favoritesRepositoryImpl.shared,
            appConfigRepository = appConfigRepositoryImpl.shared,
            mode = mode,
            pokemonCatalogItems = pokemonCatalogRepository.state.value.items
        )
        logic.initialize()
    }

    fun loadContent() = logic.loadContent()
    fun refresh() = logic.refresh()
    fun paginate() = logic.paginate()
    fun selectFormat(formatId: Int) = logic.selectFormat(formatId)
    fun toggleSortOrder() = logic.toggleSortOrder()
}

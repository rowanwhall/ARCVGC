package com.arcvgc.app.ui.contentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.AppConfigRepository
import com.arcvgc.app.data.BattleRepository
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.data.FavoritesRepository
import com.arcvgc.app.data.FormatCatalogRepository
import com.arcvgc.app.data.PokemonCatalogRepository
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import kotlinx.coroutines.flow.StateFlow

class ContentListViewModel(
    repository: BattleRepository,
    val favoritesRepository: FavoritesRepository,
    mode: ContentListMode = ContentListMode.Home,
    pokemonCatalogItems: List<PokemonPickerUiModel> = emptyList(),
    appConfigRepository: AppConfigRepository,
    private val formatCatalogRepository: FormatCatalogRepository? = null,
    pokemonCatalogRepository: PokemonCatalogRepository? = null
) : ViewModel() {

    private val logic = ContentListLogic(
        scope = viewModelScope,
        repository = repository,
        favoritesRepository = favoritesRepository,
        appConfigRepository = appConfigRepository,
        mode = mode,
        pokemonCatalogItems = pokemonCatalogItems,
        pokemonCatalogState = pokemonCatalogRepository?.state
    )

    val uiState: StateFlow<ContentListUiState> = logic.uiState
    val sortOrder: StateFlow<String> = logic.sortOrder
    val selectedFormatId: StateFlow<Int> = logic.selectedFormatId

    val formatCatalogState: StateFlow<CatalogState<FormatUiModel>>?
        get() = formatCatalogRepository?.state

    // Persisted UI state for restoration on back navigation
    var savedBattleId: Int? = null
    var savedScrollIndex: Int = 0
    var savedScrollOffset: Int = 0

    init { logic.initialize() }

    fun loadContent() = logic.loadContent()
    fun paginate() = logic.paginate()
    fun selectFormat(formatId: Int) = logic.selectFormat(formatId)
    fun toggleSortOrder() = logic.toggleSortOrder()
}

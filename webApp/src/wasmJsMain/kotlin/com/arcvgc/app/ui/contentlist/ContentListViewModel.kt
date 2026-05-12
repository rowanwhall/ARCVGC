package com.arcvgc.app.ui.contentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.AppConfigRepository
import com.arcvgc.app.data.BattleRepository
import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.OrderBy
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.data.FavoritesRepository
import com.arcvgc.app.data.FormatCatalogRepository
import com.arcvgc.app.data.PokemonCatalogRepository
import com.arcvgc.app.data.SettingsRepository
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import kotlinx.coroutines.flow.StateFlow

class ContentListViewModel(
    repository: BattleRepository,
    val favoritesRepository: FavoritesRepository,
    mode: ContentListMode = ContentListMode.Home,
    pokemonCatalogItems: List<PokemonPickerUiModel> = emptyList(),
    private val appConfigRepository: AppConfigRepository,
    private val formatCatalogRepository: FormatCatalogRepository? = null,
    pokemonCatalogRepository: PokemonCatalogRepository? = null,
    settingsRepository: SettingsRepository? = null,
    initialLookback: LookbackWindow = LookbackWindow.All
) : ViewModel() {

    private val logic = ContentListLogic(
        scope = viewModelScope,
        repository = repository,
        favoritesRepository = favoritesRepository,
        appConfigRepository = appConfigRepository,
        mode = mode,
        pokemonCatalogItems = pokemonCatalogItems,
        pokemonCatalogState = pokemonCatalogRepository?.state,
        initialLookback = initialLookback,
        settingsRepository = settingsRepository,
        isFormatHistoric = { id ->
            formatCatalogRepository?.state?.value?.items?.find { it.id == id }?.isHistoric == true
        }
    )

    val uiState: StateFlow<ContentListUiState> = logic.uiState
    val sortOrder: StateFlow<OrderBy> = logic.sortOrder
    val selectedFormatId: StateFlow<Int> = logic.selectedFormatId
    val selectedLookback: StateFlow<LookbackWindow> = logic.selectedLookback
    val searchQuery: StateFlow<String> = logic.searchQuery
    val allTopPokemonItems = logic.allTopPokemonItems

    val formatCatalogState: StateFlow<CatalogState<FormatUiModel>>?
        get() = formatCatalogRepository?.state

    val appConfigState: StateFlow<AppConfig?> = appConfigRepository.config

    // Persisted UI state for restoration on back navigation
    var savedBattleId: Int? = null
    var savedScrollIndex: Int = 0
    var savedScrollOffset: Int = 0

    // For UsageDesktopPage: the tick of the last Home → See More request whose
    // format was applied to this VM. Persisted across tab switches so the
    // pending-format effect only fires once per new click.
    //
    // Paired with `usagePendingFormatTick` which lives on WebApp composition
    // state — the two lifecycles are distinct (cached VM vs. composition), but
    // the absolute tick values don't matter: only monotonic ordering does, and
    // both sides only ever increment. If either side resets, the comparison
    // `pending > lastApplied` still correctly triggers exactly once per click.
    var lastAppliedUsageFormatTick: Int = 0

    init { logic.initialize() }

    fun loadContent() = logic.loadContent()
    suspend fun watchForStaleness() = logic.watchForStaleness()
    fun paginate() = logic.paginate()
    fun selectFormat(formatId: Int) = logic.selectFormat(formatId)
    fun selectLookback(lookback: LookbackWindow) = logic.selectLookback(lookback)
    fun toggleSortOrder() = logic.toggleSortOrder()
    fun setSearchQuery(query: String) = logic.setSearchQuery(query)
    fun setTopPokemonFetchCount(count: Int) = logic.setTopPokemonFetchCount(count)
}

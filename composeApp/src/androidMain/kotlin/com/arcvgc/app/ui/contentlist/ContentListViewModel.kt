package com.arcvgc.app.ui.contentlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.BattleRepository
import com.arcvgc.app.data.repository.FavoritesRepository
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.SettingsRepository
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.ui.mapper.ContentListItemMapper
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.model.FormatUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentListViewModel @Inject constructor(
    private val repository: BattleRepository,
    val favoritesRepository: FavoritesRepository,
    val settingsRepository: SettingsRepository,
    private val pokemonCatalogRepository: PokemonCatalogRepository,
    private val appConfigRepository: AppConfigRepository,
    val formatCatalogRepository: FormatCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentListUiState())
    val uiState: StateFlow<ContentListUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow("time")
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _selectedFormatId = MutableStateFlow(0)
    val selectedFormatId: StateFlow<Int> = _selectedFormatId.asStateFlow()

    companion object {
        private const val TAG = "ContentListViewModel"
    }

    private var mode: ContentListMode = ContentListMode.Home
    private var initialized = false

    fun initialize(mode: ContentListMode) {
        if (initialized) return
        initialized = true
        this.mode = mode
        if (mode is ContentListMode.Search) {
            _sortOrder.value = mode.params.orderBy
        }
        if (mode is ContentListMode.Pokemon) {
            _selectedFormatId.value = mode.formatId ?: appConfigRepository.config.value?.defaultFormat?.id ?: 1
        }

        if (mode is ContentListMode.Favorites && mode.contentType == FavoriteContentType.Pokemon) {
            observeFavoritePokemon()
        } else if (mode is ContentListMode.Favorites && mode.contentType == FavoriteContentType.Players) {
            observeFavoritePlayers()
        } else if (mode is ContentListMode.Home) {
            waitForConfigThenLoad()
        } else {
            loadContent()
        }
    }

    private fun waitForConfigThenLoad() {
        viewModelScope.launch {
            val currentConfig = appConfigRepository.config.value
            if (currentConfig != null) {
                loadContent()
            } else {
                _uiState.update { it.copy(isLoading = true) }
                appConfigRepository.config
                    .filterNotNull()
                    .first()
                loadContent()
            }
        }
    }

    private fun observeFavoritePokemon() {
        viewModelScope.launch {
            favoritesRepository.favoritePokemonIds.collect { ids ->
                loadFavoritePokemon(ids)
            }
        }
    }

    private fun observeFavoritePlayers() {
        viewModelScope.launch {
            favoritesRepository.favoritePlayerNames.collect { names ->
                loadFavoritePlayers(names)
            }
        }
    }

    private fun loadFavoritePokemon(ids: Set<Int>) {
        if (ids.isEmpty()) {
            _uiState.update { it.copy(items = emptyList(), isLoading = false, error = null, canPaginate = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getPokemonByIds(ids.toList())
                .onSuccess { pokemon ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = ContentListItemMapper.fromPokemon(pokemon),
                            error = null,
                            canPaginate = false
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to load favorite pokemon", throwable)
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
        }
    }

    private fun loadFavoritePlayers(names: Set<String>) {
        if (names.isEmpty()) {
            _uiState.update { it.copy(items = emptyList(), isLoading = false, error = null, canPaginate = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getPlayersByNames(names.toList())
                .onSuccess { players ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = ContentListItemMapper.fromPlayers(players),
                            error = null,
                            canPaginate = false
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to load favorite players", throwable)
                    _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                }
        }
    }

    private suspend fun fetchContent(page: Int = 1): Result<Pair<List<ContentListItem>, Pagination>> = when (val m = mode) {
        is ContentListMode.Home -> {
            val nowSeconds = System.currentTimeMillis() / 1000
            val formatId = appConfigRepository.config.value?.defaultFormat?.id ?: 1
            repository.searchMatches(
                filters = emptyList(),
                formatId = formatId,
                orderBy = "rating",
                page = page,
                timeRangeStart = nowSeconds - 86400,
                timeRangeEnd = nowSeconds
            ).map { (battles, pagination) ->
                ContentListItemMapper.fromBattles(battles) to pagination
            }
        }
        is ContentListMode.Favorites -> when (m.contentType) {
            FavoriteContentType.Battles -> {
                val ids = favoritesRepository.favoriteBattleIds.value.toList()
                if (ids.isEmpty()) {
                    Result.success(emptyList<ContentListItem>() to Pagination(1, ids.size, ids.size, 1))
                } else {
                    repository.getMatchesByIds(ids).map { battles ->
                        ContentListItemMapper.fromBattles(battles) to Pagination(1, battles.size, battles.size, 1)
                    }
                }
            }
            FavoriteContentType.Pokemon -> {
                val ids = favoritesRepository.favoritePokemonIds.value.toList()
                if (ids.isEmpty()) {
                    Result.success(emptyList<ContentListItem>() to Pagination(1, 0, 0, 1))
                } else {
                    repository.getPokemonByIds(ids).map { pokemon ->
                        ContentListItemMapper.fromPokemon(pokemon) to Pagination(1, pokemon.size, pokemon.size, 1)
                    }
                }
            }
            FavoriteContentType.Players -> {
                val names = favoritesRepository.favoritePlayerNames.value.toList()
                if (names.isEmpty()) {
                    Result.success(emptyList<ContentListItem>() to Pagination(1, 0, 0, 1))
                } else {
                    repository.getPlayersByNames(names).map { players ->
                        ContentListItemMapper.fromPlayers(players) to Pagination(1, players.size, players.size, 1)
                    }
                }
            }
        }
        is ContentListMode.Search -> coroutineScope {
            val battlesDeferred = async {
                repository.searchMatches(
                    filters = m.params.filters,
                    formatId = m.params.formatId,
                    minimumRating = m.params.minimumRating,
                    maximumRating = m.params.maximumRating,
                    unratedOnly = m.params.unratedOnly,
                    orderBy = _sortOrder.value,
                    page = page,
                    timeRangeStart = m.params.timeRangeStart,
                    timeRangeEnd = m.params.timeRangeEnd,
                    playerName = m.params.playerName
                )
            }
            val playerDeferred = m.params.playerName?.let { name ->
                if (page == 1) async { repository.getPlayersByNames(listOf(name)) }
                else null
            }

            battlesDeferred.await().map { (battles, pagination) ->
                val battleItems = ContentListItemMapper.fromBattles(battles)
                if (page == 1) {
                    val pinnedPokemon = ContentListItemMapper.fromPokemonCatalog(
                        m.params.filters.map { it.pokemonId },
                        pokemonCatalogRepository.state.value.items
                    )
                    val pinnedPlayer = playerDeferred?.await()?.getOrNull()?.firstOrNull()?.let {
                        listOf(ContentListItem.Player(id = it.id, name = it.name))
                    } ?: emptyList()
                    val sections = buildList {
                        if (pinnedPokemon.isNotEmpty()) add(ContentListItem.Section("Pokémon", pinnedPokemon))
                        if (pinnedPlayer.isNotEmpty()) add(ContentListItem.Section("Players", pinnedPlayer))
                        if (battleItems.isNotEmpty()) add(ContentListItem.Section("Battles", battleItems))
                    }
                    sections to pagination
                } else {
                    battleItems to pagination
                }
            }
        }
        is ContentListMode.Pokemon -> repository.searchMatches(
            filters = listOf(SearchFilterSlot(pokemonId = m.pokemonId)),
            formatId = _selectedFormatId.value,
            orderBy = _sortOrder.value,
            page = page
        ).map { (battles, pagination) ->
            val battleItems = ContentListItemMapper.fromBattles(battles)
            if (page == 1) {
                listOf(ContentListItem.Section("Battles", battleItems)) to pagination
            } else {
                battleItems to pagination
            }
        }
        is ContentListMode.Player -> if (page == 1) coroutineScope {
            val profileDeferred = async { repository.getPlayerProfile(m.playerId) }
            val battlesDeferred = async {
                repository.searchMatches(
                    filters = emptyList(),
                    orderBy = _sortOrder.value,
                    page = page,
                    playerName = m.playerName
                )
            }

            battlesDeferred.await().map { (battles, pagination) ->
                val battleItems = ContentListItemMapper.fromBattles(battles)
                val profile = profileDeferred.await().getOrNull()

                val sections = buildList {
                    if (profile != null) {
                        val highlightButtons = buildList {
                            profile.topRatedMatch?.let {
                                add(ContentListItem.HighlightButton("Top Rated Battle", it.rating, it.id))
                            }
                            profile.mostRecentRatedMatch?.let {
                                add(ContentListItem.HighlightButton("Latest Rated Battle", it.rating, it.id))
                            }
                        }
                        if (highlightButtons.isNotEmpty()) {
                            add(ContentListItem.HighlightButtons(highlightButtons))
                        }

                        if (profile.mostUsedPokemon.isNotEmpty()) {
                            val gridItems = profile.mostUsedPokemon.map {
                                ContentListItem.PokemonGridItem(it.id, it.name, it.imageUrl)
                            }
                            add(ContentListItem.Section("Favorite Pokémon", listOf(ContentListItem.PokemonGrid(gridItems))))
                        }
                    }

                    if (battleItems.isNotEmpty()) {
                        add(ContentListItem.Section("Battles", battleItems))
                    }
                }
                sections to pagination
            }
        } else {
            repository.searchMatches(
                filters = emptyList(),
                orderBy = _sortOrder.value,
                page = page,
                playerName = m.playerName
            ).map { (battles, pagination) ->
                ContentListItemMapper.fromBattles(battles) to pagination
            }
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            fetchContent()
                .onSuccess { (items, pagination) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            error = null,
                            currentPage = pagination.page,
                            canPaginate = pagination.page < pagination.totalPages
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to load content", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unknown error"
                        )
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            fetchContent(page = 1)
                .onSuccess { (items, pagination) ->
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            items = items,
                            error = null,
                            currentPage = pagination.page,
                            canPaginate = pagination.page < pagination.totalPages
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to refresh content", throwable)
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            error = throwable.message ?: "Unknown error"
                        )
                    }
                }
        }
    }

    fun paginate() {
        val currentState = _uiState.value
        if (currentState.isPaginating || !currentState.canPaginate || currentState.loadingSections.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginating = true) }

            val nextPage = currentState.currentPage + 1
            fetchContent(page = nextPage)
                .onSuccess { (items, pagination) ->
                    _uiState.update {
                        it.copy(
                            isPaginating = false,
                            items = (it.items + items).distinctBy { item -> item.listKey },
                            currentPage = pagination.page,
                            canPaginate = pagination.page < pagination.totalPages
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to paginate content (page $nextPage)", throwable)
                    _uiState.update { it.copy(isPaginating = false) }
                }
        }
    }

    fun selectFormat(formatId: Int) {
        if (_selectedFormatId.value == formatId) return
        _selectedFormatId.value = formatId
        viewModelScope.launch {
            _uiState.update { it.copy(loadingSections = setOf("Battles"), currentPage = 1, canPaginate = false) }

            fetchContent()
                .onSuccess { (items, pagination) ->
                    _uiState.update {
                        it.copy(
                            items = items,
                            loadingSections = emptySet(),
                            currentPage = pagination.page,
                            canPaginate = pagination.page < pagination.totalPages
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to select format", throwable)
                    _uiState.update { it.copy(loadingSections = emptySet()) }
                }
        }
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == "time") "rating" else "time"
        viewModelScope.launch {
            _uiState.update { it.copy(loadingSections = setOf("Battles"), currentPage = 1, canPaginate = false) }

            fetchContent()
                .onSuccess { (items, pagination) ->
                    _uiState.update {
                        it.copy(
                            items = items,
                            loadingSections = emptySet(),
                            currentPage = pagination.page,
                            canPaginate = pagination.page < pagination.totalPages
                        )
                    }
                }
                .onFailure { throwable ->
                    Log.e(TAG, "Failed to toggle sort order", throwable)
                    _uiState.update { it.copy(loadingSections = emptySet()) }
                }
        }
    }
}

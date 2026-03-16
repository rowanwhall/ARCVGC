package com.arcvgc.app.ui.contentlist

import com.arcvgc.app.data.AppConfigRepository
import com.arcvgc.app.data.BattleRepositoryApi
import com.arcvgc.app.data.FavoritesRepository
import com.arcvgc.app.data.currentTimeMillis
import com.arcvgc.app.domain.model.Pagination
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.mapper.ContentListItemMapper
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContentListLogic(
    private val scope: CoroutineScope,
    private val repository: BattleRepositoryApi,
    private val favoritesRepository: FavoritesRepository,
    private val appConfigRepository: AppConfigRepository,
    private var mode: ContentListMode,
    private val pokemonCatalogItems: List<PokemonPickerUiModel> = emptyList()
) {
    private val _uiState = MutableStateFlow(ContentListUiState())
    val uiState: StateFlow<ContentListUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow(
        if (mode is ContentListMode.Search) (mode as ContentListMode.Search).params.orderBy else "time"
    )
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    private val _selectedFormatId = MutableStateFlow(
        when (mode) {
            is ContentListMode.Pokemon -> (mode as ContentListMode.Pokemon).formatId
                ?: appConfigRepository.getDefaultFormatId()
            is ContentListMode.Player -> (mode as ContentListMode.Player).formatId
                ?: appConfigRepository.getDefaultFormatId()
            else -> 0
        }
    )
    val selectedFormatId: StateFlow<Int> = _selectedFormatId.asStateFlow()

    fun initialize() {
        when {
            mode is ContentListMode.Favorites &&
                    (mode as ContentListMode.Favorites).contentType == FavoriteContentType.Pokemon ->
                observeFavoritePokemon()
            mode is ContentListMode.Favorites &&
                    (mode as ContentListMode.Favorites).contentType == FavoriteContentType.Players ->
                observeFavoritePlayers()
            mode is ContentListMode.Home -> waitForConfigThenLoad()
            else -> loadContent()
        }
    }

    private fun waitForConfigThenLoad() {
        scope.launch {
            try {
                val currentConfig = appConfigRepository.config.value
                if (currentConfig != null) {
                    loadContent()
                } else {
                    _uiState.update { it.copy(isLoading = true) }
                    appConfigRepository.config.filterNotNull().first()
                    loadContent()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error") }
            }
        }
    }

    private fun observeFavoritePokemon() {
        scope.launch {
            try {
                favoritesRepository.favoritePokemonIds.collect { ids ->
                    loadFavoriteItems(
                        ids = ids.toList(),
                        isEmpty = ids.isEmpty(),
                        fetch = { repository.getPokemonByIds(it) },
                        map = { ContentListItemMapper.fromPokemon(it) }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun observeFavoritePlayers() {
        scope.launch {
            try {
                favoritesRepository.favoritePlayerNames.collect { names ->
                    loadFavoriteItems(
                        ids = names.toList(),
                        isEmpty = names.isEmpty(),
                        fetch = { repository.getPlayersByNames(it) },
                        map = { ContentListItemMapper.fromPlayers(it) }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun <K, T> loadFavoriteItems(
        ids: List<K>,
        isEmpty: Boolean,
        fetch: suspend (List<K>) -> List<T>,
        map: (List<T>) -> List<ContentListItem>
    ) {
        if (isEmpty) {
            _uiState.update { it.copy(items = emptyList(), isLoading = false, error = null, canPaginate = false) }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val items = fetch(ids)
            _uiState.update {
                it.copy(isLoading = false, items = map(items), error = null, canPaginate = false)
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun loadContent() {
        scope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                val (items, pagination) = fetchContent()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = items,
                        error = null,
                        currentPage = pagination.page,
                        canPaginate = pagination.page < pagination.totalPages
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun refresh() {
        scope.launch {
            try {
                _uiState.update { it.copy(isRefreshing = true, error = null) }
                val (items, pagination) = fetchContent(page = 1)
                _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        items = items,
                        error = null,
                        currentPage = pagination.page,
                        canPaginate = pagination.page < pagination.totalPages
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRefreshing = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun paginate() {
        val currentState = _uiState.value
        if (currentState.isPaginating || !currentState.canPaginate || currentState.loadingSections.isNotEmpty()) return

        val nextPage = currentState.currentPage + 1
        scope.launch {
            try {
                _uiState.update { it.copy(isPaginating = true) }
                val (items, pagination) = fetchContent(page = nextPage)
                _uiState.update {
                    it.copy(
                        isPaginating = false,
                        items = (it.items + items).distinctBy { item -> item.listKey },
                        currentPage = pagination.page,
                        canPaginate = pagination.page < pagination.totalPages
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isPaginating = false) }
            }
        }
    }

    fun selectFormat(formatId: Int) {
        if (_selectedFormatId.value == formatId) return
        _selectedFormatId.value = formatId
        scope.launch {
            try {
                _uiState.update { it.copy(loadingSections = setOf("Battles"), currentPage = 1, canPaginate = false) }
                val (items, pagination) = fetchContent()
                _uiState.update {
                    it.copy(
                        items = items,
                        loadingSections = emptySet(),
                        currentPage = pagination.page,
                        canPaginate = pagination.page < pagination.totalPages
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadingSections = emptySet()) }
            }
        }
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == "time") "rating" else "time"
        scope.launch {
            try {
                _uiState.update { it.copy(loadingSections = setOf("Battles"), currentPage = 1, canPaginate = false) }
                val (items, pagination) = fetchContent()
                _uiState.update {
                    it.copy(
                        items = items,
                        loadingSections = emptySet(),
                        currentPage = pagination.page,
                        canPaginate = pagination.page < pagination.totalPages
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(loadingSections = emptySet()) }
            }
        }
    }

    fun updateSearchParams(params: SearchParams) {
        mode = ContentListMode.Search(params)
        _sortOrder.value = params.orderBy
        _uiState.value = ContentListUiState()
        loadContent()
    }

    private suspend fun fetchContent(page: Int = 1): Pair<List<ContentListItem>, Pagination> = when (val m = mode) {
        is ContentListMode.Home -> {
            val nowSeconds = currentTimeMillis() / 1000
            val formatId = appConfigRepository.getDefaultFormatId()
            val result = repository.searchMatches(
                filters = emptyList(),
                formatId = formatId,
                orderBy = "rating",
                page = page,
                timeRangeStart = nowSeconds - 86400,
                timeRangeEnd = nowSeconds
            )
            ContentListItemMapper.fromBattles(result.battles) to result.pagination
        }
        is ContentListMode.Favorites -> when (m.contentType) {
            FavoriteContentType.Battles -> {
                val ids = favoritesRepository.favoriteBattleIds.value.toList()
                if (ids.isEmpty()) {
                    emptyList<ContentListItem>() to Pagination(1, 0, 0, 1)
                } else {
                    val battles = repository.getMatchesByIds(ids)
                    ContentListItemMapper.fromBattles(battles) to Pagination(1, battles.size, battles.size, 1)
                }
            }
            FavoriteContentType.Pokemon -> {
                val ids = favoritesRepository.favoritePokemonIds.value.toList()
                if (ids.isEmpty()) {
                    emptyList<ContentListItem>() to Pagination(1, 0, 0, 1)
                } else {
                    val pokemon = repository.getPokemonByIds(ids)
                    ContentListItemMapper.fromPokemon(pokemon) to Pagination(1, pokemon.size, pokemon.size, 1)
                }
            }
            FavoriteContentType.Players -> {
                val names = favoritesRepository.favoritePlayerNames.value.toList()
                if (names.isEmpty()) {
                    emptyList<ContentListItem>() to Pagination(1, 0, 0, 1)
                } else {
                    val players = repository.getPlayersByNames(names)
                    ContentListItemMapper.fromPlayers(players) to Pagination(1, players.size, players.size, 1)
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
                if (page == 1) async { runCatching { repository.getPlayersByNames(listOf(name)) } }
                else null
            }

            val result = battlesDeferred.await()
            val battleItems = ContentListItemMapper.fromBattles(result.battles)
            if (page == 1) {
                val pinnedPokemon = ContentListItemMapper.fromPokemonCatalog(
                    m.params.filters.map { it.pokemonId }, pokemonCatalogItems
                )
                val pinnedPlayer = playerDeferred?.await()?.getOrNull()?.firstOrNull()?.let {
                    listOf(ContentListItem.Player(id = it.id, name = it.name))
                } ?: emptyList()
                val sections = buildList {
                    if (pinnedPokemon.isNotEmpty()) add(ContentListItem.Section("Pokémon", pinnedPokemon))
                    if (pinnedPlayer.isNotEmpty()) add(ContentListItem.Section("Players", pinnedPlayer))
                    if (battleItems.isNotEmpty()) add(ContentListItem.Section("Battles", battleItems))
                }
                sections to result.pagination
            } else {
                battleItems to result.pagination
            }
        }
        is ContentListMode.Pokemon -> {
            val result = repository.searchMatches(
                filters = listOf(SearchFilterSlot(pokemonId = m.pokemonId)),
                formatId = _selectedFormatId.value,
                orderBy = _sortOrder.value,
                page = page
            )
            val battleItems = ContentListItemMapper.fromBattles(result.battles)
            if (page == 1) {
                val sections = buildList {
                    add(ContentListItem.FormatSelector)
                    add(ContentListItem.Section("Battles", battleItems))
                }
                sections to result.pagination
            } else {
                battleItems to result.pagination
            }
        }
        is ContentListMode.Player -> if (page == 1) coroutineScope {
            val profileDeferred = async { runCatching { repository.getPlayerProfile(m.playerId) } }
            val battlesDeferred = async {
                repository.searchMatches(
                    filters = emptyList(),
                    formatId = _selectedFormatId.value,
                    orderBy = _sortOrder.value,
                    page = page,
                    playerName = m.playerName
                )
            }

            val result = battlesDeferred.await()
            val battleItems = ContentListItemMapper.fromBattles(result.battles)
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

                add(ContentListItem.FormatSelector)
                add(ContentListItem.Section("Battles", battleItems))
            }
            sections to result.pagination
        } else {
            val result = repository.searchMatches(
                filters = emptyList(),
                formatId = _selectedFormatId.value,
                orderBy = _sortOrder.value,
                page = page,
                playerName = (mode as ContentListMode.Player).playerName
            )
            ContentListItemMapper.fromBattles(result.battles) to result.pagination
        }
    }
}

package com.arcvgc.app.ui.contentlist

import com.arcvgc.app.data.AppConfigRepository
import com.arcvgc.app.data.BattleRepositoryApi
import com.arcvgc.app.data.CatalogState
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

class ContentListLogic(
    private val scope: CoroutineScope,
    private val repository: BattleRepositoryApi,
    private val favoritesRepository: FavoritesRepository,
    private val appConfigRepository: AppConfigRepository,
    private var mode: ContentListMode,
    private val pokemonCatalogItems: List<PokemonPickerUiModel> = emptyList(),
    private val pokemonCatalogState: StateFlow<CatalogState<PokemonPickerUiModel>>? = null
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
        val catalogState = pokemonCatalogState
        scope.launch {
            try {
                if (catalogState != null) {
                    combine(
                        favoritesRepository.favoritePokemonIds,
                        catalogState.map { it.items }
                    ) { ids, catalog -> ids to catalog }.collect { (ids, catalog) ->
                        if (ids.isEmpty()) {
                            _uiState.update { it.copy(items = emptyList(), isLoading = false, error = null, canPaginate = false) }
                            return@collect
                        }
                        if (catalog.isEmpty() && catalogState.value.isLoading) {
                            _uiState.update { it.copy(isLoading = true, error = null) }
                            return@collect
                        }
                        val items = ContentListItemMapper.fromPokemonCatalog(ids.toList(), catalog)
                        _uiState.update { it.copy(isLoading = false, items = items, error = null, canPaginate = false) }
                    }
                } else {
                    favoritesRepository.favoritePokemonIds.collect { ids ->
                        if (ids.isEmpty()) {
                            _uiState.update { it.copy(items = emptyList(), isLoading = false, error = null, canPaginate = false) }
                            return@collect
                        }
                        val items = ContentListItemMapper.fromPokemonCatalog(ids.toList(), pokemonCatalogItems)
                        _uiState.update { it.copy(isLoading = false, items = items, error = null, canPaginate = false) }
                    }
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
                _uiState.update { it.copy(loadingSections = reloadSections(), currentPage = 1, canPaginate = false) }
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

    private fun reloadSections(): Set<String> = when (mode) {
        is ContentListMode.Pokemon -> setOf("format_selector", "Top Teammates", "Top Items", "Top Moves", "Top Abilities", "Top Tera Types", "Battles")
        else -> setOf("Battles")
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
                val catalog = pokemonCatalogState?.value?.items ?: pokemonCatalogItems
                if (ids.isEmpty()) {
                    emptyList<ContentListItem>() to Pagination(1, 0, 0, 1)
                } else {
                    val items = ContentListItemMapper.fromPokemonCatalog(ids, catalog)
                    items to Pagination(1, items.size, items.size, 1)
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
        is ContentListMode.Pokemon -> if (page == 1) coroutineScope {
            val profileDeferred = async { runCatching { repository.getPokemonProfile(m.pokemonId, _selectedFormatId.value) } }
            val battlesDeferred = async {
                repository.searchMatches(
                    filters = listOf(SearchFilterSlot(pokemonId = m.pokemonId)),
                    formatId = _selectedFormatId.value,
                    orderBy = _sortOrder.value,
                    page = page
                )
            }

            val result = battlesDeferred.await()
            val battleItems = ContentListItemMapper.fromBattles(result.battles)
            val profile = profileDeferred.await().getOrNull()

            val sections = buildList {
                add(ContentListItem.FormatSelector)
                if (profile != null && profile.topTeammates.isNotEmpty()) {
                    val gridItems = profile.topTeammates.map {
                        ContentListItem.PokemonGridItem(
                            it.id, it.name, it.imageUrl,
                            formatUsagePercent(it.count, profile.teamCount)
                        )
                    }
                    add(ContentListItem.Section("Top Teammates", listOf(ContentListItem.PokemonGrid(gridItems))))
                }
                if (profile != null && profile.topItems.isNotEmpty()) {
                    val chipItems = profile.topItems.map {
                        ContentListItem.StatChipItem(it.name, formatUsagePercent(it.count, profile.teamCount), it.imageUrl)
                    }
                    add(ContentListItem.Section("Top Items", listOf(ContentListItem.StatChipRow(chipItems, "items"))))
                }
                if (profile != null && profile.topMoves.isNotEmpty()) {
                    val chipItems = profile.topMoves.map {
                        ContentListItem.StatChipItem(it.name, formatUsagePercent(it.count, profile.teamCount))
                    }
                    add(ContentListItem.Section("Top Moves", listOf(ContentListItem.StatChipRow(chipItems, "moves"))))
                }
                if (profile != null && profile.topAbilities.isNotEmpty()) {
                    val chipItems = profile.topAbilities.map {
                        ContentListItem.StatChipItem(it.name, formatUsagePercent(it.count, profile.teamCount))
                    }
                    add(ContentListItem.Section("Top Abilities", listOf(ContentListItem.StatChipRow(chipItems, "abilities"))))
                }
                if (profile != null && profile.topTeraTypes.isNotEmpty()) {
                    val chipItems = profile.topTeraTypes.map {
                        ContentListItem.StatChipItem(it.name, formatUsagePercent(it.count, profile.teamCount), it.imageUrl)
                    }
                    add(ContentListItem.Section("Top Tera Types", listOf(ContentListItem.StatChipRow(chipItems, "tera_types"))))
                }
                if (battleItems.isNotEmpty()) {
                    add(ContentListItem.Section("Battles", battleItems))
                }
            }
            sections to result.pagination
        } else {
            val result = repository.searchMatches(
                filters = listOf(SearchFilterSlot(pokemonId = (mode as ContentListMode.Pokemon).pokemonId)),
                formatId = _selectedFormatId.value,
                orderBy = _sortOrder.value,
                page = page
            )
            ContentListItemMapper.fromBattles(result.battles) to result.pagination
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
                if (battleItems.isNotEmpty()) {
                    add(ContentListItem.Section("Battles", battleItems))
                }
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

    private fun formatUsagePercent(count: Int, total: Int): String? {
        if (total <= 0) return null
        val hundredths = (count.toDouble() / total * 10000).roundToLong()
        val intPart = hundredths / 100
        val decPart = (hundredths % 100).toString().padStart(2, '0')
        return "$intPart.$decPart%"
    }
}

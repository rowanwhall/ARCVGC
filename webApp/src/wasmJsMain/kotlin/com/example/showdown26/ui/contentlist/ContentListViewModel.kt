package com.example.showdown26.ui.contentlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.showdown26.data.BattleRepository
import com.example.showdown26.data.FavoritesRepository
import com.example.showdown26.domain.model.Pagination
import com.example.showdown26.domain.model.SearchFilterSlot
import com.example.showdown26.ui.mapper.ContentListItemMapper
import com.example.showdown26.ui.model.ContentListItem
import com.example.showdown26.ui.model.ContentListMode
import com.example.showdown26.ui.model.FavoriteContentType
import com.example.showdown26.ui.model.PokemonPickerUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContentListViewModel(
    private val repository: BattleRepository,
    val favoritesRepository: FavoritesRepository,
    private val mode: ContentListMode = ContentListMode.Home,
    private val pokemonCatalogItems: List<PokemonPickerUiModel> = emptyList()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentListUiState())
    val uiState: StateFlow<ContentListUiState> = _uiState.asStateFlow()

    private val _sortOrder = MutableStateFlow(
        if (mode is ContentListMode.Search) (mode as ContentListMode.Search).params.orderBy else "time"
    )
    val sortOrder: StateFlow<String> = _sortOrder.asStateFlow()

    init {
        when {
            mode is ContentListMode.Favorites && mode.contentType == FavoriteContentType.Pokemon -> observeFavoritePokemon()
            mode is ContentListMode.Favorites && mode.contentType == FavoriteContentType.Players -> observeFavoritePlayers()
            else -> loadContent()
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
            try {
                val pokemon = repository.getPokemonByIds(ids.toList())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = ContentListItemMapper.fromPokemon(pokemon),
                        error = null,
                        canPaginate = false
                    )
                }
            } catch (e: Exception) {

                _uiState.update { it.copy(isLoading = false, error = e.message) }
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
            try {
                val players = repository.getPlayersByNames(names.toList())
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = ContentListItemMapper.fromPlayers(players),
                        error = null,
                        canPaginate = false
                    )
                }
            } catch (e: Exception) {

                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun fetchContent(page: Int = 1): Pair<List<ContentListItem>, Pagination> = when (val m = mode) {
        is ContentListMode.Home -> {
            val nowSeconds = currentTimeSeconds()
            val result = repository.searchMatches(
                filters = emptyList(),
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
                formatId = 1,
                minimumRating = 1000,
                orderBy = _sortOrder.value,
                page = page
            )
            val battleItems = ContentListItemMapper.fromBattles(result.battles)
            if (page == 1) {
                listOf(ContentListItem.Section("Battles", battleItems)) to result.pagination
            } else {
                battleItems to result.pagination
            }
        }
        is ContentListMode.Player -> if (page == 1) coroutineScope {
            val profileDeferred = async { runCatching { repository.getPlayerProfile(m.playerId) } }
            val battlesDeferred = async {
                repository.searchMatches(
                    filters = emptyList(),
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

                if (battleItems.isNotEmpty()) {
                    add(ContentListItem.Section("Battles", battleItems))
                }
            }
            sections to result.pagination
        } else {
            val result = repository.searchMatches(
                filters = emptyList(),
                orderBy = _sortOrder.value,
                page = page,
                playerName = m.playerName
            )
            ContentListItemMapper.fromBattles(result.battles) to result.pagination
        }
    }

    fun loadContent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
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
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
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
            try {
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

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == "time") "rating" else "time"
        viewModelScope.launch {
            _uiState.update { it.copy(loadingSections = setOf("Battles"), currentPage = 1, canPaginate = false) }
            try {
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
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

private fun currentTimeSeconds(): Long = (jsDateNow() / 1000).toLong()

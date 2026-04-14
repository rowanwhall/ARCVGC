package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.logo
import org.jetbrains.compose.resources.painterResource
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.domain.model.appendBattleParam
import com.arcvgc.app.domain.model.encodeSearchPath
import com.arcvgc.app.domain.model.encodeTopPokemonPath
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.replaceHistoryStateWithPath
import com.arcvgc.app.ui.battledetail.BattleDetailPanel
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.components.BattleCard
import com.arcvgc.app.ui.components.EmptyView
import com.arcvgc.app.ui.components.ErrorView
import com.arcvgc.app.ui.components.LoadingIndicator
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.ThemedVerticalScrollbar
import com.arcvgc.app.ui.components.TypeIconRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.ui.components.GradientToolbarHeight
import com.arcvgc.app.ui.components.GradientToolbar
import com.arcvgc.app.ui.BattleOverlayRequest
import com.arcvgc.app.ui.hasFinePointer
import com.arcvgc.app.ui.LocalBattleOverlay
import com.arcvgc.app.ui.rememberViewModel
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.model.ContentListHeaderUiModel
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FormatSorter
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.tokens.AppTokens.BrandFontFamily
import com.arcvgc.app.ui.tokens.AppTokens.ContentListItemSpacing
import com.arcvgc.app.ui.tokens.AppTokens.HeroLogoHeight
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipVerticalPadding
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth
import kotlinx.coroutines.launch

@Composable
fun ContentListPage(
    modifier: Modifier = Modifier,
    mode: ContentListMode = ContentListMode.Home,
    onBack: (() -> Unit)? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null,
    onPokemonClick: ((id: Int, name: String, imageUrl: String?, typeImageUrls: List<String>, formatId: Int?) -> Unit)? = null,
    onPlayerClick: ((id: Int, name: String, formatId: Int?) -> Unit)? = null,
    onTopPokemonClick: ((formatId: Int?) -> Unit)? = null,
    initialBattleId: Int? = null
) {
    val viewModelKey = when (mode) {
        is ContentListMode.Home -> "content_list_home"
        is ContentListMode.Favorites -> "content_list_favorites_${mode.contentType.name}"
        is ContentListMode.Search -> "content_list_search_${mode.params}"
        is ContentListMode.Pokemon -> "content_list_pokemon_${mode.pokemonId}"
        is ContentListMode.Player -> "content_list_player_${mode.playerId}_${mode.formatId}"
        is ContentListMode.TopPokemon -> "content_list_top_pokemon_${mode.formatId}"
    }
    val viewModel = rememberViewModel(viewModelKey) {
        ContentListViewModel(
            repository = DependencyContainer.battleRepository,
            favoritesRepository = DependencyContainer.favoritesRepository,
            mode = mode,
            pokemonCatalogItems = DependencyContainer.pokemonCatalogRepository.state.value.items,
            appConfigRepository = DependencyContainer.appConfigRepository,
            formatCatalogRepository = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) DependencyContainer.formatCatalogRepository else null,
            pokemonCatalogRepository = DependencyContainer.pokemonCatalogRepository
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val favoriteBattleIds by viewModel.favoritesRepository.favoriteBattleIds.collectAsState()
    val favoritePokemonIds by viewModel.favoritesRepository.favoritePokemonIds.collectAsState()
    val favoritePlayerNames by viewModel.favoritesRepository.favoritePlayerNames.collectAsState()
    val showWinnerHighlight by DependencyContainer.settingsRepository.showWinnerHighlight.collectAsState()
    val formatCatalogState = viewModel.formatCatalogState?.collectAsState()
    val appConfig by viewModel.appConfigState.collectAsState()
    val sortedFormats = remember(formatCatalogState?.value?.items, appConfig) {
        FormatSorter.sorted(formatCatalogState?.value?.items ?: emptyList(), appConfig?.defaultFormat?.id)
    }
    val selectedFormatId by viewModel.selectedFormatId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedBattleId by remember(viewModel) { mutableStateOf(initialBattleId ?: viewModel.savedBattleId) }
    var pokemonNavTarget by remember(viewModel) { mutableStateOf<PokemonNavTarget?>(null) }
    var playerNavTarget by remember(viewModel) { mutableStateOf<PlayerNavTarget?>(null) }
    var topPokemonFormatId by remember { mutableStateOf<Int?>(null) }
    val listState = remember(viewModel) {
        LazyListState(
            firstVisibleItemIndex = viewModel.savedScrollIndex,
            firstVisibleItemScrollOffset = viewModel.savedScrollOffset
        )
    }

    // Persist selectedBattleId and scroll position in ViewModel for restoration on back navigation
    LaunchedEffect(selectedBattleId) {
        viewModel.savedBattleId = selectedBattleId
    }

    // Mirror page URL in the browser address bar
    val modePath = when (mode) {
        is ContentListMode.Pokemon -> "/pokemon/${mode.pokemonId}"
        is ContentListMode.Player -> "/player/${mode.playerName}"
        is ContentListMode.Favorites -> when (mode.contentType) {
            FavoriteContentType.Battles -> "/favorites/battles"
            FavoriteContentType.Pokemon -> "/favorites/pokemon"
            FavoriteContentType.Players -> "/favorites/players"
        }
        is ContentListMode.Search -> encodeSearchPath(mode.params)
        is ContentListMode.Home -> "/"
        is ContentListMode.TopPokemon -> encodeTopPokemonPath(mode.formatId)
    }
    LaunchedEffect(selectedBattleId) {
        val path = if (mode is ContentListMode.Home && selectedBattleId != null) {
            "/battle/$selectedBattleId"
        } else {
            appendBattleParam(modePath, selectedBattleId)
        }
        replaceHistoryStateWithPath(path)
    }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.savedScrollIndex = index
                viewModel.savedScrollOffset = offset
            }
    }

    val navigateToPokemon: (Int, String, String?, List<String>, Int?) -> Unit = { id, name, imageUrl, typeImageUrls, formatId ->
        if (onPokemonClick != null) {
            onPokemonClick(id, name, imageUrl, typeImageUrls, formatId)
        } else {
            pokemonNavTarget = PokemonNavTarget(id, name, imageUrl, typeImageUrls, formatId)
        }
    }

    val navigateToPlayer: (Int, String, Int?) -> Unit = { id, name, formatId ->
        if (onPlayerClick != null) {
            onPlayerClick(id, name, formatId)
        } else {
            playerNavTarget = PlayerNavTarget(id, name, formatId)
        }
    }

    topPokemonFormatId?.let { formatId ->
        ContentListPage(
            mode = ContentListMode.TopPokemon(formatId = formatId),
            onBack = { topPokemonFormatId = null },
            onPokemonClick = onPokemonClick,
            onPlayerClick = onPlayerClick
        )
        return
    }

    val currentPokemonNav = pokemonNavTarget
    if (currentPokemonNav != null) {
        ContentListPage(
            mode = ContentListMode.Pokemon(
                currentPokemonNav.id, currentPokemonNav.name, currentPokemonNav.imageUrl,
                currentPokemonNav.typeImageUrls.getOrNull(0),
                currentPokemonNav.typeImageUrls.getOrNull(1),
                currentPokemonNav.formatId
            ),
            onBack = { pokemonNavTarget = null },
            modifier = modifier
        )
        return
    }

    val currentPlayerNav = playerNavTarget
    if (currentPlayerNav != null) {
        ContentListPage(
            mode = ContentListMode.Player(currentPlayerNav.id, currentPlayerNav.name, currentPlayerNav.formatId),
            onBack = { playerNavTarget = null },
            modifier = modifier
        )
        return
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == WindowSizeClass.Compact
    val battleOverlay = LocalBattleOverlay.current

    // When the detail pane opens, save the current scroll position and scroll to the selected
    // battle in the narrower grid. When it closes, restore the saved position exactly.
    var scrollToBattleId by remember { mutableStateOf<Pair<Int, Int>?>(null) } // (battleId, generation)
    var scrollGeneration by remember { mutableStateOf(0) }
    var restoreExactPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var restoreBattleId by remember { mutableStateOf<Int?>(null) }
    var restoreScrollPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var paneWasOpen by remember { mutableStateOf(false) }
    LaunchedEffect(selectedBattleId) {
        if (isCompact) return@LaunchedEffect
        val battleId = selectedBattleId
        if (battleId != null) {
            if (!paneWasOpen) {
                // Pane opening — save exact position (wide-grid space, safe to restore)
                restoreExactPosition = listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
                restoreBattleId = null
            } else {
                // Pane switching — save the battle ID to resolve in wide-grid space at close time
                restoreExactPosition = null
                restoreBattleId = battleId
            }
            scrollGeneration++
            scrollToBattleId = battleId to scrollGeneration
            restoreScrollPosition = null
            paneWasOpen = true
        } else if (paneWasOpen) {
            // Pane closing — restore exact position or resolve battle ID in wide grid
            val closeBattleId = restoreBattleId
            if (closeBattleId != null) {
                scrollGeneration++
                scrollToBattleId = closeBattleId to scrollGeneration
            } else {
                scrollToBattleId = null
                restoreScrollPosition = restoreExactPosition
            }
            restoreExactPosition = null
            restoreBattleId = null
            paneWasOpen = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isCompact) {
            // Compact: full-width list, battle detail hoisted to MobileLayout via LocalBattleOverlay
            ContentListContent(
                uiState = uiState,
                header = mode.toHeaderUiModel(),
                hasToolbar = onBack != null,
                selectedBattleId = null,
                showWinnerHighlight = showWinnerHighlight,
                onRetry = viewModel::loadContent,
                onPaginate = viewModel::paginate,
                listState = listState,
                onItemClick = { item ->
                    when (item) {
                        is ContentListItem.Battle -> {
                            if (battleOverlay != null) {
                                val battle = item.uiModel
                                battleOverlay(BattleOverlayRequest(battleId = battle.id))
                            } else {
                                selectedBattleId = item.uiModel.id
                            }
                        }
                        is ContentListItem.Pokemon -> {
                            val derivedFormatId = when (mode) {
                                is ContentListMode.Home -> viewModel.selectedFormatId.value
                                is ContentListMode.TopPokemon -> viewModel.selectedFormatId.value
                                is ContentListMode.Search -> mode.params.formatId
                                is ContentListMode.Pokemon -> viewModel.selectedFormatId.value
                                is ContentListMode.Player -> viewModel.selectedFormatId.value
                                else -> null
                            }
                            navigateToPokemon(
                                item.id, item.name, item.imageUrl,
                                item.types.mapNotNull { it.imageUrl },
                                derivedFormatId
                            )
                        }
                        is ContentListItem.Player -> {
                            val derivedFormatId = when (mode) {
                                is ContentListMode.Home -> viewModel.selectedFormatId.value
                                is ContentListMode.TopPokemon -> viewModel.selectedFormatId.value
                                is ContentListMode.Search -> mode.params.formatId
                                is ContentListMode.Pokemon -> viewModel.selectedFormatId.value
                                is ContentListMode.Player -> viewModel.selectedFormatId.value
                                else -> null
                            }
                            navigateToPlayer(item.id, item.name, derivedFormatId)
                        }
                        is ContentListItem.Section -> {}
                        is ContentListItem.HighlightButtons -> {}
                        is ContentListItem.PokemonGrid -> {}
                        is ContentListItem.StatChipRow -> {}
                        is ContentListItem.FormatSelector -> {}
                        is ContentListItem.SearchField -> {}
                    }
                },
                onHighlightBattleClick = { battleId ->
                    if (battleOverlay != null) {
                        battleOverlay(BattleOverlayRequest(battleId = battleId))
                    } else {
                        selectedBattleId = battleId
                    }
                },
                onPokemonGridClick = { pokemon ->
                    val derivedFormatId = when (mode) {
                        is ContentListMode.Home -> viewModel.selectedFormatId.value
                        is ContentListMode.TopPokemon -> viewModel.selectedFormatId.value
                        is ContentListMode.Search -> mode.params.formatId
                        is ContentListMode.Pokemon -> viewModel.selectedFormatId.value
                        is ContentListMode.Player -> viewModel.selectedFormatId.value
                        else -> null
                    }
                    navigateToPokemon(pokemon.id, pokemon.name, pokemon.imageUrl, emptyList(), derivedFormatId)
                },
                searchParams = (mode as? ContentListMode.Search)?.params,
                onSearchParamsChanged = onSearchParamsChanged,
                sortOrder = when (mode) {
                    is ContentListMode.Search, is ContentListMode.Pokemon, is ContentListMode.Player -> sortOrder
                    else -> null
                },
                onToggleSortOrder = when (mode) {
                    is ContentListMode.Search, is ContentListMode.Pokemon, is ContentListMode.Player -> viewModel::toggleSortOrder
                    else -> null
                },
                formats = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) sortedFormats else emptyList(),
                selectedFormatId = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) selectedFormatId else 0,
                onFormatSelected = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) viewModel::selectFormat else null,
                searchQuery = if (mode is ContentListMode.TopPokemon) searchQuery else "",
                onSearchQueryChanged = if (mode is ContentListMode.TopPokemon) viewModel::setSearchQuery else null,
                onSeeMore = {
                    val fmtId = viewModel.selectedFormatId.value
                    if (onTopPokemonClick != null) onTopPokemonClick(fmtId) else { topPokemonFormatId = fmtId }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (onBack != null) {
                GradientToolbar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (mode is ContentListMode.Pokemon) {
                            val pId = mode.pokemonId
                            val isFav = pId in favoritePokemonIds
                            IconButton(onClick = { viewModel.favoritesRepository.togglePokemonFavorite(pId) }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFav) "Unfavorite" else "Favorite",
                                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (mode is ContentListMode.Player) {
                            val pName = mode.playerName
                            val isFav = pName in favoritePlayerNames
                            IconButton(onClick = { viewModel.favoritesRepository.togglePlayerFavorite(pName) }) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFav) "Unfavorite" else "Favorite",
                                    tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier.widthIn(max = 900.dp).align(Alignment.TopCenter)
                )
            }
        } else {
            // Expanded: master-detail Row layout
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = if (selectedBattleId != null) {
                        Modifier.weight(1f).fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    }
                ) {
                    ContentListContent(
                        uiState = uiState,
                        header = mode.toHeaderUiModel(),
                        hasToolbar = onBack != null,
                        selectedBattleId = selectedBattleId,
                        showWinnerHighlight = showWinnerHighlight,
                        scrollToBattleId = scrollToBattleId,
                        restoreScrollPosition = restoreScrollPosition,
                        listState = listState,
                        onRetry = viewModel::loadContent,
                        onPaginate = viewModel::paginate,
                        onItemClick = { item ->
                            when (item) {
                                is ContentListItem.Battle -> selectedBattleId = item.uiModel.id
                                is ContentListItem.Pokemon -> {
                                    val derivedFormatId = when (mode) {
                                        is ContentListMode.Home -> viewModel.selectedFormatId.value
                                        is ContentListMode.TopPokemon -> viewModel.selectedFormatId.value
                                        is ContentListMode.Search -> mode.params.formatId
                                        is ContentListMode.Pokemon -> viewModel.selectedFormatId.value
                                        is ContentListMode.Player -> viewModel.selectedFormatId.value
                                        else -> null
                                    }
                                    navigateToPokemon(
                                        item.id, item.name, item.imageUrl,
                                        item.types.mapNotNull { it.imageUrl },
                                        derivedFormatId
                                    )
                                }
                                is ContentListItem.Player -> {
                                    val derivedFormatId = when (mode) {
                                        is ContentListMode.Home -> viewModel.selectedFormatId.value
                                        is ContentListMode.TopPokemon -> viewModel.selectedFormatId.value
                                        is ContentListMode.Search -> mode.params.formatId
                                        is ContentListMode.Pokemon -> viewModel.selectedFormatId.value
                                        is ContentListMode.Player -> viewModel.selectedFormatId.value
                                        else -> null
                                    }
                                    navigateToPlayer(item.id, item.name, derivedFormatId)
                                }
                                is ContentListItem.Section -> {}
                                is ContentListItem.HighlightButtons -> {}
                                is ContentListItem.PokemonGrid -> {}
                                is ContentListItem.FormatSelector -> {}
                                is ContentListItem.StatChipRow -> {}
                                is ContentListItem.SearchField -> {}
                            }
                        },
                        onHighlightBattleClick = { battleId -> selectedBattleId = battleId },
                        onPokemonGridClick = { pokemon ->
                            val derivedFormatId = when (mode) {
                                is ContentListMode.Home -> viewModel.selectedFormatId.value
                                is ContentListMode.TopPokemon -> viewModel.selectedFormatId.value
                                is ContentListMode.Search -> mode.params.formatId
                                is ContentListMode.Pokemon -> viewModel.selectedFormatId.value
                                is ContentListMode.Player -> viewModel.selectedFormatId.value
                                else -> null
                            }
                            navigateToPokemon(pokemon.id, pokemon.name, pokemon.imageUrl, emptyList(), derivedFormatId)
                        },
                        searchParams = (mode as? ContentListMode.Search)?.params,
                        onSearchParamsChanged = onSearchParamsChanged,
                        sortOrder = when (mode) {
                            is ContentListMode.Search, is ContentListMode.Pokemon, is ContentListMode.Player -> sortOrder
                            else -> null
                        },
                        onToggleSortOrder = when (mode) {
                            is ContentListMode.Search, is ContentListMode.Pokemon, is ContentListMode.Player -> viewModel::toggleSortOrder
                            else -> null
                        },
                        formats = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) sortedFormats else emptyList(),
                        selectedFormatId = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) selectedFormatId else 0,
                        onFormatSelected = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) viewModel::selectFormat else null,
                        searchQuery = if (mode is ContentListMode.TopPokemon) searchQuery else "",
                        onSearchQueryChanged = if (mode is ContentListMode.TopPokemon) viewModel::setSearchQuery else null,
                        onSeeMore = {
                            val fmtId = viewModel.selectedFormatId.value
                            if (onTopPokemonClick != null) onTopPokemonClick(fmtId) else { topPokemonFormatId = fmtId }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (onBack != null) {
                        GradientToolbar(
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            actions = {
                                if (mode is ContentListMode.Pokemon) {
                                    val pId = mode.pokemonId
                                    val isFav = pId in favoritePokemonIds
                                    IconButton(onClick = { viewModel.favoritesRepository.togglePokemonFavorite(pId) }) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (isFav) "Unfavorite" else "Favorite",
                                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (mode is ContentListMode.Player) {
                                    val pName = mode.playerName
                                    val isFav = pName in favoritePlayerNames
                                    IconButton(onClick = { viewModel.favoritesRepository.togglePlayerFavorite(pName) }) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (isFav) "Unfavorite" else "Favorite",
                                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.widthIn(max = 900.dp).align(Alignment.TopCenter)
                        )
                    }
                }

                selectedBattleId?.let { battleId ->
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    BattleDetailPanel(
                        battleId = battleId,
                        isFavorited = battleId in favoriteBattleIds,
                        onToggleFavorite = { viewModel.favoritesRepository.toggleBattleFavorite(battleId) },
                        onClose = { selectedBattleId = null },
                        showWinnerHighlight = showWinnerHighlight,
                        onPokemonClick = { id, name, imageUrl, typeImageUrls, formatId ->
                            navigateToPokemon(id, name, imageUrl, typeImageUrls, formatId)
                        },
                        onPlayerClick = { id, name, formatId ->
                            navigateToPlayer(id, name, formatId)
                        },
                        modifier = Modifier.widthIn(max = DETAIL_PANEL_MAX_WIDTH).fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentListContent(
    uiState: ContentListUiState,
    modifier: Modifier = Modifier,
    header: ContentListHeaderUiModel = ContentListHeaderUiModel.None,
    hasToolbar: Boolean = false,
    selectedBattleId: Int? = null,
    showWinnerHighlight: Boolean = true,
    scrollToBattleId: Pair<Int, Int>? = null, // (battleId, generation) — generation forces re-trigger
    restoreScrollPosition: Pair<Int, Int>? = null,
    onRetry: () -> Unit,
    onPaginate: () -> Unit,
    onItemClick: (ContentListItem) -> Unit,
    onHighlightBattleClick: (Int) -> Unit = {},
    onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit = {},
    searchParams: SearchParams? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null,
    sortOrder: String? = null,
    onToggleSortOrder: (() -> Unit)? = null,
    formats: List<FormatUiModel> = emptyList(),
    selectedFormatId: Int = 0,
    onFormatSelected: ((Int) -> Unit)? = null,
    searchQuery: String = "",
    onSearchQueryChanged: ((String) -> Unit)? = null,
    onSeeMore: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState()
) {

    val shouldPaginate by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - PAGINATION_THRESHOLD
        }
    }

    LaunchedEffect(shouldPaginate) {
        snapshotFlow { shouldPaginate }
            .collect { if (it) onPaginate() }
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val scope = rememberCoroutineScope()

    val toolbarSpacing = if (hasToolbar) GradientToolbarHeight else 0.dp
    val topPadding = toolbarSpacing + when (header) {
        is ContentListHeaderUiModel.PokemonHero -> 4.dp
        is ContentListHeaderUiModel.PlayerHero -> 4.dp
        is ContentListHeaderUiModel.SearchFilters -> 8.dp
        else -> 16.dp
    }

    // Forward scroll-wheel events from the empty gutters to the LazyColumn on desktop.
    // Gated on pointer type: touch devices (even in landscape/expanded) lose fling scrolling
    // when an outer scrollable intercepts drag events.
    Box(
        modifier = modifier.then(
            if (windowSizeClass == WindowSizeClass.Expanded && hasFinePointer()) {
                Modifier.scrollable(
                    state = rememberScrollableState { delta ->
                        scope.launch { listState.scrollBy(-delta) }
                        delta
                    },
                    orientation = Orientation.Vertical
                )
            } else {
                Modifier
            }
        )
    ) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
    val availableWidth = maxWidth
    val battleColumns = computeBattleColumns(availableWidth)

    // On pane open: scroll to the selected battle's row in the narrower grid.
    // On pane close: restore the exact scroll position from before the pane opened.
    LaunchedEffect(scrollToBattleId) {
        val (targetId, _) = scrollToBattleId ?: return@LaunchedEffect
        val index = computeBattleItemIndex(
            header, uiState, targetId, battleColumns,
            hasFormats = formats.isNotEmpty() && onFormatSelected != null,
            hasSearchQuery = onSearchQueryChanged != null
        )
        if (index != null) {
            listState.scrollToItem(index)
        }
    }
    LaunchedEffect(restoreScrollPosition) {
        val (index, offset) = restoreScrollPosition ?: return@LaunchedEffect
        listState.scrollToItem(index, offset)
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            top = topPadding,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(ContentListItemSpacing),
        modifier = Modifier.fillMaxSize()
    ) {
        when (val h = header) {
            is ContentListHeaderUiModel.None -> {}
            is ContentListHeaderUiModel.HomeHero -> {
                item(key = "home_hero") {
                    CenteredItem(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.logo),
                                contentDescription = "ARC",
                                modifier = Modifier.height(HeroLogoHeight)
                            )
                            Text(
                                text = "ARC",
                                fontSize = 24.sp,
                                fontFamily = BrandFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            is ContentListHeaderUiModel.TopPokemonHero -> {
                item(key = "top_pokemon_hero") {
                    CenteredItem(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 24.dp, bottom = 8.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Top Pok\u00E9mon",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            is ContentListHeaderUiModel.FavoritesHero -> {
                // TODO: Replace with branded favorites asset when ready
            }
            is ContentListHeaderUiModel.SearchFilters -> {
                item(key = "search_filters") {
                    CenteredItem(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SearchFilterChips(
                            filters = h,
                            searchParams = searchParams,
                            onSearchParamsChanged = onSearchParamsChanged
                        )
                    }
                }
            }
            is ContentListHeaderUiModel.PokemonHero -> {
                item(key = "pokemon_hero") {
                    CenteredItem(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PokemonAvatar(
                                imageUrl = h.imageUrl,
                                contentDescription = h.name,
                                circleSize = 132.dp,
                                spriteSize = 184.dp
                            )
                            Text(
                                text = h.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (h.typeImageUrls.isNotEmpty()) {
                                TypeIconRow(
                                    types = h.typeImageUrls.map { TypeInfo("Type", it) },
                                    iconSize = 24.dp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            is ContentListHeaderUiModel.PlayerHero -> {
                item(key = "player_hero") {
                    CenteredItem(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = h.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .border(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(PlayerChipCornerRadius))
                                    .padding(horizontal = PlayerChipHorizontalPadding, vertical = PlayerChipVerticalPadding)
                            )
                        }
                    }
                }
            }
        }

        when {
            uiState.isLoading -> {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.5f),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
            }

            uiState.error != null && uiState.items.isEmpty() -> {
                item(key = "error") {
                    ErrorView(
                        onRetry = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.5f)
                    )
                }
            }

            uiState.items.none { it.isContentItem } -> {
                uiState.items.forEach { topItem ->
                    if (topItem is ContentListItem.FormatSelector && formats.isNotEmpty() && onFormatSelected != null) {
                        val isLoadingFormat = "format_selector" in uiState.loadingSections
                        item(key = topItem.listKey) {
                            CenteredItem(modifier = Modifier.padding(horizontal = 16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FormatDropdown(
                                        formats = formats,
                                        selectedFormatId = selectedFormatId,
                                        onFormatSelected = onFormatSelected
                                    )
                                    if (isLoadingFormat) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.padding(start = 8.dp).size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (topItem is ContentListItem.SearchField && onSearchQueryChanged != null) {
                        item(key = topItem.listKey) {
                            CenteredItem(modifier = Modifier.padding(horizontal = 16.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChanged,
                                    label = { Text("Search Pok\u00E9mon") },
                                    singleLine = true,
                                    trailingIcon = if (searchQuery.isNotEmpty()) {
                                        {
                                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                            }
                                        }
                                    } else null,
                                    shape = RoundedCornerShape(CardCornerRadius),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                item(key = "empty") {
                    EmptyView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.5f)
                    )
                }
            }

            else -> {
                val itemPadding = Modifier.padding(horizontal = 16.dp)
                uiState.items.forEach { topItem ->
                    when (topItem) {
                        is ContentListItem.Section -> {
                            val isLoadingSection = topItem.header in uiState.loadingSections
                            if (topItem.header.isNotEmpty()) {
                                item(key = topItem.listKey) {
                                    CenteredItem(modifier = itemPadding) {
                                        SectionHeader(
                                            title = topItem.header,
                                            isLoading = isLoadingSection,
                                            sortOrder = if (topItem.header == "Battles") sortOrder else null,
                                            onToggleSortOrder = if (topItem.header == "Battles") onToggleSortOrder else null,
                                            onSeeMore = if (topItem.trailingAction is ContentListItem.SectionAction.SeeMore) onSeeMore else null
                                        )
                                    }
                                }
                            }
                            if (topItem.items.isEmpty() && !isLoadingSection) {
                                item(key = "${topItem.listKey}_empty") {
                                    CenteredItem(modifier = itemPadding) {
                                        EmptyView(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
                                    }
                                }
                            }
                            val isBattleSection = topItem.items.firstOrNull() is ContentListItem.Battle
                            if (isBattleSection && battleColumns > 1) {
                                val loadingMod = if (isLoadingSection) Modifier.alpha(0.5f) else Modifier
                                battleGridItems(
                                    battles = topItem.items.filterIsInstance<ContentListItem.Battle>(),
                                    columns = battleColumns,
                                    selectedBattleId = selectedBattleId,
                                    showWinnerHighlight = showWinnerHighlight,
                                    onItemClick = onItemClick,
                                    extraModifier = loadingMod
                                )
                            } else {
                                items(items = topItem.items, key = { it.listKey }) { child ->
                                    val childModifier = if (isLoadingSection) Modifier.alpha(0.5f) else Modifier
                                    if (child is ContentListItem.Battle) {
                                        Box(modifier = childModifier.then(itemPadding)) {
                                            ContentListItemRow(child, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                        }
                                    } else {
                                        CenteredItem(modifier = childModifier.then(if (!child.edgeToEdge) itemPadding else Modifier)) {
                                            ContentListItemRow(child, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                        }
                                    }
                                }
                            }
                        }
                        is ContentListItem.FormatSelector -> {
                            if (formats.isNotEmpty() && onFormatSelected != null) {
                                val isLoadingFormat = "format_selector" in uiState.loadingSections
                                item(key = topItem.listKey) {
                                    CenteredItem(modifier = itemPadding) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FormatDropdown(
                                                formats = formats,
                                                selectedFormatId = selectedFormatId,
                                                onFormatSelected = onFormatSelected
                                            )
                                            if (isLoadingFormat) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.padding(start = 8.dp).size(16.dp),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is ContentListItem.SearchField -> {
                            if (onSearchQueryChanged != null) {
                                item(key = topItem.listKey) {
                                    CenteredItem(modifier = itemPadding) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = onSearchQueryChanged,
                                            label = { Text("Search Pok\u00E9mon") },
                                            singleLine = true,
                                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                                {
                                                    IconButton(onClick = { onSearchQueryChanged("") }) {
                                                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                                    }
                                                }
                                            } else null,
                                            shape = RoundedCornerShape(CardCornerRadius),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                        is ContentListItem.Battle -> {
                            // Top-level battles (pages 2+) — handled via battleGridItems below
                        }
                        else -> item(key = topItem.listKey) {
                            CenteredItem(modifier = if (!topItem.edgeToEdge) itemPadding else Modifier) {
                                ContentListItemRow(topItem, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                            }
                        }
                    }
                }

                // Render top-level battles (pages 2+) in grid
                val topLevelBattles = uiState.items.filterIsInstance<ContentListItem.Battle>()
                if (topLevelBattles.isNotEmpty()) {
                    battleGridItems(
                        battles = topLevelBattles,
                        columns = battleColumns,
                        selectedBattleId = selectedBattleId,
                        showWinnerHighlight = showWinnerHighlight,
                        onItemClick = onItemClick
                    )
                }

                if (uiState.isPaginating) {
                    item(key = "paginating") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
    if (windowSizeClass == WindowSizeClass.Expanded) {
        ThemedVerticalScrollbar(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
    }
    }
}

/**
 * Mirrors the item emission order of the LazyColumn builder to find the index
 * of the row containing a given battle ID. Returns null if not found.
 */
private fun computeBattleItemIndex(
    header: ContentListHeaderUiModel,
    uiState: ContentListUiState,
    battleId: Int,
    battleColumns: Int,
    hasFormats: Boolean,
    hasSearchQuery: Boolean
): Int? {
    var index = 0

    // Header item (all non-None/FavoritesHero headers emit one item)
    when (header) {
        is ContentListHeaderUiModel.None -> {}
        is ContentListHeaderUiModel.FavoritesHero -> {} // TODO placeholder
        else -> index++
    }

    // Content items
    when {
        uiState.isLoading -> return null
        uiState.error != null && uiState.items.isEmpty() -> return null
        uiState.items.none { it.isContentItem } -> return null
        else -> {
            for (topItem in uiState.items) {
                when (topItem) {
                    is ContentListItem.Section -> {
                        val isLoadingSection = topItem.header in uiState.loadingSections
                        if (topItem.header.isNotEmpty()) index++ // section header
                        if (topItem.items.isEmpty() && !isLoadingSection) {
                            index++ // empty view
                            continue
                        }
                        val isBattleSection = topItem.items.firstOrNull() is ContentListItem.Battle
                        if (isBattleSection && battleColumns > 1) {
                            val battles = topItem.items.filterIsInstance<ContentListItem.Battle>()
                            val chunked = battles.chunked(battleColumns)
                            for (row in chunked) {
                                if (row.any { it.uiModel.id == battleId }) return index
                                index++
                            }
                        } else {
                            for (child in topItem.items) {
                                if (child is ContentListItem.Battle && child.uiModel.id == battleId) return index
                                index++
                            }
                        }
                    }
                    is ContentListItem.FormatSelector -> { if (hasFormats) index++ }
                    is ContentListItem.SearchField -> { if (hasSearchQuery) index++ }
                    is ContentListItem.Battle -> {} // handled below
                    else -> index++
                }
            }
            // Top-level battles (pages 2+)
            val topLevelBattles = uiState.items.filterIsInstance<ContentListItem.Battle>()
            if (topLevelBattles.isNotEmpty()) {
                val chunked = topLevelBattles.chunked(battleColumns)
                for (row in chunked) {
                    if (row.any { it.uiModel.id == battleId }) return index
                    index++
                }
            }
        }
    }
    return null
}

private val CONTENT_MAX_WIDTH = 900.dp

@Composable
private fun CenteredItem(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = CONTENT_MAX_WIDTH).fillMaxWidth().then(modifier)) {
            content()
        }
    }
}

private val BATTLE_CARD_MIN_WIDTH = 420.dp
private val BATTLE_CARD_MAX_WIDTH = 600.dp
private val BATTLE_GRID_SPACING = 12.dp

private fun computeBattleColumns(availableWidth: Dp): Int {
    val usableWidth = availableWidth - 32.dp // 16dp padding each side
    return ((usableWidth + BATTLE_GRID_SPACING) / (BATTLE_CARD_MIN_WIDTH + BATTLE_GRID_SPACING))
        .toInt()
        .coerceAtLeast(1)
}

private fun LazyListScope.battleGridItems(
    battles: List<ContentListItem.Battle>,
    columns: Int,
    selectedBattleId: Int?,
    showWinnerHighlight: Boolean,
    onItemClick: (ContentListItem) -> Unit,
    extraModifier: Modifier = Modifier
) {
    val chunked = battles.chunked(columns)
    chunked.forEach { row ->
        item(key = "battle_row_${row.first().listKey}") {
            Row(
                modifier = extraModifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(BATTLE_GRID_SPACING, Alignment.CenterHorizontally)
            ) {
                row.forEach { battle ->
                    val isSelected = battle.uiModel.id == selectedBattleId
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        BattleCard(
                            uiModel = battle.uiModel,
                            showWinnerHighlight = showWinnerHighlight,
                            onClick = { onItemClick(battle) },
                            modifier = Modifier
                                .widthIn(max = BATTLE_CARD_MAX_WIDTH)
                                .fillMaxWidth()
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            RoundedCornerShape(CardCornerRadius)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
                // Fill remaining slots so cards don't stretch on the last row
                repeat(columns - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

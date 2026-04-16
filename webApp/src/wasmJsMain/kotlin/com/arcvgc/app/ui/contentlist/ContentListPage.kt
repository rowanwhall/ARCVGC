package com.arcvgc.app.ui.contentlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.domain.model.appendBattleParam
import com.arcvgc.app.domain.model.encodeSearchPath
import com.arcvgc.app.domain.model.encodeTopPokemonPath
import com.arcvgc.app.ui.BattleOverlayRequest
import com.arcvgc.app.ui.LocalBattleOverlay
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.battledetail.BattleDetailPanel
import com.arcvgc.app.ui.components.GradientToolbar
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.model.FormatSorter
import com.arcvgc.app.ui.rememberViewModel
import com.arcvgc.app.ui.replaceHistoryStateWithPath

@Composable
fun ContentListPage(
    modifier: Modifier = Modifier,
    mode: ContentListMode = ContentListMode.Home,
    onBack: (() -> Unit)? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null,
    onPokemonClick: ((id: Int, name: String, imageUrl: String?, typeImageUrls: List<String>, formatId: Int?) -> Unit)? = null,
    onPlayerClick: ((id: Int, name: String, formatId: Int?) -> Unit)? = null,
    onTopPokemonClick: ((formatId: Int?) -> Unit)? = null,
    initialBattleId: Int? = null,
    showToolbarWithoutBack: Boolean = false,
    mirrorUrl: Boolean = true
) {
    val hasToolbar = onBack != null || showToolbarWithoutBack
    val viewModelKey = when (mode) {
        is ContentListMode.Home -> "content_list_home"
        is ContentListMode.Favorites -> "content_list_favorites_${mode.contentType.name}"
        is ContentListMode.Search -> "content_list_search_${mode.params}"
        is ContentListMode.Pokemon -> "content_list_pokemon_${mode.pokemonId}_${mode.formatId}"
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
    val gridState = remember(viewModel) {
        LazyGridState(
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
    if (mirrorUrl) {
        LaunchedEffect(selectedBattleId) {
            val path = if (mode is ContentListMode.Home && selectedBattleId != null) {
                "/battle/$selectedBattleId"
            } else {
                appendBattleParam(modePath, selectedBattleId)
            }
            replaceHistoryStateWithPath(path)
        }
    }
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
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

    // When the detail pane opens/closes, scroll to the target battle immediately.
    // The grid Box width snaps to its narrow (post-animation) value in the same composition
    // that sets selectedBattleId, so by the time this effect runs the grid is already in its
    // final column count (typically 1-col). scrollToItem(N) therefore sticks without being
    // clobbered to a row-start by the next measure pass.
    val hasFormats = (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) && sortedFormats.isNotEmpty()
    val hasSearchQuery = mode is ContentListMode.TopPokemon
    var lastSelectedBattleId by remember { mutableStateOf<Int?>(null) }
    var paneWasOpen by remember { mutableStateOf(false) }
    val detailPaneState = remember { MutableTransitionState(initialBattleId != null) }
    val scrollOffsetPx = with(LocalDensity.current) { BATTLE_GRID_SPACING.roundToPx() }
    LaunchedEffect(selectedBattleId) {
        if (isCompact) return@LaunchedEffect
        val battleId = selectedBattleId
        if (battleId != null) {
            lastSelectedBattleId = battleId
            val index = computeBattleItemIndex(
                mode.toHeaderUiModel(), uiState, battleId,
                hasFormats = hasFormats,
                hasSearchQuery = hasSearchQuery,
                windowSizeClass = windowSizeClass
            )
            if (index != null) {
                if (paneWasOpen) {
                    gridState.animateScrollToItem(index, scrollOffsetPx)
                } else {
                    gridState.scrollToItem(index, scrollOffsetPx)
                }
            }
            paneWasOpen = true
        } else if (paneWasOpen) {
            val closeBattleId = lastSelectedBattleId
            if (closeBattleId != null) {
                val index = computeBattleItemIndex(
                    mode.toHeaderUiModel(), uiState, closeBattleId,
                    hasFormats = hasFormats,
                    hasSearchQuery = hasSearchQuery,
                    windowSizeClass = windowSizeClass
                )
                if (index != null) {
                    gridState.animateScrollToItem(index, scrollOffsetPx)
                }
            }
            lastSelectedBattleId = null
            paneWasOpen = false
        }
    }

    // Shared `ContentListContent` inputs used by both the Compact and Expanded
    // branches below. Hoisted here so there's a single source of truth for
    // mode-derived state and callback wiring — the per-branch call sites only
    // need to provide the bits that actually differ (battle-click handling and
    // desktop-web grid sizing).
    val contentListFormatState = ContentListFormatState(
        searchParams = (mode as? ContentListMode.Search)?.params,
        sortOrder = when (mode) {
            is ContentListMode.Search, is ContentListMode.Pokemon, is ContentListMode.Player -> sortOrder
            else -> null
        },
        formats = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) sortedFormats else emptyList(),
        selectedFormatId = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) selectedFormatId else 0,
        searchQuery = if (mode is ContentListMode.TopPokemon) searchQuery else ""
    )

    fun buildCallbacks(
        onBattleItemClick: (ContentListItem.Battle) -> Unit,
        onHighlightBattleClick: (Int) -> Unit
    ): ContentListCallbacks = ContentListCallbacks(
        onRetry = viewModel::loadContent,
        onPaginate = viewModel::paginate,
        onItemClick = { item ->
            when (item) {
                is ContentListItem.Battle -> onBattleItemClick(item)
                is ContentListItem.Pokemon -> navigateToPokemon(
                    item.id, item.name, item.imageUrl,
                    item.types.mapNotNull { it.imageUrl },
                    derivedFormatId(mode, viewModel.selectedFormatId.value)
                )
                is ContentListItem.Player -> navigateToPlayer(
                    item.id, item.name,
                    derivedFormatId(mode, viewModel.selectedFormatId.value)
                )
                is ContentListItem.Section,
                is ContentListItem.SectionGroup,
                is ContentListItem.HighlightButtons,
                is ContentListItem.PokemonGrid,
                is ContentListItem.StatChipRow,
                is ContentListItem.FormatSelector,
                is ContentListItem.SearchField -> {}
            }
        },
        onHighlightBattleClick = onHighlightBattleClick,
        onPokemonGridClick = { pokemon ->
            navigateToPokemon(
                pokemon.id, pokemon.name, pokemon.imageUrl, emptyList(),
                derivedFormatId(mode, viewModel.selectedFormatId.value)
            )
        },
        onSearchParamsChanged = onSearchParamsChanged,
        onToggleSortOrder = when (mode) {
            is ContentListMode.Search, is ContentListMode.Pokemon, is ContentListMode.Player -> viewModel::toggleSortOrder
            else -> null
        },
        onFormatSelected = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) viewModel::selectFormat else null,
        onSearchQueryChanged = if (mode is ContentListMode.TopPokemon) viewModel::setSearchQuery else null,
        onSeeMore = {
            val fmtId = viewModel.selectedFormatId.value
            if (onTopPokemonClick != null) onTopPokemonClick(fmtId) else { topPokemonFormatId = fmtId }
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isCompact) {
            // Compact: full-width list, battle detail hoisted to MobileLayout via LocalBattleOverlay
            ContentListContent(
                uiState = uiState,
                callbacks = buildCallbacks(
                    onBattleItemClick = { battle ->
                        if (battleOverlay != null) {
                            battleOverlay(BattleOverlayRequest(battleId = battle.uiModel.id))
                        } else {
                            selectedBattleId = battle.uiModel.id
                        }
                    },
                    onHighlightBattleClick = { battleId ->
                        if (battleOverlay != null) {
                            battleOverlay(BattleOverlayRequest(battleId = battleId))
                        } else {
                            selectedBattleId = battleId
                        }
                    }
                ),
                header = mode.toHeaderUiModel(),
                hasToolbar = hasToolbar,
                selectedBattleId = null,
                showWinnerHighlight = showWinnerHighlight,
                formatState = contentListFormatState,
                gridState = gridState,
                modifier = Modifier.fillMaxSize()
            )

            if (hasToolbar) {
                GradientToolbar(
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
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
            // Expanded: master-detail Row layout with animated detail pane.
            //
            // When a battle card is clicked, the grid must snap to its final (narrow) width
            // before scrollToItem runs. In a multi-column LazyVerticalGrid, scrollToItem(N)
            // for a non-row-start N gets clobbered back to the row-start by the next measure
            // pass (LazyGridScrollPosition.updateFromMeasureResult). The only layout in which
            // scrollToItem(N) sticks for any N is one where every row has exactly one item —
            // i.e. a 1-column grid. So we force the grid to its post-animation width in the
            // same composition as the state change, and let scrollToItem run against a grid
            // that's already in its final column count. The pane's AnimatedVisibility slides
            // in independently, filling the empty space beside the narrowed grid.
            detailPaneState.targetState = selectedBattleId != null
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                // Derive the battle-card cell width from the **full** window width, not
                // the grid-box width. The value is stable across pane-open/close so only
                // the column count changes during the transition, not the card size.
                val battleCardCellWidth = computeBattleCardCellWidth(maxWidth)
                // Dynamically size the pane so grid + 1dp divider + pane == maxWidth exactly.
                // On wide viewports the pane gets its full DETAIL_PANEL_MAX_WIDTH and the grid
                // takes the rest. On narrow viewports we shrink the pane and hold the grid at
                // `battleCardCellWidth` so a battle card still fits in 1 column.
                val panePostWidth = (maxWidth - battleCardCellWidth - 1.dp)
                    .coerceIn(0.dp, DETAIL_PANEL_MAX_WIDTH)
                val gridWidthWhenPaneOpen = (maxWidth - panePostWidth - 1.dp)
                    .coerceAtLeast(battleCardCellWidth)

                // Top Pokémon row — escape the battle grid's FixedSize(650) cell-pack
                // constraint so the row extends to the full grid box edge.
                // Fetch capacity is based on the pane-closed viewport width so closing the
                // pane has enough tiles ready without a re-fetch.
                val gridOuterPadding = 32.dp  // LazyVerticalGrid horizontal padding 16.dp × 2
                val topPokemonFetchMaxWidth =
                    (maxWidth - gridOuterPadding - TOP_POKEMON_CARD_INNER_PADDING_TOTAL)
                        .coerceAtLeast(0.dp)
                val paneClosedTileCapacity = computeTopPokemonTileCount(topPokemonFetchMaxWidth)
                // Only Home mode actually re-fetches on this call — Pokemon/Player modes'
                // setTopPokemonFetchCount short-circuits on the non-Home guard. Running the
                // effect for them is harmless.
                LaunchedEffect(paneClosedTileCapacity) {
                    viewModel.setTopPokemonFetchCount(paneClosedTileCapacity)
                }
                // Current display width — shrinks when the detail pane opens. Passed to
                // ResponsivePokemonGridCard so it reflows its visible tile count.
                val currentGridBoxWidth =
                    if (selectedBattleId != null) gridWidthWhenPaneOpen else maxWidth
                val topPokemonDisplayMaxWidth =
                    (currentGridBoxWidth - gridOuterPadding).coerceAtLeast(0.dp)
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = if (selectedBattleId != null) {
                            Modifier.width(gridWidthWhenPaneOpen).fillMaxHeight()
                        } else {
                            Modifier.weight(1f).fillMaxHeight()
                        }
                    ) {
                        ContentListContent(
                            uiState = uiState,
                            callbacks = buildCallbacks(
                                onBattleItemClick = { battle -> selectedBattleId = battle.uiModel.id },
                                onHighlightBattleClick = { battleId -> selectedBattleId = battleId }
                            ),
                            header = mode.toHeaderUiModel(),
                            hasToolbar = hasToolbar,
                            selectedBattleId = selectedBattleId,
                            showWinnerHighlight = showWinnerHighlight,
                            formatState = contentListFormatState,
                            gridConfig = ContentListGridConfig(
                                battleCardCellWidth = battleCardCellWidth,
                                expandedTopPokemonMaxWidth = topPokemonDisplayMaxWidth
                            ),
                            gridState = gridState,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (hasToolbar) {
                            GradientToolbar(
                                navigationIcon = {
                                    if (onBack != null) {
                                        IconButton(onClick = onBack) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = "Back"
                                            )
                                        }
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
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Remember the last non-null battle ID so content persists during exit animation
                    var lastBattleId by remember { mutableStateOf(selectedBattleId) }
                    if (selectedBattleId != null) lastBattleId = selectedBattleId

                    AnimatedVisibility(
                        visibleState = detailPaneState,
                        enter = slideInHorizontally(
                            animationSpec = tween(DETAIL_PANE_ANIM_DURATION_MS),
                            initialOffsetX = { fullWidth -> fullWidth }
                        ),
                        exit = slideOutHorizontally(
                            animationSpec = tween(DETAIL_PANE_ANIM_DURATION_MS),
                            targetOffsetX = { fullWidth -> fullWidth }
                        )
                    ) {
                        lastBattleId?.let { battleId ->
                            Row(modifier = Modifier.fillMaxHeight()) {
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
                                    modifier = Modifier.width(panePostWidth).fillMaxHeight()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


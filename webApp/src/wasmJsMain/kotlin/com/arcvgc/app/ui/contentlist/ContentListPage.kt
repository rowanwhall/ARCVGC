package com.arcvgc.app.ui.contentlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import com.arcvgc.app.ui.model.unwrapSectionGroups
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
                hasToolbar = hasToolbar,
                selectedBattleId = null,
                showWinnerHighlight = showWinnerHighlight,
                onRetry = viewModel::loadContent,
                onPaginate = viewModel::paginate,
                gridState = gridState,
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
                        is ContentListItem.SectionGroup -> {}
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
                            header = mode.toHeaderUiModel(),
                            hasToolbar = hasToolbar,
                            selectedBattleId = selectedBattleId,
                            showWinnerHighlight = showWinnerHighlight,
                            gridState = gridState,
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
                                    is ContentListItem.SectionGroup -> {}
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
                            expandedTopPokemonMaxWidth = topPokemonDisplayMaxWidth,
                            battleCardCellWidth = battleCardCellWidth,
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

@Composable
private fun ContentListContent(
    uiState: ContentListUiState,
    modifier: Modifier = Modifier,
    header: ContentListHeaderUiModel = ContentListHeaderUiModel.None,
    hasToolbar: Boolean = false,
    selectedBattleId: Int? = null,
    showWinnerHighlight: Boolean = true,
    onRetry: () -> Unit,
    onPaginate: () -> Unit,
    onItemClick: (ContentListItem) -> Unit,
    onHighlightBattleClick: (Int) -> Unit = {},
    onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit = {},
    expandedTopPokemonMaxWidth: Dp = 0.dp,
    battleCardCellWidth: Dp = BATTLE_CARD_DEFAULT_WIDTH,
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
    gridState: LazyGridState = rememberLazyGridState()
) {

    val shouldPaginate by remember {
        derivedStateOf {
            val layoutInfo = gridState.layoutInfo
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

    // Forward scroll-wheel events from the empty gutters to the grid on desktop.
    // Gated on pointer type: touch devices (even in landscape/expanded) lose fling scrolling
    // when an outer scrollable intercepts drag events.
    Box(
        modifier = modifier.then(
            if (windowSizeClass == WindowSizeClass.Expanded && hasFinePointer()) {
                Modifier.scrollable(
                    state = rememberScrollableState { delta ->
                        scope.launch { gridState.scrollBy(-delta) }
                        delta
                    },
                    orientation = Orientation.Vertical
                )
            } else {
                Modifier
            }
        )
    ) {

    val fullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

    LazyVerticalGrid(
        columns = GridCells.FixedSize(battleCardCellWidth),
        state = gridState,
        contentPadding = PaddingValues(
            top = topPadding,
            bottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(ContentListItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(BATTLE_GRID_SPACING, Alignment.Start),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        when (val h = header) {
            is ContentListHeaderUiModel.None -> {}
            is ContentListHeaderUiModel.HomeHero -> {
                item(key = "home_hero", span = fullSpan) {
                    CenteredItem(modifier = Modifier.padding(top = 24.dp)) {
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
                item(key = "top_pokemon_hero", span = fullSpan) {
                    CenteredItem(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Usage",
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
                item(key = "search_filters", span = fullSpan) {
                    CenteredItem {
                        SearchFilterChips(
                            filters = h,
                            searchParams = searchParams,
                            onSearchParamsChanged = onSearchParamsChanged
                        )
                    }
                }
            }
            is ContentListHeaderUiModel.PokemonHero -> {
                item(key = "pokemon_hero", span = fullSpan) {
                    CenteredItem {
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
                item(key = "player_hero", span = fullSpan) {
                    CenteredItem {
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
                item(key = "loading", span = fullSpan) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                }
            }

            uiState.error != null && uiState.items.isEmpty() -> {
                item(key = "error", span = fullSpan) {
                    ErrorView(
                        onRetry = onRetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            }

            uiState.items.none { it.isContentItem } -> {
                uiState.items.forEach { topItem ->
                    if (topItem is ContentListItem.FormatSelector && formats.isNotEmpty() && onFormatSelected != null) {
                        val isLoadingFormat = "format_selector" in uiState.loadingSections
                        item(key = topItem.listKey, span = fullSpan) {
                            CenteredItem {
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
                        item(key = topItem.listKey, span = fullSpan) {
                            CenteredItem {
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
                item(key = "empty", span = fullSpan) {
                    EmptyView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            }

            else -> {
                // On compact we flatten SectionGroups so the existing per-Section dispatch
                // handles them transparently. On expanded we keep groups intact so the new
                // SectionGroup branch below can lay them out as a responsive column row.
                val topLevelItems = if (windowSizeClass == WindowSizeClass.Expanded) {
                    uiState.items
                } else {
                    uiState.items.unwrapSectionGroups()
                }
                topLevelItems.forEach { topItem ->
                    when (topItem) {
                        is ContentListItem.SectionGroup -> {
                            item(key = topItem.listKey, span = fullSpan) {
                                SectionGroupLayout(
                                    group = topItem,
                                    loadingSections = uiState.loadingSections,
                                    contentMaxWidth = expandedTopPokemonMaxWidth,
                                    selectedBattleId = selectedBattleId,
                                    showWinnerHighlight = showWinnerHighlight,
                                    onItemClick = onItemClick,
                                    onHighlightBattleClick = onHighlightBattleClick,
                                    onPokemonGridClick = onPokemonGridClick
                                )
                            }
                        }
                        is ContentListItem.Section -> {
                            val isLoadingSection = topItem.header in uiState.loadingSections
                            val needsIndividualCells = topItem.items.any { it.requiresIndividualGridCells }
                            if (needsIndividualCells || windowSizeClass != WindowSizeClass.Expanded) {
                                // Battle sections (or compact layouts): header is a separate
                                // full-span grid item whose width comes from the grid's own
                                // layout math. On expanded, `BoxWithConstraints.maxWidth` inside
                                // a fullSpan item equals the cell-pack width (cols × 650 + gaps),
                                // which is exactly the right edge of the rightmost battle card.
                                // On compact, fall back to `CenteredItem` (900dp).
                                if (topItem.header.isNotEmpty()) {
                                    item(key = topItem.listKey, span = fullSpan) {
                                        if (windowSizeClass == WindowSizeClass.Expanded) {
                                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                                Box(modifier = Modifier.width(maxWidth)) {
                                                    SectionHeader(
                                                        title = topItem.header,
                                                        isLoading = isLoadingSection,
                                                        sortOrder = if (topItem.header == "Battles") sortOrder else null,
                                                        onToggleSortOrder = if (topItem.header == "Battles") onToggleSortOrder else null,
                                                        onSeeMore = if (topItem.trailingAction is ContentListItem.SectionAction.SeeMore) onSeeMore else null
                                                    )
                                                }
                                            }
                                        } else {
                                            CenteredItem {
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
                                }
                                if (topItem.items.isEmpty() && !isLoadingSection) {
                                    item(key = "${topItem.listKey}_empty", span = fullSpan) {
                                        CenteredItem {
                                            EmptyView(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
                                        }
                                    }
                                }
                                val loadingMod = if (isLoadingSection) Modifier.alpha(0.5f) else Modifier
                                if (needsIndividualCells) {
                                    items(
                                        items = topItem.items.filterIsInstance<ContentListItem.Battle>(),
                                        key = { it.listKey }
                                    ) { battle ->
                                        val isSelected = battle.uiModel.id == selectedBattleId
                                        BattleCard(
                                            uiModel = battle.uiModel,
                                            showWinnerHighlight = showWinnerHighlight,
                                            onClick = { onItemClick(battle) },
                                            modifier = Modifier
                                                .animateItem(
                                                    placementSpec = BATTLE_GRID_PLACEMENT_SPEC
                                                )
                                                .widthIn(max = battleCardCellWidth)
                                                .fillMaxWidth()
                                                .then(loadingMod)
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
                                } else {
                                    // Compact-only: non-battle children rendered individually in a
                                    // CenteredItem wrapper, matching the pre-existing compact layout.
                                    // Edge-to-edge children (e.g. StatChipRow) skip the CenteredItem
                                    // wrapper and use a layout modifier to negate the grid's
                                    // horizontal padding, so the chip carousel scrolls flush to the
                                    // viewport edges.
                                    topItem.items.forEach { child ->
                                        item(key = child.listKey, span = fullSpan) {
                                            if (child.edgeToEdge) {
                                                Box(modifier = loadingMod.escapeGridHorizontalPadding()) {
                                                    ContentListItemRow(child, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                                }
                                            } else {
                                                CenteredItem(modifier = loadingMod) {
                                                    ContentListItemRow(child, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Expanded, non-battle section: header and content share one
                                // full-span grid item. `SectionContentAlignedHeader` measures
                                // content at its natural width and sizes the header to match,
                                // so a trailing action (See More / Sort) lands at the content's
                                // actual right edge — which can extend past the cell pack via
                                // `ResponsivePokemonGridCard`'s layout escape. The item always
                                // reports the grid's cell-pack width back up so the grid places
                                // it at x=0, giving left-aligned content regardless of whether
                                // content is narrower or wider than the cell pack.
                                item(key = topItem.listKey, span = fullSpan) {
                                    SectionContentAlignedHeader(
                                        contentMeasureMaxWidth = expandedTopPokemonMaxWidth,
                                        header = if (topItem.header.isNotEmpty()) {
                                            {
                                                SectionHeader(
                                                    title = topItem.header,
                                                    isLoading = isLoadingSection,
                                                    sortOrder = if (topItem.header == "Battles") sortOrder else null,
                                                    onToggleSortOrder = if (topItem.header == "Battles") onToggleSortOrder else null,
                                                    onSeeMore = if (topItem.trailingAction is ContentListItem.SectionAction.SeeMore) onSeeMore else null
                                                )
                                            }
                                        } else null,
                                        content = {
                                            val loadingMod = if (isLoadingSection) Modifier.alpha(0.5f) else Modifier
                                            if (topItem.items.isEmpty() && !isLoadingSection) {
                                                EmptyView(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
                                            } else {
                                                Column(
                                                    modifier = loadingMod,
                                                    verticalArrangement = Arrangement.spacedBy(ContentListItemSpacing)
                                                ) {
                                                    topItem.items.forEach { child ->
                                                        if (child is ContentListItem.PokemonGrid) {
                                                            ResponsivePokemonGridCard(
                                                                pokemon = child.pokemon,
                                                                onPokemonClick = onPokemonGridClick,
                                                                availableWidth = expandedTopPokemonMaxWidth
                                                            )
                                                        } else {
                                                            ContentListItemRow(child, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        is ContentListItem.FormatSelector -> {
                            if (formats.isNotEmpty() && onFormatSelected != null) {
                                val isLoadingFormat = "format_selector" in uiState.loadingSections
                                item(key = topItem.listKey, span = fullSpan) {
                                    CenteredItem {
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
                                item(key = topItem.listKey, span = fullSpan) {
                                    CenteredItem {
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
                            // Top-level battles (pages 2+) — emitted below
                        }
                        else -> item(key = topItem.listKey, span = fullSpan) {
                            CenteredItem {
                                ContentListItemRow(topItem, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                            }
                        }
                    }
                }

                // Render top-level battles (pages 2+) as individual grid items
                val topLevelBattles = uiState.items.filterIsInstance<ContentListItem.Battle>()
                if (topLevelBattles.isNotEmpty()) {
                    items(
                        items = topLevelBattles,
                        key = { it.listKey }
                    ) { battle ->
                        val isSelected = battle.uiModel.id == selectedBattleId
                        BattleCard(
                            uiModel = battle.uiModel,
                            showWinnerHighlight = showWinnerHighlight,
                            onClick = { onItemClick(battle) },
                            modifier = Modifier
                                .animateItem(
                                    placementSpec = BATTLE_GRID_PLACEMENT_SPEC
                                )
                                .widthIn(max = battleCardCellWidth)
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

                if (uiState.isPaginating) {
                    item(key = "paginating", span = fullSpan) {
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
            gridState = gridState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
    }
}

/**
 * Mirrors the item emission order of the LazyVerticalGrid builder to find the index
 * of the grid item for a given battle ID. Returns null if not found.
 *
 * Emission order is windowSizeClass-dependent:
 * - Expanded non-battle sections are emitted as a SINGLE combined grid item
 *   (`SectionContentAlignedHeader` wrapping header + all children).
 * - Battle sections (or any section with `requiresIndividualGridCells = true`)
 *   emit one header item followed by N individual child items.
 * - Compact sections emit one header item followed by N individual child items.
 */
private fun computeBattleItemIndex(
    header: ContentListHeaderUiModel,
    uiState: ContentListUiState,
    battleId: Int,
    hasFormats: Boolean,
    hasSearchQuery: Boolean,
    windowSizeClass: WindowSizeClass
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
                        val needsIndividualCells = topItem.items.any { it.requiresIndividualGridCells }
                        val splitEmission = needsIndividualCells || windowSizeClass != WindowSizeClass.Expanded
                        if (splitEmission) {
                            if (topItem.header.isNotEmpty()) index++ // section header
                            if (topItem.items.isEmpty() && !isLoadingSection) {
                                index++ // empty view
                                continue
                            }
                            for (child in topItem.items) {
                                if (child is ContentListItem.Battle && child.uiModel.id == battleId) return index
                                index++
                            }
                        } else {
                            // Expanded non-battle section: one combined grid item. Battles
                            // cannot appear here (they'd trigger the split path via
                            // `requiresIndividualGridCells`), so no battle ID match.
                            index++
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
            for (battle in topLevelBattles) {
                if (battle.uiModel.id == battleId) return index
                index++
            }
        }
    }
    return null
}

private val CONTENT_MAX_WIDTH = 900.dp

/**
 * Re-measures the wrapped content at `constraints.maxWidth + 2 * [extraPadding]` and
 * places it at `x = -extraPadding`, so content draws out beyond each horizontal edge
 * of the layout. Used by compact edge-to-edge items (e.g. `StatChipRow`) to negate
 * the parent `LazyVerticalGrid`'s `padding(horizontal = 16.dp)` and render flush to
 * the viewport edges. Still reports the original layout width so siblings are
 * unaffected.
 */
private fun Modifier.escapeGridHorizontalPadding(extraPadding: Dp = 16.dp): Modifier =
    this.layout { measurable, constraints ->
        val extraPx = (extraPadding * 2).roundToPx()
        val extendedMax = (constraints.maxWidth + extraPx).coerceAtLeast(0)
        val placeable = measurable.measure(
            constraints.copy(minWidth = extendedMax, maxWidth = extendedMax)
        )
        layout(constraints.maxWidth, placeable.height) {
            placeable.place(-extraPadding.roundToPx(), 0)
        }
    }

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

/**
 * Combined section item for the expanded grid layout: emits the section's header and
 * content as a single full-span grid item, with the header's width matched to the
 * content's actual rendered width via `SubcomposeLayout`.
 *
 * Layout goals:
 * - Content is left-aligned at x=0 of the grid's cell pack (same left edge as battle
 *   cards), regardless of whether it's narrower or wider than the cell pack.
 * - The header is sized to match the content's actual drawn width so a trailing
 *   action (See More / Sort) lands at the content's right edge. For wide content
 *   (e.g. `ResponsivePokemonGridCard`'s layout escape, which can draw past the cell
 *   pack), the trailing action is pushed out into the grid's trailing gutter.
 *
 * Why `contentMeasureMaxWidth`: `LazyVerticalGrid` with `GridCells.FixedSize(650)`
 * passes `constraints.maxWidth = cellPackWidth` to full-span items. Measuring with
 * those constraints would clamp `ResponsivePokemonGridCard`'s reported width to the
 * cell pack, even when it visibly draws ~36dp past it. We measure content with a
 * looser upper bound (the grid-box inner width) so the card's `coerceAtMost` no
 * longer clamps and we recover the true drawn width for header sizing.
 *
 * Why always report `constraints.maxWidth`: reporting a narrower width (when content
 * is narrower than the cell pack) causes `LazyVerticalGrid` to center the full-span
 * item within the cell-pack slot. Pinning the reported width to the grid's original
 * max keeps placement at x=0. Placeables are placed at (0, y) and draw at their
 * natural widths — narrow content sits at the left edge, wide content overflows into
 * the grid's unused trailing gutter to the right of the cell pack.
 *
 * A vertical gap of [headerContentSpacing] separates the header from the content,
 * matching the gap the grid's `verticalArrangement` provides between the Battles
 * header and its cards.
 *
 * Not used for sections containing items with `requiresIndividualGridCells = true`
 * (e.g. battles) — those need individual grid cells and are emitted separately. Not
 * used on compact either — compact falls back to the legacy `CenteredItem` path.
 */
@Composable
private fun SectionContentAlignedHeader(
    contentMeasureMaxWidth: Dp,
    header: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
    headerContentSpacing: Dp = ContentListItemSpacing
) {
    SubcomposeLayout { constraints ->
        val looseMaxPx = contentMeasureMaxWidth.roundToPx()
            .coerceAtLeast(constraints.maxWidth)
        val looseConstraints = constraints.copy(minWidth = 0, maxWidth = looseMaxPx)
        val contentPlaceables = subcompose("content") { content() }
            .map { it.measure(looseConstraints) }
        val contentWidth = contentPlaceables.maxOfOrNull { it.width } ?: 0
        val contentHeight = contentPlaceables.sumOf { it.height }

        // Fall back to the grid's cell-pack width when content is zero-width (e.g. an
        // empty loading-Battles placeholder on the Pokemon/Player page's first render),
        // otherwise the header would be measured at min=max=0 and render invisibly.
        val headerWidthPx = if (contentWidth > 0) contentWidth else constraints.maxWidth
        val headerPlaceables = if (header != null) {
            subcompose("header") { header() }.map {
                it.measure(constraints.copy(minWidth = headerWidthPx, maxWidth = headerWidthPx))
            }
        } else emptyList()
        val headerHeight = headerPlaceables.sumOf { it.height }

        val spacingPx = if (headerPlaceables.isNotEmpty() && contentPlaceables.isNotEmpty()) {
            headerContentSpacing.roundToPx()
        } else 0

        // Always report the grid's full cell-pack width so `LazyVerticalGrid` places
        // this item at x=0 of the content area (reporting narrower would cause the
        // grid to center the full-span item within its slot). Placeables place at
        // their true widths and may draw beyond the reported box into the gutter.
        layout(constraints.maxWidth, headerHeight + spacingPx + contentHeight) {
            var y = 0
            headerPlaceables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height
            }
            y += spacingPx
            contentPlaceables.forEach { placeable ->
                placeable.place(0, y)
                y += placeable.height
            }
        }
    }
}

// Battle card sizing (desktop web).
//
// `DEFAULT` is the natural designed width and the fallback used for compact mobile
// layouts and any viewport too narrow for multi-column. On wider viewports the cell
// width is derived from the window width at discrete column-count breakpoints, so
// cards grow within `[MIN, MAX]` to fill the available space instead of leaving a
// fixed 620dp column with dead gutter to the right.
//
// The derivation keys on **total window width**, not the grid-box width that
// shrinks when the detail pane opens. That way the cell width is stable across
// pane-open/close transitions — only the column count changes, so the individual
// card size doesn't snap mid-animation.
private val BATTLE_CARD_MIN_WIDTH = 560.dp
private val BATTLE_CARD_DEFAULT_WIDTH = 620.dp
// Upper bound on the dynamically-grown cell width. Named "GROWN_MAX" rather
// than "MAX" because this isn't a universal ceiling — narrow viewports still
// produce cards at [BATTLE_CARD_DEFAULT_WIDTH] (or smaller on sub-2-col
// viewports via the fallback path). This constant only bounds how far cards
// grow past default when a wide viewport has surplus space to distribute.
private val BATTLE_CARD_GROWN_MAX_WIDTH = 780.dp
private val BATTLE_GRID_SPACING = 12.dp
// LazyVerticalGrid's horizontal contentPadding (16.dp × 2). Subtracted when
// converting a window width into the space available for battle-card cells.
private val BATTLE_GRID_HORIZONTAL_PADDING = 32.dp

// Minimum detail-pane width at which `BattleDetailContent`'s `PlayerTeamSection`
// fits 3 Pokemon cards per row in each team card. Derivation:
//   - Section applies `fillMaxWidth().padding(horizontal = 16.dp)`, so the
//     BoxWithConstraints inside sees `maxWidth = paneWidth − 32`.
//   - Inside, `availableForCards = maxWidth − innerPadding × 2 = maxWidth − 32`.
//   - 3 cols requires `(availableForCards + 12) / 292 ≥ 3`, i.e.
//     `availableForCards ≥ 864` → `paneWidth ≥ 928`.
// Battle-card growth is capped so opening the pane leaves at least this much
// room beside the grid — otherwise wider battle cards would squeeze the pane
// below the 3-pokemon-per-row threshold.
private val DETAIL_PANEL_PREFERRED_MIN_WIDTH = 928.dp

internal const val DETAIL_PANE_ANIM_DURATION_MS = 300

/**
 * Picks the desired battle-card column count for a given window width. Uses
 * [BATTLE_CARD_DEFAULT_WIDTH] as the per-column minimum so cards never drop
 * below the designed width just to squeeze in another column.
 */
private fun desiredBattleColumns(windowWidth: Dp): Int {
    // N columns fit when `N * stepWidth - spacing + padding ≤ windowWidth`,
    // i.e. `N ≤ (windowWidth - padding + spacing) / stepWidth`. The `+ spacing`
    // is the inverse of "N columns need only N−1 spacings between them", so it
    // cancels the trailing spacing that `stepWidth * N` over-counts.
    val stepWidth = BATTLE_CARD_DEFAULT_WIDTH + BATTLE_GRID_SPACING
    val available = (windowWidth - BATTLE_GRID_HORIZONTAL_PADDING + BATTLE_GRID_SPACING)
        .coerceAtLeast(0.dp)
    val raw = (available.value / stepWidth.value).toInt()
    return raw.coerceAtLeast(1)
}

/**
 * Derives the battle-card cell width from the full window width. Passed to
 * `GridCells.FixedSize` so the grid fits [desiredBattleColumns] columns exactly,
 * with each column sized to fill the available width within [[BATTLE_CARD_MIN_WIDTH],
 * [BATTLE_CARD_GROWN_MAX_WIDTH]].
 *
 * Narrow viewports (below the 2-column breakpoint) return the default width so
 * compact mobile layouts behave exactly like before the dynamic sizing was added.
 *
 * When the card would otherwise grow past [BATTLE_CARD_DEFAULT_WIDTH], it is
 * further capped so the detail pane retains at least [DETAIL_PANEL_PREFERRED_MIN_WIDTH]
 * room when open — keeping 3-pokemon-per-row pokemon in each team card. The
 * cap floor is pinned at the default width so this only shrinks cards relative
 * to their unconstrained growth; cards never drop *below* the default just to
 * buy pane width, which avoids a visible card-width jolt when resizing through
 * the threshold where the cap first becomes relevant.
 */
private fun computeBattleCardCellWidth(windowWidth: Dp): Dp {
    val twoColMinWidth = BATTLE_CARD_DEFAULT_WIDTH * 2 +
        BATTLE_GRID_SPACING +
        BATTLE_GRID_HORIZONTAL_PADDING
    if (windowWidth < twoColMinWidth) return BATTLE_CARD_DEFAULT_WIDTH
    val cols = desiredBattleColumns(windowWidth)
    val interCardSpacing = BATTLE_GRID_SPACING * (cols - 1)
    val available = (windowWidth - BATTLE_GRID_HORIZONTAL_PADDING - interCardSpacing)
        .coerceAtLeast(0.dp)
    val natural = (available / cols).coerceIn(BATTLE_CARD_MIN_WIDTH, BATTLE_CARD_GROWN_MAX_WIDTH)

    if (natural <= BATTLE_CARD_DEFAULT_WIDTH) return natural
    // `− 1.dp` matches the 1dp VerticalDivider the expanded branch places
    // between the grid and the detail pane, so `grid + divider + pane` sums to
    // exactly `windowWidth` when the pane is open.
    val paneReservedCap = (windowWidth - DETAIL_PANEL_PREFERRED_MIN_WIDTH - 1.dp)
        .coerceAtLeast(BATTLE_CARD_DEFAULT_WIDTH)
    return natural.coerceAtMost(paneReservedCap)
}

internal val TOP_POKEMON_TILE_WIDTH = 140.dp
internal val TOP_POKEMON_TILE_SPACING = 8.dp
internal const val TOP_POKEMON_MIN_TILES = 3
// Outer padding of the Top Pokémon card (`cardInnerPadding × 2`), subtracted when
// converting an available width into a tile-fit width. Kept in sync with
// `ResponsivePokemonGridCard.cardInnerPadding`.
internal val TOP_POKEMON_CARD_INNER_PADDING_TOTAL = 24.dp

internal fun computeTopPokemonTileCount(availableWidth: Dp): Int {
    val tile = (TOP_POKEMON_TILE_WIDTH + TOP_POKEMON_TILE_SPACING).value
    val raw = ((availableWidth.value + TOP_POKEMON_TILE_SPACING.value) / tile).toInt()
    return raw.coerceAtLeast(TOP_POKEMON_MIN_TILES)
}

// Placement spec for battle card reflow when the detail pane opens/closes.
// Tweak this to play with animation feel — swap for spring(), tween() with different
// easings/durations, or keyframes. IntOffset generic because grid animateItem() animates
// the item's (x, y) placement in the viewport.
private val BATTLE_GRID_PLACEMENT_SPEC: FiniteAnimationSpec<IntOffset> =
    tween(durationMillis = DETAIL_PANE_ANIM_DURATION_MS, easing = FastOutSlowInEasing)

// Responsive breakpoints for `SectionGroup` column count on desktop web. Content width is
// the grid's inner display width (`expandedTopPokemonMaxWidth`), which shrinks when the
// battle detail pane is open. A just-above-compact window lands in the 1-col bucket; a
// standard laptop lands in 2; a widescreen lands in 3.
private val SECTION_GROUP_2_COL_MIN_WIDTH = 900.dp
private val SECTION_GROUP_3_COL_MIN_WIDTH = 1500.dp

private fun sectionGroupColumnCount(contentWidth: Dp): Int = when {
    contentWidth >= SECTION_GROUP_3_COL_MIN_WIDTH -> 3
    contentWidth >= SECTION_GROUP_2_COL_MIN_WIDTH -> 2
    else -> 1
}

/**
 * Lays out a [ContentListItem.SectionGroup] as a responsive 1/2/3-column block
 * inside the battle grid's full-span slot.
 *
 * Uses [SubcomposeLayout] so each section can be actually composed and measured
 * at the target column width before packing decisions are made. This gives us
 * real rendered heights (including `FlowRow` wrap behavior) instead of the
 * approximations we'd get from counting chips. The sections are then packed
 * greedily into columns — each section (in shared-emission order) goes into the
 * column with the smallest cumulative height, with ties broken toward the
 * leftmost column. Since shared emits in priority order (Teammates, Items, Tera,
 * Moves, Abilities), this naturally places Teammates/Items on row 1 and drops
 * Abilities into whichever column has the most room, with left-first bias on
 * genuine ties.
 *
 * The `reportedWidth` trick mirrors `ResponsivePokemonGridCard`: the grid passes
 * `constraints.maxWidth == cellPackWidth` (e.g. `cols × 650 + gaps`), which is
 * narrower than the true grid-box width. We measure content against the larger
 * `contentMaxWidth` (the grid-box inner width) and report the cell-pack width
 * back up so the grid places this item at `x = 0` of the content area. Placeables
 * beyond the reported box draw into the grid's unused trailing gutter — same
 * hit-testing caveat as `ResponsivePokemonGridCard`.
 */
@Composable
private fun SectionGroupLayout(
    group: ContentListItem.SectionGroup,
    loadingSections: Set<String>,
    contentMaxWidth: Dp,
    selectedBattleId: Int?,
    showWinnerHighlight: Boolean,
    onItemClick: (ContentListItem) -> Unit,
    onHighlightBattleClick: (Int) -> Unit,
    onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit
) {
    val cols = sectionGroupColumnCount(contentMaxWidth)
        .coerceAtMost(group.sections.size)
        .coerceAtLeast(1)
    SubcomposeLayout { constraints ->
        val contentMaxWidthPx = contentMaxWidth.roundToPx()
            .coerceAtLeast(constraints.maxWidth)
        val spacingPx = ContentListItemSpacing.roundToPx()
        val colWidthPx = ((contentMaxWidthPx - (cols - 1) * spacingPx) / cols)
            .coerceAtLeast(0)
        val colConstraints = Constraints(
            minWidth = colWidthPx,
            maxWidth = colWidthPx,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )

        // Subcompose + measure each section at the target column width so we get
        // real rendered heights (post-FlowRow-wrap) for packing decisions.
        val sectionPlaceables = group.sections.mapIndexed { i, section ->
            subcompose("section_$i") {
                SectionGroupItem(
                    section = section,
                    isLoading = section.header in loadingSections,
                    selectedBattleId = selectedBattleId,
                    showWinnerHighlight = showWinnerHighlight,
                    onItemClick = onItemClick,
                    onHighlightBattleClick = onHighlightBattleClick,
                    onPokemonGridClick = onPokemonGridClick
                )
            }.map { it.measure(colConstraints) }
        }

        // Greedy shortest-column packing with left-first tiebreak on height.
        // `indices.minBy` returns the leftmost column when heights are tied, so
        // sections bias toward the left column — including Abilities when both
        // columns would otherwise end up the same height.
        val colHeights = IntArray(cols)
        val colAssignments = List(cols) { mutableListOf<Int>() }
        group.sections.indices.forEach { sectionIdx ->
            val sectionHeight = sectionPlaceables[sectionIdx].sumOf { it.height }
            val target = colHeights.indices.minBy { colHeights[it] }
            val prevCount = colAssignments[target].size
            colAssignments[target].add(sectionIdx)
            colHeights[target] += sectionHeight +
                if (prevCount > 0) spacingPx else 0
        }

        val totalHeight = colHeights.maxOrNull() ?: 0
        val reportedWidth = constraints.maxWidth.coerceAtMost(contentMaxWidthPx)

        layout(reportedWidth, totalHeight) {
            colAssignments.forEachIndexed { colIdx, sectionIndices ->
                val x = colIdx * (colWidthPx + spacingPx)
                var y = 0
                sectionIndices.forEachIndexed { innerIdx, sectionIdx ->
                    if (innerIdx > 0) y += spacingPx
                    sectionPlaceables[sectionIdx].forEach { placeable ->
                        placeable.place(x, y)
                        y += placeable.height
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionGroupItem(
    section: ContentListItem.Section,
    isLoading: Boolean,
    selectedBattleId: Int?,
    showWinnerHighlight: Boolean,
    onItemClick: (ContentListItem) -> Unit,
    onHighlightBattleClick: (Int) -> Unit,
    onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit
) {
    val loadingMod = if (isLoading) Modifier.alpha(0.5f) else Modifier
    Column(
        verticalArrangement = Arrangement.spacedBy(ContentListItemSpacing)
    ) {
        if (section.header.isNotEmpty()) {
            SectionHeader(
                title = section.header,
                isLoading = isLoading
            )
        }
        Column(modifier = loadingMod) {
            section.items.forEach { child ->
                ContentListItemRow(
                    item = child,
                    selectedBattleId = selectedBattleId,
                    showWinnerHighlight = showWinnerHighlight,
                    onItemClick = onItemClick,
                    onHighlightBattleClick = onHighlightBattleClick,
                    onPokemonGridClick = onPokemonGridClick
                )
            }
        }
    }
}


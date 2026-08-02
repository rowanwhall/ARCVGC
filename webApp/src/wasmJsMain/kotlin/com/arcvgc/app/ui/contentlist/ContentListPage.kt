package com.arcvgc.app.ui.contentlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.VectorConverter
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AddLink
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.domain.model.appendBattleParam
import com.arcvgc.app.domain.model.encodePokemonPath
import com.arcvgc.app.domain.model.encodeSearchPath
import com.arcvgc.app.domain.model.encodeTopPokemonPath
import com.arcvgc.app.ui.BattleOverlayRequest
import com.arcvgc.app.ui.LocalBattleOverlay
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.battledetail.BattleDetailPanel
import com.arcvgc.app.ui.components.GradientToolbar
import com.arcvgc.app.ui.components.TopPlayerDialog
import com.arcvgc.app.ui.components.TutorialDialog
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.model.FormatSorter
import com.arcvgc.app.ui.model.excludeHistoric
import com.arcvgc.app.ui.rememberViewModel
import com.arcvgc.app.ui.replaceHistoryStateWithPath
import com.arcvgc.app.ui.submitreplay.SubmitReplayDialogHost

@Composable
fun ContentListPage(
    modifier: Modifier = Modifier,
    mode: ContentListMode = ContentListMode.Home,
    onBack: (() -> Unit)? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null,
    onPokemonClick: ((id: Int, name: String, imageUrl: String?, typeImageUrls: List<String>, formatId: Int?, lookback: LookbackWindow?) -> Unit)? = null,
    onPlayerClick: ((id: Int, name: String, formatId: Int?) -> Unit)? = null,
    onBattleDetailOpenChanged: ((Boolean) -> Unit)? = null,
    // Settled width for the expanded master-detail layout while a battle is
    // open: the width this page will occupy once a host-side CollapsibleSidePane
    // (Usage rankings / Search filters) finishes collapsing. Hosts with such a
    // pane must pass their full content width so the detail-pane math resolves
    // its final geometry up front instead of chasing the pane's animating
    // width. Standalone hosts leave this null and the page's own measured
    // width is used (it's already stable for them).
    settledWidthWhenBattleOpen: Dp? = null,
    initialBattleId: Int? = null,
    initialLookback: LookbackWindow = LookbackWindow.All,
    showToolbarWithoutBack: Boolean = false,
    mirrorUrl: Boolean = true
) {
    val hasToolbar = onBack != null || showToolbarWithoutBack || mode is ContentListMode.Home
    var showSubmitReplayDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }
    val viewModelKey = contentListViewModelKey(mode)
    val viewModel = rememberViewModel(viewModelKey) {
        ContentListViewModel(
            repository = DependencyContainer.battleRepository,
            favoritesRepository = DependencyContainer.favoritesRepository,
            mode = mode,
            pokemonCatalogItems = DependencyContainer.pokemonCatalogRepository.state.value.items,
            appConfigRepository = DependencyContainer.appConfigRepository,
            formatCatalogRepository = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) DependencyContainer.formatCatalogRepository else null,
            pokemonCatalogRepository = DependencyContainer.pokemonCatalogRepository,
            settingsRepository = DependencyContainer.settingsRepository,
            initialLookback = initialLookback
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val favoriteBattleIds by viewModel.favoritesRepository.favoriteBattleIds.collectAsState()
    val favoritePokemonIds by viewModel.favoritesRepository.favoritePokemonIds.collectAsState()
    val favoritePlayerNames by viewModel.favoritesRepository.favoritePlayerNames.collectAsState()
    val showWinnerHighlight by DependencyContainer.settingsRepository.showWinnerHighlight.collectAsState()
    val enableAnimations by DependencyContainer.settingsRepository.enableAnimations.collectAsState()
    val formatCatalogState = viewModel.formatCatalogState?.collectAsState()
    val appConfig by viewModel.appConfigState.collectAsState()
    val preferredFormatId by DependencyContainer.settingsRepository.preferredFormatId.collectAsState()
    val pinnedFormatId = if (preferredFormatId != 0) preferredFormatId else appConfig?.defaultFormat?.id
    val isHomeMode = mode is ContentListMode.Home
    val selectorFormats = remember(formatCatalogState?.value?.items, isHomeMode, pinnedFormatId) {
        val items = formatCatalogState?.value?.items ?: emptyList()
        if (isHomeMode) items.excludeHistoric(keepId = pinnedFormatId) else items
    }
    val sortedFormats = remember(selectorFormats, pinnedFormatId) {
        FormatSorter.sorted(selectorFormats, pinnedFormatId)
    }
    val selectedFormatId by viewModel.selectedFormatId.collectAsState()
    val selectedLookback by viewModel.selectedLookback.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedBattleId by remember(viewModel) { mutableStateOf(initialBattleId ?: viewModel.savedBattleId) }
    var pokemonNavTarget by remember(viewModel) { mutableStateOf<PokemonNavTarget?>(null) }
    var playerNavTarget by remember(viewModel) { mutableStateOf<PlayerNavTarget?>(null) }
    var topPlayerDialogTarget by remember { mutableStateOf<ContentListItem.TopPlayerChipItem?>(null) }
    val gridState = remember(viewModel) {
        LazyGridState(
            firstVisibleItemIndex = viewModel.savedScrollIndex,
            firstVisibleItemScrollOffset = viewModel.savedScrollOffset
        )
    }

    // Single write-path for user-driven battle selection: updates the state and
    // notifies the host synchronously, so a host-side CollapsibleSidePane starts
    // collapsing on the same frame the grid snaps and the detail pane starts
    // sliding — not one frame later via the LaunchedEffect below.
    val setSelectedBattleId: (Int?) -> Unit = { id ->
        selectedBattleId = id
        onBattleDetailOpenChanged?.invoke(id != null)
    }

    // Persist selectedBattleId and scroll position in ViewModel for restoration on back
    // navigation. Also re-notifies the host (idempotently) to cover non-click paths
    // that change selection, e.g. the initial restored/deep-linked battle.
    LaunchedEffect(selectedBattleId) {
        viewModel.savedBattleId = selectedBattleId
        onBattleDetailOpenChanged?.invoke(selectedBattleId != null)
    }

    // The ViewModelStore caches this VM across tab switches so cached state
    // renders instantly. While the page is in composition, this loop wakes
    // exactly once per server ingestion boundary (:05 / :35 of the hour) to
    // silently merge a fresh page 1 into the cached state — pagination tail
    // is preserved, so the user isn't kicked out of their browsing position.
    //
    // Gated to modes whose data actually changes on the :00/:30 cadence:
    // - Favorites: catalog/local data
    // - TopPokemon: usage stats change slowly; this page is also rarely held
    //   open long enough for a boundary to matter
    val watchesStaleness = mode is ContentListMode.Home ||
        mode is ContentListMode.Search ||
        mode is ContentListMode.Pokemon ||
        mode is ContentListMode.Player
    if (watchesStaleness) {
        LaunchedEffect(viewModel) {
            viewModel.watchForStaleness()
        }
    }

    // Mirror page URL in the browser address bar
    val modePath = when (mode) {
        is ContentListMode.Pokemon -> encodePokemonPath(mode.pokemonId, selectedLookback)
        is ContentListMode.Player -> "/player/${mode.playerName}"
        is ContentListMode.Favorites -> when (mode.contentType) {
            FavoriteContentType.Battles -> "/favorites/battles"
            FavoriteContentType.Pokemon -> "/favorites/pokemon"
            FavoriteContentType.Players -> "/favorites/players"
        }
        is ContentListMode.Search -> encodeSearchPath(mode.params)
        is ContentListMode.Home -> "/"
        is ContentListMode.TopPokemon -> encodeTopPokemonPath(
            formatId = mode.formatId,
            lookback = selectedLookback
        )
    }
    if (mirrorUrl) {
        LaunchedEffect(selectedBattleId, modePath) {
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

    val navigateToPokemon: (Int, String, String?, List<String>, Int?, LookbackWindow?) -> Unit = { id, name, imageUrl, typeImageUrls, formatId, lookback ->
        if (onPokemonClick != null) {
            onPokemonClick(id, name, imageUrl, typeImageUrls, formatId, lookback)
        } else {
            pokemonNavTarget = PokemonNavTarget(id, name, imageUrl, typeImageUrls, formatId, lookback)
        }
    }

    val navigateToPlayer: (Int, String, Int?) -> Unit = { id, name, formatId ->
        if (onPlayerClick != null) {
            onPlayerClick(id, name, formatId)
        } else {
            playerNavTarget = PlayerNavTarget(id, name, formatId)
        }
    }

    val currentPokemonNav = pokemonNavTarget
    if (currentPokemonNav != null) {
        ContentListPage(
            mode = ContentListMode.Pokemon(
                currentPokemonNav.id, currentPokemonNav.name, currentPokemonNav.imageUrl,
                currentPokemonNav.typeImageUrls.getOrNull(0),
                currentPokemonNav.typeImageUrls.getOrNull(1),
                currentPokemonNav.formatId,
                currentPokemonNav.lookback
            ),
            onBack = { pokemonNavTarget = null },
            modifier = modifier,
            onBattleDetailOpenChanged = onBattleDetailOpenChanged,
            settledWidthWhenBattleOpen = settledWidthWhenBattleOpen
        )
        return
    }

    val currentPlayerNav = playerNavTarget
    if (currentPlayerNav != null) {
        ContentListPage(
            mode = ContentListMode.Player(currentPlayerNav.id, currentPlayerNav.name, currentPlayerNav.formatId),
            onBack = { playerNavTarget = null },
            modifier = modifier,
            onBattleDetailOpenChanged = onBattleDetailOpenChanged,
            settledWidthWhenBattleOpen = settledWidthWhenBattleOpen
        )
        return
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == WindowSizeClass.Compact
    val battleOverlay = LocalBattleOverlay.current
    val isTopPokemonCompact = isCompact && mode is ContentListMode.TopPokemon
    val filteredUiState = remember(uiState, isTopPokemonCompact) {
        if (!isTopPokemonCompact) uiState
        else uiState.copy(items = uiState.items.filter {
            it !is ContentListItem.FormatSelector &&
            it !is ContentListItem.LookbackSelector &&
            it !is ContentListItem.SearchField
        })
    }

    // When the detail pane opens/closes, scroll to the target battle.
    // On open: scroll to the selected battle so it's visible next to the detail pane.
    // On close: restore to wherever the user was last looking (firstVisibleItemIndex),
    // which naturally tracks both auto-scrolls from battle selection and manual scrolling.
    val hasFormats = (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) && sortedFormats.isNotEmpty()
    val hasSearchQuery = mode is ContentListMode.TopPokemon
    var paneWasOpen by remember { mutableStateOf(false) }
    // Seeded from savedBattleId (like selectedBattleId above) so re-entering with
    // a restored battle composes the pane already open instead of re-animating it in.
    val detailPaneState = remember(viewModel) {
        MutableTransitionState((initialBattleId ?: viewModel.savedBattleId) != null)
    }
    val scrollOffsetPx = with(LocalDensity.current) { BATTLE_GRID_SPACING.roundToPx() }
    LaunchedEffect(selectedBattleId) {
        if (isCompact) return@LaunchedEffect
        val battleId = selectedBattleId
        if (battleId != null) {
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
            val restoreIndex = gridState.firstVisibleItemIndex
            val restoreOffset = gridState.firstVisibleItemScrollOffset
            gridState.animateScrollToItem(restoreIndex, restoreOffset)
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
        selectedLookback = selectedLookback,
        lookbackOptions = if (mode is ContentListMode.Home) LookbackWindow.homeOptions else LookbackWindow.entries,
        showLookbackInfo = mode is ContentListMode.Home,
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
                    derivedFormatId(mode, viewModel.selectedFormatId.value),
                    derivedLookback(mode, viewModel.selectedLookback.value)
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
                is ContentListItem.TopPlayerChipRow,
                is ContentListItem.FormatSelector,
                is ContentListItem.LookbackSelector,
                is ContentListItem.SearchField -> {}
            }
        },
        onHighlightBattleClick = onHighlightBattleClick,
        onPokemonGridClick = { pokemon ->
            navigateToPokemon(
                pokemon.id, pokemon.name, pokemon.imageUrl, emptyList(),
                derivedFormatId(mode, viewModel.selectedFormatId.value),
                derivedLookback(mode, viewModel.selectedLookback.value)
            )
        },
        onTopPlayerChipClick = { player -> topPlayerDialogTarget = player },
        onSearchParamsChanged = onSearchParamsChanged,
        onToggleSortOrder = when (mode) {
            is ContentListMode.Search, is ContentListMode.Pokemon, is ContentListMode.Player -> viewModel::toggleSortOrder
            else -> null
        },
        onFormatSelected = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player || mode is ContentListMode.Home || mode is ContentListMode.TopPokemon) viewModel::selectFormat else null,
        onLookbackSelected = if (mode is ContentListMode.Pokemon || mode is ContentListMode.TopPokemon || mode is ContentListMode.Home) viewModel::selectLookback else null,
        onSearchQueryChanged = if (mode is ContentListMode.TopPokemon) viewModel::setSearchQuery else null,
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isCompact) {
            // Compact: full-width list, battle detail hoisted to MobileLayout via LocalBattleOverlay
            ContentListContent(
                uiState = filteredUiState,
                callbacks = buildCallbacks(
                    onBattleItemClick = { battle ->
                        if (battleOverlay != null) {
                            battleOverlay(BattleOverlayRequest(battleId = battle.uiModel.id))
                        } else {
                            setSelectedBattleId(battle.uiModel.id)
                        }
                    },
                    onHighlightBattleClick = { battleId ->
                        if (battleOverlay != null) {
                            battleOverlay(BattleOverlayRequest(battleId = battleId))
                        } else {
                            setSelectedBattleId(battleId)
                        }
                    }
                ),
                header = mode.toHeaderUiModel(),
                hasToolbar = hasToolbar,
                selectedBattleId = null,
                showWinnerHighlight = showWinnerHighlight,
                formatState = contentListFormatState,
                gridConfig = ContentListGridConfig(animateListItems = enableAnimations),
                gridState = gridState,
                extraBottomPadding = if (isTopPokemonCompact) UsageBottomBarReservedHeight else 0.dp,
                modifier = Modifier.fillMaxSize()
            )

            if (isTopPokemonCompact) {
                val isSelectedFormatHistoric = sortedFormats.find { it.id == selectedFormatId }?.isHistoric == true
                UsageBottomBar(
                    formats = sortedFormats,
                    selectedFormatId = selectedFormatId,
                    onFormatSelected = viewModel::selectFormat,
                    isLoadingFormat = "format_selector" in uiState.loadingSections,
                    selectedLookback = selectedLookback,
                    onLookbackSelected = viewModel::selectLookback,
                    searchQuery = searchQuery,
                    onSearchQueryChanged = viewModel::setSearchQuery,
                    modifier = Modifier.align(Alignment.BottomCenter),
                    showLookback = !isSelectedFormatHistoric
                )
            }

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
                        if (mode is ContentListMode.Home) {
                            IconButton(onClick = { showSubmitReplayDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.AddLink,
                                    contentDescription = "Submit replay",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showTutorialDialog = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = "Help",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
            // True from the frame a battle is selected until the pane's exit animation
            // finishes. The grid holds its pane-open geometry for this whole window: on
            // open it snaps immediately (scrollToItem, above); on close it stays frozen
            // until the pane has fully slid out, then reflows once into the reclaimed
            // space instead of chasing intermediate widths.
            val paneOpenOrAnimating = detailPaneState.targetState || detailPaneState.currentState
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                // The width all master-detail math is computed against. This must be a
                // *settled* width — never one mid-animation. When this page sits beside
                // a host CollapsibleSidePane (Usage/Search), maxWidth itself animates
                // frame-by-frame while that pane collapses, and computing against it
                // reflows the grid twice (e.g. 3 cols -> 1 col -> 2 cols). So while the
                // battle pane is open or exiting, use the host-provided post-collapse
                // width; otherwise maxWidth, which is stable in the settled closed state.
                val layoutBasisWidth =
                    if (paneOpenOrAnimating && settledWidthWhenBattleOpen != null) {
                        settledWidthWhenBattleOpen
                    } else {
                        maxWidth
                    }
                // Battle-card cell width, derived from the settled basis width, not the
                // grid-box width. The *target* only changes at a pane open/close
                // boundary — never mid-animation. The rendered value eases toward it so
                // cards scale smoothly between their pane-open and pane-closed sizes
                // instead of resizing in a single frame — most visible on close, where
                // the basis flips back only after the pane's exit animation finishes,
                // so an instant resize would land in isolation with nothing else moving.
                // Everything else (pane width, grid width, tile sizing) stays keyed on
                // the target so panes and the grid box still snap to settled geometry.
                val targetBattleCardCellWidth = computeBattleCardCellWidth(layoutBasisWidth)
                // Animatable rather than animateDpAsState so the easing applies *only*
                // to pane open/close flips — a target change with no pane-state change
                // is a window resize, and cards must track a drag-resize instantly
                // (a 300ms lag against the instantly-tracking grid box would bounce
                // the derived column count around mid-drag).
                val battleCardCellAnim = remember {
                    Animatable(targetBattleCardCellWidth, Dp.VectorConverter)
                }
                var lastCellAnimPaneState by remember { mutableStateOf(paneOpenOrAnimating) }
                LaunchedEffect(targetBattleCardCellWidth, paneOpenOrAnimating) {
                    val paneStateFlipped = paneOpenOrAnimating != lastCellAnimPaneState
                    lastCellAnimPaneState = paneOpenOrAnimating
                    if (paneStateFlipped && enableAnimations) {
                        battleCardCellAnim.animateTo(
                            targetBattleCardCellWidth,
                            tween(DETAIL_PANE_ANIM_DURATION_MS, easing = FastOutSlowInEasing)
                        )
                    } else {
                        battleCardCellAnim.snapTo(targetBattleCardCellWidth)
                    }
                }
                val battleCardCellWidth = battleCardCellAnim.value
                // Dynamically size the pane so grid + 1dp divider + pane == basis width
                // exactly. The pane is snapped down to a column-fitting threshold (see
                // `snapDetailPaneWidth`) so it never has empty horizontal gutters beside
                // the team-section cards; the leftover width returns to the grid.
                val naturalPaneWidth = (layoutBasisWidth - targetBattleCardCellWidth - 1.dp)
                    .coerceAtLeast(0.dp)
                val panePostWidth = snapDetailPaneWidth(naturalPaneWidth)
                val gridWidthWhenPaneOpen = (layoutBasisWidth - panePostWidth - 1.dp)
                    .coerceAtLeast(targetBattleCardCellWidth)

                // Pokémon grid row (Player "Favorite Pokémon") — sized to match the battle
                // grid's rendered width so both sections align visually.
                val currentGridBoxWidth =
                    if (paneOpenOrAnimating) gridWidthWhenPaneOpen else layoutBasisWidth
                val topPokemonDisplayMaxWidth =
                    (currentGridBoxWidth - BATTLE_GRID_HORIZONTAL_PADDING).coerceAtLeast(0.dp)
                // Cap to the grid content area so the pokemon grid doesn't overflow
                // when the detail pane is open and the battle card width exceeds the
                // content area (battle cards are centered by the grid arrangement,
                // but fullSpan items like the pokemon grid are not).
                val currentGridRendered =
                    computeBattleGridRenderedWidth(currentGridBoxWidth, targetBattleCardCellWidth)
                        .coerceAtMost(topPokemonDisplayMaxWidth)
                val currentInner =
                    (currentGridRendered - TOP_POKEMON_CARD_INNER_PADDING_TOTAL)
                        .coerceAtLeast(0.dp)
                val currentTileCount = computeTopPokemonTileCount(currentInner)
                val currentTileWidth = computeTopPokemonTileWidth(currentInner, currentTileCount)
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = if (paneOpenOrAnimating) {
                            Modifier.width(gridWidthWhenPaneOpen).fillMaxHeight()
                        } else {
                            Modifier.weight(1f).fillMaxHeight()
                        }
                    ) {
                        ContentListContent(
                            uiState = uiState,
                            callbacks = buildCallbacks(
                                onBattleItemClick = { battle -> setSelectedBattleId(battle.uiModel.id) },
                                onHighlightBattleClick = { battleId -> setSelectedBattleId(battleId) }
                            ),
                            header = mode.toHeaderUiModel(),
                            hasToolbar = hasToolbar,
                            selectedBattleId = selectedBattleId,
                            showWinnerHighlight = showWinnerHighlight,
                            formatState = contentListFormatState,
                            gridConfig = ContentListGridConfig(
                                battleCardCellWidth = battleCardCellWidth,
                                expandedTopPokemonMaxWidth = topPokemonDisplayMaxWidth,
                                topPokemonTargetWidth = currentGridRendered,
                                topPokemonTileCount = currentTileCount,
                                topPokemonTileWidth = currentTileWidth,
                                animateListItems = enableAnimations
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
                                    if (mode is ContentListMode.Home) {
                                        IconButton(onClick = { showSubmitReplayDialog = true }) {
                                            Icon(
                                                imageVector = Icons.Default.AddLink,
                                                contentDescription = "Submit replay",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { showTutorialDialog = true }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                                contentDescription = "Help",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
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
                        enter = if (enableAnimations) {
                            slideInHorizontally(
                                animationSpec = tween(DETAIL_PANE_ANIM_DURATION_MS),
                                initialOffsetX = { fullWidth -> fullWidth }
                            )
                        } else EnterTransition.None,
                        exit = if (enableAnimations) {
                            slideOutHorizontally(
                                animationSpec = tween(DETAIL_PANE_ANIM_DURATION_MS),
                                targetOffsetX = { fullWidth -> fullWidth }
                            )
                        } else ExitTransition.None
                    ) {
                        lastBattleId?.let { battleId ->
                            // While a host-side CollapsibleSidePane is still collapsing
                            // (or re-expanding on close), the Row offers this slot less
                            // than divider + panePostWidth — the grid is already at its
                            // final width. Plain `width()` would clamp the panel to that
                            // deficit and its team grid would compose at a narrower
                            // column count, then visibly reflow (2x2 -> 3x3) once the
                            // side pane settles. Unbounded start-aligned measurement lets
                            // the panel lay out at its full settled width immediately,
                            // overflowing offscreen-right into the space being reclaimed.
                            Row(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .wrapContentWidth(align = Alignment.Start, unbounded = true)
                            ) {
                                VerticalDivider(modifier = Modifier.fillMaxHeight())
                                BattleDetailPanel(
                                    battleId = battleId,
                                    isFavorited = battleId in favoriteBattleIds,
                                    onToggleFavorite = { viewModel.favoritesRepository.toggleBattleFavorite(battleId) },
                                    onClose = { setSelectedBattleId(null) },
                                    showWinnerHighlight = showWinnerHighlight,
                                    onPokemonClick = { id, name, imageUrl, typeImageUrls, formatId, lookback ->
                                        navigateToPokemon(id, name, imageUrl, typeImageUrls, formatId, lookback)
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

        if (showSubmitReplayDialog) {
            SubmitReplayDialogHost(onDismiss = { showSubmitReplayDialog = false })
        }

        if (showTutorialDialog) {
            TutorialDialog(
                onDismiss = { showTutorialDialog = false },
                showArrows = !isCompact
            )
        }

        topPlayerDialogTarget?.let { player ->
            val dialogFormatId = derivedFormatId(mode, viewModel.selectedFormatId.value)
            TopPlayerDialog(
                player = player,
                onDismiss = { topPlayerDialogTarget = null },
                onViewPlayer = { id, name ->
                    topPlayerDialogTarget = null
                    navigateToPlayer(id, name, dialogFormatId)
                },
                onBattleClick = { battleId ->
                    topPlayerDialogTarget = null
                    if (isCompact && battleOverlay != null) {
                        battleOverlay(BattleOverlayRequest(battleId = battleId))
                    } else {
                        setSelectedBattleId(battleId)
                    }
                }
            )
        }
    }
}


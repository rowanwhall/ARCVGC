package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.logo
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.components.BattleCard
import com.arcvgc.app.ui.components.EmptyView
import com.arcvgc.app.ui.components.ErrorView
import com.arcvgc.app.ui.components.GradientToolbarHeight
import com.arcvgc.app.ui.components.LoadingIndicator
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.ThemedVerticalScrollbar
import com.arcvgc.app.ui.components.TypeIconRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.ui.hasFinePointer
import com.arcvgc.app.ui.model.ContentListHeaderUiModel
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.unwrapSectionGroups
import com.arcvgc.app.ui.tokens.AppTokens.BrandFontFamily
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.ContentListItemSpacing
import com.arcvgc.app.ui.tokens.AppTokens.HeroLogoHeight
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipVerticalPadding
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

// ---------------------------------------------------------------------------
// ContentListContent composable + helpers
//
// Split from ContentListPage.kt so the page orchestrator stays focused on
// navigation, state hoisting, and master-detail layout while this file owns
// the grid-builder body, section emission, and helper layouts.
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ContentListContent(
    uiState: ContentListUiState,
    callbacks: ContentListCallbacks,
    modifier: Modifier = Modifier,
    header: ContentListHeaderUiModel = ContentListHeaderUiModel.None,
    hasToolbar: Boolean = false,
    selectedBattleId: Int? = null,
    showWinnerHighlight: Boolean = true,
    formatState: ContentListFormatState = ContentListFormatState(),
    gridConfig: ContentListGridConfig = ContentListGridConfig(),
    gridState: LazyGridState = rememberLazyGridState(),
    extraBottomPadding: Dp = 0.dp
) {
    val onRetry = callbacks.onRetry
    val onPaginate = callbacks.onPaginate
    val onItemClick = callbacks.onItemClick
    val onHighlightBattleClick = callbacks.onHighlightBattleClick
    val onPokemonGridClick = callbacks.onPokemonGridClick
    val onSearchParamsChanged = callbacks.onSearchParamsChanged
    val onToggleSortOrder = callbacks.onToggleSortOrder
    val onFormatSelected = callbacks.onFormatSelected
    val onSearchQueryChanged = callbacks.onSearchQueryChanged
    val onSeeMore = callbacks.onSeeMore
    val searchParams = formatState.searchParams
    val sortOrder = formatState.sortOrder
    val formats = formatState.formats
    val selectedFormatId = formatState.selectedFormatId
    val searchQuery = formatState.searchQuery
    val battleCardCellWidth = gridConfig.battleCardCellWidth
    val expandedTopPokemonMaxWidth = gridConfig.expandedTopPokemonMaxWidth
    val topPokemonTargetWidth = gridConfig.topPokemonTargetWidth
    val topPokemonTileCount = gridConfig.topPokemonTileCount
    val topPokemonTileWidth = gridConfig.topPokemonTileWidth

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
            bottom = 16.dp + extraBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(ContentListItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(BATTLE_GRID_SPACING, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
        emitPageHeader(header, windowSizeClass, searchParams, onSearchParamsChanged, fullSpan)

        when {
            uiState.isLoading -> {
                animatedItem(key = "loading", span = fullSpan) {
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
                animatedItem(key = "error", span = fullSpan) {
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
                    if (topItem is ContentListItem.FormatSelector) {
                        emitFormatSelectorItem(topItem, formats, selectedFormatId, onFormatSelected, "format_selector" in uiState.loadingSections, fullSpan)
                    }
                    if (topItem is ContentListItem.SearchField) {
                        emitSearchFieldItem(topItem, searchQuery, onSearchQueryChanged, fullSpan)
                    }
                }
                animatedItem(key = "empty", span = fullSpan) {
                    EmptyView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            }

            else -> {
                val topLevelItems = if (windowSizeClass == WindowSizeClass.Expanded) {
                    uiState.items
                } else {
                    uiState.items.unwrapSectionGroups()
                }
                topLevelItems.forEach { topItem ->
                    when (topItem) {
                        is ContentListItem.SectionGroup -> {
                            animatedItem(key = topItem.listKey, span = fullSpan) {
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
                            val isFlowTileSection = windowSizeClass == WindowSizeClass.Expanded &&
                                !needsIndividualCells &&
                                topItem.items.isNotEmpty() &&
                                topItem.items.all { it is ContentListItem.Pokemon || it is ContentListItem.Player }
                            if (isFlowTileSection) {
                                animatedItem(key = topItem.listKey, span = fullSpan) {
                                    val loadingMod = if (isLoadingSection) Modifier.alpha(0.5f) else Modifier
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        if (topItem.header.isNotEmpty()) {
                                            SectionHeader(
                                                title = topItem.header,
                                                isLoading = isLoadingSection,
                                                sortOrder = null,
                                                onToggleSortOrder = null,
                                                onSeeMore = if (topItem.trailingAction is ContentListItem.SectionAction.SeeMore) onSeeMore else null
                                            )
                                            Spacer(modifier = Modifier.height(ContentListItemSpacing))
                                        }
                                        FlowRow(
                                            modifier = loadingMod.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            topItem.items.forEach { child ->
                                                when (child) {
                                                    is ContentListItem.Pokemon -> PokemonFlowTile(
                                                        name = child.name,
                                                        imageUrl = child.imageUrl,
                                                        onClick = { onItemClick(child) },
                                                        usagePercent = child.usagePercent
                                                    )
                                                    is ContentListItem.Player -> PlayerFlowTile(
                                                        name = child.name,
                                                        onClick = { onItemClick(child) }
                                                    )
                                                    else -> {}
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (needsIndividualCells || windowSizeClass != WindowSizeClass.Expanded) {
                                if (topItem.header.isNotEmpty()) {
                                    animatedItem(key = topItem.listKey, span = fullSpan) {
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
                                    animatedItem(key = "${topItem.listKey}_empty", span = fullSpan) {
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
                                                    placementSpec = GRID_ITEM_PLACEMENT_SPEC
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
                                    topItem.items.forEach { child ->
                                        animatedItem(key = child.listKey, span = fullSpan) {
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
                                animatedItem(key = topItem.listKey, span = fullSpan) {
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
                                                                availableWidth = if (topPokemonTargetWidth > 0.dp) topPokemonTargetWidth else expandedTopPokemonMaxWidth,
                                                                tileCount = topPokemonTileCount,
                                                                tileWidth = topPokemonTileWidth
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
                            emitFormatSelectorItem(topItem, formats, selectedFormatId, onFormatSelected, "format_selector" in uiState.loadingSections, fullSpan)
                        }
                        is ContentListItem.SearchField -> {
                            emitSearchFieldItem(topItem, searchQuery, onSearchQueryChanged, fullSpan)
                        }
                        is ContentListItem.Battle -> {
                            // Top-level battles (pages 2+) — emitted below
                        }
                        else -> animatedItem(key = topItem.listKey, span = fullSpan) {
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
                                    placementSpec = GRID_ITEM_PLACEMENT_SPEC
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
                    animatedItem(key = "paginating", span = fullSpan) {
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

// ---------------------------------------------------------------------------
// LazyGridScope subscope extensions
// ---------------------------------------------------------------------------

private fun LazyGridScope.emitPageHeader(
    header: ContentListHeaderUiModel,
    windowSizeClass: WindowSizeClass,
    searchParams: SearchParams?,
    onSearchParamsChanged: ((SearchParams) -> Unit)?,
    fullSpan: LazyGridItemSpanScope.() -> GridItemSpan
) {
    when (val h = header) {
        is ContentListHeaderUiModel.None -> {}
        is ContentListHeaderUiModel.HomeHero -> {
            animatedItem(key = "home_hero", span = fullSpan) {
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
        is ContentListHeaderUiModel.FavoritesHero -> {
            // TODO: Replace with branded favorites asset when ready
        }
        is ContentListHeaderUiModel.SearchFilters -> {
            animatedItem(key = "search_filters", span = fullSpan) {
                if (windowSizeClass == WindowSizeClass.Expanded) {
                    SearchFilterChips(
                        filters = h,
                        searchParams = searchParams,
                        onSearchParamsChanged = onSearchParamsChanged
                    )
                } else {
                    CenteredItem {
                        SearchFilterChips(
                            filters = h,
                            searchParams = searchParams,
                            onSearchParamsChanged = onSearchParamsChanged
                        )
                    }
                }
            }
        }
        is ContentListHeaderUiModel.PokemonHero -> {
            animatedItem(key = "pokemon_hero", span = fullSpan) {
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
            animatedItem(key = "player_hero", span = fullSpan) {
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
}

private fun LazyGridScope.emitFormatSelectorItem(
    item: ContentListItem.FormatSelector,
    formats: List<FormatUiModel>,
    selectedFormatId: Int,
    onFormatSelected: ((Int) -> Unit)?,
    isLoading: Boolean,
    fullSpan: LazyGridItemSpanScope.() -> GridItemSpan
) {
    if (formats.isEmpty() || onFormatSelected == null) return
    animatedItem(key = item.listKey, span = fullSpan) {
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
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(start = 8.dp).size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

private fun LazyGridScope.emitSearchFieldItem(
    item: ContentListItem.SearchField,
    searchQuery: String,
    onSearchQueryChanged: ((String) -> Unit)?,
    fullSpan: LazyGridItemSpanScope.() -> GridItemSpan
) {
    if (onSearchQueryChanged == null) return
    animatedItem(key = item.listKey, span = fullSpan) {
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

// ---------------------------------------------------------------------------
// Grid item index computation (scroll-to-battle support)
// ---------------------------------------------------------------------------

internal fun computeBattleItemIndex(
    header: ContentListHeaderUiModel,
    uiState: ContentListUiState,
    battleId: Int,
    hasFormats: Boolean,
    hasSearchQuery: Boolean,
    windowSizeClass: WindowSizeClass
): Int? {
    var index = 0

    when (header) {
        is ContentListHeaderUiModel.None -> {}
        is ContentListHeaderUiModel.FavoritesHero -> {}
        else -> index++
    }

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
                            if (topItem.header.isNotEmpty()) index++
                            if (topItem.items.isEmpty() && !isLoadingSection) {
                                index++
                                continue
                            }
                            for (child in topItem.items) {
                                if (child is ContentListItem.Battle && child.uiModel.id == battleId) return index
                                index++
                            }
                        } else {
                            index++
                        }
                    }
                    is ContentListItem.FormatSelector -> { if (hasFormats) index++ }
                    is ContentListItem.SearchField -> { if (hasSearchQuery) index++ }
                    is ContentListItem.Battle -> {}
                    else -> index++
                }
            }
            val topLevelBattles = uiState.items.filterIsInstance<ContentListItem.Battle>()
            for (battle in topLevelBattles) {
                if (battle.uiModel.id == battleId) return index
                index++
            }
        }
    }
    return null
}

// ---------------------------------------------------------------------------
// Grid helpers
// ---------------------------------------------------------------------------

private fun LazyGridScope.animatedItem(
    key: Any?,
    span: (LazyGridItemSpanScope.() -> GridItemSpan)? = null,
    content: @Composable () -> Unit
) {
    item(key = key, span = span) {
        Box(modifier = Modifier.animateItem(placementSpec = GRID_ITEM_PLACEMENT_SPEC)) {
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// Layout helpers
// ---------------------------------------------------------------------------

private val CONTENT_MAX_WIDTH = 900.dp

@Composable
internal fun CenteredItem(
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

        val contentX = (constraints.maxWidth - contentWidth) / 2
        val headerX = (constraints.maxWidth - headerWidthPx) / 2
        layout(constraints.maxWidth, headerHeight + spacingPx + contentHeight) {
            var y = 0
            headerPlaceables.forEach { placeable ->
                placeable.place(headerX, y)
                y += placeable.height
            }
            y += spacingPx
            contentPlaceables.forEach { placeable ->
                placeable.place(contentX, y)
                y += placeable.height
            }
        }
    }
}

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
    val cols = sectionGroupColumnCount(contentMaxWidth, group.sections.size)
    SubcomposeLayout { constraints ->
        val contentMaxWidthPx = contentMaxWidth.roundToPx()
            .coerceAtLeast(constraints.maxWidth)
        val spacingPx = ContentListItemSpacing.roundToPx()
        val availablePerColPx =
            (contentMaxWidthPx + spacingPx) / cols.coerceAtLeast(1) - spacingPx
        val slotMaxPx = SECTION_GROUP_ITEM_WIDTH.roundToPx()
            .coerceAtMost(availablePerColPx.coerceAtLeast(1))
        val colConstraints = Constraints(
            minWidth = 0,
            maxWidth = slotMaxPx,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )

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

        val colWidthPx = sectionPlaceables
            .maxOfOrNull { placeables -> placeables.maxOfOrNull { it.width } ?: 0 }
            ?.coerceAtLeast(1)
            ?: SECTION_GROUP_ITEM_WIDTH.roundToPx()

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
        val totalContentWidth = cols * colWidthPx + (cols - 1) * spacingPx
        val baseX = (reportedWidth - totalContentWidth) / 2

        layout(reportedWidth, totalHeight) {
            colAssignments.forEachIndexed { colIdx, sectionIndices ->
                val x = baseX + colIdx * (colWidthPx + spacingPx)
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
        modifier = Modifier.width(SECTION_GROUP_ITEM_WIDTH),
        verticalArrangement = Arrangement.spacedBy(ContentListItemSpacing)
    ) {
        if (section.header.isNotEmpty()) {
            Text(
                text = section.header,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
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

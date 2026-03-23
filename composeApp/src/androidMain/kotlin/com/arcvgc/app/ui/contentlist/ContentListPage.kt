package com.arcvgc.app.ui.contentlist

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcvgc.app.ui.battledetail.BattleDetailPage
import com.arcvgc.app.ui.battledetail.BattleDetailViewModel
import com.arcvgc.app.ui.battledetail.ReplayOverlay
import com.arcvgc.app.ui.components.EmptyView
import com.arcvgc.app.ui.components.ErrorView
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.TypeIconRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.model.ContentListHeaderUiModel
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FormatSorter
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.shareBattleUrl
import com.arcvgc.app.ui.shareUrlForMode
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentListPage(
    modifier: Modifier = Modifier,
    mode: ContentListMode = ContentListMode.Home,
    onBack: (() -> Unit)? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null,
    consumeTopInsets: Boolean = true,
    viewModel: ContentListViewModel = hiltViewModel(
        key = when (mode) {
            is ContentListMode.Home -> "content_list_home"
            is ContentListMode.Favorites -> "content_list_favorites_${mode.contentType.name}"
            is ContentListMode.Search -> "content_list_search_${mode.params}"
            is ContentListMode.Pokemon -> "content_list_pokemon_${mode.pokemonId}"
            is ContentListMode.Player -> "content_list_player_${mode.playerId}_${mode.formatId}"
            is ContentListMode.TopPokemon -> "content_list_top_pokemon_${mode.formatId}"
        }
    )
) {
    LaunchedEffect(viewModel) {
        viewModel.initialize(mode)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val favoriteBattleIds by viewModel.favoritesRepository.favoriteBattleIds.collectAsStateWithLifecycle()
    val favoritePokemonIds by viewModel.favoritesRepository.favoritePokemonIds.collectAsStateWithLifecycle()
    val favoritePlayerNames by viewModel.favoritesRepository.favoritePlayerNames.collectAsStateWithLifecycle()
    val showWinnerHighlight by viewModel.settingsRepository.showWinnerHighlight.collectAsStateWithLifecycle()
    val formatCatalogState by viewModel.formatCatalogRepository.state.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfigRepository.config.collectAsStateWithLifecycle()
    val sortedFormats = remember(formatCatalogState.items, appConfig) {
        FormatSorter.sorted(formatCatalogState.items, appConfig?.defaultFormat?.id)
    }
    val selectedFormatId by viewModel.selectedFormatId.collectAsStateWithLifecycle()
    var selectedBattleId by remember { mutableStateOf<Int?>(null) }
    var replayUrl by remember { mutableStateOf<String?>(null) }
    var pokemonNavTarget by remember { mutableStateOf<PokemonNavTarget?>(null) }
    var topPokemonFormatId by remember { mutableStateOf<Int?>(null) }
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var playerNavTarget by remember { mutableStateOf<PlayerNavTarget?>(null) }

    if (onBack != null && selectedBattleId == null && pokemonNavTarget == null && playerNavTarget == null && topPokemonFormatId == null) {
        BackHandler { onBack() }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        ContentListContent(
            uiState = uiState,
            header = mode.toHeaderUiModel(),
            hasToolbar = onBack != null,
            consumeTopInsets = consumeTopInsets,
            showWinnerHighlight = showWinnerHighlight,
            onRefresh = {
                if (mode is ContentListMode.Favorites) viewModel.loadContent() else viewModel.refresh()
            },
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
                        pokemonNavTarget = PokemonNavTarget(
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
                        playerNavTarget = PlayerNavTarget(item.id, item.name, derivedFormatId)
                    }
                    is ContentListItem.Section -> {}
                    is ContentListItem.HighlightButtons -> {}
                    is ContentListItem.PokemonGrid -> {}
                    is ContentListItem.StatChipRow -> {}
                    is ContentListItem.FormatSelector -> {}
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
                pokemonNavTarget = PokemonNavTarget(pokemon.id, pokemon.name, pokemon.imageUrl, formatId = derivedFormatId)
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
            onSeeMore = { topPokemonFormatId = viewModel.selectedFormatId.value }
        )

        if (onBack != null) {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                                )
                            )
                        )
                )
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (mode !is ContentListMode.TopPokemon) {
                            val actionContext = LocalContext.current
                            IconButton(onClick = {
                                val url = shareUrlForMode(mode, null)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, url)
                                    type = "text/plain"
                                }
                                actionContext.startActivity(Intent.createChooser(sendIntent, null))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
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
                    windowInsets = if (consumeTopInsets) TopAppBarDefaults.windowInsets else WindowInsets(0),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }

        val lastSelectedBattleId = rememberLastNonNull(selectedBattleId)
        AnimatedVisibility(
            visible = selectedBattleId != null,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            lastSelectedBattleId?.let { battleId ->
                BackHandler { selectedBattleId = null }
                val battleDetailViewModel: BattleDetailViewModel = hiltViewModel(
                    key = "battle_detail_$battleId"
                )
                val battleDetailState by battleDetailViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(battleId) {
                    battleDetailViewModel.loadBattleDetail(battleId)
                }

                val selectedBattle = uiState.items.findBattle(battleId)
                val patchedState = battleDetailState.battleDetail?.let { detail ->
                    battleDetailState.copy(
                        battleDetail = detail.copy(
                            player1 = detail.player1.copy(isWinner = selectedBattle?.uiModel?.player1?.isWinner),
                            player2 = detail.player2.copy(isWinner = selectedBattle?.uiModel?.player2?.isWinner)
                        )
                    )
                } ?: battleDetailState

                val context = LocalContext.current
                val shareUrl = shareBattleUrl(battleId)

                BattleDetailPage(
                    state = patchedState,
                    onBack = { selectedBattleId = null },
                    onRetry = { battleDetailViewModel.loadBattleDetail(battleId) },
                    isFavorited = battleId in favoriteBattleIds,
                    showWinnerHighlight = showWinnerHighlight,
                    onToggleFavorite = { viewModel.favoritesRepository.toggleBattleFavorite(battleId) },
                    onShare = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, shareUrl)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    },
                    onViewReplay = { url -> replayUrl = url },
                    onPokemonClick = { id, name, imageUrl, typeImageUrls ->
                        val formatId = patchedState.battleDetail?.formatId
                        pokemonNavTarget = PokemonNavTarget(id, name, imageUrl, typeImageUrls, formatId)
                    },
                    onPlayerClick = { id, name ->
                        val formatId = patchedState.battleDetail?.formatId
                        playerNavTarget = PlayerNavTarget(id, name, formatId)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val lastReplayUrl = rememberLastNonNull(replayUrl)
        AnimatedVisibility(
            visible = replayUrl != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            lastReplayUrl?.let { url ->
                ReplayOverlay(
                    replayUrl = url,
                    onDismiss = { replayUrl = null }
                )
            }
        }

        val lastTopPokemonFormatId = rememberLastNonNull(topPokemonFormatId)
        AnimatedVisibility(
            visible = topPokemonFormatId != null,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            lastTopPokemonFormatId?.let { formatId ->
                BackHandler { topPokemonFormatId = null }
                ContentListPage(
                    mode = ContentListMode.TopPokemon(formatId = formatId),
                    onBack = { topPokemonFormatId = null },
                    consumeTopInsets = consumeTopInsets,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val lastPokemonNavTarget = rememberLastNonNull(pokemonNavTarget)
        AnimatedVisibility(
            visible = pokemonNavTarget != null,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            lastPokemonNavTarget?.let { target ->
                BackHandler { pokemonNavTarget = null }
                ContentListPage(
                    mode = ContentListMode.Pokemon(
                        target.id, target.name, target.imageUrl,
                        target.typeImageUrls.getOrNull(0),
                        target.typeImageUrls.getOrNull(1),
                        target.formatId
                    ),
                    onBack = { pokemonNavTarget = null },
                    consumeTopInsets = consumeTopInsets,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        val lastPlayerNavTarget = rememberLastNonNull(playerNavTarget)
        AnimatedVisibility(
            visible = playerNavTarget != null,
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            lastPlayerNavTarget?.let { target ->
                BackHandler { playerNavTarget = null }
                ContentListPage(
                    mode = ContentListMode.Player(target.id, target.name, target.formatId),
                    onBack = { playerNavTarget = null },
                    consumeTopInsets = consumeTopInsets,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun <T> rememberLastNonNull(value: T?): T? {
    var last by remember { mutableStateOf(value) }
    if (value != null) last = value
    return last
}

private val ToolbarHeight = 72.dp

@Composable
private fun ContentListContent(
    uiState: ContentListUiState,
    modifier: Modifier = Modifier,
    header: ContentListHeaderUiModel = ContentListHeaderUiModel.None,
    hasToolbar: Boolean = false,
    consumeTopInsets: Boolean = true,
    showWinnerHighlight: Boolean = true,
    onRefresh: () -> Unit,
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
    onSeeMore: (() -> Unit)? = null
) {
    val listState = rememberLazyListState()

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

    val statusBarHeight = if (consumeTopInsets) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() else 0.dp
    val toolbarSpacing = if (hasToolbar) ToolbarHeight + statusBarHeight else 0.dp
    val topPadding = toolbarSpacing + when (header) {
        is ContentListHeaderUiModel.PokemonHero -> 4.dp
        is ContentListHeaderUiModel.PlayerHero -> 0.dp
        is ContentListHeaderUiModel.SearchFilters -> 8.dp
        is ContentListHeaderUiModel.FavoritesHero -> 16.dp
        else -> 16.dp
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (val h = header) {
                is ContentListHeaderUiModel.None -> {}
                is ContentListHeaderUiModel.HomeHero -> {
                    item(key = "home_hero") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 24.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ARC",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                is ContentListHeaderUiModel.TopPokemonHero -> {
                    item(key = "top_pokemon_hero") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 24.dp, bottom = 8.dp),
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
                is ContentListHeaderUiModel.FavoritesHero -> {
                    item(key = "favorites_hero") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(top = 24.dp, bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorites",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Favorites",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                is ContentListHeaderUiModel.SearchFilters -> {
                    item(key = "search_filters") {
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
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
                is ContentListHeaderUiModel.PlayerHero -> {
                    item(key = "player_hero") {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = h.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
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
                            CircularProgressIndicator()
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
                        if (topItem is ContentListItem.SearchField && onSearchQueryChanged != null) {
                            item(key = topItem.listKey) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChanged,
                                    label = { Text("Search Pok\u00E9mon") },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                            }
                        }
                        if (topItem is ContentListItem.FormatSelector && formats.isNotEmpty() && onFormatSelected != null) {
                            val isLoadingFormat = "format_selector" in uiState.loadingSections
                            item(key = topItem.listKey) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                                        SectionHeader(
                                            title = topItem.header,
                                            isLoading = isLoadingSection,
                                            sortOrder = if (topItem.header == "Battles") sortOrder else null,
                                            onToggleSortOrder = if (topItem.header == "Battles") onToggleSortOrder else null,
                                            onSeeMore = if (topItem.trailingAction is ContentListItem.SectionAction.SeeMore) onSeeMore else null,
                                            modifier = itemPadding
                                        )
                                    }
                                }
                                if (topItem.items.isEmpty() && !isLoadingSection) {
                                    item(key = "${topItem.listKey}_empty") {
                                        EmptyView(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp).then(itemPadding))
                                    }
                                }
                                items(items = topItem.items, key = { it.listKey }) { child ->
                                    val childModifier = if (isLoadingSection) Modifier.alpha(0.5f) else Modifier
                                    Box(modifier = childModifier.then(if (!child.edgeToEdge) itemPadding else Modifier)) {
                                        ContentListItemRow(child, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                    }
                                }
                            }
                            is ContentListItem.SearchField -> {
                                if (onSearchQueryChanged != null) {
                                    item(key = topItem.listKey) {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = onSearchQueryChanged,
                                            label = { Text("Search Pok\u00E9mon") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(itemPadding)
                                        )
                                    }
                                }
                            }
                            is ContentListItem.FormatSelector -> {
                                if (formats.isNotEmpty() && onFormatSelected != null) {
                                    val isLoadingFormat = "format_selector" in uiState.loadingSections
                                    item(key = topItem.listKey) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().then(itemPadding),
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
                            else -> item(key = topItem.listKey) {
                                Box(modifier = if (!topItem.edgeToEdge) itemPadding else Modifier) {
                                    ContentListItemRow(topItem, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                }
                            }
                        }
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
    }
}


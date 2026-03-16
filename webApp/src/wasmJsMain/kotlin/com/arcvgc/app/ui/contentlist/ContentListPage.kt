package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.ui.battledetail.BattleDetailPanel
import com.arcvgc.app.ui.components.BattleCard
import com.arcvgc.app.ui.components.EmptyView
import com.arcvgc.app.ui.components.FillPokemonAvatar
import com.arcvgc.app.ui.components.ErrorView
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.ThemedVerticalScrollbar
import com.arcvgc.app.ui.components.TypeIconRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.ui.BattleOverlayRequest
import com.arcvgc.app.ui.LocalBattleOverlay
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.rememberViewModel
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.model.ContentListHeaderUiModel
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FormatUiModel

private data class PokemonNavTarget(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val typeImageUrls: List<String> = emptyList(),
    val formatId: Int? = null
)

private data class PlayerNavTarget(val id: Int, val name: String, val formatId: Int? = null)

private const val PAGINATION_THRESHOLD = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentListPage(
    modifier: Modifier = Modifier,
    mode: ContentListMode = ContentListMode.Home,
    onBack: (() -> Unit)? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null,
    onPokemonClick: ((id: Int, name: String, imageUrl: String?, typeImageUrls: List<String>, formatId: Int?) -> Unit)? = null,
    onPlayerClick: ((id: Int, name: String, formatId: Int?) -> Unit)? = null
) {
    val viewModelKey = when (mode) {
        is ContentListMode.Home -> "content_list_home"
        is ContentListMode.Favorites -> "content_list_favorites_${mode.contentType.name}"
        is ContentListMode.Search -> "content_list_search_${mode.params}"
        is ContentListMode.Pokemon -> "content_list_pokemon_${mode.pokemonId}"
        is ContentListMode.Player -> "content_list_player_${mode.playerId}_${mode.formatId}"
    }
    val viewModel = rememberViewModel(viewModelKey) {
        ContentListViewModel(
            repository = DependencyContainer.battleRepository,
            favoritesRepository = DependencyContainer.favoritesRepository,
            mode = mode,
            pokemonCatalogItems = DependencyContainer.pokemonCatalogRepository.state.value.items,
            appConfigRepository = DependencyContainer.appConfigRepository,
            formatCatalogRepository = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player) DependencyContainer.formatCatalogRepository else null
        )
    }

    val uiState by viewModel.uiState.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val favoriteBattleIds by viewModel.favoritesRepository.favoriteBattleIds.collectAsState()
    val favoritePokemonIds by viewModel.favoritesRepository.favoritePokemonIds.collectAsState()
    val favoritePlayerNames by viewModel.favoritesRepository.favoritePlayerNames.collectAsState()
    val showWinnerHighlight by DependencyContainer.settingsRepository.showWinnerHighlight.collectAsState()
    val formatCatalogState = viewModel.formatCatalogState?.collectAsState()
    val selectedFormatId by viewModel.selectedFormatId.collectAsState()
    var selectedBattleId by remember(viewModel) { mutableStateOf(viewModel.savedBattleId) }
    var pokemonNavTarget by remember(viewModel) { mutableStateOf<PokemonNavTarget?>(null) }
    var playerNavTarget by remember(viewModel) { mutableStateOf<PlayerNavTarget?>(null) }
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        if (isCompact) {
            // Compact: full-width list, battle detail hoisted to MobileLayout via LocalBattleOverlay
            Column(modifier = Modifier.fillMaxSize()) {
                if (onBack != null) {
                    ContentListToolbar(
                        mode = mode,
                        favoritePokemonIds = favoritePokemonIds,
                        favoritePlayerNames = favoritePlayerNames,
                        viewModel = viewModel,
                        onBack = onBack
                    )
                }

                ContentListContent(
                    uiState = uiState,
                    header = mode.toHeaderUiModel(),
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
                                    battleOverlay(BattleOverlayRequest(
                                        battleId = battle.id,
                                        player1IsWinner = battle.player1.isWinner,
                                        player2IsWinner = battle.player2.isWinner
                                    ))
                                } else {
                                    selectedBattleId = item.uiModel.id
                                }
                            }
                            is ContentListItem.Pokemon -> {
                                val derivedFormatId = when (mode) {
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
                        }
                    },
                    onHighlightBattleClick = { battleId ->
                        if (battleOverlay != null) {
                            battleOverlay(BattleOverlayRequest(battleId = battleId, player1IsWinner = null, player2IsWinner = null))
                        } else {
                            selectedBattleId = battleId
                        }
                    },
                    onPokemonGridClick = { pokemon ->
                        val derivedFormatId = when (mode) {
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
                    formats = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player) formatCatalogState?.value?.items ?: emptyList() else emptyList(),
                    selectedFormatId = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player) selectedFormatId else 0,
                    onFormatSelected = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player) viewModel::selectFormat else null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Expanded: master-detail Row layout
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = if (selectedBattleId != null) {
                        Modifier.weight(0.4f).fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    }
                ) {
                    if (onBack != null) {
                        ContentListToolbar(
                            mode = mode,
                            favoritePokemonIds = favoritePokemonIds,
                            favoritePlayerNames = favoritePlayerNames,
                            viewModel = viewModel,
                            onBack = onBack
                        )
                    }

                    ContentListContent(
                        uiState = uiState,
                        header = mode.toHeaderUiModel(),
                        selectedBattleId = selectedBattleId,
                        showWinnerHighlight = showWinnerHighlight,
                        listState = listState,
                        onRetry = viewModel::loadContent,
                        onPaginate = viewModel::paginate,
                        onItemClick = { item ->
                            when (item) {
                                is ContentListItem.Battle -> selectedBattleId = item.uiModel.id
                                is ContentListItem.Pokemon -> {
                                    val derivedFormatId = when (mode) {
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
                            }
                        },
                        onHighlightBattleClick = { battleId -> selectedBattleId = battleId },
                        onPokemonGridClick = { pokemon ->
                            val derivedFormatId = when (mode) {
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
                        formats = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player) formatCatalogState?.value?.items ?: emptyList() else emptyList(),
                        selectedFormatId = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player) selectedFormatId else 0,
                        onFormatSelected = if (mode is ContentListMode.Pokemon || mode is ContentListMode.Player) viewModel::selectFormat else null,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                selectedBattleId?.let { battleId ->
                    val selectedBattle = uiState.items.findBattle(battleId)
                    VerticalDivider(modifier = Modifier.fillMaxHeight())
                    BattleDetailPanel(
                        battleId = battleId,
                        isFavorited = battleId in favoriteBattleIds,
                        onToggleFavorite = { viewModel.favoritesRepository.toggleBattleFavorite(battleId) },
                        onClose = { selectedBattleId = null },
                        player1IsWinner = selectedBattle?.uiModel?.player1?.isWinner,
                        player2IsWinner = selectedBattle?.uiModel?.player2?.isWinner,
                        showWinnerHighlight = showWinnerHighlight,
                        onPokemonClick = { id, name, imageUrl, typeImageUrls, formatId ->
                            navigateToPokemon(id, name, imageUrl, typeImageUrls, formatId)
                        },
                        onPlayerClick = { id, name, formatId ->
                            navigateToPlayer(id, name, formatId)
                        },
                        modifier = Modifier.weight(0.6f).fillMaxHeight()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentListToolbar(
    mode: ContentListMode,
    favoritePokemonIds: Set<Int>,
    favoritePlayerNames: Set<String>,
    viewModel: ContentListViewModel,
    onBack: () -> Unit
) {
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
            IconButton(onClick = { viewModel.loadContent() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

private val FilterChipHeight = 44.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFilterChips(
    filters: ContentListHeaderUiModel.SearchFilters,
    searchParams: SearchParams? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null
) {
    val context = LocalPlatformContext.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        filters.formatName?.let { name ->
            Box(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
        filters.pokemonChips.forEach { chip ->
            val label = buildString {
                append(chip.name)
                chip.itemName?.let { append(" @ $it") }
            }
            val canRemove = searchParams?.canRemovePokemonAt(chip.index) == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(start = 4.dp, end = if (canRemove) 0.dp else 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                chip.teraTypeImageUrl?.let { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Tera type",
                        modifier = Modifier.size(27.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                chip.imageUrl?.let { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .crossfade(true)
                            .build(),
                        contentDescription = chip.name,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removePokemonAt(chip.index))
                    }
                }
            }
        }
        filters.minimumRating?.let { rating ->
            val canRemove = searchParams?.canRemoveMinRating() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rating}+",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeMinRating())
                    }
                }
            }
        }
        filters.maximumRating?.let { rating ->
            val canRemove = searchParams?.canRemoveMaxRating() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rating}-",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeMaxRating())
                    }
                }
            }
        }
        if (filters.unratedOnly) {
            val canRemove = searchParams?.canRemoveUnrated() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unrated",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeUnrated())
                    }
                }
            }
        }
        filters.playerName?.let { name ->
            val canRemove = searchParams?.canRemovePlayerName() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removePlayerName())
                    }
                }
            }
        }
        if (filters.timeRangeStart != null || filters.timeRangeEnd != null) {
            val startStr = filters.timeRangeStart?.let { formatEpochDate(it) } ?: "..."
            val endStr = filters.timeRangeEnd?.let { formatEpochDate(it) } ?: "..."
            val canRemove = searchParams?.canRemoveTimeRange() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$startStr – $endStr",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeTimeRange())
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipCloseButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove filter",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatEpochDate(epochSeconds: Long): String {
    val totalDays = (epochSeconds / 86400).toInt()
    val remaining = totalDays + 719468
    val era = (if (remaining >= 0) remaining else remaining - 146096) / 146097
    val doe = remaining - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = mp + (if (mp < 10) 3 else -9)
    val year = y + (if (m <= 2) 1 else 0)
    val yy = year % 100
    return "${m.toString().padStart(2, '0')}/${d.toString().padStart(2, '0')}/${yy.toString().padStart(2, '0')}"
}

@Composable
private fun ContentListContent(
    uiState: ContentListUiState,
    modifier: Modifier = Modifier,
    header: ContentListHeaderUiModel = ContentListHeaderUiModel.None,
    selectedBattleId: Int? = null,
    showWinnerHighlight: Boolean = true,
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

    val topPadding = when (header) {
        is ContentListHeaderUiModel.PokemonHero -> 4.dp
        is ContentListHeaderUiModel.PlayerHero -> 4.dp
        is ContentListHeaderUiModel.SearchFilters -> 8.dp
        else -> 16.dp
    }

    Box(modifier = modifier) {
    Box(
        modifier = Modifier
            .widthIn(max = 900.dp)
            .fillMaxHeight()
            .align(Alignment.TopCenter)
    ) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
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
                            .padding(top = 24.dp, bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ARC",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Today's Top Battles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            is ContentListHeaderUiModel.FavoritesHero -> {
                item(key = "favorites_hero") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
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
                    SearchFilterChips(
                        filters = h,
                        searchParams = searchParams,
                        onSearchParamsChanged = onSearchParamsChanged
                    )
                }
            }
            is ContentListHeaderUiModel.PokemonHero -> {
                item(key = "pokemon_hero") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PokemonAvatar(
                            imageUrl = h.imageUrl,
                            contentDescription = h.name,
                            circleSize = 158.dp,
                            spriteSize = 227.dp
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
            is ContentListHeaderUiModel.PlayerHero -> {
                item(key = "player_hero") {
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

            uiState.items.isEmpty() -> {
                item(key = "empty") {
                    EmptyView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(0.5f)
                    )
                }
            }

            else -> {
                uiState.items.forEach { topItem ->
                    when (topItem) {
                        is ContentListItem.Section -> {
                            val isLoadingSection = topItem.header in uiState.loadingSections
                            item(key = topItem.listKey) {
                                SectionHeader(
                                    title = topItem.header,
                                    isLoading = isLoadingSection,
                                    sortOrder = if (topItem.header == "Battles") sortOrder else null,
                                    onToggleSortOrder = if (topItem.header == "Battles") onToggleSortOrder else null
                                )
                            }
                            if (topItem.items.isEmpty() && !isLoadingSection) {
                                item(key = "${topItem.listKey}_empty") {
                                    EmptyView(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
                                }
                            }
                            items(items = topItem.items, key = { it.listKey }) { child ->
                                Box(modifier = if (isLoadingSection) Modifier.alpha(0.5f) else Modifier) {
                                    ContentListItemRow(child, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
                                }
                            }
                        }
                        is ContentListItem.FormatSelector -> {
                            if (formats.isNotEmpty() && onFormatSelected != null) {
                                item(key = topItem.listKey) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        FormatDropdown(
                                            formats = formats,
                                            selectedFormatId = selectedFormatId,
                                            onFormatSelected = onFormatSelected
                                        )
                                    }
                                }
                            }
                        }
                        else -> item(key = topItem.listKey) {
                            ContentListItemRow(topItem, selectedBattleId, showWinnerHighlight, onItemClick, onHighlightBattleClick, onPokemonGridClick)
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
    if (windowSizeClass == WindowSizeClass.Expanded) {
        ThemedVerticalScrollbar(
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
    }
    }
}

@Composable
private fun ContentListItemRow(
    item: ContentListItem,
    selectedBattleId: Int?,
    showWinnerHighlight: Boolean,
    onItemClick: (ContentListItem) -> Unit,
    onHighlightBattleClick: (Int) -> Unit = {},
    onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit = {}
) {
    when (item) {
        is ContentListItem.Battle -> {
            val isSelected = item.uiModel.id == selectedBattleId
            BattleCard(
                uiModel = item.uiModel,
                showWinnerHighlight = showWinnerHighlight,
                onClick = { onItemClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(12.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
            )
        }
        is ContentListItem.Pokemon -> PokemonListRow(
            name = item.name,
            imageUrl = item.imageUrl,
            types = item.types,
            onClick = { onItemClick(item) }
        )
        is ContentListItem.Player -> PlayerListRow(
            name = item.name,
            onClick = { onItemClick(item) }
        )
        is ContentListItem.HighlightButtons -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.buttons.forEach { button ->
                    Surface(
                        onClick = { onHighlightBattleClick(button.battleId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = button.label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = button.rating.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        is ContentListItem.PokemonGrid -> {
            val isCompactGrid = LocalWindowSizeClass.current == WindowSizeClass.Compact
            val columns = if (isCompactGrid) 3 else item.pokemon.size.coerceAtMost(6)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.pokemon.chunked(columns).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { pokemon ->
                                Surface(
                                    onClick = { onPokemonGridClick(pokemon) },
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .widthIn(max = 160.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        FillPokemonAvatar(
                                            imageUrl = pokemon.imageUrl,
                                            contentDescription = pokemon.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                        )
                                        Text(
                                            text = pokemon.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            repeat(columns - row.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        is ContentListItem.Section -> {}
        is ContentListItem.FormatSelector -> {}
    }
}

@Composable
private fun SectionHeader(
    title: String,
    isLoading: Boolean = false,
    sortOrder: String? = null,
    onToggleSortOrder: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (sortOrder != null && onToggleSortOrder != null) {
            Spacer(modifier = Modifier.weight(1f))
            SortToggleButton(sortOrder = sortOrder, isLoading = isLoading, onClick = onToggleSortOrder)
        }
    }
}

@Composable
private fun SortToggleButton(sortOrder: String, isLoading: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .then(if (isLoading) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (sortOrder == "rating") "Rating" else "Time",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun List<ContentListItem>.findBattle(battleId: Int): ContentListItem.Battle? {
    for (item in this) {
        if (item is ContentListItem.Battle && item.uiModel.id == battleId) return item
        if (item is ContentListItem.Section) {
            val found = item.items.findBattle(battleId)
            if (found != null) return found
        }
    }
    return null
}

@Composable
private fun PokemonListRow(
    name: String,
    imageUrl: String?,
    types: List<com.arcvgc.app.ui.model.TypeUiModel>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PokemonAvatar(
                imageUrl = imageUrl,
                contentDescription = name,
                circleSize = 40.dp,
                spriteSize = 56.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            TypeIconRow(
                types = types.map { TypeInfo(it.name, it.imageUrl) }
            )
        }
    }
}

@Composable
private fun PlayerListRow(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FormatDropdown(
    formats: List<FormatUiModel>,
    selectedFormatId: Int,
    onFormatSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedFormat = formats.find { it.id == selectedFormatId }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = selectedFormat?.displayName ?: "Format",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select format",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            formats.forEach { format ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = format.displayName,
                            fontWeight = if (format.id == selectedFormatId) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onFormatSelected(format.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arcvgc.app.NavEntry
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.ui.components.LoadingIndicator
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FormatSorter
import com.arcvgc.app.ui.rememberViewModel
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

@Composable
internal fun UsageDesktopPage(
    pendingInitialFormatId: Int?,
    pendingInitialFormatTick: Int,
    initialSelectedPokemonId: Int?,
    nestedStack: List<NavEntry>,
    onPushNestedEntry: (NavEntry) -> Unit,
    onPopNestedEntry: () -> Unit,
    onClearNestedStack: () -> Unit,
    onSelectedPokemonChanged: (formatId: Int?, pokemonId: Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModelKey = "usage_desktop_top_pokemon"
    val listViewModel = rememberViewModel(viewModelKey) {
        ContentListViewModel(
            repository = DependencyContainer.battleRepository,
            favoritesRepository = DependencyContainer.favoritesRepository,
            mode = ContentListMode.TopPokemon(formatId = pendingInitialFormatId),
            appConfigRepository = DependencyContainer.appConfigRepository,
            formatCatalogRepository = DependencyContainer.formatCatalogRepository,
            pokemonCatalogRepository = DependencyContainer.pokemonCatalogRepository
        )
    }

    val allItems by listViewModel.allTopPokemonItems.collectAsState()
    val uiState by listViewModel.uiState.collectAsState()
    val searchQuery by listViewModel.searchQuery.collectAsState()
    val selectedFormatId by listViewModel.selectedFormatId.collectAsState()

    // One-shot format push from Home → See More. Each click increments
    // pendingInitialFormatTick; we apply it to the VM only when the tick
    // exceeds the last applied one, so tab switches back to Usage don't
    // re-reset any format the user has since picked via the dropdown.
    LaunchedEffect(pendingInitialFormatTick) {
        val fmt = pendingInitialFormatId
        if (fmt != null &&
            pendingInitialFormatTick > listViewModel.lastAppliedUsageFormatTick
        ) {
            listViewModel.lastAppliedUsageFormatTick = pendingInitialFormatTick
            if (fmt != listViewModel.selectedFormatId.value) {
                listViewModel.selectFormat(fmt)
            }
        }
    }
    val formatCatalogState by DependencyContainer.formatCatalogRepository.state.collectAsState()
    val appConfig by DependencyContainer.appConfigRepository.config.collectAsState()
    val sortedFormats = remember(formatCatalogState.items, appConfig) {
        FormatSorter.sorted(formatCatalogState.items, appConfig?.defaultFormat?.id)
    }

    var selectedPokemon by remember { mutableStateOf<ContentListItem.Pokemon?>(null) }
    // Usage format captured at the moment the pokemon was selected. The nested
    // Pokemon page is keyed on this so each (pokemon, usage-format) pair gets
    // its own session — re-clicking the same pokemon after changing the usage
    // format re-initializes the page with the new format.
    var formatAtSelection by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(allItems, initialSelectedPokemonId, uiState.loadingSections) {
        if (allItems.isEmpty()) return@LaunchedEffect
        // Don't auto-select while the VM is mid-reload — allItems may still
        // be the previous format's data until fetchContent completes.
        if (uiState.loadingSections.isNotEmpty()) return@LaunchedEffect
        if (selectedPokemon == null || allItems.none { it.id == selectedPokemon?.id }) {
            val initial = initialSelectedPokemonId?.let { id -> allItems.find { it.id == id } }
            selectedPokemon = initial ?: allItems.first()
            formatAtSelection = selectedFormatId
        }
    }

    LaunchedEffect(selectedPokemon?.id, formatAtSelection) {
        onSelectedPokemonChanged(formatAtSelection, selectedPokemon?.id)
    }

    val isReady = !uiState.isLoading && allItems.isNotEmpty() && sortedFormats.isNotEmpty() && selectedPokemon != null

    if (!isReady) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val nameStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
    val percentStyle: TextStyle = MaterialTheme.typography.bodySmall

    val rowAvatarSize = 40.dp
    val rowSpriteSize = 56.dp
    val rowInnerHorizontalPadding = 12.dp
    val avatarToTextSpacing = 12.dp
    val nameToPercentSpacing = 12.dp
    val leftPanePadding = 16.dp
    val minLeftPaneWidth = 260.dp

    // FormatDropdown intrinsic layout: 12dp horizontal padding × 2 + bodySmall text + 4dp gap + 16dp chevron
    val formatDropdownChromeWidth = 12.dp * 2 + 4.dp + 16.dp
    val formatNameStyle: TextStyle = MaterialTheme.typography.bodySmall

    val leftPaneWidth = remember(allItems, sortedFormats, selectedFormatId) {
        with(density) {
            val maxNameWidthPx = allItems.maxOf { item ->
                textMeasurer.measure(item.name, nameStyle).size.width
            }
            val maxPercentWidthPx = allItems.maxOf { item ->
                textMeasurer.measure(item.usagePercent ?: "", percentStyle).size.width
            }
            val maxFormatNameWidthPx = sortedFormats.maxOf { fmt ->
                textMeasurer.measure(fmt.displayName, formatNameStyle).size.width
            }
            val rowContentWidth = rowAvatarSize +
                avatarToTextSpacing +
                maxNameWidthPx.toDp() +
                nameToPercentSpacing +
                maxPercentWidthPx.toDp() +
                rowInnerHorizontalPadding * 2
            val formatDropdownContentWidth = maxFormatNameWidthPx.toDp() + formatDropdownChromeWidth
            val widest = maxOf(rowContentWidth, formatDropdownContentWidth)
            val total = widest + leftPanePadding * 2
            maxOf(total, minLeftPaneWidth)
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .width(leftPaneWidth)
                .fillMaxHeight()
                .padding(horizontal = leftPanePadding)
                .padding(top = leftPanePadding)
        ) {
            val formFieldSpacing = 12.dp

            // OutlinedTextField has ~8dp of intrinsic vertical inset around its
            // visible border (reserved for the floating label), so the dropdown
            // needs less bottom spacing to visually match the search→list gap.
            FormatDropdown(
                formats = sortedFormats,
                selectedFormatId = selectedFormatId,
                onFormatSelected = listViewModel::selectFormat,
                fillMaxWidth = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = listViewModel::setSearchQuery,
                label = { Text("Search Pok\u00E9mon") },
                singleLine = true,
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { listViewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                } else null,
                shape = RoundedCornerShape(CardCornerRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = formFieldSpacing)
            )

            val visibleItems = if (searchQuery.isBlank()) {
                allItems
            } else {
                allItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            val listState = rememberLazyListState()
            val isReloading = uiState.loadingSections.isNotEmpty()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isReloading) Modifier.alpha(0.5f) else Modifier),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(visibleItems, key = { it.listKey }) { pokemon ->
                    UsagePokemonRow(
                        name = pokemon.name,
                        imageUrl = pokemon.imageUrl,
                        usagePercent = pokemon.usagePercent ?: "",
                        avatarSize = rowAvatarSize,
                        spriteSize = rowSpriteSize,
                        horizontalPadding = rowInnerHorizontalPadding,
                        avatarToTextSpacing = avatarToTextSpacing,
                        nameToPercentSpacing = nameToPercentSpacing,
                        isSelected = pokemon.id == selectedPokemon?.id,
                        onClick = {
                            val sameSelection = pokemon.id == selectedPokemon?.id &&
                                selectedFormatId == formatAtSelection
                            if (!sameSelection) {
                                selectedPokemon = pokemon
                                formatAtSelection = selectedFormatId
                                onClearNestedStack()
                            }
                        }
                    )
                }
            }
        }

        VerticalDivider()

        Box(modifier = Modifier.fillMaxSize()) {
            val nestedTop = nestedStack.lastOrNull()
            val nestedPokemonClick: (Int, String, String?, List<String>, Int?) -> Unit =
                { id, name, imageUrl, typeImageUrls, fmt ->
                    onPushNestedEntry(NavEntry.Pokemon(id, name, imageUrl, typeImageUrls, fmt))
                }
            val nestedPlayerClick: (Int, String, Int?) -> Unit = { id, name, fmt ->
                onPushNestedEntry(NavEntry.Player(id, name, fmt))
            }

            when (nestedTop) {
                null -> {
                    val sel = selectedPokemon ?: return@Box
                    ContentListPage(
                        mode = ContentListMode.Pokemon(
                            pokemonId = sel.id,
                            name = sel.name,
                            imageUrl = sel.imageUrl,
                            typeImageUrl1 = sel.types.getOrNull(0)?.imageUrl,
                            typeImageUrl2 = sel.types.getOrNull(1)?.imageUrl,
                            formatId = formatAtSelection
                        ),
                        modifier = Modifier.fillMaxSize(),
                        onPokemonClick = nestedPokemonClick,
                        onPlayerClick = nestedPlayerClick,
                        showToolbarWithoutBack = true,
                        mirrorUrl = false
                    )
                }
                is NavEntry.Pokemon -> ContentListPage(
                    mode = ContentListMode.Pokemon(
                        pokemonId = nestedTop.id,
                        name = nestedTop.name,
                        imageUrl = nestedTop.imageUrl,
                        typeImageUrl1 = nestedTop.typeImageUrls.getOrNull(0),
                        typeImageUrl2 = nestedTop.typeImageUrls.getOrNull(1),
                        formatId = nestedTop.formatId
                    ),
                    onBack = onPopNestedEntry,
                    modifier = Modifier.fillMaxSize(),
                    onPokemonClick = nestedPokemonClick,
                    onPlayerClick = nestedPlayerClick,
                    mirrorUrl = false
                )
                is NavEntry.Player -> ContentListPage(
                    mode = ContentListMode.Player(
                        playerId = nestedTop.id,
                        playerName = nestedTop.name,
                        formatId = nestedTop.formatId
                    ),
                    onBack = onPopNestedEntry,
                    modifier = Modifier.fillMaxSize(),
                    onPokemonClick = nestedPokemonClick,
                    onPlayerClick = nestedPlayerClick,
                    mirrorUrl = false
                )
                is NavEntry.BattleDetail, is NavEntry.TopPokemon -> {
                    // Not used in the usage nested stack
                }
            }
        }
    }
}

@Composable
private fun UsagePokemonRow(
    name: String,
    imageUrl: String?,
    usagePercent: String,
    avatarSize: Dp,
    spriteSize: Dp,
    horizontalPadding: Dp,
    avatarToTextSpacing: Dp,
    nameToPercentSpacing: Dp,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }
    val borderWidth = if (isSelected) StandardBorderWidth * 2 else StandardBorderWidth
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PokemonAvatar(
                imageUrl = imageUrl,
                contentDescription = name,
                circleSize = avatarSize,
                spriteSize = spriteSize
            )
            Spacer(modifier = Modifier.width(avatarToTextSpacing))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(nameToPercentSpacing))
            Text(
                text = usagePercent,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

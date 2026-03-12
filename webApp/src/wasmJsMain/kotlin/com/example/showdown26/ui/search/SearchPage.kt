package com.example.showdown26.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.showdown26.di.DependencyContainer
import com.example.showdown26.domain.model.SearchFilterSlot
import com.example.showdown26.domain.model.SearchParams
import com.example.showdown26.ui.components.ThemedVerticalScrollbar
import com.example.showdown26.ui.LocalWindowSizeClass
import com.example.showdown26.ui.WindowSizeClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    onSearch: (SearchParams) -> Unit = {}
) {
    val viewModel = remember {
        SearchViewModel(
            DependencyContainer.pokemonCatalogRepository,
            DependencyContainer.itemCatalogRepository,
            DependencyContainer.teraTypeCatalogRepository,
            DependencyContainer.formatCatalogRepository
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    val pokemonCatalog by viewModel.pokemonCatalogState.collectAsState()
    val itemCatalog by viewModel.itemCatalogState.collectAsState()
    val teraTypeCatalog by viewModel.teraTypeCatalogState.collectAsState()
    val formatCatalog by viewModel.formatCatalogState.collectAsState()

    val todayMillis = remember { currentTimeMillis() }

    var showPokemonPicker by remember { mutableStateOf(false) }
    var itemPickerSlotIndex by remember { mutableIntStateOf(-1) }
    var teraPickerSlotIndex by remember { mutableIntStateOf(-1) }
    var showFormatPicker by remember { mutableStateOf(false) }
    var showMinRatingPicker by remember { mutableStateOf(false) }
    var showMaxRatingPicker by remember { mutableStateOf(false) }
    var showSortOrderPicker by remember { mutableStateOf(false) }
    var showPlayerNamePicker by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val windowSizeClass = LocalWindowSizeClass.current
    val listState = rememberLazyListState()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
    Box(
        modifier = Modifier
            .widthIn(max = 900.dp)
            .fillMaxHeight()
            .align(Alignment.TopCenter)
    ) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = uiState.filterSlots,
            key = { index, slot -> "${slot.pokemonId}_$index" }
        ) { index, slot ->
            SearchFilterCard(
                slot = slot,
                onRemove = { viewModel.removePokemon(index) },
                onItemClick = { itemPickerSlotIndex = index },
                onTeraClick = { teraPickerSlotIndex = index }
            )
        }

        if (uiState.canAddMore) {
            item {
                SearchOptionButton(
                    text = "Add Pokémon Filter",
                    onClick = { showPokemonPicker = true }
                )
            }
        }

        item {
            when {
                formatCatalog.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                formatCatalog.error == null -> {
                    val displayName = uiState.selectedFormat?.displayName
                        ?: formatCatalog.items.firstOrNull()?.displayName
                    SearchOptionButton(
                        text = "Format: ${displayName ?: "Select"}",
                        onClick = { showFormatPicker = true }
                    )
                }
            }
        }

        // Player Name button
        item {
            val playerText = if (uiState.playerName.isBlank()) "Showdown Username"
                else "Showdown Username: ${uiState.playerName}"
            DateOptionButton(
                text = playerText,
                onClick = { showPlayerNamePicker = true },
                onClear = if (uiState.playerName.isNotBlank()) {
                    { viewModel.setPlayerName("") }
                } else null
            )
        }

        // Min/Max Rating buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val minText = uiState.selectedMinRating?.let { "Min Rating: $it" }
                    ?: "Min Rating: None"
                SearchOptionButton(
                    text = minText,
                    onClick = { showMinRatingPicker = true },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.unratedOnly
                )
                val maxText = uiState.selectedMaxRating?.let { "Max Rating: $it" }
                    ?: "Max Rating: None"
                SearchOptionButton(
                    text = maxText,
                    onClick = { showMaxRatingPicker = true },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.unratedOnly
                )
            }
        }

        // Unrated Only toggle button
        item {
            val bgColor = if (uiState.unratedOnly) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            val textColor = if (uiState.unratedOnly) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { viewModel.setUnratedOnly(!uiState.unratedOnly) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unrated Only",
                    color = textColor,
                    fontSize = 14.sp
                )
            }
        }

        // Date range buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val startText = uiState.timeRangeStart?.let {
                    "Start: ${formatEpochDate(it)}"
                } ?: "Start Date"
                DateOptionButton(
                    text = startText,
                    onClick = { showStartDatePicker = true },
                    onClear = if (uiState.timeRangeStart != null) {
                        { viewModel.setTimeRange(null, uiState.timeRangeEnd) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                val endText = uiState.timeRangeEnd?.let {
                    "End: ${formatEpochDate(it)}"
                } ?: "End Date"
                DateOptionButton(
                    text = endText,
                    onClick = { showEndDatePicker = true },
                    onClear = if (uiState.timeRangeEnd != null) {
                        { viewModel.setTimeRange(uiState.timeRangeStart, null) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sort Order button
        item {
            val orderLabel = if (uiState.selectedOrderBy == "time") "Time" else "Rating"
            SearchOptionButton(
                text = "Sort by: $orderLabel",
                onClick = { showSortOrderPicker = true },
                enabled = !uiState.unratedOnly
            )
        }

        item {
            val searchEnabled = uiState.filterSlots.isNotEmpty()
                    || uiState.selectedMinRating != null
                    || uiState.selectedMaxRating != null
                    || uiState.unratedOnly
                    || uiState.playerName.isNotBlank()
                    || (uiState.timeRangeStart != null && uiState.timeRangeEnd != null)

            Button(
                onClick = {
                    val filters = uiState.filterSlots.map { slot ->
                        SearchFilterSlot(
                            pokemonId = slot.pokemonId,
                            itemId = slot.item?.id,
                            teraTypeId = slot.teraType?.id,
                            pokemonName = slot.pokemonName,
                            pokemonImageUrl = slot.pokemonImageUrl,
                            itemName = slot.item?.name,
                            teraTypeImageUrl = slot.teraType?.imageUrl
                        )
                    }
                    val resolvedFormatId = uiState.selectedFormat?.id
                        ?: formatCatalog.items.firstOrNull()?.id
                        ?: 1
                    val resolvedFormatName = uiState.selectedFormat?.displayName
                        ?: formatCatalog.items.firstOrNull()?.displayName
                    val resolvedOrderBy = uiState.selectedOrderBy
                    onSearch(
                        SearchParams(
                            filters = filters,
                            formatId = resolvedFormatId,
                            minimumRating = if (uiState.unratedOnly) null else uiState.selectedMinRating,
                            maximumRating = if (uiState.unratedOnly) null else uiState.selectedMaxRating,
                            unratedOnly = uiState.unratedOnly,
                            orderBy = resolvedOrderBy,
                            timeRangeStart = uiState.timeRangeStart,
                            timeRangeEnd = uiState.timeRangeEnd,
                            playerName = uiState.playerName.ifBlank { null },
                            formatName = resolvedFormatName
                        )
                    )
                },
                enabled = searchEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
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

    if (showPokemonPicker) {
        PokemonPickerDialog(
            catalogState = pokemonCatalog,
            excludeIds = uiState.filterSlots.map { it.pokemonId }.toSet(),
            onSelect = { pokemon ->
                viewModel.addPokemon(pokemon)
                showPokemonPicker = false
            },
            onDismiss = { showPokemonPicker = false }
        )
    }

    if (itemPickerSlotIndex >= 0) {
        ItemPickerDialog(
            catalogState = itemCatalog,
            onSelect = { item ->
                viewModel.setItem(itemPickerSlotIndex, item)
                itemPickerSlotIndex = -1
            },
            onDismiss = { itemPickerSlotIndex = -1 }
        )
    }

    if (teraPickerSlotIndex >= 0) {
        TeraTypePickerDialog(
            catalogState = teraTypeCatalog,
            onSelect = { teraType ->
                viewModel.setTeraType(teraPickerSlotIndex, teraType)
                teraPickerSlotIndex = -1
            },
            onDismiss = { teraPickerSlotIndex = -1 }
        )
    }

    if (showFormatPicker) {
        FormatPickerDialog(
            catalogState = formatCatalog,
            onSelect = { format ->
                viewModel.setFormat(format)
                showFormatPicker = false
            },
            onDismiss = { showFormatPicker = false }
        )
    }

    if (showMinRatingPicker) {
        MinRatingPickerDialog(
            selectedRating = uiState.selectedMinRating,
            disabledAbove = uiState.selectedMaxRating,
            onSelect = { rating ->
                viewModel.setMinRating(rating)
                showMinRatingPicker = false
            },
            onDismiss = { showMinRatingPicker = false }
        )
    }

    if (showMaxRatingPicker) {
        MaxRatingPickerDialog(
            selectedRating = uiState.selectedMaxRating,
            disabledBelow = uiState.selectedMinRating,
            onSelect = { rating ->
                viewModel.setMaxRating(rating)
                showMaxRatingPicker = false
            },
            onDismiss = { showMaxRatingPicker = false }
        )
    }

    if (showSortOrderPicker) {
        SortOrderPickerDialog(
            selectedOrderBy = uiState.selectedOrderBy,
            onSelect = { orderBy ->
                viewModel.setOrderBy(orderBy)
                showSortOrderPicker = false
            },
            onDismiss = { showSortOrderPicker = false }
        )
    }

    if (showPlayerNamePicker) {
        PlayerNamePickerDialog(
            currentName = uiState.playerName,
            onConfirm = { name ->
                viewModel.setPlayerName(name)
                showPlayerNamePicker = false
            },
            onDismiss = { showPlayerNamePicker = false }
        )
    }

    // Start date picker dialog
    if (showStartDatePicker) {
        val upperBound = uiState.timeRangeEnd?.let { minOf(it * 1000, todayMillis) } ?: todayMillis
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.timeRangeStart?.let { it * 1000 },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= upperBound
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.setTimeRange(millis / 1000, uiState.timeRangeEnd)
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // End date picker dialog
    if (showEndDatePicker) {
        val lowerBound = uiState.timeRangeStart?.let { it * 1000 }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.timeRangeEnd?.let { it * 1000 },
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    if (utcTimeMillis > todayMillis) return false
                    if (lowerBound != null && utcTimeMillis < lowerBound) return false
                    return true
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        viewModel.setTimeRange(uiState.timeRangeStart, millis / 1000)
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

private fun currentTimeMillis(): Long = jsDateNow().toLong()

private fun formatEpochDate(epochSeconds: Long): String {
    // MM/DD/YY format for wasmJs (no java.text.SimpleDateFormat)
    val totalDays = (epochSeconds / 86400).toInt()
    var remaining = totalDays + 719468 // days from year 0 to epoch
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
private fun SearchOptionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DateOptionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(start = 12.dp, end = if (onClear != null) 4.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        if (onClear != null) {
            IconButton(
                onClick = onClear,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

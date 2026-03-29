package com.arcvgc.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.ui.components.LoadingIndicator
import com.arcvgc.app.ui.components.SimplePokemonRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.ui.model.AbilityUiModel
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.tokens.AppTokens.DialogWidth

@Composable
fun PokemonPickerDialog(
    catalogState: CatalogState<PokemonPickerUiModel>,
    excludeIds: Set<Int> = emptySet(),
    onSelect: (PokemonPickerUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth).height(600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var query by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search Pokémon") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    catalogState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                    catalogState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catalogState.error.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        val filtered = remember(catalogState.items, query, excludeIds) {
                            catalogState.items
                                .filter { it.id !in excludeIds }
                                .let { available ->
                                    if (query.isBlank()) available
                                    else available.filter { it.name.contains(query, ignoreCase = true) }
                                }
                        }

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(
                                items = filtered,
                                key = { it.id }
                            ) { pokemon ->
                                SimplePokemonRow(
                                    imageUrl = pokemon.imageUrl,
                                    name = pokemon.name,
                                    types = pokemon.types.map { TypeInfo(it.name, it.imageUrl) },
                                    circleSize = 46.dp,
                                    spriteSize = 64.dp,
                                    onClick = { onSelect(pokemon) },
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant
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
fun ItemPickerDialog(
    catalogState: CatalogState<ItemUiModel>,
    onSelect: (ItemUiModel?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth).height(600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var query by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search Items") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    catalogState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                    catalogState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catalogState.error.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        val filtered = remember(catalogState.items, query) {
                            if (query.isBlank()) catalogState.items
                            else catalogState.items.filter {
                                it.name.contains(query, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Text(
                                    text = "None",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(null) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                            items(
                                items = filtered,
                                key = { it.name }
                            ) { item ->
                                ItemPickerRow(
                                    item = item,
                                    onClick = { onSelect(item) }
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
private fun ItemPickerRow(
    item: ItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalPlatformContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = item.name,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        } ?: Spacer(modifier = Modifier.size(32.dp))

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AbilityPickerDialog(
    catalogState: CatalogState<AbilityUiModel>,
    onSelect: (AbilityUiModel?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth).height(600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var query by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search Abilities") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    catalogState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                    catalogState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catalogState.error.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        val filtered = remember(catalogState.items, query) {
                            if (query.isBlank()) catalogState.items
                            else catalogState.items.filter {
                                it.name.contains(query, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Text(
                                    text = "None",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(null) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                            items(
                                items = filtered,
                                key = { it.name }
                            ) { ability ->
                                Text(
                                    text = ability.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(ability) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
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
fun TeraTypePickerDialog(
    catalogState: CatalogState<TeraTypeUiModel>,
    onSelect: (TeraTypeUiModel?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth).height(600.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var query by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search Tera Types") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    catalogState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                    catalogState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catalogState.error.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        val filtered = remember(catalogState.items, query) {
                            if (query.isBlank()) catalogState.items
                            else catalogState.items.filter {
                                it.name.contains(query, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Text(
                                    text = "None",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(null) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                            items(
                                items = filtered,
                                key = { it.name }
                            ) { teraType ->
                                TeraTypePickerRow(
                                    teraType = teraType,
                                    onClick = { onSelect(teraType) }
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
private fun TeraTypePickerRow(
    teraType: TeraTypeUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalPlatformContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        teraType.imageUrl?.let { imageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = teraType.name,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        } ?: Spacer(modifier = Modifier.size(32.dp))

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = teraType.name,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun FormatPickerDialog(
    catalogState: CatalogState<FormatUiModel>,
    onSelect: (FormatUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                var query by remember { mutableStateOf("") }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search Formats") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                when {
                    catalogState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                    catalogState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catalogState.error.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {
                        val filtered = remember(catalogState.items, query) {
                            if (query.isBlank()) catalogState.items
                            else catalogState.items.filter {
                                it.displayName.contains(query, ignoreCase = true)
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.height(400.dp)
                        ) {
                            items(
                                items = filtered,
                                key = { it.id }
                            ) { format ->
                                Text(
                                    text = format.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelect(format) }
                                        .padding(vertical = 8.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val ratingOptions = listOf(1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MinRatingPickerDialog(
    selectedRating: Int?,
    disabledAbove: Int? = null,
    onSelect: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Minimum Rating",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRating == null,
                        onClick = { onSelect(null) },
                        label = { Text("None") }
                    )
                    ratingOptions.forEach { rating ->
                        val disabled = disabledAbove != null && rating >= disabledAbove
                        FilterChip(
                            selected = selectedRating == rating,
                            onClick = { onSelect(rating) },
                            enabled = !disabled,
                            label = { Text(rating.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaxRatingPickerDialog(
    selectedRating: Int?,
    disabledBelow: Int? = null,
    onSelect: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Select Maximum Rating",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRating == null,
                        onClick = { onSelect(null) },
                        label = { Text("None") }
                    )
                    ratingOptions.forEach { rating ->
                        val disabled = disabledBelow != null && rating <= disabledBelow
                        FilterChip(
                            selected = selectedRating == rating,
                            onClick = { onSelect(rating) },
                            enabled = !disabled,
                            label = { Text(rating.toString()) }
                        )
                    }
                }
            }
        }
    }
}

private val sortOptions = listOf("rating" to "Rating", "time" to "Time")

@Composable
fun SortOrderPickerDialog(
    selectedOrderBy: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sort Order",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                sortOptions.forEach { (value, label) ->
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            RadioButton(
                                selected = selectedOrderBy == value,
                                onClick = { onSelect(value) }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerNamePickerDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 0.dp,
            modifier = Modifier.width(DialogWidth)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Showdown Username",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text("Enter player name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Button(
                    onClick = { onConfirm(text.trim()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

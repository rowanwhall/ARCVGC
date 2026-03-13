package com.arcvgc.app.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.ui.components.SimplePokemonRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.ui.model.PokemonPickerUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonPickerSheet(
    catalogState: CatalogState<PokemonPickerUiModel>,
    excludeIds: Set<Int> = emptySet(),
    onSelect: (PokemonPickerUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            var query by remember { mutableStateOf("") }

            when {
                catalogState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                catalogState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
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

                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = filtered,
                            key = { it.id }
                        ) { pokemon ->
                            PokemonPickerRow(
                                pokemon = pokemon,
                                onClick = { onSelect(pokemon) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search Pokémon") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun PokemonPickerRow(
    pokemon: PokemonPickerUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SimplePokemonRow(
        imageUrl = pokemon.imageUrl,
        name = pokemon.name,
        types = pokemon.types.map { TypeInfo(it.name, it.imageUrl) },
        circleSize = 46.dp,
        spriteSize = 64.dp,
        onClick = onClick,
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

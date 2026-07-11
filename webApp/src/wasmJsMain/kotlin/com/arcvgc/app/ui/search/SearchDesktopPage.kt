package com.arcvgc.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.contentlist.ContentListPage
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.rememberViewModel

private val LeftPaneWidth = 440.dp

@Composable
fun SearchDesktopPage(
    searchOverlayParams: SearchParams?,
    pendingHydrationTick: Int,
    onSearch: (SearchParams) -> Unit,
    onSearchBack: () -> Unit,
    onPokemonClick: (id: Int, name: String, imageUrl: String?, typeImageUrls: List<String>, formatId: Int?, lookback: LookbackWindow?) -> Unit,
    onPlayerClick: (id: Int, name: String, formatId: Int?) -> Unit,
    modifier: Modifier = Modifier,
    initialBattleId: Int? = null
) {
    val viewModel = rememberViewModel("search") {
        SearchViewModel(
            DependencyContainer.pokemonCatalogRepository,
            DependencyContainer.itemCatalogRepository,
            DependencyContainer.teraTypeCatalogRepository,
            DependencyContainer.abilityCatalogRepository,
            DependencyContainer.formatCatalogRepository,
            DependencyContainer.appConfigRepository,
            DependencyContainer.settingsRepository
        )
    }

    // Apply-once hydration from a deep link. The tick is only bumped during
    // /search?... deep-link arrival, not when the user clicks Search themselves —
    // so this never clobbers in-pane edits.
    LaunchedEffect(pendingHydrationTick) {
        val params = searchOverlayParams
        if (params != null && pendingHydrationTick > viewModel.lastAppliedHydrationTick) {
            viewModel.hydrate(params)
            viewModel.lastAppliedHydrationTick = pendingHydrationTick
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .width(LeftPaneWidth)
                .fillMaxHeight()
        ) {
            SearchFiltersPane(
                onSearch = onSearch,
                modifier = Modifier.fillMaxSize()
            )
        }
        VerticalDivider(modifier = Modifier.fillMaxHeight())
        Box(modifier = Modifier.fillMaxSize()) {
            if (searchOverlayParams == null) {
                SearchEmptyHint(modifier = Modifier.fillMaxSize())
            } else {
                ContentListPage(
                    mode = ContentListMode.Search(searchOverlayParams),
                    onBack = onSearchBack,
                    onSearchParamsChanged = onSearch,
                    modifier = Modifier.fillMaxSize(),
                    onPokemonClick = onPokemonClick,
                    onPlayerClick = onPlayerClick,
                    initialBattleId = initialBattleId
                )
            }
        }
    }
}

@Composable
private fun SearchEmptyHint(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Configure filters and tap Search",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

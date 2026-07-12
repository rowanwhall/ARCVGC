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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.LocalViewModelStore
import com.arcvgc.app.ui.components.CollapsibleSidePane
import com.arcvgc.app.ui.components.PaneDividerWidth
import com.arcvgc.app.ui.contentlist.ContentListPage
import com.arcvgc.app.ui.contentlist.ContentListViewModel
import com.arcvgc.app.ui.contentlist.searchContentListKey
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.rememberViewModel

private val LeftPaneWidth = 440.dp

@Composable
internal fun SearchDesktopPage(
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

    val enableAnimations by DependencyContainer.settingsRepository.enableAnimations.collectAsState()
    // Whether the results page has a battle detail pane open. While open, the
    // filter pane slides offscreen-left so the battle detail has room to render.
    // Seeded true for ?battle= deep links, and from the cached results-page VM
    // on tab re-entry (which restores savedBattleId), so the page composes with
    // the pane already hidden instead of flashing an exit animation on load.
    val viewModelStore = LocalViewModelStore.current
    var battleDetailOpen by remember {
        mutableStateOf(
            initialBattleId != null || searchOverlayParams?.let { params ->
                viewModelStore.peek<ContentListViewModel>(searchContentListKey(params))?.savedBattleId
            } != null
        )
    }
    // The results page (the only thing that can report an open battle) is gone —
    // without this reset the pane would stay hidden beside the empty hint.
    LaunchedEffect(searchOverlayParams) {
        if (searchOverlayParams == null) battleDetailOpen = false
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        CollapsibleSidePane(
            visible = !battleDetailOpen,
            paneWidth = LeftPaneWidth + PaneDividerWidth,
            animate = enableAnimations
        ) {
            Row(modifier = Modifier.fillMaxHeight()) {
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
            }
        }
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
                    onBattleDetailOpenChanged = { battleDetailOpen = it },
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

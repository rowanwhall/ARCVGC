package com.arcvgc.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.arcvgc.app.ui.contentlist.ContentListPage
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.rememberViewModel

private class FavoritesViewModel : ViewModel() {
    var savedSubTab: Int = 0
}

@Composable
fun FavoritesPage(
    modifier: Modifier = Modifier,
    initialSubTab: Int? = null,
    initialBattleId: Int? = null,
    onPokemonClick: ((id: Int, name: String, imageUrl: String?, typeImageUrls: List<String>, formatId: Int?) -> Unit)? = null,
    onPlayerClick: ((id: Int, name: String, formatId: Int?) -> Unit)? = null
) {
    val viewModel = rememberViewModel("favorites") { FavoritesViewModel() }
    var selectedSubTab by remember(viewModel) { mutableIntStateOf(initialSubTab ?: viewModel.savedSubTab) }
    val subTabs = listOf("Battles", "Pokémon", "Players")

    LaunchedEffect(selectedSubTab) {
        viewModel.savedSubTab = selectedSubTab
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Only pass initialBattleId to the sub-tab that was deep-linked to
        val battleIdForTab = initialBattleId.takeIf { selectedSubTab == (initialSubTab ?: 0) }

        when (selectedSubTab) {
            0 -> ContentListPage(
                mode = ContentListMode.Favorites(FavoriteContentType.Battles),
                modifier = Modifier.fillMaxSize(),
                onPokemonClick = onPokemonClick,
                onPlayerClick = onPlayerClick,
                initialBattleId = battleIdForTab
            )
            1 -> ContentListPage(
                mode = ContentListMode.Favorites(FavoriteContentType.Pokemon),
                modifier = Modifier.fillMaxSize(),
                onPokemonClick = onPokemonClick,
                onPlayerClick = onPlayerClick,
                initialBattleId = battleIdForTab
            )
            2 -> ContentListPage(
                mode = ContentListMode.Favorites(FavoriteContentType.Players),
                modifier = Modifier.fillMaxSize(),
                onPokemonClick = onPokemonClick,
                onPlayerClick = onPlayerClick,
                initialBattleId = battleIdForTab
            )
        }
    }
}

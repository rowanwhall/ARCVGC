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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.contentlist.ContentListPage
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType

@Composable
fun FavoritesPage(modifier: Modifier = Modifier) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf("Battles", "Pokémon", "Players")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
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

        when (selectedSubTab) {
            0 -> ContentListPage(
                mode = ContentListMode.Favorites(FavoriteContentType.Battles),
                modifier = Modifier.fillMaxSize()
            )
            1 -> ContentListPage(
                mode = ContentListMode.Favorites(FavoriteContentType.Pokemon),
                modifier = Modifier.fillMaxSize()
            )
            2 -> ContentListPage(
                mode = ContentListMode.Favorites(FavoriteContentType.Players),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

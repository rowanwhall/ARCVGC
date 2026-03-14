package com.arcvgc.app.ui.battledetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.components.ErrorView
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PlayerDetailUiModel
import com.arcvgc.app.ui.model.PokemonDetailUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.TypeUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleDetailSheet(
    state: BattleDetailState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorited: Boolean = false,
    showWinnerHighlight: Boolean = true,
    onToggleFavorite: () -> Unit = {},
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp, bottom = 2.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(2.dp)
                        )
                )
                // X and heart buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorited) "Unfavorite" else "Favorite",
                            tint = if (isFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) {
        BattleDetailContent(state = state, onRetry = onRetry, showWinnerHighlight = showWinnerHighlight, onPokemonClick = onPokemonClick, onPlayerClick = onPlayerClick)
    }
}

@Composable
private fun BattleDetailContent(
    state: BattleDetailState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    when {
        state.isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        state.error != null -> {
            ErrorView(
                onRetry = onRetry,
                modifier = modifier.fillMaxSize()
            )
        }

        state.battleDetail != null -> {
            BattleDetailTabs(battleDetail = state.battleDetail, showWinnerHighlight = showWinnerHighlight, onPokemonClick = onPokemonClick, onPlayerClick = onPlayerClick)
        }
    }
}

@Composable
private fun BattleDetailTabs(
    battleDetail: BattleDetailUiModel,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Team Preview", "Replay")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> TeamPreviewTab(battleDetail = battleDetail, showWinnerHighlight = showWinnerHighlight, onPokemonClick = onPokemonClick, onPlayerClick = onPlayerClick)
            1 -> ReplayTab(replayUrl = battleDetail.replayUrl)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BattleDetailTabsPreview() {
    val samplePokemon = PokemonDetailUiModel(
        id = 149,
        name = "Dragonite",
        imageUrl = null,
        item = ItemUiModel(id = 1, name = "Choice Band", imageUrl = null),
        abilityName = "Multiscale",
        moves = listOf("Dragon Claw", "Extreme Speed", "Earthquake", "Ice Punch"),
        types = listOf(TypeUiModel("Dragon", null), TypeUiModel("Flying", null)),
        teraType = TeraTypeUiModel(id = 1, name = "Normal", imageUrl = null)
    )
    val sampleBattle = BattleDetailUiModel(
        id = 1,
        player1 = PlayerDetailUiModel(id = 1, name = "Player1", isWinner = true, team = List(6) { samplePokemon }),
        player2 = PlayerDetailUiModel(id = 2, name = "Opponent", isWinner = false, team = List(6) { samplePokemon }),
        formatId = 1,
        formatName = "VGC 2026 Reg H",
        rating = 1542,
        formattedTime = "Feb 8, 5:03 PM",
        replayUrl = "https://replay.pokemonshowdown.com/example"
    )
    MaterialTheme {
        BattleDetailTabs(battleDetail = sampleBattle)
    }
}

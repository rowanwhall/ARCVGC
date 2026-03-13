package com.arcvgc.app.ui.battledetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.ui.components.ErrorView

@Composable
fun BattleDetailPanel(
    battleId: Int,
    isFavorited: Boolean,
    onToggleFavorite: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    player1IsWinner: Boolean? = null,
    player2IsWinner: Boolean? = null,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    val viewModel = remember(battleId) {
        BattleDetailViewModel(
            repository = DependencyContainer.battleRepository,
            battleId = battleId
        )
    }
    val state by viewModel.state.collectAsState()

    // Patch winner values from the list endpoint (detail endpoint doesn't return them)
    val displayDetail = state.battleDetail?.let { detail ->
        detail.copy(
            player1 = detail.player1.copy(isWinner = player1IsWinner),
            player2 = detail.player2.copy(isWinner = player2IsWinner)
        )
    }

    Column(modifier = modifier) {
        // Header with close and heart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
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

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                ErrorView(
                    onRetry = { viewModel.loadBattleDetail(battleId) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            displayDetail != null -> {
                TeamPreviewTab(
                    battleDetail = displayDetail,
                    showWinnerHighlight = showWinnerHighlight,
                    onPokemonClick = onPokemonClick,
                    onPlayerClick = onPlayerClick
                )
            }
        }
    }
}

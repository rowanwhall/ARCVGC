package com.arcvgc.app.ui.battledetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.ui.components.ErrorView
import com.arcvgc.app.ui.components.LoadingIndicator
import com.arcvgc.app.ui.components.GradientToolbarScaffold
import com.arcvgc.app.ui.rememberViewModel

@Composable
fun BattleDetailPanel(
    battleId: Int,
    isFavorited: Boolean,
    onToggleFavorite: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    showBackArrow: Boolean = false,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>, Int?) -> Unit)? = null,
    onPlayerClick: ((Int, String, Int?) -> Unit)? = null
) {
    val viewModel = rememberViewModel("battle_detail_$battleId") {
        BattleDetailViewModel(
            repository = DependencyContainer.battleRepository,
            battleId = battleId
        )
    }
    val state by viewModel.state.collectAsState()

    val displayDetail = state.battleDetail

    val wrappedOnPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = onPokemonClick?.let { callback ->
        { id: Int, name: String, imageUrl: String?, typeImageUrls: List<String> ->
            callback(id, name, imageUrl, typeImageUrls, displayDetail?.formatId)
        }
    }

    val wrappedOnPlayerClick: ((Int, String) -> Unit)? = onPlayerClick?.let { callback ->
        { id: Int, name: String ->
            callback(id, name, displayDetail?.formatId)
        }
    }

    GradientToolbarScaffold(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = if (showBackArrow) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                    contentDescription = if (showBackArrow) "Back" else "Close"
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorited) "Unfavorite" else "Favorite",
                    tint = if (isFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { topPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(top = topPadding),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }

            state.error != null -> {
                ErrorView(
                    onRetry = { viewModel.loadBattleDetail(battleId) },
                    modifier = Modifier.fillMaxSize().padding(top = topPadding)
                )
            }

            displayDetail != null -> {
                BattleDetailContent(
                    battleDetail = displayDetail,
                    topPadding = topPadding,
                    showWinnerHighlight = showWinnerHighlight,
                    onPokemonClick = wrappedOnPokemonClick,
                    onPlayerClick = wrappedOnPlayerClick
                )
            }
        }
    }
}

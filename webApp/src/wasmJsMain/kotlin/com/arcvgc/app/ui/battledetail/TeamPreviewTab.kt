package com.arcvgc.app.ui.battledetail

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import com.arcvgc.app.ui.components.ThemedVerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.components.VsDivider
import com.arcvgc.app.ui.mapper.ShowdownPasteFormatter
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.ui.model.PlayerDetailUiModel
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CARD_WIDTH = 280.dp

@Composable
fun TeamPreviewTab(
    battleDetail: BattleDetailUiModel,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val windowSizeClass = LocalWindowSizeClass.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val onTeamCopied: () -> Unit = {
        scope.launch { snackbarHostState.showSnackbar("Team copied to clipboard") }
    }
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { window.open(battleDetail.replayUrl, "_blank") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("View Replay")
            }

            PlayerTeamSection(player = battleDetail.player1, showWinnerHighlight = showWinnerHighlight, onPokemonClick = onPokemonClick, onPlayerClick = onPlayerClick, onTeamCopied = onTeamCopied)

            VsDivider(modifier = Modifier.padding(horizontal = 16.dp))

            PlayerTeamSection(player = battleDetail.player2, showWinnerHighlight = showWinnerHighlight, onPokemonClick = onPokemonClick, onPlayerClick = onPlayerClick, onTeamCopied = onTeamCopied)
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
        if (windowSizeClass == WindowSizeClass.Expanded) {
            ThemedVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(text) => { navigator.clipboard.writeText(text); }")
private external fun copyToClipboard(text: String)

@Composable
private fun PlayerTeamHeader(
    player: PlayerDetailUiModel,
    modifier: Modifier = Modifier,
    onPlayerClick: ((Int, String) -> Unit)? = null,
    onTeamCopied: (() -> Unit)? = null
) {
    var showCopied by remember { mutableStateOf(false) }

    LaunchedEffect(showCopied) {
        if (showCopied) {
            delay(1500)
            showCopied = false
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable { onPlayerClick?.invoke(player.id, player.name) }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = player.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = {
                val text = ShowdownPasteFormatter.format(player.team)
                copyToClipboard(text)
                showCopied = true
                onTeamCopied?.invoke()
            },
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (showCopied) Icons.Default.Check else Icons.Outlined.ContentCopy,
                contentDescription = "Copy team",
                modifier = Modifier.size(20.dp),
                tint = if (showCopied) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerTeamSection(
    player: PlayerDetailUiModel,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null,
    onTeamCopied: (() -> Unit)? = null
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val isCompact = windowSizeClass == WindowSizeClass.Compact

    val primaryColor = MaterialTheme.colorScheme.primary
    val winnerBorder = if (showWinnerHighlight && player.isWinner == true) {
        if (isCompact) Modifier.border(2.dp, primaryColor)
        else Modifier.border(2.dp, primaryColor, RoundedCornerShape(12.dp))
    } else {
        Modifier
    }

    if (isCompact) {
        // Compact: match Android layout — edge-to-edge card with LazyRow carousel
        Column(
            modifier = modifier
                .fillMaxWidth()
                .then(winnerBorder)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = 8.dp)
        ) {
            PlayerTeamHeader(
                player = player,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                onPlayerClick = onPlayerClick,
                onTeamCopied = onTeamCopied
            )

            Spacer(modifier = Modifier.height(4.dp))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val cardWidth = maxWidth * 0.7f
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(player.team) { _, pokemon ->
                        PokemonDetailCard(
                            pokemon = pokemon,
                            modifier = Modifier.width(cardWidth),
                            onPokemonClick = onPokemonClick
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    } else {
        // Expanded: FlowRow grid with fixed card width
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .then(winnerBorder)
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            PlayerTeamHeader(
                player = player,
                modifier = Modifier.padding(bottom = 8.dp),
                onPlayerClick = onPlayerClick,
                onTeamCopied = onTeamCopied
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                player.team.forEach { pokemon ->
                    PokemonDetailCard(
                        pokemon = pokemon,
                        modifier = Modifier.width(CARD_WIDTH),
                        onPokemonClick = onPokemonClick
                    )
                }
            }
        }
    }
}

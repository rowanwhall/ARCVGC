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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.components.InfoButton
import com.arcvgc.app.ui.components.InfoDialog
import com.arcvgc.app.ui.components.VsDivider
import com.arcvgc.app.ui.mapper.ShowdownPasteFormatter
import com.arcvgc.app.ui.model.InfoContentProvider
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.InfoButtonSize
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipVerticalPadding
import com.arcvgc.app.ui.tokens.AppTokens.SecondaryIconAlpha
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth
import com.arcvgc.app.ui.tokens.AppTokens.WinnerBorderWidth
import com.arcvgc.app.ui.model.PlayerDetailUiModel
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CARD_WIDTH = 280.dp

private data class GameButton(val positionInSet: Int?, val replayUrl: String, val isCurrent: Boolean)

@Composable
fun BattleDetailContent(
    battleDetail: BattleDetailUiModel,
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp,
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
                .padding(top = topPadding, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var showUnratedInfo by remember { mutableStateOf(false) }
            var showReplayInfo by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val headerText = if (battleDetail.rating != null) {
                        "${battleDetail.formatName} \u2022 ${battleDetail.rating}"
                    } else {
                        "${battleDetail.formatName} \u2022 Unrated"
                    }
                    if (battleDetail.rating == null) {
                        Spacer(Modifier.size(InfoButtonSize))
                    }
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (battleDetail.rating == null) {
                        InfoButton(onClick = { showUnratedInfo = true })
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val allGames = buildList {
                        add(GameButton(battleDetail.positionInSet, battleDetail.replayUrl, true))
                        battleDetail.setMatches.forEach { add(GameButton(it.positionInSet, it.replayUrl, false)) }
                    }.sortedBy { it.positionInSet ?: Int.MAX_VALUE }

                    Spacer(Modifier.size(InfoButtonSize))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allGames.forEach { game ->
                            val label = game.positionInSet?.let { "Game $it" } ?: "View Replay"
                            if (game.isCurrent) {
                                Button(onClick = { window.open(game.replayUrl, "_blank") }) {
                                    Text(label)
                                }
                            } else {
                                OutlinedButton(onClick = { window.open(game.replayUrl, "_blank") }) {
                                    Text(label)
                                }
                            }
                        }
                    }
                    InfoButton(onClick = { showReplayInfo = true })
                }
            }

            if (showUnratedInfo) {
                InfoContentProvider.get("unrated")?.let { content ->
                    InfoDialog(content = content, onDismiss = { showUnratedInfo = false })
                }
            }

            if (showReplayInfo) {
                InfoContentProvider.get("replay")?.let { content ->
                    InfoDialog(content = content, onDismiss = { showReplayInfo = false })
                }
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
                .clip(RoundedCornerShape(PlayerChipCornerRadius))
                .background(MaterialTheme.colorScheme.surface)
                .border(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(PlayerChipCornerRadius))
                .clickable { onPlayerClick?.invoke(player.id, player.name) }
                .padding(horizontal = PlayerChipHorizontalPadding, vertical = PlayerChipVerticalPadding),
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
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = SecondaryIconAlpha)
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
                else MaterialTheme.colorScheme.onSurface.copy(alpha = SecondaryIconAlpha)
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
        if (isCompact) Modifier.border(WinnerBorderWidth, primaryColor)
        else Modifier.border(WinnerBorderWidth, primaryColor, RoundedCornerShape(CardCornerRadius))
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
        // Expanded: FlowRow grid with fixed card width, container adapts to content
        val cardSpacing = 12.dp
        val innerPadding = 16.dp
        BoxWithConstraints(
            contentAlignment = Alignment.TopCenter,
            modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            val availableForCards = maxWidth - innerPadding * 2
            val columns = ((availableForCards + cardSpacing) / (CARD_WIDTH + cardSpacing))
                .toInt()
                .coerceIn(1, player.team.size.coerceAtLeast(1))
            val flowRowWidth = CARD_WIDTH * columns + cardSpacing * (columns - 1)
            val containerWidth = flowRowWidth + innerPadding * 2

            Column(
                modifier = Modifier
                    .width(containerWidth)
                    .then(winnerBorder)
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(CardCornerRadius))
                    .padding(innerPadding)
            ) {
                PlayerTeamHeader(
                    player = player,
                    modifier = Modifier.padding(bottom = 8.dp),
                    onPlayerClick = onPlayerClick,
                    onTeamCopied = onTeamCopied
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                    verticalArrangement = Arrangement.spacedBy(cardSpacing),
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
}

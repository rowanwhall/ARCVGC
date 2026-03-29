package com.arcvgc.app.ui.battledetail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.ui.components.ErrorView
import com.arcvgc.app.ui.components.LoadingIndicator
import com.arcvgc.app.ui.components.GradientToolbarScaffold
import com.arcvgc.app.ui.components.InfoButton
import com.arcvgc.app.ui.components.InfoSheet
import com.arcvgc.app.ui.components.VsDivider
import com.arcvgc.app.ui.mapper.ShowdownPasteFormatter
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.ui.model.InfoContentProvider
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PlayerDetailUiModel
import com.arcvgc.app.ui.model.PokemonDetailUiModel
import com.arcvgc.app.ui.model.ReplayNavState
import com.arcvgc.app.ui.model.SetMatchUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.toReplayNavState
import com.arcvgc.app.ui.model.TypeUiModel
import com.arcvgc.app.ui.tokens.AppTokens.BulletSeparator
import com.arcvgc.app.ui.tokens.AppTokens.InfoButtonSize
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.PlayerChipVerticalPadding
import com.arcvgc.app.ui.tokens.AppTokens.SecondaryIconAlpha
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth
import com.arcvgc.app.ui.tokens.AppTokens.WinnerBorderWidth
import kotlinx.coroutines.delay

@Composable
fun BattleDetailPage(
    state: BattleDetailState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    statusBarPadding: Dp = 0.dp,
    isFavorited: Boolean = false,
    showWinnerHighlight: Boolean = true,
    onToggleFavorite: () -> Unit = {},
    onShare: (() -> Unit)? = null,
    onViewReplay: (ReplayNavState) -> Unit = {},
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    GradientToolbarScaffold(
        modifier = modifier,
        statusBarPadding = statusBarPadding,
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            onShare?.let { share ->
                IconButton(onClick = share) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxSize().padding(top = topPadding)
                )
            }

            state.battleDetail != null -> {
                BattleDetailBody(
                    battleDetail = state.battleDetail,
                    topPadding = topPadding,
                    showWinnerHighlight = showWinnerHighlight,
                    onViewReplay = onViewReplay,
                    onPokemonClick = onPokemonClick,
                    onPlayerClick = onPlayerClick
                )
            }
        }
    }
}

private data class GameButton(val positionInSet: Int?, val replayUrl: String, val isCurrent: Boolean)

@Composable
private fun BattleDetailBody(
    battleDetail: BattleDetailUiModel,
    modifier: Modifier = Modifier,
    topPadding: Dp = 0.dp,
    showWinnerHighlight: Boolean = true,
    onViewReplay: (ReplayNavState) -> Unit = {},
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(top = topPadding, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Format name, rating, and replay buttons
        var showUnratedInfo by remember { mutableStateOf(false) }
        var showReplayInfo by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val headerText = if (battleDetail.rating != null) {
                    "${battleDetail.formatName} $BulletSeparator ${battleDetail.rating}"
                } else {
                    "${battleDetail.formatName} $BulletSeparator Unrated"
                }
                if (battleDetail.rating == null) {
                    Spacer(Modifier.size(InfoButtonSize))
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (battleDetail.rating == null) {
                    InfoButton(onClick = { showUnratedInfo = true })
                }
            }

            Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
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
                        Button(onClick = { onViewReplay(battleDetail.toReplayNavState(game.replayUrl)) }) {
                            Text(label)
                        }
                    } else {
                        OutlinedButton(onClick = { onViewReplay(battleDetail.toReplayNavState(game.replayUrl)) }) {
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
                InfoSheet(content = content, onDismiss = { showUnratedInfo = false })
            }
        }

        if (showReplayInfo) {
            InfoContentProvider.get("replay")?.let { content ->
                InfoSheet(content = content, onDismiss = { showReplayInfo = false })
            }
        }

        // Player teams
        PlayerTeamSection(player = battleDetail.player1, showWinnerHighlight = showWinnerHighlight, onPokemonClick = onPokemonClick, onPlayerClick = onPlayerClick)

        VsDivider(modifier = Modifier.padding(horizontal = 16.dp))

        PlayerTeamSection(player = battleDetail.player2, showWinnerHighlight = showWinnerHighlight, onPokemonClick = onPokemonClick, onPlayerClick = onPlayerClick)
    }
}

@Composable
private fun PlayerTeamSection(
    player: PlayerDetailUiModel,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cardWidth = screenWidth * 0.7f

    val primaryColor = MaterialTheme.colorScheme.primary
    val winnerBorder = if (showWinnerHighlight && player.isWinner == true) {
        Modifier.border(WinnerBorderWidth, primaryColor)
    } else {
        Modifier
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var showCopied by remember { mutableStateOf(false) }

    LaunchedEffect(showCopied) {
        if (showCopied) {
            delay(1500)
            showCopied = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(winnerBorder)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
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
                    clipboardManager.setText(AnnotatedString(text))
                    showCopied = true
                    Toast.makeText(context, "Team copied to clipboard", Toast.LENGTH_SHORT).show()
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

        Spacer(modifier = Modifier.height(4.dp))

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

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun BattleDetailPagePreview() {
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
        replayUrl = "https://replay.pokemonshowdown.com/example",
        positionInSet = 1,
        setMatches = listOf(
            SetMatchUiModel(id = 2, positionInSet = 2, replayUrl = "https://replay.pokemonshowdown.com/example2"),
            SetMatchUiModel(id = 3, positionInSet = 3, replayUrl = "https://replay.pokemonshowdown.com/example3")
        )
    )
    MaterialTheme {
        BattleDetailPage(
            state = BattleDetailState(battleDetail = sampleBattle),
            onBack = {},
            onRetry = {}
        )
    }
}

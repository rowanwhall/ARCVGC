package com.arcvgc.app.ui.battledetail

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.ui.components.VsDivider
import com.arcvgc.app.ui.mapper.ShowdownPasteFormatter
import com.arcvgc.app.ui.model.BattleDetailUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PlayerDetailUiModel
import com.arcvgc.app.ui.model.PokemonDetailUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.TypeUiModel
import kotlinx.coroutines.delay

@Composable
fun TeamPreviewTab(
    battleDetail: BattleDetailUiModel,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null,
    onPlayerClick: ((Int, String) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        Modifier.border(2.dp, primaryColor)
    } else {
        Modifier
    }

    val clipboardManager = LocalClipboardManager.current
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
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
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
                    clipboardManager.setText(AnnotatedString(text))
                    showCopied = true
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
private fun TeamPreviewTabPreview() {
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
        formatName = "VGC 2026 Reg H",
        rating = 1542,
        formattedTime = "Feb 8, 5:03 PM",
        replayUrl = "https://replay.pokemonshowdown.com/example"
    )
    MaterialTheme {
        TeamPreviewTab(battleDetail = sampleBattle)
    }
}


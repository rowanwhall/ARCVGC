package com.example.showdown26.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.showdown26.R
import com.example.showdown26.ui.model.BattleCardUiModel
import com.example.showdown26.ui.model.ItemUiModel
import com.example.showdown26.ui.model.PlayerUiModel
import com.example.showdown26.ui.model.PokemonSlotUiModel
import com.example.showdown26.ui.model.TeraTypeUiModel

private val CardCornerRadius = 12.dp

@Composable
fun BattleCard(
    uiModel: BattleCardUiModel,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true,
    onClick: () -> Unit = {},
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Header row with timestamp and rating
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uiModel.formattedTime,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiModel.rating,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlayerTeamSection(player = uiModel.player1, showWinnerHighlight = showWinnerHighlight)
                VsDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PlayerTeamSection(player = uiModel.player2, showWinnerHighlight = showWinnerHighlight)
            }
        }
    }
}

@Composable
private fun PlayerTeamSection(
    player: PlayerUiModel,
    modifier: Modifier = Modifier,
    showWinnerHighlight: Boolean = true
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val winnerBorder = if (showWinnerHighlight && player.isWinner == true) {
        Modifier.border(2.dp, primaryColor, RoundedCornerShape(CardCornerRadius))
    } else {
        Modifier
    }

    // Team rectangle with rounded corners
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(winnerBorder)
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(4.dp)
    ) {
        // Player name at top-left
        Text(
            text = player.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            player.team.forEach { pokemonSlot ->
                PokemonWithItem(
                    pokemonSlot = pokemonSlot,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PokemonWithItem(
    pokemonSlot: PokemonSlotUiModel,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val slotSize = maxWidth
        val itemSize = slotSize * 0.35f
        val teraTypeSize = slotSize * 0.35f * 0.75f
        val teraTypeInset = (itemSize - teraTypeSize) / 2

        FillPokemonAvatar(
            imageUrl = pokemonSlot.imageUrl,
            contentDescription = pokemonSlot.name,
            modifier = Modifier.fillMaxSize()
        )

        if (pokemonSlot.item != null) {
            PreviewAsyncImage(
                url = pokemonSlot.item?.imageUrl,
                previewDrawable = R.drawable.preview_item,
                contentDescription = pokemonSlot.item?.name,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(itemSize)
            )
        }

        if (pokemonSlot.teraType != null) {
            PreviewAsyncImage(
                url = pokemonSlot.teraType?.imageUrl,
                previewDrawable = R.drawable.preview_tera,
                contentDescription = pokemonSlot.teraType?.name,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -teraTypeInset, y = teraTypeInset)
                    .size(teraTypeSize)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BattleCardPreview() {
    val samplePokemonSlot = PokemonSlotUiModel(
        name = "Flutter Mane",
        imageUrl = null,
        item = ItemUiModel(
            id = 0,
            name = "Booster Energy",
            imageUrl = null
        ),
        teraType = TeraTypeUiModel(
            id = 1,
            name = "Normal",
            imageUrl = null
        )
    )

    val samplePlayer1 = PlayerUiModel(
        name = "Player1",
        isWinner = true,
        team = List(6) { samplePokemonSlot }
    )

    val samplePlayer2 = PlayerUiModel(
        name = "Opponent",
        isWinner = false,
        team = List(6) { samplePokemonSlot }
    )

    val sampleUiModel = BattleCardUiModel(
        id = 1,
        player1 = samplePlayer1,
        player2 = samplePlayer2,
        formatName = "VGC 2026 Reg H",
        rating = "1542",
        formattedTime = "Feb 8, 5:03 PM"
    )

    MaterialTheme {
        BattleCard(
            uiModel = sampleUiModel,
            modifier = Modifier.padding(16.dp)
        )
    }
}

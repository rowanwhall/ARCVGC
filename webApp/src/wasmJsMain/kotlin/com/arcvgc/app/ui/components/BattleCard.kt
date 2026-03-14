package com.arcvgc.app.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.PlayerUiModel
import com.arcvgc.app.ui.model.PokemonSlotUiModel

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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = uiModel.formatName,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(winnerBorder)
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(4.dp)
    ) {
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
        val context = LocalPlatformContext.current
        val slotSize = maxWidth
        val itemSize = slotSize * 0.40f
        val itemOffset = slotSize * 0.05f
        val teraTypeSize = slotSize * 0.40f * 0.75f

        FillPokemonAvatar(
            imageUrl = pokemonSlot.imageUrl,
            contentDescription = pokemonSlot.name,
            modifier = Modifier.fillMaxSize()
        )

        pokemonSlot.item?.imageUrl?.let { itemImageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(itemImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = pokemonSlot.item?.name,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = -itemOffset, y = -itemOffset)
                    .size(itemSize),
                contentScale = ContentScale.Fit
            )
        }

        pokemonSlot.teraType?.imageUrl?.let { teraTypeImageUrl ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(teraTypeImageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = pokemonSlot.teraType?.name,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -(itemOffset + (itemSize - teraTypeSize) / 2), y = itemOffset + (itemSize - teraTypeSize) / 2)
                    .size(teraTypeSize),
                contentScale = ContentScale.Fit
            )
        }
    }
}

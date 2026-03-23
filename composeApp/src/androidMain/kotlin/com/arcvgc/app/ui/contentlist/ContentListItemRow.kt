package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arcvgc.app.ui.components.BattleCard
import com.arcvgc.app.ui.components.FillPokemonAvatar
import com.arcvgc.app.ui.model.ContentListItem

@Composable
internal fun ContentListItemRow(
    item: ContentListItem,
    showWinnerHighlight: Boolean,
    onItemClick: (ContentListItem) -> Unit,
    onHighlightBattleClick: (Int) -> Unit = {},
    onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit = {}
) {
    when (item) {
        is ContentListItem.Battle -> BattleCard(
            uiModel = item.uiModel,
            modifier = Modifier.fillMaxWidth(),
            showWinnerHighlight = showWinnerHighlight,
            onClick = { onItemClick(item) }
        )
        is ContentListItem.Pokemon -> PokemonListRow(
            id = item.id,
            name = item.name,
            imageUrl = item.imageUrl,
            types = item.types,
            usagePercent = item.usagePercent,
            onClick = { onItemClick(item) }
        )
        is ContentListItem.Player -> PlayerListRow(
            name = item.name,
            onClick = { onItemClick(item) }
        )
        is ContentListItem.HighlightButtons -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item.buttons.forEach { button ->
                    Surface(
                        onClick = { onHighlightBattleClick(button.battleId) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = button.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = button.rating.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        is ContentListItem.PokemonGrid -> {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.pokemon.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { pokemon ->
                                Surface(
                                    onClick = { onPokemonGridClick(pokemon) },
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        FillPokemonAvatar(
                                            imageUrl = pokemon.imageUrl,
                                            contentDescription = pokemon.name,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                        )
                                        Text(
                                            text = pokemon.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        pokemon.usagePercent?.let { pct ->
                                            Text(
                                                text = pct,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(3 - row.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        is ContentListItem.StatChipRow -> {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(item.chips, key = { it.name }) { chip ->
                    StatChip(name = chip.name, usagePercent = chip.usagePercent, imageUrl = chip.imageUrl)
                }
            }
        }
        is ContentListItem.Section -> {}
        is ContentListItem.FormatSelector -> {}
        is ContentListItem.SearchField -> {}
    }
}

@Composable
internal fun StatChip(name: String, usagePercent: String?, imageUrl: String? = null) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                usagePercent?.let { pct ->
                    Text(
                        text = pct,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

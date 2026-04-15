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
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.components.BattleCard
import com.arcvgc.app.ui.components.FillPokemonAvatar
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

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
                        shape = RoundedCornerShape(CardCornerRadius),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
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
            val columns = if (LocalWindowSizeClass.current == WindowSizeClass.Compact) {
                3
            } else {
                item.pokemon.size.coerceAtMost(6)
            }
            Surface(
                shape = RoundedCornerShape(CardCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.pokemon.chunked(columns).forEach { row ->
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
                                            fontWeight = FontWeight.Medium,
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
                            repeat(columns - row.size) {
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
                    StatChip(
                        name = chip.name,
                        usagePercent = chip.usagePercent,
                        imageUrl = chip.imageUrl,
                        pokemonId = chip.pokemonId,
                        onClick = chip.pokemonId?.let {
                            {
                                onPokemonGridClick(
                                    ContentListItem.PokemonGridItem(it, chip.name, chip.imageUrl, chip.usagePercent)
                                )
                            }
                        }
                    )
                }
            }
        }
        is ContentListItem.Section -> {}
        is ContentListItem.SectionGroup -> {}
        is ContentListItem.FormatSelector -> {}
        is ContentListItem.SearchField -> {}
    }
}

@Composable
internal fun StatChip(
    name: String,
    usagePercent: String?,
    imageUrl: String? = null,
    pokemonId: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(CardCornerRadius)
    val color = MaterialTheme.colorScheme.surface
    val border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Surface(onClick = onClick, shape = shape, color = color, border = border) {
            StatChipContent(name, usagePercent, imageUrl, pokemonId)
        }
    } else {
        Surface(shape = shape, color = color, border = border) {
            StatChipContent(name, usagePercent, imageUrl, pokemonId)
        }
    }
}

@Composable
private fun StatChipContent(
    name: String,
    usagePercent: String?,
    imageUrl: String?,
    pokemonId: Int?
) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (pokemonId != null) {
            PokemonAvatar(
                imageUrl = imageUrl,
                contentDescription = name,
                circleSize = 20.dp,
                spriteSize = 32.dp
            )
        } else if (imageUrl != null) {
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
                fontWeight = FontWeight.Medium,
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

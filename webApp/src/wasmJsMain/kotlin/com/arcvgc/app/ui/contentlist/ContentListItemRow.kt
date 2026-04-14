package com.arcvgc.app.ui.contentlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.components.BattleCard
import com.arcvgc.app.ui.components.FillPokemonAvatar
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

@Composable
internal fun ContentListItemRow(
    item: ContentListItem,
    selectedBattleId: Int?,
    showWinnerHighlight: Boolean,
    onItemClick: (ContentListItem) -> Unit,
    onHighlightBattleClick: (Int) -> Unit = {},
    onPokemonGridClick: (ContentListItem.PokemonGridItem) -> Unit = {}
) {
    when (item) {
        is ContentListItem.Battle -> {
            val isSelected = item.uiModel.id == selectedBattleId
            BattleCard(
                uiModel = item.uiModel,
                showWinnerHighlight = showWinnerHighlight,
                onClick = { onItemClick(item) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isSelected) {
                            Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(CardCornerRadius)
                            )
                        } else {
                            Modifier
                        }
                    )
            )
        }
        is ContentListItem.Pokemon -> PokemonListRow(
            name = item.name,
            imageUrl = item.imageUrl,
            types = item.types,
            onClick = { onItemClick(item) },
            usagePercent = item.usagePercent
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
            val isCompactGrid = LocalWindowSizeClass.current == WindowSizeClass.Compact
            val columns = if (isCompactGrid) 3 else item.pokemon.size.coerceAtMost(6)
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
                                    modifier = Modifier
                                        .weight(1f)
                                        .widthIn(max = 160.dp)
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
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
            val isCompact = LocalWindowSizeClass.current == WindowSizeClass.Compact
            if (isCompact) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(item.chips, key = { it.name }) { chip ->
                        StatChip(name = chip.name, usagePercent = chip.usagePercent, imageUrl = chip.imageUrl)
                    }
                }
            } else {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item.chips.forEach { chip ->
                        StatChip(name = chip.name, usagePercent = chip.usagePercent, imageUrl = chip.imageUrl)
                    }
                }
            }
        }
        is ContentListItem.Section -> {}
        is ContentListItem.FormatSelector -> {}
        is ContentListItem.SearchField -> {}
    }
}

/**
 * Desktop-web single-row Pokemon card used by Home "Top Pokémon", Player "Favorite Pokémon",
 * and Pokemon "Top Teammates". Fills the grid's full-span width, left-aligned, and reflows
 * tile count based on measured width — dropped tiles fade out, revealed tiles fade in.
 * Receives all fetched Pokemon; shows only the first N that fit and hides the rest via
 * AnimatedVisibility so the transition is smooth when the battle detail pane opens/closes.
 */
@Composable
internal fun ResponsivePokemonGridCard(
    pokemon: List<ContentListItem.PokemonGridItem>,
    onPokemonClick: (ContentListItem.PokemonGridItem) -> Unit,
    availableWidth: Dp,
    modifier: Modifier = Modifier
) {
    // Computed from the parent-provided availableWidth (full grid-box width, not the
    // battle grid's cell-packed fullSpan width). Subtract card padding (12.dp × 2).
    val cardInnerPadding = 12.dp
    val innerWidth = (availableWidth - cardInnerPadding * 2).coerceAtLeast(0.dp)
    val fitCount = computeTopPokemonTileCount(innerWidth).coerceAtMost(pokemon.size)
    // Escape the LazyVerticalGrid's FixedSize(650) cell-pack max width: re-measure
    // the child with our larger target constraint, but *report* the grid's original
    // maxWidth so the grid's horizontal arrangement still places us start-aligned at
    // position 0 of the content area. The content draws beyond that reported width
    // into the grid's otherwise-unused trailing space.
    // Note on hit-testing: Compose tests pointer events against the placeable's
    // actual (wider) bounds, so tiles drawn beyond `reportedWidth` still receive
    // clicks. This works today but is an implementation detail — if it ever breaks,
    // the symptom would be that the rightmost tiles become unclickable while still
    // visible.
    val escapeCellWidth = Modifier.layout { measurable, constraints ->
        val targetMaxPx = availableWidth.roundToPx().coerceAtLeast(0)
        val placeable = measurable.measure(
            constraints.copy(minWidth = 0, maxWidth = targetMaxPx)
        )
        val reportedWidth = constraints.maxWidth.coerceAtMost(placeable.width)
        layout(reportedWidth, placeable.height) {
            placeable.place(0, 0)
        }
    }
    Surface(
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.then(escapeCellWidth)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TOP_POKEMON_TILE_SPACING, Alignment.Start),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .padding(cardInnerPadding)
                .clipToBounds()
        ) {
            pokemon.forEachIndexed { index, item ->
                AnimatedVisibility(
                    visible = index < fitCount,
                    enter = fadeIn(animationSpec = tween(DETAIL_PANE_ANIM_DURATION_MS)),
                    exit = fadeOut(animationSpec = tween(DETAIL_PANE_ANIM_DURATION_MS))
                ) {
                    Surface(
                        onClick = { onPokemonClick(item) },
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.width(TOP_POKEMON_TILE_WIDTH)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            FillPokemonAvatar(
                                imageUrl = item.imageUrl,
                                contentDescription = item.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            item.usagePercent?.let { pct ->
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
        }
    }
}

@Composable
internal fun StatChip(name: String, usagePercent: String?, imageUrl: String? = null) {
    Surface(
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
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
                    fontWeight = FontWeight.SemiBold,
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

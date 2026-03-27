package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.model.ContentListHeaderUiModel
import com.arcvgc.app.ui.tokens.AppTokens.FilterChipCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.FilterChipHeight

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SearchFilterChips(
    filters: ContentListHeaderUiModel.SearchFilters,
    searchParams: SearchParams? = null,
    onSearchParamsChanged: ((SearchParams) -> Unit)? = null
) {
    val context = LocalPlatformContext.current
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        filters.formatName?.let { name ->
            Box(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(FilterChipCornerRadius)
                    )
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
        filters.team1Chips.forEach { chip ->
            PokemonFilterChip(
                chip = chip,
                canRemove = searchParams?.canRemovePokemonAt(chip.index) == true,
                onRemove = onSearchParamsChanged?.let { callback ->
                    { callback(searchParams!!.removePokemonAt(chip.index)) }
                },
                context = context
            )
        }
        if (filters.team2Chips.isNotEmpty()) {
            Box(
                modifier = Modifier.height(FilterChipHeight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "vs",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Italic
                )
            }
            filters.team2Chips.forEach { chip ->
                PokemonFilterChip(
                    chip = chip,
                    canRemove = searchParams?.canRemoveTeam2PokemonAt(chip.index) == true,
                    onRemove = onSearchParamsChanged?.let { callback ->
                        { callback(searchParams!!.removeTeam2PokemonAt(chip.index)) }
                    },
                    context = context
                )
            }
        }
        filters.minimumRating?.let { rating ->
            val canRemove = searchParams?.canRemoveMinRating() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(FilterChipCornerRadius)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rating}+",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeMinRating())
                    }
                }
            }
        }
        filters.maximumRating?.let { rating ->
            val canRemove = searchParams?.canRemoveMaxRating() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(FilterChipCornerRadius)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${rating}-",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeMaxRating())
                    }
                }
            }
        }
        if (filters.unratedOnly) {
            val canRemove = searchParams?.canRemoveUnrated() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(FilterChipCornerRadius)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unrated",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeUnrated())
                    }
                }
            }
        }
        filters.playerName?.let { name ->
            val canRemove = searchParams?.canRemovePlayerName() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(FilterChipCornerRadius)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removePlayerName())
                    }
                }
            }
        }
        if (filters.timeRangeStart != null || filters.timeRangeEnd != null) {
            val dateFormat = java.text.SimpleDateFormat("MM/dd/yy", java.util.Locale.getDefault())
            val startStr = filters.timeRangeStart?.let { dateFormat.format(java.util.Date(it * 1000)) } ?: "..."
            val endStr = filters.timeRangeEnd?.let { dateFormat.format(java.util.Date(it * 1000)) } ?: "..."
            val canRemove = searchParams?.canRemoveTimeRange() == true
            Row(
                modifier = Modifier
                    .height(FilterChipHeight)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(FilterChipCornerRadius)
                    )
                    .padding(start = 6.dp, end = if (canRemove) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$startStr \u2013 $endStr",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (canRemove && onSearchParamsChanged != null) {
                    FilterChipCloseButton {
                        onSearchParamsChanged(searchParams.removeTimeRange())
                    }
                }
            }
        }
    }
}

@Composable
private fun PokemonFilterChip(
    chip: com.arcvgc.app.ui.model.PokemonChip,
    canRemove: Boolean,
    onRemove: (() -> Unit)?,
    context: coil3.PlatformContext
) {
    val label = buildString {
        append(chip.name)
        chip.itemName?.let { append(" @ $it") }
    }
    Row(
        modifier = Modifier
            .height(FilterChipHeight)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(FilterChipCornerRadius)
            )
            .padding(start = 4.dp, end = if (canRemove) 0.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        chip.teraTypeImageUrl?.let { url ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Tera type",
                modifier = Modifier.size(27.dp),
                contentScale = ContentScale.Fit
            )
        }
        chip.imageUrl?.let { url ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = chip.name,
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
        if (canRemove && onRemove != null) {
            FilterChipCloseButton(onClick = onRemove)
        }
    }
}

@Composable
internal fun FilterChipCloseButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove filter",
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

package com.arcvgc.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.arcvgc.app.domain.model.SearchFilterRestrictions
import com.arcvgc.app.ui.components.AutoSizeText
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.model.SearchFilterSlotUiModel
import com.arcvgc.app.ui.tokens.AppTokens.SmallFilterButtonCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.SmallFilterButtonFontSize
import com.arcvgc.app.ui.tokens.AppTokens.SmallFilterButtonHorizontalPadding
import com.arcvgc.app.ui.tokens.AppTokens.SmallFilterButtonVerticalPadding

@Composable
fun SearchFilterCard(
    slot: SearchFilterSlotUiModel,
    onRemove: () -> Unit,
    onItemClick: () -> Unit,
    onTeraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalPlatformContext.current

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PokemonAvatar(
                imageUrl = slot.pokemonImageUrl,
                contentDescription = slot.pokemonName,
                circleSize = 40.dp,
                spriteSize = 56.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            AutoSizeText(
                text = slot.pokemonName,
                maxFontSize = 16.sp,
                minFontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (SearchFilterRestrictions.canFilterByItem(slot.pokemonName)) {
                    val item = slot.item
                    val itemImageUrl = item?.imageUrl
                    if (item != null && itemImageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(itemImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onItemClick() },
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        SmallFilterButton(
                            label = item?.name ?: "Item",
                            onClick = onItemClick
                        )
                    }
                }

                if (SearchFilterRestrictions.canFilterByTeraType(slot.pokemonName)) {
                    val teraType = slot.teraType
                    val teraImageUrl = teraType?.imageUrl
                    if (teraType != null && teraImageUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(teraImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = teraType.name,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onTeraClick() },
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        SmallFilterButton(
                            label = teraType?.name ?: "Tera",
                            onClick = onTeraClick
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SmallFilterButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(SmallFilterButtonCornerRadius))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = SmallFilterButtonHorizontalPadding, vertical = SmallFilterButtonVerticalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = SmallFilterButtonFontSize,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

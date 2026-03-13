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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.R
import com.arcvgc.app.domain.model.SearchFilterRestrictions
import com.arcvgc.app.ui.components.AutoSizeText
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.PreviewAsyncImage
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.SearchFilterSlotUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel

@Composable
fun SearchFilterCard(
    slot: SearchFilterSlotUiModel,
    onRemove: () -> Unit,
    onItemClick: () -> Unit,
    onTeraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pokemon image — circle background with pokemon overlaying it
            PokemonAvatar(
                imageUrl = slot.pokemonImageUrl,
                contentDescription = slot.pokemonName,
                circleSize = 40.dp,
                spriteSize = 56.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Pokemon name
            AutoSizeText(
                text = slot.pokemonName,
                maxFontSize = 16.sp,
                minFontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // Item and Tera buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item button / image
                if (SearchFilterRestrictions.canFilterByItem(slot.pokemonName)) {
                    val item = slot.item
                    if (item != null && (isPreview || item.imageUrl != null)) {
                        PreviewAsyncImage(
                            url = item.imageUrl,
                            previewDrawable = R.drawable.preview_item,
                            contentDescription = item.name,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onItemClick() }
                        )
                    } else {
                        SmallFilterButton(
                            label = item?.name ?: "Item",
                            onClick = onItemClick
                        )
                    }
                }

                // Tera button / image
                if (SearchFilterRestrictions.canFilterByTeraType(slot.pokemonName)) {
                    val teraType = slot.teraType
                    if (teraType != null && (isPreview || teraType.imageUrl != null)) {
                        PreviewAsyncImage(
                            url = teraType.imageUrl,
                            previewDrawable = R.drawable.preview_tera,
                            contentDescription = teraType.name,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onTeraClick() }
                        )
                    } else {
                        SmallFilterButton(
                            label = teraType?.name ?: "Tera",
                            onClick = onTeraClick
                        )
                    }
                }

                // Remove button
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
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchFilterCardPreview() {
    MaterialTheme {
        SearchFilterCard(
            slot = SearchFilterSlotUiModel(
                pokemonId = 149,
                pokemonName = "Dragonite",
                pokemonImageUrl = null,
                item = null,
                teraType = null
            ),
            onRemove = {},
            onItemClick = {},
            onTeraClick = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchFilterCardWithFiltersPreview() {
    MaterialTheme {
        SearchFilterCard(
            slot = SearchFilterSlotUiModel(
                pokemonId = 149,
                pokemonName = "Dragonite",
                pokemonImageUrl = null,
                item = ItemUiModel(id = 1, name = "Choice Band", imageUrl = null),
                teraType = TeraTypeUiModel(id = 1, name = "Normal", imageUrl = null)
            ),
            onRemove = {},
            onItemClick = {},
            onTeraClick = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}


package com.arcvgc.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.preview_item
import com.arcvgc.app.shared.preview_tera
import com.arcvgc.app.domain.model.SearchFilterRestrictions
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.components.AutoSizeText
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.PreviewAsyncImage
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.SearchFilterSlotUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
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
    onAbilityClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    selectedFormat: FormatUiModel? = null
) {
    val isPreview = LocalInspectionMode.current
    val isMobile = LocalWindowSizeClass.current == WindowSizeClass.Compact
    val showSetFilters = selectedFormat?.isOpenTeamsheet != false

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
                .padding(if (isMobile && compact) 8.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isMobile) {
                PokemonAvatar(
                    imageUrl = slot.pokemonImageUrl,
                    contentDescription = slot.pokemonName,
                    circleSize = 32.dp,
                    spriteSize = 44.dp
                )
                if (!compact) {
                    Spacer(modifier = Modifier.width(8.dp))
                    AutoSizeText(
                        text = slot.pokemonName,
                        maxFontSize = 16.sp,
                        minFontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (showSetFilters) {
                    FilterBadges(slot, isPreview)
                }
                if (compact) {
                    Spacer(modifier = Modifier.weight(1f))
                }
                if (showSetFilters) {
                    CompactFilterMenu(
                        slot = slot,
                        onItemClick = onItemClick,
                        onTeraClick = onTeraClick,
                        onAbilityClick = onAbilityClick
                    )
                }
            } else {
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
                if (showSetFilters) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InlineItemButton(slot, isPreview, onItemClick)
                        InlineTeraButton(slot, isPreview, onTeraClick)
                        InlineAbilityButton(slot, onAbilityClick)
                    }
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

@Composable
private fun FilterBadges(
    slot: SearchFilterSlotUiModel,
    isPreview: Boolean
) {
    val itemIcon = slot.item?.let { if (isPreview || it.imageUrl != null) it else null }
    val teraIcon = slot.teraType?.let { if (isPreview || it.imageUrl != null) it else null }
    val hasAbility = slot.ability != null

    val badgeCount = listOfNotNull(itemIcon, teraIcon).size + (if (hasAbility) 1 else 0)
    if (badgeCount == 0) return

    val badgeSize = 24.dp
    val badgeOverlap = 8.dp
    val badgesWidth = badgeSize + (badgeSize - badgeOverlap) * (badgeCount - 1)
    val borderWidth = 1.dp
    val borderColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier.size(width = badgesWidth, height = badgeSize),
        contentAlignment = Alignment.CenterStart
    ) {
        var offsetX = 0.dp
        if (itemIcon != null) {
            FilterBadge(
                url = itemIcon.imageUrl,
                previewDrawable = Res.drawable.preview_item,
                contentDescription = itemIcon.name,
                badgeSize = badgeSize,
                borderWidth = borderWidth,
                borderColor = borderColor,
                modifier = Modifier.offset(x = offsetX)
            )
            offsetX += badgeSize - badgeOverlap
        }
        if (teraIcon != null) {
            FilterBadge(
                url = teraIcon.imageUrl,
                previewDrawable = Res.drawable.preview_tera,
                contentDescription = teraIcon.name,
                badgeSize = badgeSize,
                borderWidth = borderWidth,
                borderColor = borderColor,
                modifier = Modifier.offset(x = offsetX)
            )
            offsetX += badgeSize - badgeOverlap
        }
        if (hasAbility) {
            AbilityBadge(
                initials = abilityInitials(slot.ability!!.name),
                badgeSize = badgeSize,
                borderWidth = borderWidth,
                borderColor = borderColor,
                modifier = Modifier.offset(x = offsetX)
            )
        }
    }
}

@Composable
private fun FilterBadge(
    url: String?,
    previewDrawable: org.jetbrains.compose.resources.DrawableResource,
    contentDescription: String,
    badgeSize: Dp,
    borderWidth: Dp,
    borderColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(RoundedCornerShape(badgeSize / 2))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(borderWidth, borderColor, RoundedCornerShape(badgeSize / 2)),
        contentAlignment = Alignment.Center
    ) {
        PreviewAsyncImage(
            url = url,
            previewDrawable = previewDrawable,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun AbilityBadge(
    initials: String,
    badgeSize: Dp,
    borderWidth: Dp,
    borderColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(badgeSize)
            .clip(RoundedCornerShape(badgeSize / 2))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(borderWidth, borderColor, RoundedCornerShape(badgeSize / 2)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactFilterMenu(
    slot: SearchFilterSlotUiModel,
    onItemClick: () -> Unit,
    onTeraClick: () -> Unit,
    onAbilityClick: () -> Unit
) {
    val canItem = SearchFilterRestrictions.canFilterByItem(slot.pokemonName)
    val canTera = SearchFilterRestrictions.canFilterByTeraType(slot.pokemonName)

    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Filter options",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (canItem) {
                val itemLabel = slot.item?.name?.let { "Item: $it" } ?: "Item"
                DropdownMenuItem(
                    text = { Text(itemLabel) },
                    onClick = { expanded = false; onItemClick() }
                )
            }
            if (canTera) {
                val teraLabel = slot.teraType?.name?.let { "Tera: $it" } ?: "Tera"
                DropdownMenuItem(
                    text = { Text(teraLabel) },
                    onClick = { expanded = false; onTeraClick() }
                )
            }
            val abilityLabel = slot.ability?.name?.let { "Ability: $it" } ?: "Ability"
            DropdownMenuItem(
                text = { Text(abilityLabel) },
                onClick = { expanded = false; onAbilityClick() }
            )
        }
    }
}

@Composable
private fun InlineItemButton(
    slot: SearchFilterSlotUiModel,
    isPreview: Boolean,
    onItemClick: () -> Unit
) {
    if (!SearchFilterRestrictions.canFilterByItem(slot.pokemonName)) return
    val item = slot.item
    val itemImageUrl = item?.imageUrl
    if (item != null && (itemImageUrl != null || isPreview)) {
        PreviewAsyncImage(
            url = itemImageUrl,
            previewDrawable = Res.drawable.preview_item,
            contentDescription = item.name,
            modifier = Modifier.size(32.dp).clickable { onItemClick() }
        )
    } else {
        SmallFilterButton(label = item?.name ?: "Item", onClick = onItemClick)
    }
}

@Composable
private fun InlineTeraButton(
    slot: SearchFilterSlotUiModel,
    isPreview: Boolean,
    onTeraClick: () -> Unit
) {
    if (!SearchFilterRestrictions.canFilterByTeraType(slot.pokemonName)) return
    val teraType = slot.teraType
    val teraImageUrl = teraType?.imageUrl
    if (teraType != null && (teraImageUrl != null || isPreview)) {
        PreviewAsyncImage(
            url = teraImageUrl,
            previewDrawable = Res.drawable.preview_tera,
            contentDescription = teraType.name,
            modifier = Modifier.size(32.dp).clickable { onTeraClick() }
        )
    } else {
        SmallFilterButton(label = teraType?.name ?: "Tera", onClick = onTeraClick)
    }
}

@Composable
private fun InlineAbilityButton(
    slot: SearchFilterSlotUiModel,
    onAbilityClick: () -> Unit
) {
    val ability = slot.ability
    if (ability != null) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .clickable { onAbilityClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = abilityInitials(ability.name),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        SmallFilterButton(label = "Ability", onClick = onAbilityClick)
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
            onAbilityClick = {},
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
            onAbilityClick = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchFilterCardCompactPreview() {
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
            onAbilityClick = {},
            compact = true,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchFilterCardClosedTeamsheetPreview() {
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
            onAbilityClick = {},
            modifier = Modifier.padding(8.dp),
            selectedFormat = FormatUiModel(
                id = 1,
                displayName = "Reg G (Closed Sheets)",
                isOpenTeamsheet = false
            )
        )
    }
}

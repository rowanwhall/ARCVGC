package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.OrderBy
import com.arcvgc.app.ui.components.InfoButton
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.FilterChipCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.InfoButtonSize
import com.arcvgc.app.ui.tokens.AppTokens.SearchButtonCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

data class PokemonNavTarget(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val typeImageUrls: List<String> = emptyList(),
    val formatId: Int? = null,
    val lookback: LookbackWindow? = null
)

data class PlayerNavTarget(val id: Int, val name: String, val formatId: Int? = null)

const val PAGINATION_THRESHOLD = 5

fun List<ContentListItem>.findBattle(battleId: Int): ContentListItem.Battle? {
    for (item in this) {
        if (item is ContentListItem.Battle && item.uiModel.id == battleId) return item
        if (item is ContentListItem.Section) {
            val found = item.items.findBattle(battleId)
            if (found != null) return found
        }
    }
    return null
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    sortOrder: OrderBy? = null,
    onToggleSortOrder: (() -> Unit)? = null,
    centerTitle: Boolean = false,
    onInfoClick: (() -> Unit)? = null
) {
    val hasTrailing = sortOrder != null && onToggleSortOrder != null
    val isCentered = centerTitle && !hasTrailing
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCentered) Arrangement.Center else Arrangement.Start
    ) {
        // When the title is centered and carries an info button, balance the
        // trailing button with an equal leading gutter so the title text itself
        // stays centered rather than the title+button group.
        if (isCentered && onInfoClick != null) {
            Spacer(modifier = Modifier.size(InfoButtonSize))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (onInfoClick != null) {
            InfoButton(onClick = onInfoClick)
        }
        if (sortOrder != null && onToggleSortOrder != null) {
            Spacer(modifier = Modifier.weight(1f))
            SortToggleButton(sortOrder = sortOrder, isLoading = isLoading, onClick = onToggleSortOrder)
        }
    }
}

@Composable
fun SortToggleButton(sortOrder: OrderBy, isLoading: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .border(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(FilterChipCornerRadius))
            .then(if (isLoading) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = "Sort",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (sortOrder == OrderBy.Rating) "Rating" else "Time",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlayerListRow(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LookbackSegmentedSelector(
    selectedLookback: LookbackWindow,
    onLookbackSelected: (LookbackWindow) -> Unit,
    modifier: Modifier = Modifier,
    options: List<LookbackWindow> = LookbackWindow.entries,
    onInfoClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading gutter mirroring the trailing info button so the segments stay
        // centered relative to the rest of the page.
        if (onInfoClick != null) {
            Spacer(modifier = Modifier.size(InfoButtonSize))
        }
        options.forEach { window ->
            val isSelected = window == selectedLookback
            val accentColor = MaterialTheme.colorScheme.primary
            val borderColor = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
            val borderWidth = if (isSelected) StandardBorderWidth * 2 else StandardBorderWidth
            val textColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .border(borderWidth, borderColor, RoundedCornerShape(SearchButtonCornerRadius))
                    .clickable { onLookbackSelected(window) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = window.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
        if (onInfoClick != null) {
            InfoButton(onClick = onInfoClick)
        }
    }
}

@Composable
fun FormatDropdown(
    formats: List<FormatUiModel>,
    selectedFormatId: Int,
    onFormatSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    fillMaxWidth: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedFormat = formats.find { it.id == selectedFormatId }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .then(if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                .clickable { expanded = true }
                .border(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(SearchButtonCornerRadius))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = selectedFormat?.displayName ?: "Format",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = if (fillMaxWidth) Modifier.weight(1f) else Modifier
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select format",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            formats.forEach { format ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = format.displayName,
                            fontWeight = if (format.id == selectedFormatId) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onFormatSelected(format.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

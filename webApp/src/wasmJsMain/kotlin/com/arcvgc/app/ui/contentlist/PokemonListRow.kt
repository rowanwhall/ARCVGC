package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.TypeIconRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.ui.model.TypeUiModel
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

// SectionHeader, SortToggleButton, PlayerListRow, FormatDropdown,
// PokemonNavTarget, PlayerNavTarget, PAGINATION_THRESHOLD, findBattle()
// are in shared module: com.arcvgc.app.ui.contentlist.ContentListComponents

internal val DETAIL_PANEL_MAX_WIDTH = 960.dp

@Composable
internal fun PokemonListRow(
    name: String,
    imageUrl: String?,
    types: List<TypeUiModel>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    usagePercent: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PokemonAvatar(
                imageUrl = imageUrl,
                contentDescription = name,
                circleSize = 40.dp,
                spriteSize = 56.dp
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (usagePercent != null) {
                    Text(
                        text = usagePercent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            TypeIconRow(
                types = types.map { TypeInfo(it.name, it.imageUrl) }
            )
        }
    }
}

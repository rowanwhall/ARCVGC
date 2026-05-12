package com.arcvgc.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

/**
 * `compact = true` (mobile/compact web): name above rating, matching the
 * stacked StatChip layout used by sibling Top* sections.
 *
 * `compact = false` (desktop web): inline `name • rating` to make better
 * use of the wider horizontal space.
 */
@Composable
fun TopPlayerChip(
    name: String,
    maxRating: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(StandardBorderWidth, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        if (compact) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (maxRating != null) {
                    Text(
                        text = maxRating.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        } else {
            // Vertical padding bumped from 8 → 14 so the inline chip's overall
            // height matches the two-line StatChip used by sibling Top* sections.
            // Without this the chip is ~12dp shorter, which makes the section's
            // surrounding 12dp grid + header spacing look disproportionately large.
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (maxRating != null) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = maxRating.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

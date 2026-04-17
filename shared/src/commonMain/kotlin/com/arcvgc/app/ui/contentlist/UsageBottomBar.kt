package com.arcvgc.app.ui.contentlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.tokens.AppTokens.CardCornerRadius
import com.arcvgc.app.ui.tokens.AppTokens.StandardBorderWidth

/**
 * Approximate vertical space (including the bar's outer vertical margin) reserved
 * at the bottom of the list so scrollable content doesn't tuck underneath the
 * anchored [UsageBottomBar]. Kept as a rough upper bound — measuring the bar at
 * runtime would be more precise but adds layout complexity for little visible gain.
 */
val UsageBottomBarReservedHeight = 160.dp

@Composable
fun UsageBottomBar(
    formats: List<FormatUiModel>,
    selectedFormatId: Int,
    onFormatSelected: (Int) -> Unit,
    isLoadingFormat: Boolean,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(
            thickness = StandardBorderWidth,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            FormatDropdown(
                formats = formats,
                selectedFormatId = selectedFormatId,
                onFormatSelected = onFormatSelected
            )
            if (isLoadingFormat) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 8.dp).size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            label = { Text("Search Pok\u00E9mon") },
            singleLine = true,
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            } else null,
            shape = RoundedCornerShape(CardCornerRadius),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )
        }
    }
}

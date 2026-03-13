package com.arcvgc.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val sortOptions = listOf("rating" to "Rating", "time" to "Time")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOrderPickerSheet(
    selectedOrderBy: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = "Sort Order",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        sortOptions.forEach { (value, label) ->
            ListItem(
                headlineContent = { Text(label) },
                leadingContent = {
                    RadioButton(
                        selected = selectedOrderBy == value,
                        onClick = { onSelect(value) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(value) }
            )
        }
    }
}

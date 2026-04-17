package com.arcvgc.app.ui.contentlist

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.arcvgc.app.ui.model.FormatUiModel

// UsageBottomBar composable lives in the shared module at
// com.arcvgc.app.ui.contentlist.UsageBottomBar — this file hosts only the
// Android @Preview wrapper per the shared-compose convention.

@Preview(showBackground = true)
@Composable
private fun UsageBottomBarPreview() {
    var query by remember { mutableStateOf("") }
    Surface(color = MaterialTheme.colorScheme.surface) {
        UsageBottomBar(
            formats = listOf(
                FormatUiModel(id = 1, displayName = "[Gen 9] VGC 2026 Reg A"),
                FormatUiModel(id = 2, displayName = "[Gen 9] VGC 2026 Reg M-A (Bo3)")
            ),
            selectedFormatId = 2,
            onFormatSelected = {},
            isLoadingFormat = false,
            searchQuery = query,
            onSearchQueryChanged = { query = it }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UsageBottomBarLoadingPreview() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        UsageBottomBar(
            formats = listOf(FormatUiModel(id = 1, displayName = "[Gen 9] VGC 2026 Reg A")),
            selectedFormatId = 1,
            onFormatSelected = {},
            isLoadingFormat = true,
            searchQuery = "Inc",
            onSearchQueryChanged = {}
        )
    }
}

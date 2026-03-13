package com.arcvgc.app.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val scrollbarStyle: ScrollbarStyle
    @Composable get() = ScrollbarStyle(
        minimalHeight = 48.dp,
        thickness = 8.dp,
        shape = RoundedCornerShape(4.dp),
        hoverDurationMillis = 300,
        unhoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        hoverColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )

@Composable
fun ThemedVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(listState),
        modifier = modifier,
        style = scrollbarStyle
    )
}

@Composable
fun ThemedVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        modifier = modifier,
        style = scrollbarStyle
    )
}

package com.arcvgc.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val GradientToolbarHeight = 72.dp

/**
 * Toolbar overlay only — renders a transparent TopAppBar with a vertical gradient background.
 * Use this when the toolbar floats over content managed by the caller (e.g., ContentListPage).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientToolbar(
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                        )
                    )
                )
        )
        TopAppBar(
            title = {},
            navigationIcon = navigationIcon,
            actions = actions,
            windowInsets = WindowInsets(0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

/**
 * Full scaffold — wraps content with a solid background and a gradient toolbar overlay.
 * Use this when the scaffold owns the content (e.g., BattleDetailScreen).
 */
@Composable
fun GradientToolbarScaffold(
    navigationIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    statusBarPadding: Dp = 0.dp,
    content: @Composable (topPadding: Dp) -> Unit
) {
    val topPadding = GradientToolbarHeight + statusBarPadding

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        content(topPadding)

        GradientToolbar(
            navigationIcon = navigationIcon,
            actions = actions
        )
    }
}

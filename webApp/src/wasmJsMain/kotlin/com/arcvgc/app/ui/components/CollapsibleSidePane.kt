package com.arcvgc.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.contentlist.DETAIL_PANE_ANIM_DURATION_MS

// Width of the 1dp VerticalDivider callers include at the pane's trailing edge.
// Added to the content width when computing [CollapsibleSidePane]'s paneWidth so
// the divider collapses along with the pane.
internal val PaneDividerWidth = 1.dp

/**
 * A left side pane that slides offscreen-left to make room for other content.
 *
 * The pane's layout width animates from [paneWidth] to zero while the content
 * stays anchored to the pane's trailing edge (via unbounded end-aligned
 * measurement) and is clipped, so it appears to translate offscreen as the
 * neighboring content grows into the reclaimed space in the same frames.
 *
 * Animation timing matches the battle detail pane
 * ([DETAIL_PANE_ANIM_DURATION_MS] + FastOutSlowInEasing) so a side-pane exit
 * and a detail-pane entrance move in lockstep. When [animate] is false
 * (the "Enable Animations" setting is off), the width snaps instead.
 *
 * The content stays composed at zero width (clipped, not removed), so state
 * inside the pane — e.g. a LazyColumn's scroll position — survives a
 * collapse/expand cycle.
 */
@Composable
internal fun CollapsibleSidePane(
    visible: Boolean,
    paneWidth: Dp,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    content: @Composable () -> Unit
) {
    val targetWidth = if (visible) paneWidth else 0.dp
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(
            durationMillis = if (animate) DETAIL_PANE_ANIM_DURATION_MS else 0,
            easing = FastOutSlowInEasing
        )
    )
    Box(
        modifier = modifier
            .width(animatedWidth)
            .fillMaxHeight()
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth(align = Alignment.End, unbounded = true)
                .width(paneWidth)
                .fillMaxHeight()
        ) {
            content()
        }
    }
}

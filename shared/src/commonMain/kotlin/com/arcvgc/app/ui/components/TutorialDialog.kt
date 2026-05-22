package com.arcvgc.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arcvgc.app.ui.tutorial.TutorialConfig
import com.arcvgc.app.ui.tutorial.TutorialContentProvider
import com.arcvgc.app.ui.tutorial.TutorialStep
import com.arcvgc.app.ui.tutorial.tutorialImageResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

private val TutorialDialogMaxWidth: Dp = 720.dp
private val TutorialDialogMaxImageHeight: Dp = 360.dp
// Non-pager height: top padding + dots row + spacers + bottom padding.
private val TutorialDialogChromeHeight: Dp = 84.dp

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    steps: List<TutorialStep> = TutorialConfig.steps,
    showArrows: Boolean = false
) {
    if (steps.isEmpty()) return

    val painters: List<Painter?> = steps.map { step ->
        tutorialImageResource(step.imageId)?.let { painterResource(it) }
    }
    // Largest image (by pixel area) drives the image-slot height so the layout
    // stays stable across swipes instead of jittering per page.
    val largestPainter = painters.filterNotNull().maxByOrNull {
        it.intrinsicSize.width * it.intrinsicSize.height
    }
    val largestAspectRatio = largestPainter?.let {
        it.intrinsicSize.width / it.intrinsicSize.height
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val targetDialogWidth = (maxWidth * 0.92f).coerceAtMost(TutorialDialogMaxWidth)
            // Upper bound for the pager so the dialog never exceeds the screen; the
            // actual height is measured from content (see AdaptiveHeightPager).
            val maxPagerHeight = (maxHeight * 0.88f - TutorialDialogChromeHeight)
                .coerceAtLeast(200.dp)
            val imageSlotHeight = if (largestAspectRatio != null) {
                (targetDialogWidth / largestAspectRatio)
                    .coerceAtMost(TutorialDialogMaxImageHeight)
                    .coerceAtMost(maxPagerHeight * 0.5f)
            } else {
                0.dp
            }

            Surface(
                shape = MaterialTheme.shapes.large,
                modifier = modifier
                    .widthIn(max = targetDialogWidth)
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                val pagerState = rememberPagerState(pageCount = { steps.size })
                val scope = rememberCoroutineScope()

                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp)) {
                        AdaptiveHeightPager(
                            steps = steps,
                            painters = painters,
                            imageSlotHeight = imageSlotHeight,
                            pagerState = pagerState,
                            maxPagerHeight = maxPagerHeight,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (showArrows) {
                                val canGoBack = pagerState.currentPage > 0
                                IconButton(
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                    },
                                    enabled = canGoBack
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous"
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }

                            PagerDots(
                                count = steps.size,
                                currentPage = pagerState.currentPage,
                                modifier = Modifier.weight(1f)
                            )

                            if (showArrows) {
                                val canGoForward = pagerState.currentPage < steps.size - 1
                                IconButton(
                                    onClick = {
                                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                    },
                                    enabled = canGoForward
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Next"
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sizes the pager to the tallest page's natural content height (capped at
 * [maxPagerHeight]) so there's no wasted empty space and no cutoff. Every page is
 * laid out at that uniform height, so shorter pages center their content and the
 * dialog doesn't resize while swiping. Content taller than the cap scrolls.
 */
@Composable
private fun AdaptiveHeightPager(
    steps: List<TutorialStep>,
    painters: List<Painter?>,
    imageSlotHeight: Dp,
    pagerState: PagerState,
    maxPagerHeight: Dp,
    modifier: Modifier = Modifier
) {
    SubcomposeLayout(modifier) { constraints ->
        val measureConstraints = Constraints(maxWidth = constraints.maxWidth)
        val tallestContentPx = steps.indices.maxOfOrNull { index ->
            subcompose("measure_$index") {
                TutorialPageContent(steps[index], painters[index], imageSlotHeight)
            }.firstOrNull()?.measure(measureConstraints)?.height ?: 0
        } ?: 0

        val pagerHeightPx = tallestContentPx.coerceAtMost(maxPagerHeight.roundToPx())

        val pagerPlaceable = subcompose("pager") {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(pagerHeightPx.toDp())
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TutorialPageContent(steps[page], painters[page], imageSlotHeight)
                }
            }
        }.first().measure(constraints.copy(minHeight = pagerHeightPx, maxHeight = pagerHeightPx))

        layout(pagerPlaceable.width, pagerPlaceable.height) {
            pagerPlaceable.place(0, 0)
        }
    }
}

@Composable
private fun TutorialPageContent(
    step: TutorialStep,
    painter: Painter?,
    imageSlotHeight: Dp
) {
    val strings = TutorialContentProvider.resolve(step.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (painter != null && imageSlotHeight > 0.dp) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageSlotHeight)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = strings?.title.orEmpty(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = strings?.body.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PagerDots(
    count: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val selected = index == currentPage
            val color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (selected) 9.dp else 7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

package com.arcvgc.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.tokens.AppTokens.HeroLogoHeight
import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.loading_frame_0
import com.arcvgc.app.shared.loading_frame_1
import com.arcvgc.app.shared.loading_frame_2
import com.arcvgc.app.shared.loading_frame_3
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

private val frames = listOf(
    Res.drawable.loading_frame_0,
    Res.drawable.loading_frame_1,
    Res.drawable.loading_frame_2,
    Res.drawable.loading_frame_3
)

private val durations = listOf(130L, 100L, 130L, 100L)

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    var frameIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(durations[frameIndex])
            frameIndex = (frameIndex + 1) % frames.size
        }
    }

    Image(
        painter = painterResource(frames[frameIndex]),
        contentDescription = "Loading",
        modifier = modifier.height(HeroLogoHeight)
    )
}

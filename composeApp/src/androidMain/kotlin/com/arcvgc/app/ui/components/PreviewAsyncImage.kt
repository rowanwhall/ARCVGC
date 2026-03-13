package com.arcvgc.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * Renders a local drawable in Compose preview mode, or an [AsyncImage] at runtime.
 *
 * In preview mode the [previewDrawable] is always shown (regardless of [url]).
 * At runtime, the network image is loaded when [url] is non-null; nothing is
 * rendered when [url] is null.
 */
@Composable
fun PreviewAsyncImage(
    url: String?,
    @DrawableRes previewDrawable: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(previewDrawable),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else if (url != null) {
        val context = LocalPlatformContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

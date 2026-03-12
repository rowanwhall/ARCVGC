package com.example.showdown26.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

data class TypeInfo(
    val name: String,
    val imageUrl: String?
)

@Composable
fun TypeIconRow(
    types: List<TypeInfo>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp
) {
    val context = LocalPlatformContext.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        types.forEach { type ->
            type.imageUrl?.let { typeImageUrl ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(typeImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = type.name,
                    modifier = Modifier.size(iconSize),
                    contentScale = ContentScale.Fit
                )
            } ?: Spacer(modifier = Modifier.size(iconSize))
        }
    }
}

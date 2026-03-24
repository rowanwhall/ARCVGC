package com.arcvgc.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.preview_type_1
import com.arcvgc.app.shared.preview_type_2

data class TypeInfo(
    val name: String,
    val imageUrl: String?
)

private val previewResources = listOf(Res.drawable.preview_type_1, Res.drawable.preview_type_2)

@Composable
fun TypeIconRow(
    types: List<TypeInfo>,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        types.forEachIndexed { index, type ->
            PreviewAsyncImage(
                url = type.imageUrl,
                previewDrawable = previewResources.getOrElse(index) { previewResources[0] },
                contentDescription = type.name,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

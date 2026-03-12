package com.example.showdown26.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.showdown26.R

data class TypeInfo(
    val name: String,
    val imageUrl: String?
)

private val previewResources = listOf(R.drawable.preview_type_1, R.drawable.preview_type_2)

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

@Preview(showBackground = true)
@Composable
private fun TypeIconRowPreview() {
    MaterialTheme {
        TypeIconRow(
            types = listOf(
                TypeInfo("Dragon", "https://example.com/dragon.png"),
                TypeInfo("Flying", "https://example.com/flying.png")
            ),
            modifier = Modifier.padding(8.dp)
        )
    }
}

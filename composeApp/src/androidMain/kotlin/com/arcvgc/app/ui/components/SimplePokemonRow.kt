package com.arcvgc.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SimplePokemonRow(
    imageUrl: String?,
    name: String,
    types: List<TypeInfo>,
    circleSize: Dp,
    spriteSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PokemonAvatar(
            imageUrl = imageUrl,
            contentDescription = name,
            circleSize = circleSize,
            spriteSize = spriteSize
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = fontWeight,
            modifier = Modifier.weight(1f)
        )

        TypeIconRow(types = types)
    }
}

@Preview(showBackground = true)
@Composable
private fun SimplePokemonRowPreview() {
    MaterialTheme {
        SimplePokemonRow(
            imageUrl = null,
            name = "Dragonite",
            types = listOf(
                TypeInfo("Dragon", null),
                TypeInfo("Flying", null)
            ),
            circleSize = 46.dp,
            spriteSize = 64.dp,
            onClick = {},
            modifier = Modifier.padding(8.dp)
        )
    }
}

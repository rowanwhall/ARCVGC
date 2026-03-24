package com.arcvgc.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Previews for shared module composables in com.arcvgc.app.ui.components

@Preview(showBackground = true)
@Composable
private fun VsDividerPreview() {
    MaterialTheme {
        VsDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    MaterialTheme {
        EmptyView()
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorViewPreview() {
    MaterialTheme {
        ErrorView(onRetry = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoButtonPreview() {
    MaterialTheme {
        InfoButton(onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoSizeTextPreview() {
    MaterialTheme {
        AutoSizeText(
            text = "This Is A Long Pok\u00E9mon Name",
            maxFontSize = 16.sp,
            minFontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonAvatarPreview() {
    MaterialTheme {
        PokemonAvatar(
            imageUrl = null,
            contentDescription = "Dragonite",
            circleSize = 100.dp,
            spriteSize = 144.dp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FillPokemonAvatarPreview() {
    MaterialTheme {
        FillPokemonAvatar(
            imageUrl = null,
            contentDescription = "Dragonite",
            modifier = Modifier.size(100.dp)
        )
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

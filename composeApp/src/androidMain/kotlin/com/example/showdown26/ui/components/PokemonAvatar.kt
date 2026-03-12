package com.example.showdown26.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.showdown26.R

@Composable
fun PokemonAvatar(
    imageUrl: String?,
    contentDescription: String?,
    circleSize: Dp,
    spriteSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(spriteSize),
        contentAlignment = Alignment.Center
    ) {
        PokeballCircle(modifier = Modifier.size(circleSize))
        PreviewAsyncImage(
            url = imageUrl,
            previewDrawable = R.drawable.preview_pokemon,
            contentDescription = contentDescription,
            modifier = Modifier.size(spriteSize)
        )
    }
}

@Composable
fun FillPokemonAvatar(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    circleFraction: Float = 0.7f
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        PokeballCircle(modifier = Modifier.fillMaxSize(circleFraction))
        PreviewAsyncImage(
            url = imageUrl,
            previewDrawable = R.drawable.preview_pokemon,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PokeballCircle(modifier: Modifier = Modifier) {
    val themeColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.04f
        val inset = strokeWidth / 2
        val drawSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val center = Offset(size.width / 2, size.height / 2)
        val radius = drawSize.minDimension / 2
        val topLeft = Offset(inset, inset)

        // Top half (theme color)
        drawArc(
            color = themeColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = topLeft,
            size = drawSize
        )

        // Bottom half (white)
        drawArc(
            color = Color.White,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = topLeft,
            size = drawSize
        )

        // Horizontal line
        drawLine(
            color = Color.Black,
            start = Offset(inset, center.y),
            end = Offset(size.width - inset, center.y),
            strokeWidth = strokeWidth
        )

        // Circle outline
        drawCircle(
            color = Color.Black,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Center button
        val buttonRadius = radius * 0.24f
        drawCircle(color = Color.White, radius = buttonRadius, center = center)
        drawCircle(
            color = Color.Black,
            radius = buttonRadius,
            center = center,
            style = Stroke(width = strokeWidth)
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

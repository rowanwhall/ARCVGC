package com.arcvgc.app.ui.battledetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.arcvgc.app.shared.Res
import com.arcvgc.app.shared.preview_item
import com.arcvgc.app.shared.preview_tera
import com.arcvgc.app.ui.components.PokemonAvatar
import com.arcvgc.app.ui.components.PreviewAsyncImage
import com.arcvgc.app.ui.components.TypeIconRow
import com.arcvgc.app.ui.components.TypeInfo
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonDetailUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.TypeUiModel
import com.arcvgc.app.ui.tokens.AppTokens.BulletSeparator
import com.arcvgc.app.ui.tokens.AppTokens.MoveChipCornerRadius

@Composable
fun PokemonDetailCard(
    pokemon: PokemonDetailUiModel,
    modifier: Modifier = Modifier,
    onPokemonClick: ((Int, String, String?, List<String>) -> Unit)? = null
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        onClick = if (onPokemonClick != null) {
            { onPokemonClick(pokemon.id, pokemon.name, pokemon.imageUrl, pokemon.types.mapNotNull { it.imageUrl }) }
        } else {
            {}
        }
    ) {
        val context = LocalPlatformContext.current
        val isPreview = LocalInspectionMode.current

        Box(modifier = Modifier.fillMaxWidth()) {
            // Main centered content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image area — pokemon overflows the circle background
                // Layer order: pokemon circle → pokemon image → item circle → item image
                Box(
                    modifier = Modifier.size(144.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PokemonAvatar(
                        imageUrl = pokemon.imageUrl,
                        contentDescription = pokemon.name,
                        circleSize = 100.dp,
                        spriteSize = 144.dp
                    )

                    // Item circle background + image — over pokemon image
                    if (pokemon.item != null && isPreview) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Image(
                                painter = painterResource(Res.drawable.preview_item),
                                contentDescription = pokemon.item?.name,
                                modifier = Modifier.size(28.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        pokemon.item?.imageUrl?.let { itemImageUrl ->
                            var itemImageLoaded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (itemImageLoaded) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                }
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(itemImageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = pokemon.item?.name,
                                    modifier = Modifier.size(28.dp),
                                    contentScale = ContentScale.Fit,
                                    onSuccess = { itemImageLoaded = true }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Pokemon name
                Text(
                    text = pokemon.name,
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Ability · Item
                if (pokemon.abilityName != null || pokemon.item != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        pokemon.abilityName?.let { ability ->
                            Text(
                                text = ability,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            )
                        }
                        pokemon.item?.let { item ->
                            if (pokemon.abilityName != null) {
                                Text(
                                    text = " $BulletSeparator ",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                )
                            }
                            Text(
                                text = item.name,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            )
                        }
                    }
                }

                // Moves — full-width rows with centered text
                val moves = pokemon.moves.take(4)
                if (moves.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            moves.getOrNull(0)?.let {
                                MoveChip(moveName = it, modifier = Modifier.weight(1f))
                            }
                            moves.getOrNull(1)?.let {
                                MoveChip(moveName = it, modifier = Modifier.weight(1f))
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            moves.getOrNull(2)?.let {
                                MoveChip(moveName = it, modifier = Modifier.weight(1f))
                            }
                            moves.getOrNull(3)?.let {
                                MoveChip(moveName = it, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Type icons — top start
            TypeIconRow(
                types = pokemon.types.map { TypeInfo(it.name, it.imageUrl) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )

            // Tera type icon — top end
            if (pokemon.teraType != null) {
                PreviewAsyncImage(
                    url = pokemon.teraType?.imageUrl,
                    previewDrawable = Res.drawable.preview_tera,
                    contentDescription = pokemon.teraType?.name,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun MoveChip(moveName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(MoveChipCornerRadius)
            )
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = moveName,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonDetailCardPreview() {
    MaterialTheme {
        PokemonDetailCard(
            pokemon = PokemonDetailUiModel(
                id = 149,
                name = "Dragonite",
                imageUrl = null,
                item = ItemUiModel(id = 1, name = "Choice Band", imageUrl = null),
                abilityName = "Multiscale",
                moves = listOf("Dragon Claw", "Extreme Speed", "Earthquake", "Ice Punch"),
                types = listOf(
                    TypeUiModel("Dragon", null),
                    TypeUiModel("Flying", null)
                ),
                teraType = TeraTypeUiModel(id = 1, name = "Normal", imageUrl = null)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PokemonDetailCardClosedTeamsheetPreview() {
    MaterialTheme {
        PokemonDetailCard(
            pokemon = PokemonDetailUiModel(
                id = 149,
                name = "Dragonite",
                imageUrl = null,
                item = null,
                abilityName = null,
                moves = emptyList(),
                types = listOf(
                    TypeUiModel("Dragon", null),
                    TypeUiModel("Flying", null)
                ),
                teraType = null
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoveChipPreview() {
    MaterialTheme {
        MoveChip(moveName = "Extreme Speed")
    }
}

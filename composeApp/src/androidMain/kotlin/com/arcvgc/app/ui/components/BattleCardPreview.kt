package com.arcvgc.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PlayerUiModel
import com.arcvgc.app.ui.model.PokemonSlotUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel

// BattleCard composable is in shared module: com.arcvgc.app.ui.components.BattleCard

@Preview(showBackground = true)
@Composable
private fun BattleCardPreview() {
    val samplePokemonSlot = PokemonSlotUiModel(
        name = "Flutter Mane",
        imageUrl = null,
        item = ItemUiModel(
            id = 0,
            name = "Booster Energy",
            imageUrl = null
        ),
        teraType = TeraTypeUiModel(
            id = 1,
            name = "Normal",
            imageUrl = null
        )
    )

    val samplePlayer1 = PlayerUiModel(
        name = "Player1",
        isWinner = true,
        team = List(6) { samplePokemonSlot }
    )

    val samplePlayer2 = PlayerUiModel(
        name = "Opponent",
        isWinner = false,
        team = List(6) { samplePokemonSlot }
    )

    val sampleUiModel = BattleCardUiModel(
        id = 1,
        player1 = samplePlayer1,
        player2 = samplePlayer2,
        formatName = "VGC 2026 Reg H",
        rating = "1542",
        formattedTime = "Feb 8, 5:03 PM"
    )

    MaterialTheme {
        BattleCard(
            uiModel = sampleUiModel,
            modifier = Modifier.padding(16.dp)
        )
    }
}

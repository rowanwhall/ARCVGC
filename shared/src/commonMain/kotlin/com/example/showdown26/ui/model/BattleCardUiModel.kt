package com.example.showdown26.ui.model

/**
 * UI model for displaying a battle card with two teams.
 * This is the single source of truth for BattleCard on both Android and iOS.
 */
data class BattleCardUiModel(
    val id: Int,
    val player1: PlayerUiModel,
    val player2: PlayerUiModel,
    val formatName: String,
    val rating: String,
    val formattedTime: String
)

data class PlayerUiModel(
    val name: String,
    val isWinner: Boolean?,
    val team: List<PokemonSlotUiModel>
)

data class PokemonSlotUiModel(
    val name: String,
    val imageUrl: String?,
    val item: ItemUiModel?,
    val teraType: TeraTypeUiModel? = null
)

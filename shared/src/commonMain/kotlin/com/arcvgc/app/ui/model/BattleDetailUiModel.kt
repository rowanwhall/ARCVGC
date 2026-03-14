package com.arcvgc.app.ui.model

data class BattleDetailUiModel(
    val id: Int,
    val player1: PlayerDetailUiModel,
    val player2: PlayerDetailUiModel,
    val formatId: Int,
    val formatName: String,
    val rating: Int,
    val formattedTime: String,
    val replayUrl: String
)

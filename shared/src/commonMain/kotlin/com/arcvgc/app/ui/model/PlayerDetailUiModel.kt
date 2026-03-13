package com.arcvgc.app.ui.model

data class PlayerDetailUiModel(
    val id: Int,
    val name: String,
    val isWinner: Boolean?,
    val team: List<PokemonDetailUiModel>
)

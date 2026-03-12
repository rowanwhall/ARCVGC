package com.example.showdown26.ui.model

data class PlayerDetailUiModel(
    val id: Int,
    val name: String,
    val isWinner: Boolean?,
    val team: List<PokemonDetailUiModel>
)

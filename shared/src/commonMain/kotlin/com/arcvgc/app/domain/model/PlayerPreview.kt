package com.arcvgc.app.domain.model

data class PlayerPreview(
    val id: Int,
    val name: String,
    val isWinner: Boolean?,
    val team: List<PokemonPreview>
)

package com.arcvgc.app.ui.model

data class PokemonDetailUiModel(
    val id: Int,
    val name: String,
    val imageUrl: String?,
    val item: ItemUiModel?,
    val abilityName: String,
    val moves: List<String>,
    val types: List<TypeUiModel>,
    val teraType: TeraTypeUiModel? = null
)

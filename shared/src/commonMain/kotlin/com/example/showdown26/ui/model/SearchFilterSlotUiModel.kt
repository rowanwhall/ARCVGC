package com.example.showdown26.ui.model

data class SearchFilterSlotUiModel(
    val pokemonId: Int,
    val pokemonName: String,
    val pokemonImageUrl: String?,
    val item: ItemUiModel?,
    val teraType: TeraTypeUiModel?
)

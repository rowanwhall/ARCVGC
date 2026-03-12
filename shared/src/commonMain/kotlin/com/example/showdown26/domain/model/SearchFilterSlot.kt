package com.example.showdown26.domain.model

data class SearchFilterSlot(
    val pokemonId: Int,
    val itemId: Int? = null,
    val teraTypeId: Int? = null,
    val pokemonName: String = "",
    val pokemonImageUrl: String? = null,
    val itemName: String? = null,
    val teraTypeImageUrl: String? = null
)

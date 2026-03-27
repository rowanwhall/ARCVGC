package com.arcvgc.app.domain.model

data class SearchFilterSlot(
    val pokemonId: Int,
    val itemId: Int? = null,
    val teraTypeId: Int? = null,
    val abilityId: Int? = null,
    val pokemonName: String = "",
    val pokemonImageUrl: String? = null,
    val itemName: String? = null,
    val itemImageUrl: String? = null,
    val teraTypeImageUrl: String? = null,
    val abilityName: String? = null
)

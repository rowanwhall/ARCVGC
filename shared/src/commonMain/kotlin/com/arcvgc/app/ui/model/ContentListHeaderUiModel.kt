package com.arcvgc.app.ui.model

import com.arcvgc.app.domain.model.WinnerFilter

sealed class ContentListHeaderUiModel {
    data object None : ContentListHeaderUiModel()
    data object HomeHero : ContentListHeaderUiModel()
    data object FavoritesHero : ContentListHeaderUiModel()
    data object TopPokemonHero : ContentListHeaderUiModel()
    data class PokemonHero(val name: String, val imageUrl: String?, val typeImageUrls: List<String> = emptyList()) : ContentListHeaderUiModel()
    data class PlayerHero(val name: String) : ContentListHeaderUiModel()
    data class SearchFilters(
        val team1Chips: List<PokemonChip>,
        val team2Chips: List<PokemonChip> = emptyList(),
        val formatName: String?,
        val minimumRating: Int?,
        val maximumRating: Int?,
        val unratedOnly: Boolean,
        val playerName: String?,
        val timeRangeStart: Long?,
        val timeRangeEnd: Long?,
        val winnerFilter: WinnerFilter = WinnerFilter.NONE
    ) : ContentListHeaderUiModel()
}

data class PokemonChip(
    val index: Int,
    val name: String,
    val imageUrl: String?,
    val itemName: String?,
    val itemImageUrl: String? = null,
    val teraTypeImageUrl: String?,
    val abilityName: String? = null
)

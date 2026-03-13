package com.arcvgc.app.ui.model

import com.arcvgc.app.domain.model.SearchParams

sealed class ContentListMode {
    data object Home : ContentListMode()
    data class Favorites(val contentType: FavoriteContentType = FavoriteContentType.Battles) : ContentListMode()
    data class Search(val params: SearchParams) : ContentListMode()
    data class Pokemon(val pokemonId: Int, val name: String, val imageUrl: String?, val typeImageUrl1: String?, val typeImageUrl2: String?) : ContentListMode()
    data class Player(val playerId: Int, val playerName: String) : ContentListMode()

    fun toHeaderUiModel(): ContentListHeaderUiModel = when (this) {
        is Home -> ContentListHeaderUiModel.HomeHero
        is Favorites -> ContentListHeaderUiModel.FavoritesHero
        is Search -> ContentListHeaderUiModel.SearchFilters(
            pokemonChips = params.filters.mapIndexed { index, slot ->
                PokemonChip(
                    index = index,
                    name = slot.pokemonName,
                    imageUrl = slot.pokemonImageUrl,
                    itemName = slot.itemName,
                    teraTypeImageUrl = slot.teraTypeImageUrl
                )
            },
            formatName = params.formatName,
            minimumRating = params.minimumRating?.takeIf { it > 0 },
            maximumRating = params.maximumRating?.takeIf { it > 0 },
            unratedOnly = params.unratedOnly,
            playerName = params.playerName,
            timeRangeStart = params.timeRangeStart,
            timeRangeEnd = params.timeRangeEnd
        )
        is Pokemon -> ContentListHeaderUiModel.PokemonHero(
            name, imageUrl, listOfNotNull(typeImageUrl1, typeImageUrl2)
        )
        is Player -> ContentListHeaderUiModel.PlayerHero(playerName)
    }
}

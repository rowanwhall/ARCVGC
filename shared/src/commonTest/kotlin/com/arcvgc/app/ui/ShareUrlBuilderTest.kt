package com.arcvgc.app.ui

import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.FavoriteContentType
import kotlin.test.Test
import kotlin.test.assertEquals

class ShareUrlBuilderTest {

    @Test
    fun homeMode() {
        assertEquals(
            "https://arcvgc.com/",
            shareUrlForMode(ContentListMode.Home, null)
        )
    }

    @Test
    fun homeWithBattle() {
        assertEquals(
            "https://arcvgc.com/?battle=42",
            shareUrlForMode(ContentListMode.Home, 42)
        )
    }

    @Test
    fun pokemonMode() {
        assertEquals(
            "https://arcvgc.com/pokemon/150",
            shareUrlForMode(ContentListMode.Pokemon(150, "Mewtwo", null, null, null), null)
        )
    }

    @Test
    fun pokemonWithBattle() {
        assertEquals(
            "https://arcvgc.com/pokemon/150?battle=99",
            shareUrlForMode(ContentListMode.Pokemon(150, "Mewtwo", null, null, null), 99)
        )
    }

    @Test
    fun playerMode() {
        assertEquals(
            "https://arcvgc.com/player/Wolfe+Glick",
            shareUrlForMode(ContentListMode.Player(1, "Wolfe Glick"), null)
        )
    }

    @Test
    fun playerWithBattle() {
        assertEquals(
            "https://arcvgc.com/player/Wolfe+Glick?battle=55",
            shareUrlForMode(ContentListMode.Player(1, "Wolfe Glick"), 55)
        )
    }

    @Test
    fun favoritesBattles() {
        assertEquals(
            "https://arcvgc.com/favorites/battles",
            shareUrlForMode(ContentListMode.Favorites(FavoriteContentType.Battles), null)
        )
    }

    @Test
    fun favoritesPokemon() {
        assertEquals(
            "https://arcvgc.com/favorites/pokemon",
            shareUrlForMode(ContentListMode.Favorites(FavoriteContentType.Pokemon), null)
        )
    }

    @Test
    fun favoritesPlayers() {
        assertEquals(
            "https://arcvgc.com/favorites/players",
            shareUrlForMode(ContentListMode.Favorites(FavoriteContentType.Players), null)
        )
    }

    @Test
    fun favoritesWithBattle() {
        assertEquals(
            "https://arcvgc.com/favorites/battles?battle=10",
            shareUrlForMode(ContentListMode.Favorites(FavoriteContentType.Battles), 10)
        )
    }

    @Test
    fun searchMode() {
        val params = SearchParams(
            filters = listOf(SearchFilterSlot(pokemonId = 150, pokemonName = "Mewtwo", pokemonImageUrl = null)),
            formatId = 1,
            formatName = "Reg G",
            orderBy = "rating"
        )
        assertEquals(
            "https://arcvgc.com/search?p=150&f=1&order=rating",
            shareUrlForMode(ContentListMode.Search(params), null)
        )
    }

    @Test
    fun searchWithBattle() {
        val params = SearchParams(
            filters = listOf(SearchFilterSlot(pokemonId = 150, pokemonName = "Mewtwo", pokemonImageUrl = null)),
            formatId = 1,
            formatName = "Reg G",
            orderBy = "rating"
        )
        assertEquals(
            "https://arcvgc.com/search?p=150&f=1&order=rating&battle=42",
            shareUrlForMode(ContentListMode.Search(params), 42)
        )
    }
}

package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeepLinkTargetTest {

    @Test
    fun parseBattleLink() {
        val result = parseDeepLink("/battle/42")
        assertIs<DeepLinkTarget.Battle>(result)
        assertEquals(42, result.id)
    }

    @Test
    fun parsePokemonLink() {
        val result = parseDeepLink("/pokemon/150")
        assertIs<DeepLinkTarget.Pokemon>(result)
        assertEquals(150, result.id)
    }

    @Test
    fun parsePlayerLink() {
        val result = parseDeepLink("/player/Wolfe Glick")
        assertIs<DeepLinkTarget.Player>(result)
        assertEquals("Wolfe Glick", result.name)
    }

    @Test
    fun parsePlayerLinkWithSlashes() {
        val result = parseDeepLink("/player/name/with/slashes")
        assertIs<DeepLinkTarget.Player>(result)
        assertEquals("name/with/slashes", result.name)
    }

    @Test
    fun parseBattleLinkWithoutLeadingSlash() {
        val result = parseDeepLink("battle/42")
        assertIs<DeepLinkTarget.Battle>(result)
        assertEquals(42, result.id)
    }

    @Test
    fun parseRootReturnsNull() {
        assertNull(parseDeepLink("/"))
    }

    @Test
    fun parseEmptyReturnsNull() {
        assertNull(parseDeepLink(""))
    }

    @Test
    fun parseUnknownPathReturnsNull() {
        assertNull(parseDeepLink("/unknown"))
    }

    @Test
    fun parseBattleWithNonNumericIdReturnsNull() {
        assertNull(parseDeepLink("/battle/abc"))
    }

    @Test
    fun parsePokemonWithNonNumericIdReturnsNull() {
        assertNull(parseDeepLink("/pokemon/pikachu"))
    }

    @Test
    fun parseBattleWithExtraSegmentsReturnsNull() {
        assertNull(parseDeepLink("/battle/42/extra"))
    }

    @Test
    fun parsePlayerWithBlankNameReturnsNull() {
        assertNull(parseDeepLink("/player/"))
    }

    // Favorites tests

    @Test
    fun parseFavoritesBattles() {
        val result = parseDeepLink("/favorites/battles")
        assertIs<DeepLinkTarget.Favorites>(result)
        assertEquals("battles", result.contentType)
    }

    @Test
    fun parseFavoritesPokemon() {
        val result = parseDeepLink("/favorites/pokemon")
        assertIs<DeepLinkTarget.Favorites>(result)
        assertEquals("pokemon", result.contentType)
    }

    @Test
    fun parseFavoritesPlayers() {
        val result = parseDeepLink("/favorites/players")
        assertIs<DeepLinkTarget.Favorites>(result)
        assertEquals("players", result.contentType)
    }

    @Test
    fun parseFavoritesInvalidTypeReturnsNull() {
        assertNull(parseDeepLink("/favorites/invalid"))
    }

    @Test
    fun parseFavoritesNoTypeReturnsNull() {
        assertNull(parseDeepLink("/favorites"))
    }

    // Search tests

    @Test
    fun parseSearchMinimal() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating")
        assertIs<DeepLinkTarget.Search>(result)
        assertEquals(listOf(150), result.params.pokemonIds)
        assertEquals(1, result.params.formatId)
        assertEquals("rating", result.params.orderBy)
    }

    @Test
    fun parseSearchFull() {
        val result = parseDeepLink("/search?p=150,42&i=_,5&t=_,3&f=1&min=1500&max=1800&order=date&player=Wolfe&start=123&end=456&unrated")
        assertIs<DeepLinkTarget.Search>(result)
        val params = result.params
        assertEquals(listOf(150, 42), params.pokemonIds)
        assertEquals(listOf(null, 5), params.itemIds)
        assertEquals(listOf(null, 3), params.teraTypeIds)
        assertEquals(1, params.formatId)
        assertEquals(1500, params.minimumRating)
        assertEquals(1800, params.maximumRating)
        assertTrue(params.unratedOnly)
        assertEquals("date", params.orderBy)
        assertEquals("Wolfe", params.playerName)
        assertEquals(123L, params.timeRangeStart)
        assertEquals(456L, params.timeRangeEnd)
    }

    @Test
    fun parseSearchNoPokemonReturnsNull() {
        assertNull(parseDeepLink("/search?f=1&order=rating"))
    }

    @Test
    fun parseSearchNoFormatReturnsNull() {
        assertNull(parseDeepLink("/search?p=150&order=rating"))
    }

    @Test
    fun parseSearchNoQueryReturnsSearchTab() {
        assertIs<DeepLinkTarget.SearchTab>(parseDeepLink("/search"))
    }

    @Test
    fun parseSearchPlayerNameWithSpaces() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating&player=Wolfe+Glick")
        assertIs<DeepLinkTarget.Search>(result)
        assertEquals("Wolfe Glick", result.params.playerName)
    }

    @Test
    fun parseSearchPlayerNamePercentEncoded() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating&player=Wolfe%20Glick")
        assertIs<DeepLinkTarget.Search>(result)
        assertEquals("Wolfe Glick", result.params.playerName)
    }

    // Tab deep links

    @Test
    fun parseSearchTab() {
        val result = parseDeepLink("/search")
        assertIs<DeepLinkTarget.SearchTab>(result)
    }

    @Test
    fun parseSettingsTab() {
        val result = parseDeepLink("/settings")
        assertIs<DeepLinkTarget.SettingsTab>(result)
    }

    // Round-trip test

    @Test
    fun encodeSearchPathRoundTrip() {
        val original = SearchParams(
            filters = listOf(
                SearchFilterSlot(pokemonId = 150, itemId = null, teraTypeId = 3, pokemonName = "Mewtwo"),
                SearchFilterSlot(pokemonId = 42, itemId = 5, teraTypeId = null, pokemonName = "Golbat")
            ),
            formatId = 1,
            minimumRating = 1500,
            maximumRating = null,
            unratedOnly = false,
            orderBy = "date",
            playerName = "Wolfe Glick"
        )
        val encoded = encodeSearchPath(original)
        val parsed = parseDeepLink(encoded)
        assertIs<DeepLinkTarget.Search>(parsed)
        val params = parsed.params
        assertEquals(listOf(150, 42), params.pokemonIds)
        assertEquals(listOf(null, 5), params.itemIds)
        assertEquals(listOf(3, null), params.teraTypeIds)
        assertEquals(1, params.formatId)
        assertEquals(1500, params.minimumRating)
        assertNull(params.maximumRating)
        assertEquals("date", params.orderBy)
        assertEquals("Wolfe Glick", params.playerName)
    }
}

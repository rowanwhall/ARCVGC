package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeepLinkTargetTest {

    // Battle deep links (backwards compat → Home root + battleId)

    @Test
    fun parseBattleLink() {
        val result = parseDeepLink("/battle/42")!!
        assertIs<DeepLinkTarget.Home>(result.target)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parseBattleLinkWithoutLeadingSlash() {
        val result = parseDeepLink("battle/42")!!
        assertIs<DeepLinkTarget.Home>(result.target)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parseBattleWithNonNumericIdReturnsNull() {
        assertNull(parseDeepLink("/battle/abc"))
    }

    @Test
    fun parseBattleWithExtraSegmentsReturnsNull() {
        assertNull(parseDeepLink("/battle/42/extra"))
    }

    // Pokemon deep links

    @Test
    fun parsePokemonLink() {
        val result = parseDeepLink("/pokemon/150")!!
        assertIs<DeepLinkTarget.Pokemon>(result.target)
        assertEquals(150, (result.target as DeepLinkTarget.Pokemon).id)
        assertNull(result.battleId)
    }

    @Test
    fun parsePokemonWithBattle() {
        val result = parseDeepLink("/pokemon/150?battle=42")!!
        assertIs<DeepLinkTarget.Pokemon>(result.target)
        assertEquals(150, (result.target as DeepLinkTarget.Pokemon).id)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parsePokemonWithNonNumericIdReturnsNull() {
        assertNull(parseDeepLink("/pokemon/pikachu"))
    }

    // Player deep links

    @Test
    fun parsePlayerLink() {
        val result = parseDeepLink("/player/Wolfe Glick")!!
        assertIs<DeepLinkTarget.Player>(result.target)
        assertEquals("Wolfe Glick", (result.target as DeepLinkTarget.Player).name)
        assertNull(result.battleId)
    }

    @Test
    fun parsePlayerWithBattle() {
        val result = parseDeepLink("/player/Wolfe?battle=42")!!
        assertIs<DeepLinkTarget.Player>(result.target)
        assertEquals("Wolfe", (result.target as DeepLinkTarget.Player).name)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parsePlayerLinkWithSlashes() {
        val result = parseDeepLink("/player/name/with/slashes")!!
        assertIs<DeepLinkTarget.Player>(result.target)
        assertEquals("name/with/slashes", (result.target as DeepLinkTarget.Player).name)
    }

    @Test
    fun parsePlayerWithBlankNameReturnsNull() {
        assertNull(parseDeepLink("/player/"))
    }

    // Favorites deep links

    @Test
    fun parseFavoritesBattles() {
        val result = parseDeepLink("/favorites/battles")!!
        assertIs<DeepLinkTarget.Favorites>(result.target)
        assertEquals("battles", (result.target as DeepLinkTarget.Favorites).contentType)
    }

    @Test
    fun parseFavoritesPokemon() {
        val result = parseDeepLink("/favorites/pokemon")!!
        assertIs<DeepLinkTarget.Favorites>(result.target)
        assertEquals("pokemon", (result.target as DeepLinkTarget.Favorites).contentType)
    }

    @Test
    fun parseFavoritesPlayers() {
        val result = parseDeepLink("/favorites/players")!!
        assertIs<DeepLinkTarget.Favorites>(result.target)
        assertEquals("players", (result.target as DeepLinkTarget.Favorites).contentType)
    }

    @Test
    fun parseFavoritesWithBattle() {
        val result = parseDeepLink("/favorites/battles?battle=42")!!
        assertIs<DeepLinkTarget.Favorites>(result.target)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parseFavoritesInvalidTypeReturnsNull() {
        assertNull(parseDeepLink("/favorites/invalid"))
    }

    @Test
    fun parseFavoritesNoTypeReturnsNull() {
        assertNull(parseDeepLink("/favorites"))
    }

    // Search deep links

    @Test
    fun parseSearchMinimal() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        val params = (result.target as DeepLinkTarget.Search).params
        assertEquals(listOf(150), params.pokemonIds)
        assertEquals(1, params.formatId)
        assertEquals("rating", params.orderBy)
    }

    @Test
    fun parseSearchFull() {
        val result = parseDeepLink("/search?p=150,42&i=_,5&t=_,3&f=1&min=1500&max=1800&order=date&player=Wolfe&start=123&end=456&unrated")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        val params = (result.target as DeepLinkTarget.Search).params
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
    fun parseSearchWithBattle() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating&battle=42")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        assertEquals(42, result.battleId)
        val params = (result.target as DeepLinkTarget.Search).params
        assertEquals(listOf(150), params.pokemonIds)
    }

    @Test
    fun parseSearchNoPokemonFallsToSearchTab() {
        val result = parseDeepLink("/search?f=1&order=rating")!!
        assertIs<DeepLinkTarget.SearchTab>(result.target)
    }

    @Test
    fun parseSearchNoFormatReturnsNull() {
        assertNull(parseDeepLink("/search?p=150&order=rating"))
    }

    @Test
    fun parseSearchNoQueryReturnsSearchTab() {
        val result = parseDeepLink("/search")!!
        assertIs<DeepLinkTarget.SearchTab>(result.target)
    }

    @Test
    fun parseSearchPlayerNameWithSpaces() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating&player=Wolfe+Glick")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        assertEquals("Wolfe Glick", (result.target as DeepLinkTarget.Search).params.playerName)
    }

    @Test
    fun parseSearchPlayerNamePercentEncoded() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating&player=Wolfe%20Glick")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        assertEquals("Wolfe Glick", (result.target as DeepLinkTarget.Search).params.playerName)
    }

    // Tab deep links

    @Test
    fun parseSettingsTab() {
        val result = parseDeepLink("/settings")!!
        assertIs<DeepLinkTarget.SettingsTab>(result.target)
    }

    // Home with battle

    @Test
    fun parseHomeWithBattle() {
        val result = parseDeepLink("/?battle=42")!!
        assertIs<DeepLinkTarget.Home>(result.target)
        assertEquals(42, result.battleId)
    }

    // Null cases

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

    // Round-trip tests

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
        val parsed = parseDeepLink(encoded)!!
        assertIs<DeepLinkTarget.Search>(parsed.target)
        val params = (parsed.target as DeepLinkTarget.Search).params
        assertEquals(listOf(150, 42), params.pokemonIds)
        assertEquals(listOf(null, 5), params.itemIds)
        assertEquals(listOf(3, null), params.teraTypeIds)
        assertEquals(1, params.formatId)
        assertEquals(1500, params.minimumRating)
        assertNull(params.maximumRating)
        assertEquals("date", params.orderBy)
        assertEquals("Wolfe Glick", params.playerName)
    }

    @Test
    fun appendBattleParamToSimplePath() {
        assertEquals("/pokemon/150?battle=42", appendBattleParam("/pokemon/150", 42))
    }

    @Test
    fun appendBattleParamToPathWithQuery() {
        assertEquals("/search?p=150&f=1&battle=42", appendBattleParam("/search?p=150&f=1", 42))
    }

    @Test
    fun appendBattleParamNull() {
        assertEquals("/pokemon/150", appendBattleParam("/pokemon/150", null))
    }
}

package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
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

    // TopPokemon deep links

    @Test
    fun parseTopPokemonLink() {
        val result = parseDeepLink("/top-pokemon")!!
        assertIs<DeepLinkTarget.TopPokemon>(result.target)
        assertNull((result.target as DeepLinkTarget.TopPokemon).formatId)
        assertNull(result.battleId)
    }

    @Test
    fun parseTopPokemonWithFormat() {
        val result = parseDeepLink("/top-pokemon?f=5")!!
        assertIs<DeepLinkTarget.TopPokemon>(result.target)
        assertEquals(5, (result.target as DeepLinkTarget.TopPokemon).formatId)
        assertNull(result.battleId)
    }

    @Test
    fun parseTopPokemonWithBattle() {
        val result = parseDeepLink("/top-pokemon?battle=42")!!
        assertIs<DeepLinkTarget.TopPokemon>(result.target)
        assertNull((result.target as DeepLinkTarget.TopPokemon).formatId)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parseTopPokemonWithFormatAndBattle() {
        val result = parseDeepLink("/top-pokemon?f=5&battle=42")!!
        assertIs<DeepLinkTarget.TopPokemon>(result.target)
        assertEquals(5, (result.target as DeepLinkTarget.TopPokemon).formatId)
        assertEquals(42, result.battleId)
    }

    @Test
    fun encodeTopPokemonPathRoundTrip() {
        val encoded = encodeTopPokemonPath(5)
        val parsed = parseDeepLink(encoded)!!
        assertIs<DeepLinkTarget.TopPokemon>(parsed.target)
        assertEquals(5, (parsed.target as DeepLinkTarget.TopPokemon).formatId)
    }

    @Test
    fun encodeTopPokemonPathNullFormat() {
        val encoded = encodeTopPokemonPath(null)
        assertEquals("/top-pokemon", encoded)
        val parsed = parseDeepLink(encoded)!!
        assertIs<DeepLinkTarget.TopPokemon>(parsed.target)
        assertNull((parsed.target as DeepLinkTarget.TopPokemon).formatId)
    }

    @Test
    fun parseUsagePathAlias() {
        val result = parseDeepLink("/usage")!!
        assertIs<DeepLinkTarget.TopPokemon>(result.target)
        assertNull((result.target as DeepLinkTarget.TopPokemon).formatId)
    }

    @Test
    fun parseUsagePathAliasWithFormat() {
        val result = parseDeepLink("/usage?f=5")!!
        assertIs<DeepLinkTarget.TopPokemon>(result.target)
        assertEquals(5, (result.target as DeepLinkTarget.TopPokemon).formatId)
    }

    @Test
    fun parseUsagePathWithPokemon() {
        val result = parseDeepLink("/usage?pokemon=150")!!
        assertIs<DeepLinkTarget.TopPokemon>(result.target)
        val target = result.target as DeepLinkTarget.TopPokemon
        assertNull(target.formatId)
        assertEquals(150, target.pokemonId)
    }

    @Test
    fun parseUsagePathWithFormatAndPokemon() {
        val result = parseDeepLink("/usage?f=5&pokemon=150")!!
        val target = result.target as DeepLinkTarget.TopPokemon
        assertEquals(5, target.formatId)
        assertEquals(150, target.pokemonId)
    }

    @Test
    fun encodeTopPokemonPathWithPokemonRoundTrip() {
        val encoded = encodeTopPokemonPath(formatId = 5, pokemonId = 150)
        assertEquals("/top-pokemon?f=5&pokemon=150", encoded)
        val parsed = parseDeepLink(encoded)!!
        val target = parsed.target as DeepLinkTarget.TopPokemon
        assertEquals(5, target.formatId)
        assertEquals(150, target.pokemonId)
    }

    @Test
    fun encodeTopPokemonPathPokemonOnly() {
        val encoded = encodeTopPokemonPath(formatId = null, pokemonId = 150)
        assertEquals("/top-pokemon?pokemon=150", encoded)
        val parsed = parseDeepLink(encoded)!!
        val target = parsed.target as DeepLinkTarget.TopPokemon
        assertNull(target.formatId)
        assertEquals(150, target.pokemonId)
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
    fun parseRootReturnsHome() {
        val result = parseDeepLink("/")
        assertNotNull(result)
        assertIs<DeepLinkTarget.Home>(result.target)
        assertNull(result.battleId)
    }

    @Test
    fun parseEmptyReturnsHome() {
        val result = parseDeepLink("")
        assertNotNull(result)
        assertIs<DeepLinkTarget.Home>(result.target)
        assertNull(result.battleId)
    }

    @Test
    fun parseUnknownPathReturnsNull() {
        assertNull(parseDeepLink("/unknown"))
    }

    // Battle param edge cases

    @Test
    fun parsePokemonWithNonNumericBattleIgnoresBattle() {
        val result = parseDeepLink("/pokemon/150?battle=abc")!!
        assertIs<DeepLinkTarget.Pokemon>(result.target)
        assertEquals(150, (result.target as DeepLinkTarget.Pokemon).id)
        assertNull(result.battleId)
    }

    @Test
    fun parseSettingsWithBattle() {
        val result = parseDeepLink("/settings?battle=42")!!
        assertIs<DeepLinkTarget.SettingsTab>(result.target)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parseSearchTabWithBattle() {
        val result = parseDeepLink("/search?battle=42")!!
        assertIs<DeepLinkTarget.SearchTab>(result.target)
        assertEquals(42, result.battleId)
    }

    @Test
    fun parseBattlePathIgnoresQueryBattle() {
        // /battle/42 uses the path ID, not the query battle param
        val result = parseDeepLink("/battle/42?battle=99")!!
        assertIs<DeepLinkTarget.Home>(result.target)
        assertEquals(42, result.battleId)
    }

    // Trailing slash edge cases (iOS custom scheme can produce these)

    @Test
    fun parsePokemonWithTrailingSlash() {
        val result = parseDeepLink("/pokemon/150/")
        // Trailing slash adds an empty segment → size 3, doesn't match
        assertNull(result)
    }

    @Test
    fun parseSettingsWithTrailingSlash() {
        // "/settings/" → segments = ["settings", ""] → size 2, doesn't match size 1
        assertNull(parseDeepLink("/settings/"))
    }

    // Ability deep link tests

    @Test
    fun parseSearchWithAbilities() {
        val result = parseDeepLink("/search?p=150,42&a=7,_&f=1&order=rating")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        val params = (result.target as DeepLinkTarget.Search).params
        assertEquals(listOf(150, 42), params.pokemonIds)
        assertEquals(listOf(7, null), params.abilityIds)
    }

    // Team 2 deep link tests

    @Test
    fun parseSearchWithTeam2() {
        val result = parseDeepLink("/search?p=150&p2=25,143&f=1&order=rating")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        val params = (result.target as DeepLinkTarget.Search).params
        assertEquals(listOf(150), params.pokemonIds)
        assertEquals(listOf(25, 143), params.team2PokemonIds)
    }

    @Test
    fun parseSearchWithTeam2SubFilters() {
        val result = parseDeepLink("/search?p=150&p2=25&i2=5&t2=3&a2=12&f=1&order=rating")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        val params = (result.target as DeepLinkTarget.Search).params
        assertEquals(listOf(25), params.team2PokemonIds)
        assertEquals(listOf(5), params.team2ItemIds)
        assertEquals(listOf(3), params.team2TeraTypeIds)
        assertEquals(listOf(12), params.team2AbilityIds)
    }

    // Winner filter deep link tests

    @Test
    fun parseSearchWithWinnerFilterTeam1() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating&w=1")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        assertEquals(WinnerFilter.TEAM1, (result.target as DeepLinkTarget.Search).params.winnerFilter)
    }

    @Test
    fun parseSearchWithWinnerFilterTeam2() {
        val result = parseDeepLink("/search?p=150&p2=25&f=1&order=rating&w=2")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        assertEquals(WinnerFilter.TEAM2, (result.target as DeepLinkTarget.Search).params.winnerFilter)
    }

    @Test
    fun parseSearchWithInvalidWinnerFilterDefaultsToNone() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating&w=3")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        assertEquals(WinnerFilter.NONE, (result.target as DeepLinkTarget.Search).params.winnerFilter)
    }

    @Test
    fun parseSearchWithNoWinnerFilterDefaultsToNone() {
        val result = parseDeepLink("/search?p=150&f=1&order=rating")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        assertEquals(WinnerFilter.NONE, (result.target as DeepLinkTarget.Search).params.winnerFilter)
    }

    // Backward compatibility

    @Test
    fun backwardCompatibilityOldUrlsStillParse() {
        val result = parseDeepLink("/search?p=150,42&i=_,5&t=_,3&f=1&min=1500&order=rating")!!
        assertIs<DeepLinkTarget.Search>(result.target)
        val params = (result.target as DeepLinkTarget.Search).params
        assertEquals(listOf(150, 42), params.pokemonIds)
        assertEquals(listOf(null, 5), params.itemIds)
        assertEquals(listOf(null, 3), params.teraTypeIds)
        assertTrue(params.abilityIds.isEmpty())
        assertTrue(params.team2PokemonIds.isEmpty())
        assertEquals(WinnerFilter.NONE, params.winnerFilter)
    }

    // Edge cases

    @Test
    fun parseSearchWithTeam2OnlyFallsToSearchTab() {
        // team2 without team1 is invalid — p is required, so this falls through to SearchTab
        val result = parseDeepLink("/search?p2=25&f=1&order=rating")!!
        assertIs<DeepLinkTarget.SearchTab>(result.target)
    }

    // Round-trip tests

    @Test
    fun encodeSearchPathRoundTripWinnerFilterTeam2() {
        val original = SearchParams(
            filters = listOf(
                SearchFilterSlot(pokemonId = 150, pokemonName = "Mewtwo")
            ),
            team2Filters = listOf(
                SearchFilterSlot(pokemonId = 25, pokemonName = "Pikachu")
            ),
            formatId = 1,
            orderBy = "rating",
            winnerFilter = WinnerFilter.TEAM2
        )
        val encoded = encodeSearchPath(original)
        val parsed = parseDeepLink(encoded)!!
        assertIs<DeepLinkTarget.Search>(parsed.target)
        val params = (parsed.target as DeepLinkTarget.Search).params
        assertEquals(WinnerFilter.TEAM2, params.winnerFilter)
        assertEquals(listOf(150), params.pokemonIds)
        assertEquals(listOf(25), params.team2PokemonIds)
    }

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
    fun encodeSearchPathRoundTripFull() {
        val original = SearchParams(
            filters = listOf(
                SearchFilterSlot(pokemonId = 150, itemId = null, teraTypeId = 3, abilityId = 7, pokemonName = "Mewtwo"),
                SearchFilterSlot(pokemonId = 42, itemId = 5, teraTypeId = null, abilityId = null, pokemonName = "Golbat")
            ),
            team2Filters = listOf(
                SearchFilterSlot(pokemonId = 25, itemId = 10, teraTypeId = null, abilityId = 12, pokemonName = "Pikachu")
            ),
            formatId = 1,
            minimumRating = 1500,
            orderBy = "date",
            winnerFilter = WinnerFilter.TEAM1
        )
        val encoded = encodeSearchPath(original)
        val parsed = parseDeepLink(encoded)!!
        assertIs<DeepLinkTarget.Search>(parsed.target)
        val params = (parsed.target as DeepLinkTarget.Search).params
        assertEquals(listOf(150, 42), params.pokemonIds)
        assertEquals(listOf(null, 5), params.itemIds)
        assertEquals(listOf(3, null), params.teraTypeIds)
        assertEquals(listOf(7, null), params.abilityIds)
        assertEquals(listOf(25), params.team2PokemonIds)
        assertEquals(listOf(10), params.team2ItemIds)
        assertTrue(params.team2TeraTypeIds.isEmpty()) // none non-null, so not encoded
        assertEquals(listOf(12), params.team2AbilityIds)
        assertEquals(WinnerFilter.TEAM1, params.winnerFilter)
        assertEquals(1500, params.minimumRating)
        assertEquals("date", params.orderBy)
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

package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

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
        assertNull(parseDeepLink("/settings"))
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
}

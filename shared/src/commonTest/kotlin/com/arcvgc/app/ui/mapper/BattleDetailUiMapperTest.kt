package com.arcvgc.app.ui.mapper

import com.arcvgc.app.testutil.testAbility
import com.arcvgc.app.testutil.testFormat
import com.arcvgc.app.domain.model.SetMatch
import com.arcvgc.app.testutil.testMatchDetail
import com.arcvgc.app.testutil.testMove
import com.arcvgc.app.testutil.testPlayerDetail
import com.arcvgc.app.testutil.testPokemonDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BattleDetailUiMapperTest {

    @Test
    fun mapsFullMatchDetailCorrectly() {
        val detail = testMatchDetail(
            id = 10,
            showdownId = "gen9vgc2024regh-99999",
            rating = 1500,
            format = testFormat(id = 5, formattedName = "Reg H"),
            players = listOf(
                testPlayerDetail(1, "Alice", true),
                testPlayerDetail(2, "Bob", false)
            )
        )

        val result = BattleDetailUiMapper.map(detail)

        assertEquals(10, result.id)
        assertEquals("Alice", result.player1.name)
        assertEquals("Bob", result.player2.name)
        assertEquals(5, result.formatId)
        assertEquals("Reg H", result.formatName)
        assertEquals(1500, result.rating)
        assertEquals("https://replay.pokemonshowdown.com/gen9vgc2024regh-99999", result.replayUrl)
    }

    @Test
    fun nullRatingPassedThrough() {
        val detail = testMatchDetail(rating = null)

        val result = BattleDetailUiMapper.map(detail)

        assertNull(result.rating)
    }

    @Test
    fun ratingPassedThrough() {
        val detail = testMatchDetail(rating = 1500)

        val result = BattleDetailUiMapper.map(detail)

        assertEquals(1500, result.rating)
    }

    @Test
    fun replayUrlDerivedFromShowdownId() {
        val detail = testMatchDetail(showdownId = "gen9vgc2024regh-12345")

        val result = BattleDetailUiMapper.map(detail)

        assertEquals("https://replay.pokemonshowdown.com/gen9vgc2024regh-12345", result.replayUrl)
    }

    @Test
    fun abilityDisplayNameFormatted() {
        val pokemon = testPokemonDetail(
            ability = testAbility(name = "SandStream")
        )
        val detail = testMatchDetail(
            players = listOf(
                testPlayerDetail(team = listOf(pokemon)),
                testPlayerDetail(id = 2, name = "PlayerTwo")
            )
        )

        val result = BattleDetailUiMapper.map(detail)

        assertEquals("Sand Stream", result.player1.team[0].abilityName)
    }

    @Test
    fun moveDisplayNamesFormatted() {
        val pokemon = testPokemonDetail(
            moves = listOf(
                testMove(1, "ShadowBall"),
                testMove(2, "VoltSwitch")
            )
        )
        val detail = testMatchDetail(
            players = listOf(
                testPlayerDetail(team = listOf(pokemon)),
                testPlayerDetail(id = 2, name = "PlayerTwo")
            )
        )

        val result = BattleDetailUiMapper.map(detail)

        assertEquals("Shadow Ball", result.player1.team[0].moves[0])
        assertEquals("Volt Switch", result.player1.team[0].moves[1])
    }

    @Test
    fun nullAbilityMapsToNullAbilityName() {
        val pokemon = testPokemonDetail(ability = null)
        val detail = testMatchDetail(
            players = listOf(
                testPlayerDetail(team = listOf(pokemon)),
                testPlayerDetail(id = 2, name = "PlayerTwo")
            )
        )

        val result = BattleDetailUiMapper.map(detail)

        assertNull(result.player1.team[0].abilityName)
    }

    @Test
    fun closedTeamsheetPokemonMapsGracefully() {
        val pokemon = testPokemonDetail(
            ability = null,
            item = null,
            moves = emptyList(),
            teraType = null
        )
        val detail = testMatchDetail(
            players = listOf(
                testPlayerDetail(team = listOf(pokemon)),
                testPlayerDetail(id = 2, name = "PlayerTwo")
            )
        )

        val result = BattleDetailUiMapper.map(detail)

        val uiPokemon = result.player1.team[0]
        assertNull(uiPokemon.abilityName)
        assertNull(uiPokemon.item)
        assertTrue(uiPokemon.moves.isEmpty())
        assertNull(uiPokemon.teraType)
    }

    @Test
    fun zeroPlayersProducesTwoUnknownWithIdZero() {
        val detail = testMatchDetail(players = emptyList())

        val result = BattleDetailUiMapper.map(detail)

        assertEquals(0, result.player1.id)
        assertEquals("Unknown", result.player1.name)
        assertNull(result.player1.isWinner)
        assertTrue(result.player1.team.isEmpty())

        assertEquals(0, result.player2.id)
        assertEquals("Unknown", result.player2.name)
        assertNull(result.player2.isWinner)
        assertTrue(result.player2.team.isEmpty())
    }

    @Test
    fun formatFormattedNameUsedWhenAvailable() {
        val detail = testMatchDetail(
            format = testFormat(name = "gen9vgc2024regh", formattedName = "Reg H")
        )

        val result = BattleDetailUiMapper.map(detail)

        assertEquals("Reg H", result.formatName)
    }

    @Test
    fun formatFallsBackToNameWhenNoFormattedName() {
        val detail = testMatchDetail(
            format = testFormat(name = "gen9vgc2024regh", formattedName = null)
        )

        val result = BattleDetailUiMapper.map(detail)

        assertEquals("gen9vgc2024regh", result.formatName)
    }

    @Test
    fun setMatchesMappedAndCurrentMatchFiltered() {
        val setMatches = listOf(
            SetMatch(id = 94, showdownId = "gen9vgc2026regfbo3-111", positionInSet = 3),
            SetMatch(id = 95, showdownId = "gen9vgc2026regfbo3-222", positionInSet = 2),
            SetMatch(id = 100, showdownId = "gen9vgc2026regfbo3-333", positionInSet = 1)
        )
        val detail = testMatchDetail(
            id = 100,
            positionInSet = 1,
            setMatches = setMatches
        )

        val result = BattleDetailUiMapper.map(detail)

        assertEquals(1, result.positionInSet)
        assertEquals(2, result.setMatches.size)
        assertEquals(95, result.setMatches[0].id)
        assertEquals(2, result.setMatches[0].positionInSet)
        assertEquals("https://replay.pokemonshowdown.com/gen9vgc2026regfbo3-222", result.setMatches[0].replayUrl)
        assertEquals(94, result.setMatches[1].id)
        assertEquals(3, result.setMatches[1].positionInSet)
        assertEquals("https://replay.pokemonshowdown.com/gen9vgc2026regfbo3-111", result.setMatches[1].replayUrl)
    }

    @Test
    fun emptySetMatchesProducesEmptyList() {
        val detail = testMatchDetail()

        val result = BattleDetailUiMapper.map(detail)

        assertNull(result.positionInSet)
        assertTrue(result.setMatches.isEmpty())
    }

    @Test
    fun singleSetMatchFilteredOut_producesEmptyList() {
        val setMatches = listOf(
            SetMatch(id = 100, showdownId = "gen9vgc2026regfbo3-333", positionInSet = 1)
        )
        val detail = testMatchDetail(
            id = 100,
            positionInSet = 1,
            setMatches = setMatches
        )

        val result = BattleDetailUiMapper.map(detail)

        assertTrue(result.setMatches.isEmpty())
    }
}

package com.arcvgc.app.ui.mapper

import com.arcvgc.app.testutil.testAbility
import com.arcvgc.app.testutil.testFormat
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
    fun nullRatingDefaultsTo1000() {
        val detail = testMatchDetail(rating = null)

        val result = BattleDetailUiMapper.map(detail)

        assertEquals(1000, result.rating)
    }

    @Test
    fun ratingBelow1000ClampedTo1000() {
        val detail = testMatchDetail(rating = 500)

        val result = BattleDetailUiMapper.map(detail)

        assertEquals(1000, result.rating)
    }

    @Test
    fun rating1500PassedThrough() {
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
}

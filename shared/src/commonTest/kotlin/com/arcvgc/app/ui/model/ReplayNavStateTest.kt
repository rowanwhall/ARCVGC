package com.arcvgc.app.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ReplayNavStateTest {

    private fun sampleBattle(
        replayUrl: String = "https://replay.pokemonshowdown.com/game1",
        positionInSet: Int? = null,
        setMatches: List<SetMatchUiModel> = emptyList()
    ) = BattleDetailUiModel(
        id = 1,
        player1 = PlayerDetailUiModel(id = 1, name = "P1", isWinner = true, team = emptyList()),
        player2 = PlayerDetailUiModel(id = 2, name = "P2", isWinner = false, team = emptyList()),
        formatId = 1,
        formatName = "VGC 2026",
        rating = 1500,
        formattedTime = "Mar 29",
        replayUrl = replayUrl,
        positionInSet = positionInSet,
        setMatches = setMatches
    )

    @Test
    fun singleGameReturnsSingleElementList() {
        val battle = sampleBattle()
        val result = battle.toReplayNavState("https://replay.pokemonshowdown.com/game1")

        assertEquals(1, result.games.size)
        assertEquals(0, result.initialIndex)
        assertEquals("https://replay.pokemonshowdown.com/game1", result.games[0].replayUrl)
    }

    @Test
    fun multiGameSetReturnsSortedList() {
        val battle = sampleBattle(
            replayUrl = "https://replay.pokemonshowdown.com/game1",
            positionInSet = 1,
            setMatches = listOf(
                SetMatchUiModel(id = 2, positionInSet = 2, replayUrl = "https://replay.pokemonshowdown.com/game2"),
                SetMatchUiModel(id = 3, positionInSet = 3, replayUrl = "https://replay.pokemonshowdown.com/game3")
            )
        )
        val result = battle.toReplayNavState("https://replay.pokemonshowdown.com/game2")

        assertEquals(3, result.games.size)
        assertEquals(1, result.initialIndex)
        assertEquals(1, result.games[0].positionInSet)
        assertEquals(2, result.games[1].positionInSet)
        assertEquals(3, result.games[2].positionInSet)
    }

    @Test
    fun tappedUrlNotFoundDefaultsToZero() {
        val battle = sampleBattle()
        val result = battle.toReplayNavState("https://replay.pokemonshowdown.com/nonexistent")

        assertEquals(0, result.initialIndex)
    }

    @Test
    fun nullPositionInSetSortsToEnd() {
        val battle = sampleBattle(
            replayUrl = "https://replay.pokemonshowdown.com/game1",
            positionInSet = null,
            setMatches = listOf(
                SetMatchUiModel(id = 2, positionInSet = 1, replayUrl = "https://replay.pokemonshowdown.com/game2")
            )
        )
        val result = battle.toReplayNavState("https://replay.pokemonshowdown.com/game1")

        assertEquals(2, result.games.size)
        assertEquals(1, result.games[0].positionInSet)
        assertEquals(null, result.games[1].positionInSet)
        assertEquals(1, result.initialIndex)
    }
}

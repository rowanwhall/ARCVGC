package com.arcvgc.app.ui.mapper

import com.arcvgc.app.testutil.testFormat
import com.arcvgc.app.testutil.testMatchDetail
import com.arcvgc.app.testutil.testMatchPreview
import com.arcvgc.app.testutil.testPlayerDetail
import com.arcvgc.app.testutil.testPlayerPreview
import kotlin.test.Test
import kotlin.test.assertEquals

class BattleCardUiMapperTest {

    @Test
    fun mapsMatchPreviewWithTwoPlayers() {
        val preview = testMatchPreview(
            id = 42,
            rating = 1500,
            format = testFormat(formattedName = "Reg H"),
            players = listOf(
                testPlayerPreview(1, "Alice", true),
                testPlayerPreview(2, "Bob", false)
            )
        )

        val result = BattleCardUiMapper.map(preview)

        assertEquals(42, result.id)
        assertEquals("Alice", result.player1.name)
        assertEquals("Bob", result.player2.name)
        assertEquals("Reg H", result.formatName)
        assertEquals("1500", result.rating)
        assertEquals(formatUploadTime(preview.uploadTime), result.formattedTime)
    }

    @Test
    fun mapsMatchDetailWithTwoPlayers() {
        val detail = testMatchDetail(
            id = 99,
            rating = 1700,
            format = testFormat(formattedName = "Reg G"),
            players = listOf(
                testPlayerDetail(1, "Charlie", true),
                testPlayerDetail(2, "Dana", false)
            )
        )

        val result = BattleCardUiMapper.map(detail)

        assertEquals(99, result.id)
        assertEquals("Charlie", result.player1.name)
        assertEquals("Dana", result.player2.name)
        assertEquals("Reg G", result.formatName)
        assertEquals("1700", result.rating)
    }

    @Test
    fun nullRatingShowsUnrated() {
        val preview = testMatchPreview(rating = null)

        val result = BattleCardUiMapper.map(preview)

        assertEquals("Unrated", result.rating)
    }

    @Test
    fun zeroPlayersProducesTwoUnknownPlayers() {
        val preview = testMatchPreview(players = emptyList())

        val result = BattleCardUiMapper.map(preview)

        assertEquals("Unknown", result.player1.name)
        assertEquals("Unknown", result.player2.name)
    }

    @Test
    fun onePlayerProducesUnknownPlayer2() {
        val preview = testMatchPreview(
            players = listOf(testPlayerPreview(1, "Solo"))
        )

        val result = BattleCardUiMapper.map(preview)

        assertEquals("Solo", result.player1.name)
        assertEquals("Unknown", result.player2.name)
    }

    @Test
    fun formatWithFormattedNameUsesFormattedName() {
        val preview = testMatchPreview(
            format = testFormat(name = "gen9vgc2024regh", formattedName = "Reg H")
        )

        val result = BattleCardUiMapper.map(preview)

        assertEquals("Reg H", result.formatName)
    }

    @Test
    fun formatWithoutFormattedNameUsesName() {
        val preview = testMatchPreview(
            format = testFormat(name = "gen9vgc2024regh", formattedName = null)
        )

        val result = BattleCardUiMapper.map(preview)

        assertEquals("gen9vgc2024regh", result.formatName)
    }

    @Test
    fun mapListPreservesOrder() {
        val previews = listOf(
            testMatchPreview(id = 1),
            testMatchPreview(id = 2),
            testMatchPreview(id = 3)
        )

        val result = BattleCardUiMapper.mapList(previews)

        assertEquals(3, result.size)
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
        assertEquals(3, result[2].id)
    }
}

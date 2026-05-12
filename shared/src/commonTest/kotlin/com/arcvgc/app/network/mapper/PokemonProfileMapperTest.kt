package com.arcvgc.app.network.mapper

import com.arcvgc.app.network.model.PokemonProfileDto
import com.arcvgc.app.network.model.TopAbilityDto
import com.arcvgc.app.network.model.TopItemDto
import com.arcvgc.app.network.model.TopMoveDto
import com.arcvgc.app.network.model.TopPlayerDto
import com.arcvgc.app.network.model.TopPlayerMatchDto
import com.arcvgc.app.network.model.TopTeammateDto
import com.arcvgc.app.network.model.TopTeraTypeDto
import com.arcvgc.app.network.model.TypeDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PokemonProfileMapperTest {

    @Test
    fun pokemonProfileDto_mapsBasicFields() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(147, result.id)
        assertEquals("Dragonite", result.name)
        assertEquals(149, result.pokedexNumber)
        assertEquals("OU", result.tier)
        assertEquals(712, result.matchCount)
        assertEquals(1.197, result.matchPercent, 0.001)
        assertNull(result.baseSpecies)
    }

    @Test
    fun pokemonProfileDto_mapsTypes() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(2, result.types.size)
        assertEquals("Dragon", result.types[0].name)
        assertEquals("Flying", result.types[1].name)
    }

    @Test
    fun pokemonProfileDto_mapsTopItems() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(2, result.topItems.size)
        assertEquals(639, result.topItems[0].count)
        assertEquals("Choice Band", result.topItems[0].name)
        assertEquals(50, result.topItems[1].count)
    }

    @Test
    fun pokemonProfileDto_mapsTopTeraTypes() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(2, result.topTeraTypes.size)
        assertEquals("Normal", result.topTeraTypes[0].name)
        assertEquals(676, result.topTeraTypes[0].count)
    }

    @Test
    fun pokemonProfileDto_mapsTopMoves() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(2, result.topMoves.size)
        assertEquals("Extreme Speed", result.topMoves[0].name)
        assertEquals(693, result.topMoves[0].count)
    }

    @Test
    fun pokemonProfileDto_mapsTopAbilities() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(2, result.topAbilities.size)
        assertEquals("Inner Focus", result.topAbilities[0].name)
        assertEquals(665, result.topAbilities[0].count)
    }

    @Test
    fun pokemonProfileDto_mapsTopTeammates() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(2, result.topTeammates.size)
        assertEquals("Chien-Pao", result.topTeammates[0].name)
        assertEquals(1002, result.topTeammates[0].pokedexNumber)
        assertEquals(632, result.topTeammates[0].count)
    }

    @Test
    fun pokemonProfileDto_toPokemonListItem_extractsBasicFields() {
        val dto = testPokemonProfileDto()

        val profile = dto.toDomain()
        val listItem = profile.toPokemonListItem()

        assertEquals(147, listItem.id)
        assertEquals("Dragonite", listItem.name)
        assertEquals(149, listItem.pokedexNumber)
        assertEquals("OU", listItem.tier)
        assertEquals(2, listItem.types.size)
    }

    @Test
    fun pokemonProfileDto_emptyLists_mapsToEmptyLists() {
        val dto = testPokemonProfileDto(
            topItems = emptyList(),
            topTeraTypes = emptyList(),
            topMoves = emptyList(),
            topAbilities = emptyList(),
            topTeammates = emptyList(),
            topPlayers = emptyList()
        )

        val result = dto.toDomain()

        assertTrue(result.topItems.isEmpty())
        assertTrue(result.topTeraTypes.isEmpty())
        assertTrue(result.topMoves.isEmpty())
        assertTrue(result.topAbilities.isEmpty())
        assertTrue(result.topTeammates.isEmpty())
        assertTrue(result.topPlayers.isEmpty())
    }

    @Test
    fun pokemonProfileDto_mapsTopPlayers() {
        val dto = testPokemonProfileDto()

        val result = dto.toDomain()

        assertEquals(2, result.topPlayers.size)
        val first = result.topPlayers[0]
        assertEquals(11594, first.id)
        assertEquals("dambro222", first.name)
        assertEquals(3, first.rankedWinCount)
        assertEquals(1361.3, first.avgRating!!, 0.001)
        assertEquals(1408, first.maxRating)
        assertEquals(1314, first.minRating)
        assertEquals(2, first.topMatches.size)
        assertEquals(161755, first.topMatches[0].id)
        assertEquals("gen9championsvgc2026regmabo3-2605822168", first.topMatches[0].showdownId)
        assertEquals("2026-05-09T15:59:36", first.topMatches[0].uploadTime)
        assertEquals(1253, first.topMatches[0].rating)
    }

    @Test
    fun pokemonProfileDto_mapsTopPlayers_withNullRating() {
        val dto = testPokemonProfileDto(
            topPlayers = listOf(
                TopPlayerDto(
                    id = 1,
                    name = "anon",
                    top5Matches = listOf(
                        TopPlayerMatchDto(
                            id = 99,
                            showdownId = "unrated-match-id",
                            uploadTime = "2026-05-09T15:59:36",
                            rating = null
                        )
                    )
                )
            )
        )

        val result = dto.toDomain()

        assertEquals(1, result.topPlayers.size)
        assertEquals(1, result.topPlayers[0].topMatches.size)
        assertNull(result.topPlayers[0].topMatches[0].rating)
        assertNull(result.topPlayers[0].avgRating)
    }

    @Test
    fun pokemonProfileDto_omittedTopPlayers_mapsToEmptyList() {
        val dto = PokemonProfileDto(
            id = 1,
            name = "Test",
            pokedexNumber = 1,
            tier = "OU",
            types = emptyList()
        )

        val result = dto.toDomain()

        assertTrue(result.topPlayers.isEmpty())
    }

    @Test
    fun pokemonProfileDto_defaultMatchFields_mapSafely() {
        val dto = PokemonProfileDto(
            id = 1,
            name = "Test",
            pokedexNumber = 1,
            tier = "OU",
            types = emptyList()
        )

        val result = dto.toDomain()

        assertEquals(0, result.matchCount)
        assertEquals(0.0, result.matchPercent, 0.001)
        assertTrue(result.topTeammates.isEmpty())
        assertTrue(result.topItems.isEmpty())
    }

    private fun testPokemonProfileDto(
        topItems: List<TopItemDto> = listOf(
            TopItemDto(count = 639, id = 52, name = "Choice Band", imageUrl = "https://arcvgc.com/static/images/items/choiceband.png"),
            TopItemDto(count = 50, id = 186, name = "Loaded Dice", imageUrl = "https://arcvgc.com/static/images/items/loadeddice.png")
        ),
        topTeraTypes: List<TopTeraTypeDto> = listOf(
            TopTeraTypeDto(count = 676, id = 13, name = "Normal", imageUrl = "https://arcvgc.com/static/images/types/normal.png"),
            TopTeraTypeDto(count = 31, id = 17, name = "Steel", imageUrl = "https://arcvgc.com/static/images/types/steel.png")
        ),
        topMoves: List<TopMoveDto> = listOf(
            TopMoveDto(count = 693, id = 10, name = "Extreme Speed"),
            TopMoveDto(count = 613, id = 145, name = "Outrage")
        ),
        topAbilities: List<TopAbilityDto> = listOf(
            TopAbilityDto(count = 665, id = 46, name = "Inner Focus"),
            TopAbilityDto(count = 64, id = 132, name = "Multiscale")
        ),
        topTeammates: List<TopTeammateDto> = listOf(
            TopTeammateDto(count = 632, id = 1000, name = "Chien-Pao", pokedexNumber = 1002, imageUrl = "https://arcvgc.com/static/images/pokemon/chien-pao.png"),
            TopTeammateDto(count = 607, id = 1382, name = "Zamazenta-Crowned", pokedexNumber = 889, imageUrl = "https://arcvgc.com/static/images/pokemon/zamazenta-crowned.png")
        ),
        topPlayers: List<TopPlayerDto> = listOf(
            TopPlayerDto(
                id = 11594,
                name = "dambro222",
                top5Matches = listOf(
                    TopPlayerMatchDto(
                        id = 161755,
                        showdownId = "gen9championsvgc2026regmabo3-2605822168",
                        uploadTime = "2026-05-09T15:59:36",
                        rating = 1253
                    ),
                    TopPlayerMatchDto(
                        id = 154801,
                        showdownId = "gen9championsvgc2026regmabo3-2601590987",
                        uploadTime = "2026-05-04T14:59:43",
                        rating = 1534
                    )
                ),
                rankedWinCount = 3,
                avgRating = 1361.3,
                maxRating = 1408,
                minRating = 1314
            ),
            TopPlayerDto(
                id = 20,
                name = "darthmorton",
                top5Matches = emptyList(),
                rankedWinCount = 1,
                avgRating = 1534.0,
                maxRating = 1534,
                minRating = 1534
            )
        )
    ) = PokemonProfileDto(
        id = 147,
        name = "Dragonite",
        pokedexNumber = 149,
        tier = "OU",
        types = listOf(
            TypeDto(id = 3, name = "Dragon", imageUrl = "https://arcvgc.com/static/images/types/dragon.png"),
            TypeDto(id = 8, name = "Flying", imageUrl = "https://arcvgc.com/static/images/types/flying.png")
        ),
        imageUrl = "https://arcvgc.com/static/images/pokemon/dragonite.png",
        baseSpecies = null,
        matchCount = 712,
        matchPercent = 1.1973027057023224,
        topItems = topItems,
        topTeraTypes = topTeraTypes,
        topMoves = topMoves,
        topAbilities = topAbilities,
        topTeammates = topTeammates,
        topPlayers = topPlayers
    )
}

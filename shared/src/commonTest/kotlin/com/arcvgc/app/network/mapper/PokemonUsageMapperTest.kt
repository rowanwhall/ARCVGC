package com.arcvgc.app.network.mapper

import com.arcvgc.app.network.model.PokemonUsageDataDto
import com.arcvgc.app.network.model.PokemonUsageDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PokemonUsageMapperTest {

    @Test
    fun usageDataDto_mapsTotals() {
        val dto = testUsageDataDto()

        val result = dto.toDomain()

        assertEquals(15432, result.prevPeriodTotalTeams)
        assertEquals(22382, result.currentPeriodTotalTeams)
    }

    @Test
    fun usageDataDto_mapsIncreasedAndDecreased() {
        val dto = testUsageDataDto()

        val result = dto.toDomain()

        assertEquals(1, result.increased.size)
        assertEquals(1, result.decreased.size)
        assertEquals("Whimsicott", result.increased[0].name)
        assertEquals("Aerodactyl", result.decreased[0].name)
    }

    @Test
    fun pokemonUsageDto_mapsAllFields() {
        val result = testIncreasedDto().toDomain()

        assertEquals(545, result.id)
        assertEquals("Whimsicott", result.name)
        assertEquals(547, result.pokedexNumber)
        assertEquals("https://arcvgc.com/static/images/pokemon/whimsicott.png", result.imageUrl)
        assertEquals(2224, result.prevPeriodTeamCount)
        assertEquals(14.41, result.prevPeriodTeamPercent)
        assertEquals(4364, result.currentPeriodTeamCount)
        assertEquals(19.5, result.currentPeriodTeamPercent)
        assertEquals(5.09, result.usageChangePercent)
    }

    @Test
    fun pokemonUsageDto_mapsNegativeChange() {
        val result = testDecreasedDto().toDomain()

        assertEquals(-6.74, result.usageChangePercent)
    }

    @Test
    fun usageDataDto_nullLists_mapToEmpty() {
        val dto = testUsageDataDto(increased = null, decreased = null)

        val result = dto.toDomain()

        assertTrue(result.increased.isEmpty())
        assertTrue(result.decreased.isEmpty())
    }

    @Test
    fun pokemonUsageDto_nullPokedexNumber_mapsToNull() {
        val result = testIncreasedDto().copy(pokedexNumber = null).toDomain()

        assertNull(result.pokedexNumber)
    }

    private fun testIncreasedDto() = PokemonUsageDto(
        id = 545,
        name = "Whimsicott",
        pokedexNumber = 547,
        imageUrl = "https://arcvgc.com/static/images/pokemon/whimsicott.png",
        prevPeriodTeamCount = 2224,
        prevPeriodTeamPercent = 14.41,
        currentPeriodTeamCount = 4364,
        currentPeriodTeamPercent = 19.5,
        usageChangePercent = 5.09
    )

    private fun testDecreasedDto() = PokemonUsageDto(
        id = 140,
        name = "Aerodactyl",
        pokedexNumber = 142,
        imageUrl = "https://arcvgc.com/static/images/pokemon/aerodactyl.png",
        prevPeriodTeamCount = 3805,
        prevPeriodTeamPercent = 24.66,
        currentPeriodTeamCount = 4009,
        currentPeriodTeamPercent = 17.91,
        usageChangePercent = -6.74
    )

    private fun testUsageDataDto(
        increased: List<PokemonUsageDto>? = listOf(testIncreasedDto()),
        decreased: List<PokemonUsageDto>? = listOf(testDecreasedDto())
    ) = PokemonUsageDataDto(
        prevPeriodTotalTeams = 15432,
        currentPeriodTotalTeams = 22382,
        increased = increased,
        decreased = decreased
    )
}

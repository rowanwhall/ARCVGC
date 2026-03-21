package com.arcvgc.app.network.mapper

import com.arcvgc.app.network.model.BaseSpeciesDto
import com.arcvgc.app.network.model.FormatDetailDto
import com.arcvgc.app.network.model.TopPokemonDto
import com.arcvgc.app.network.model.TypeDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FormatDetailMapperTest {

    @Test
    fun formatDetailDto_mapsBasicFields() {
        val dto = testFormatDetailDto()

        val result = dto.toDomain()

        assertEquals(2, result.id)
        assertEquals("gen9vgc2026regibo3", result.name)
        assertEquals("[Gen 9] VGC 2026 Reg I (Bo3)", result.formattedName)
        assertEquals(11241, result.matchCount)
        assertEquals(22482, result.teamCount)
    }

    @Test
    fun formatDetailDto_mapsTopPokemon() {
        val dto = testFormatDetailDto()

        val result = dto.toDomain()

        assertEquals(2, result.topPokemon.size)
        assertEquals(725, result.topPokemon[0].id)
        assertEquals("Incineroar", result.topPokemon[0].name)
        assertEquals(727, result.topPokemon[0].pokedexNumber)
        assertEquals(10365, result.topPokemon[0].count)
        assertEquals(2, result.topPokemon[0].types.size)
        assertEquals("Dark", result.topPokemon[0].types[0].name)
        assertEquals("Fire", result.topPokemon[0].types[1].name)
    }

    @Test
    fun topPokemonDto_mapsImageUrl() {
        val dto = testTopPokemonDto()

        val result = dto.toDomain()

        assertEquals("https://arcvgc.com/static/images/pokemon/incineroar.png", result.imageUrl)
    }

    @Test
    fun formatDetailDto_nullTopPokemon_mapsToEmptyList() {
        val dto = testFormatDetailDto(topPokemon = null)

        val result = dto.toDomain()

        assertTrue(result.topPokemon.isEmpty())
    }

    @Test
    fun formatDetailDto_emptyTopPokemon_mapsToEmptyList() {
        val dto = testFormatDetailDto(topPokemon = emptyList())

        val result = dto.toDomain()

        assertTrue(result.topPokemon.isEmpty())
    }

    @Test
    fun formatDetailDto_nullFormattedName_mapsToNull() {
        val dto = testFormatDetailDto().copy(formattedName = null)

        val result = dto.toDomain()

        assertNull(result.formattedName)
    }

    @Test
    fun topPokemonDto_mapsTypes() {
        val dto = testTopPokemonDto()

        val result = dto.toDomain()

        assertEquals(2, result.types.size)
        assertEquals("Dark", result.types[0].name)
        assertEquals("Fire", result.types[1].name)
    }

    private fun testTopPokemonDto() = TopPokemonDto(
        id = 725,
        name = "Incineroar",
        pokedexNumber = 727,
        tier = "NU",
        types = listOf(
            TypeDto(id = 2, name = "Dark", imageUrl = "https://arcvgc.com/static/images/types/dark.png"),
            TypeDto(id = 7, name = "Fire", imageUrl = "https://arcvgc.com/static/images/types/fire.png")
        ),
        imageUrl = "https://arcvgc.com/static/images/pokemon/incineroar.png",
        count = 10365
    )

    private fun testFormatDetailDto(
        topPokemon: List<TopPokemonDto>? = listOf(
            testTopPokemonDto(),
            TopPokemonDto(
                id = 1006,
                name = "Miraidon",
                pokedexNumber = 1008,
                tier = "AG",
                types = listOf(
                    TypeDto(id = 3, name = "Dragon", imageUrl = "https://arcvgc.com/static/images/types/dragon.png"),
                    TypeDto(id = 4, name = "Electric", imageUrl = "https://arcvgc.com/static/images/types/electric.png")
                ),
                imageUrl = "https://arcvgc.com/static/images/pokemon/miraidon.png",
                count = 7226
            )
        )
    ) = FormatDetailDto(
        id = 2,
        name = "gen9vgc2026regibo3",
        formattedName = "[Gen 9] VGC 2026 Reg I (Bo3)",
        matchCount = 11241,
        teamCount = 22482,
        topPokemon = topPokemon
    )
}

package com.arcvgc.app.ui.mapper

import com.arcvgc.app.testutil.testDomainItem
import com.arcvgc.app.testutil.testFormat
import com.arcvgc.app.testutil.testPokemonListItem
import com.arcvgc.app.testutil.testPokemonType
import com.arcvgc.app.testutil.testTeraType
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogUiMappersTest {

    // --- PokemonPickerUiMapper ---

    @Test
    fun pokemonPickerMapperMapsFields() {
        val pokemon = testPokemonListItem(
            id = 25,
            name = "Pikachu",
            imageUrl = "https://arcvgc.com/img/pokemon/pikachu.png",
            types = listOf(
                testPokemonType(1, "Electric", "https://arcvgc.com/img/types/electric.png")
            )
        )

        val result = PokemonPickerUiMapper.map(pokemon)

        assertEquals(25, result.id)
        assertEquals("Pikachu", result.name)
        assertEquals("https://arcvgc.com/img/pokemon/pikachu.png", result.imageUrl)
        assertEquals(1, result.types.size)
        assertEquals("Electric", result.types[0].name)
        assertEquals("https://arcvgc.com/img/types/electric.png", result.types[0].imageUrl)
    }

    @Test
    fun pokemonPickerMapListPreservesOrder() {
        val list = listOf(
            testPokemonListItem(id = 1, name = "Bulbasaur"),
            testPokemonListItem(id = 4, name = "Charmander"),
            testPokemonListItem(id = 7, name = "Squirtle")
        )

        val result = PokemonPickerUiMapper.mapList(list)

        assertEquals(3, result.size)
        assertEquals(1, result[0].id)
        assertEquals(4, result[1].id)
        assertEquals(7, result[2].id)
    }

    // --- ItemUiMapper ---

    @Test
    fun itemMapperMapsFieldsUsingRawName() {
        val item = testDomainItem(
            id = 10,
            name = "BoosterEnergy",
            imageUrl = "https://arcvgc.com/img/items/boosterenergy.png"
        )

        val result = ItemUiMapper.map(item)

        assertEquals(10, result.id)
        assertEquals("BoosterEnergy", result.name)
        assertEquals("https://arcvgc.com/img/items/boosterenergy.png", result.imageUrl)
    }

    @Test
    fun itemMapListPreservesOrder() {
        val items = listOf(
            testDomainItem(id = 1, name = "ChoiceBand"),
            testDomainItem(id = 2, name = "LifeOrb")
        )

        val result = ItemUiMapper.mapList(items)

        assertEquals(2, result.size)
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
    }

    // --- TeraTypeUiMapper ---

    @Test
    fun teraTypeMapperMapsFields() {
        val tera = testTeraType(
            id = 3,
            name = "Water",
            imageUrl = "https://arcvgc.com/img/tera/water.png"
        )

        val result = TeraTypeUiMapper.map(tera)

        assertEquals(3, result.id)
        assertEquals("Water", result.name)
        assertEquals("https://arcvgc.com/img/tera/water.png", result.imageUrl)
    }

    @Test
    fun teraTypeMapListPreservesOrder() {
        val types = listOf(
            testTeraType(id = 1, name = "Fire"),
            testTeraType(id = 2, name = "Water"),
            testTeraType(id = 3, name = "Grass")
        )

        val result = TeraTypeUiMapper.mapList(types)

        assertEquals(3, result.size)
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
        assertEquals(3, result[2].id)
    }

    // --- FormatUiMapper ---

    @Test
    fun formatMapperWithFormattedNameUsesFormattedName() {
        val format = testFormat(id = 1, name = "gen9vgc2024regh", formattedName = "Reg H")

        val result = FormatUiMapper.map(format)

        assertEquals(1, result.id)
        assertEquals("Reg H", result.displayName)
    }

    @Test
    fun formatMapperWithoutFormattedNameUsesName() {
        val format = testFormat(id = 2, name = "gen9vgc2024regh", formattedName = null)

        val result = FormatUiMapper.map(format)

        assertEquals(2, result.id)
        assertEquals("gen9vgc2024regh", result.displayName)
    }

    @Test
    fun formatMapListPreservesOrder() {
        val formats = listOf(
            testFormat(id = 1, name = "format1"),
            testFormat(id = 2, name = "format2"),
            testFormat(id = 3, name = "format3")
        )

        val result = FormatUiMapper.mapList(formats)

        assertEquals(3, result.size)
        assertEquals(1, result[0].id)
        assertEquals(2, result[1].id)
        assertEquals(3, result[2].id)
    }
}

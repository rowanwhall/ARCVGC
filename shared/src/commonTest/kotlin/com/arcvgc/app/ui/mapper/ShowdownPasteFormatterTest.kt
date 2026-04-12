package com.arcvgc.app.ui.mapper

import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonDetailUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.TypeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals

class ShowdownPasteFormatterTest {

    private fun pokemon(
        name: String = "Pikachu",
        item: ItemUiModel? = ItemUiModel(1, "Choice Scarf", null),
        abilityName: String? = "Static",
        moves: List<String> = listOf("Thunderbolt", "Volt Switch", "Surf", "Protect"),
        teraType: TeraTypeUiModel? = TeraTypeUiModel(1, "Electric", null)
    ) = PokemonDetailUiModel(
        id = 1,
        name = name,
        imageUrl = null,
        item = item,
        abilityName = abilityName,
        moves = moves,
        types = listOf(TypeUiModel("Electric", null)),
        teraType = teraType
    )

    @Test
    fun singlePokemonFullFormat() {
        val result = ShowdownPasteFormatter.format(listOf(pokemon()))
        val expected = """
            Pikachu @ Choice Scarf
            Ability: Static
            Level: 50
            Tera Type: Electric
            - Thunderbolt
            - Volt Switch
            - Surf
            - Protect
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun pokemonWithoutItem_noAtLine() {
        val result = ShowdownPasteFormatter.format(listOf(pokemon(item = null)))
        val expected = """
            Pikachu
            Ability: Static
            Level: 50
            Tera Type: Electric
            - Thunderbolt
            - Volt Switch
            - Surf
            - Protect
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun pokemonWithoutTeraType_noTeraLine() {
        val result = ShowdownPasteFormatter.format(listOf(pokemon(teraType = null)))
        val expected = """
            Pikachu @ Choice Scarf
            Ability: Static
            Level: 50
            - Thunderbolt
            - Volt Switch
            - Surf
            - Protect
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun multiplePokemon_doubleNewlineSeparated() {
        val mon1 = pokemon(name = "Pikachu")
        val mon2 = pokemon(
            name = "Charizard",
            item = ItemUiModel(2, "Life Orb", null),
            abilityName = "Solar Power",
            moves = listOf("Flamethrower", "Air Slash"),
            teraType = TeraTypeUiModel(2, "Fire", null)
        )
        val result = ShowdownPasteFormatter.format(listOf(mon1, mon2))
        val expected = """
            Pikachu @ Choice Scarf
            Ability: Static
            Level: 50
            Tera Type: Electric
            - Thunderbolt
            - Volt Switch
            - Surf
            - Protect

            Charizard @ Life Orb
            Ability: Solar Power
            Level: 50
            Tera Type: Fire
            - Flamethrower
            - Air Slash
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun closedTeamsheet_omitsAbilityAndMoves() {
        val result = ShowdownPasteFormatter.format(listOf(pokemon(
            abilityName = null,
            item = null,
            moves = emptyList(),
            teraType = null
        )))
        val expected = """
            Pikachu
            Level: 50
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun emptyMovesList_noMoveLines() {
        val result = ShowdownPasteFormatter.format(listOf(pokemon(moves = emptyList())))
        val expected = """
            Pikachu @ Choice Scarf
            Ability: Static
            Level: 50
            Tera Type: Electric
        """.trimIndent()
        assertEquals(expected, result)
    }
}

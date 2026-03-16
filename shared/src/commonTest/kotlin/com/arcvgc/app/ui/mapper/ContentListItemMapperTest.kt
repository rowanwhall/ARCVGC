package com.arcvgc.app.ui.mapper

import com.arcvgc.app.testutil.testPlayerListItem
import com.arcvgc.app.testutil.testPokemonListItem
import com.arcvgc.app.testutil.testPokemonType
import com.arcvgc.app.ui.model.BattleCardUiModel
import com.arcvgc.app.ui.model.ContentListItem
import com.arcvgc.app.ui.model.PlayerUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TypeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentListItemMapperTest {

    private fun testBattleCardUiModel(id: Int = 1): BattleCardUiModel {
        val emptyPlayer = PlayerUiModel(name = "Player", isWinner = null, team = emptyList())
        return BattleCardUiModel(
            id = id,
            player1 = emptyPlayer,
            player2 = emptyPlayer,
            formatName = "Reg H",
            rating = "1500",
            formattedTime = "Feb 8, 5:03 PM"
        )
    }

    private fun testPokemonPickerUiModel(
        id: Int = 1,
        name: String = "Pikachu",
        imageUrl: String? = "https://arcvgc.com/img/pokemon/pikachu.png",
        types: List<TypeUiModel> = listOf(TypeUiModel("Electric", "https://arcvgc.com/img/types/electric.png"))
    ) = PokemonPickerUiModel(id = id, name = name, imageUrl = imageUrl, types = types)

    @Test
    fun fromBattlesWrapsEachInBattle() {
        val battles = listOf(testBattleCardUiModel(1), testBattleCardUiModel(2))

        val result = ContentListItemMapper.fromBattles(battles)

        assertEquals(2, result.size)
        assertTrue(result[0] is ContentListItem.Battle)
        assertEquals(1, (result[0] as ContentListItem.Battle).uiModel.id)
        assertEquals(2, (result[1] as ContentListItem.Battle).uiModel.id)
    }

    @Test
    fun fromPokemonExtractsFieldsCorrectly() {
        val pokemon = listOf(
            testPokemonListItem(
                id = 25,
                name = "Pikachu",
                imageUrl = "https://arcvgc.com/img/pokemon/pikachu.png",
                types = listOf(testPokemonType(1, "Electric", "https://arcvgc.com/img/types/electric.png"))
            )
        )

        val result = ContentListItemMapper.fromPokemon(pokemon)

        assertEquals(1, result.size)
        val item = result[0] as ContentListItem.Pokemon
        assertEquals(25, item.id)
        assertEquals("Pikachu", item.name)
        assertEquals("https://arcvgc.com/img/pokemon/pikachu.png", item.imageUrl)
        assertEquals(1, item.types.size)
        assertEquals("Electric", item.types[0].name)
    }

    @Test
    fun fromPlayersWrapsInPlayer() {
        val players = listOf(
            testPlayerListItem(id = 1, name = "Alice"),
            testPlayerListItem(id = 2, name = "Bob")
        )

        val result = ContentListItemMapper.fromPlayers(players)

        assertEquals(2, result.size)
        val p1 = result[0] as ContentListItem.Player
        assertEquals(1, p1.id)
        assertEquals("Alice", p1.name)
        val p2 = result[1] as ContentListItem.Player
        assertEquals(2, p2.id)
        assertEquals("Bob", p2.name)
    }

    @Test
    fun fromPokemonCatalogMapsIdsToEntries() {
        val catalog = listOf(
            testPokemonPickerUiModel(id = 25, name = "Pikachu"),
            testPokemonPickerUiModel(id = 6, name = "Charizard")
        )

        val result = ContentListItemMapper.fromPokemonCatalog(
            pokemonIds = listOf(6, 25),
            catalog = catalog
        )

        assertEquals(2, result.size)
        assertEquals("Charizard", (result[0] as ContentListItem.Pokemon).name)
        assertEquals("Pikachu", (result[1] as ContentListItem.Pokemon).name)
    }

    @Test
    fun fromPokemonCatalogFiltersMissingIds() {
        val catalog = listOf(
            testPokemonPickerUiModel(id = 25, name = "Pikachu")
        )

        val result = ContentListItemMapper.fromPokemonCatalog(
            pokemonIds = listOf(25, 999),
            catalog = catalog
        )

        assertEquals(1, result.size)
        assertEquals("Pikachu", (result[0] as ContentListItem.Pokemon).name)
    }

    @Test
    fun emptyListProducesEmptyResult() {
        assertTrue(ContentListItemMapper.fromBattles(emptyList()).isEmpty())
        assertTrue(ContentListItemMapper.fromPokemon(emptyList()).isEmpty())
        assertTrue(ContentListItemMapper.fromPlayers(emptyList()).isEmpty())
        assertTrue(ContentListItemMapper.fromPokemonCatalog(emptyList(), emptyList()).isEmpty())
    }
}

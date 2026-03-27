package com.arcvgc.app.ui.search

import com.arcvgc.app.ui.model.AbilityUiModel
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.TypeUiModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SearchStateReducerTest {

    // --- Initial State ---

    @Test
    fun initialState_hasEmptyFilterSlots() {
        val state = SearchStateReducer.initialState()
        assertTrue(state.filterSlots.isEmpty())
    }

    @Test
    fun initialState_canAddMore() {
        val state = SearchStateReducer.initialState()
        assertTrue(state.canAddMoreTeam1)
    }

    @Test
    fun initialState_defaultOrderByIsRating() {
        val state = SearchStateReducer.initialState()
        assertEquals("rating", state.selectedOrderBy)
    }

    @Test
    fun initialState_noRatingFilters() {
        val state = SearchStateReducer.initialState()
        assertNull(state.selectedMinRating)
        assertNull(state.selectedMaxRating)
        assertFalse(state.unratedOnly)
    }

    // --- addPokemon ---

    @Test
    fun addPokemon_addsToFilterSlots() {
        val state = SearchStateReducer.initialState()
        val result = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 10, name = "Pikachu"))

        assertEquals(1, result.filterSlots.size)
        assertEquals(10, result.filterSlots[0].pokemonId)
        assertEquals("Pikachu", result.filterSlots[0].pokemonName)
    }

    @Test
    fun addPokemon_multipleAdds() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Bulbasaur"))
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 4, name = "Charmander"))
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 7, name = "Squirtle"))

        assertEquals(3, state.filterSlots.size)
        assertEquals("Squirtle", state.filterSlots[2].pokemonName)
    }

    @Test
    fun addPokemon_maxSixSlots() {
        var state = SearchStateReducer.initialState()
        repeat(6) { i -> state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = i, name = "Mon$i")) }

        assertFalse(state.canAddMoreTeam1)
        assertEquals(6, state.filterSlots.size)

        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 99, name = "Extra"))
        assertEquals(6, state.filterSlots.size)
    }

    // --- removePokemon ---

    @Test
    fun removePokemon_removesAtIndex() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Bulbasaur"))
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 4, name = "Charmander"))
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 7, name = "Squirtle"))

        state = SearchStateReducer.removePokemon(state, 1)

        assertEquals(2, state.filterSlots.size)
        assertEquals("Bulbasaur", state.filterSlots[0].pokemonName)
        assertEquals("Squirtle", state.filterSlots[1].pokemonName)
    }

    @Test
    fun removePokemon_restoresCanAddMore() {
        var state = SearchStateReducer.initialState()
        repeat(6) { i -> state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = i, name = "Mon$i")) }
        assertFalse(state.canAddMoreTeam1)

        state = SearchStateReducer.removePokemon(state, 0)
        assertTrue(state.canAddMoreTeam1)
    }

    // --- setItem ---

    @Test
    fun setItem_updatesSlot() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))
        assertNull(state.filterSlots[0].item)

        val item = ItemUiModel(id = 5, name = "Choice Band", imageUrl = null)
        state = SearchStateReducer.setItem(state, 0, item)

        assertEquals(5, state.filterSlots[0].item?.id)
        assertEquals("Choice Band", state.filterSlots[0].item?.name)
    }

    @Test
    fun setItem_doesNotAffectOtherSlots() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 2, name = "Charizard"))

        state = SearchStateReducer.setItem(state, 0, ItemUiModel(id = 5, name = "Choice Band", imageUrl = null))

        assertNull(state.filterSlots[1].item)
    }

    // --- setTeraType ---

    @Test
    fun setTeraType_updatesSlot() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))

        val tera = TeraTypeUiModel(id = 3, name = "Water", imageUrl = null)
        state = SearchStateReducer.setTeraType(state, 0, tera)

        assertEquals(3, state.filterSlots[0].teraType?.id)
        assertEquals("Water", state.filterSlots[0].teraType?.name)
    }

    // --- setFormat ---

    @Test
    fun setFormat_updatesSelectedFormat() {
        val state = SearchStateReducer.initialState()
        val format = FormatUiModel(id = 3, displayName = "Reg F")

        val result = SearchStateReducer.setFormat(state, format)

        assertEquals(3, result.selectedFormat?.id)
        assertEquals("Reg F", result.selectedFormat?.displayName)
    }

    // --- setDefaultFormat ---

    @Test
    fun setDefaultFormat_setsFormatWhenNull() {
        val state = SearchStateReducer.initialState()
        val format = FormatUiModel(id = 5, displayName = "Reg G")

        val result = SearchStateReducer.setDefaultFormat(state, format)

        assertEquals(5, result.selectedFormat?.id)
        assertEquals("Reg G", result.selectedFormat?.displayName)
    }

    @Test
    fun setDefaultFormat_doesNotOverwriteExistingFormat() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.setFormat(state, FormatUiModel(id = 99, displayName = "Custom"))

        state = SearchStateReducer.setDefaultFormat(state, FormatUiModel(id = 5, displayName = "Reg G"))

        assertEquals(99, state.selectedFormat?.id)
    }

    // --- Rating ---

    @Test
    fun setMinRating_updatesState() {
        val state = SearchStateReducer.initialState()
        val result = SearchStateReducer.setMinRating(state, 1500)
        assertEquals(1500, result.selectedMinRating)
    }

    @Test
    fun setMaxRating_updatesState() {
        val state = SearchStateReducer.initialState()
        val result = SearchStateReducer.setMaxRating(state, 1800)
        assertEquals(1800, result.selectedMaxRating)
    }

    @Test
    fun setMinRating_null_clearsRating() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.setMinRating(state, 1500)
        state = SearchStateReducer.setMinRating(state, null)
        assertNull(state.selectedMinRating)
    }

    // --- setUnratedOnly ---

    @Test
    fun setUnratedOnly_true_clearsRatingsAndSwitchesSortOrder() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.setMinRating(state, 1500)
        state = SearchStateReducer.setMaxRating(state, 1800)
        state = SearchStateReducer.setOrderBy(state, "rating")

        state = SearchStateReducer.setUnratedOnly(state, true)

        assertTrue(state.unratedOnly)
        assertNull(state.selectedMinRating)
        assertNull(state.selectedMaxRating)
        assertEquals("time", state.selectedOrderBy)
    }

    @Test
    fun setUnratedOnly_true_doesNotSwitchNonRatingSortOrder() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.setOrderBy(state, "time")

        state = SearchStateReducer.setUnratedOnly(state, true)

        assertEquals("time", state.selectedOrderBy)
    }

    @Test
    fun setUnratedOnly_false_clearsUnratedFlag() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.setUnratedOnly(state, true)
        state = SearchStateReducer.setUnratedOnly(state, false)
        assertFalse(state.unratedOnly)
    }

    // --- Time Range ---

    @Test
    fun setTimeRange_updatesState() {
        val state = SearchStateReducer.initialState()
        val result = SearchStateReducer.setTimeRange(state, 1000L, 2000L)
        assertEquals(1000L, result.timeRangeStart)
        assertEquals(2000L, result.timeRangeEnd)
    }

    @Test
    fun setTimeRange_nulls_clearsRange() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.setTimeRange(state, 1000L, 2000L)
        state = SearchStateReducer.setTimeRange(state, null, null)
        assertNull(state.timeRangeStart)
        assertNull(state.timeRangeEnd)
    }

    // --- Player Name ---

    @Test
    fun setPlayerName_updatesState() {
        val state = SearchStateReducer.initialState()
        val result = SearchStateReducer.setPlayerName(state, "WolfeGlick")
        assertEquals("WolfeGlick", result.playerName)
    }

    // --- Order By ---

    @Test
    fun setOrderBy_updatesState() {
        val state = SearchStateReducer.initialState()
        val result = SearchStateReducer.setOrderBy(state, "time")
        assertEquals("time", result.selectedOrderBy)
    }

    // --- Team 2 ---

    @Test
    fun addTeam2Pokemon_addsToTeam2FilterSlots() {
        val state = SearchStateReducer.initialState()
        val result = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 10, name = "Pikachu"))

        assertEquals(1, result.team2FilterSlots.size)
        assertEquals(10, result.team2FilterSlots[0].pokemonId)
        assertTrue(result.filterSlots.isEmpty())
    }

    @Test
    fun addTeam2Pokemon_maxSixSlots() {
        var state = SearchStateReducer.initialState()
        repeat(6) { i -> state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = i, name = "Mon$i")) }

        assertFalse(state.canAddMoreTeam2)
        assertEquals(6, state.team2FilterSlots.size)

        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 99, name = "Extra"))
        assertEquals(6, state.team2FilterSlots.size)
    }

    @Test
    fun removeTeam2Pokemon_removesAtIndex() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 1, name = "Bulbasaur"))
        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 4, name = "Charmander"))

        state = SearchStateReducer.removeTeam2Pokemon(state, 0)

        assertEquals(1, state.team2FilterSlots.size)
        assertEquals("Charmander", state.team2FilterSlots[0].pokemonName)
    }

    @Test
    fun setTeam2Item_updatesSlot() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))

        val item = ItemUiModel(id = 5, name = "Choice Band", imageUrl = null)
        state = SearchStateReducer.setTeam2Item(state, 0, item)

        assertEquals(5, state.team2FilterSlots[0].item?.id)
    }

    @Test
    fun setTeam2TeraType_updatesSlot() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))

        val tera = TeraTypeUiModel(id = 3, name = "Water", imageUrl = null)
        state = SearchStateReducer.setTeam2TeraType(state, 0, tera)

        assertEquals(3, state.team2FilterSlots[0].teraType?.id)
    }

    @Test
    fun hasTeam2_isTrueWhenTeam2HasSlots() {
        var state = SearchStateReducer.initialState()
        assertFalse(state.hasTeam2)

        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 1, name = "Bulbasaur"))
        assertTrue(state.hasTeam2)
    }

    @Test
    fun removePokemon_promotesTeam2WhenTeam1Empty() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Bulbasaur"))
        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 4, name = "Charmander"))

        state = SearchStateReducer.removePokemon(state, 0)

        assertEquals(1, state.filterSlots.size)
        assertEquals("Charmander", state.filterSlots[0].pokemonName)
        assertTrue(state.team2FilterSlots.isEmpty())
        assertFalse(state.hasTeam2)
    }

    // --- Ability ---

    @Test
    fun setAbility_updatesSlot() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))

        val ability = AbilityUiModel(id = 10, name = "Static")
        state = SearchStateReducer.setAbility(state, 0, ability)

        assertEquals(10, state.filterSlots[0].ability?.id)
        assertEquals("Static", state.filterSlots[0].ability?.name)
    }

    @Test
    fun setAbility_doesNotAffectOtherSlots() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))
        state = SearchStateReducer.addPokemon(state, testPokemonPicker(id = 2, name = "Charizard"))

        state = SearchStateReducer.setAbility(state, 0, AbilityUiModel(id = 10, name = "Static"))

        assertNull(state.filterSlots[1].ability)
    }

    @Test
    fun setTeam2Ability_updatesSlot() {
        var state = SearchStateReducer.initialState()
        state = SearchStateReducer.addTeam2Pokemon(state, testPokemonPicker(id = 1, name = "Pikachu"))

        val ability = AbilityUiModel(id = 10, name = "Static")
        state = SearchStateReducer.setTeam2Ability(state, 0, ability)

        assertEquals(10, state.team2FilterSlots[0].ability?.id)
    }

    // --- Helpers ---

    private fun testPokemonPicker(id: Int, name: String) = PokemonPickerUiModel(
        id = id,
        name = name,
        imageUrl = null,
        types = listOf(TypeUiModel(name = "Normal", imageUrl = null))
    )
}

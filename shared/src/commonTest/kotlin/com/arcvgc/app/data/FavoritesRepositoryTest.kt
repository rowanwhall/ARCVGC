package com.arcvgc.app.data

import com.arcvgc.app.testutil.FakeFavoritesStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FavoritesRepositoryTest {

    private fun createRepo(storage: FakeFavoritesStorage = FakeFavoritesStorage()) =
        FavoritesRepository(storage)

    @Test
    fun togglePokemonFavorite_addsNewId() {
        val repo = createRepo()
        repo.togglePokemonFavorite(42)
        assertEquals(setOf(42), repo.favoritePokemonIds.value)
    }

    @Test
    fun togglePokemonFavorite_twiceRemovesId() {
        val repo = createRepo()
        repo.togglePokemonFavorite(42)
        repo.togglePokemonFavorite(42)
        assertEquals(emptySet(), repo.favoritePokemonIds.value)
    }

    @Test
    fun isPokemonFavorited_trueAfterToggle() {
        val repo = createRepo()
        repo.togglePokemonFavorite(10)
        assertTrue(repo.isPokemonFavorited(10))
    }

    @Test
    fun isPokemonFavorited_falseAfterDoubleToggle() {
        val repo = createRepo()
        repo.togglePokemonFavorite(10)
        repo.togglePokemonFavorite(10)
        assertFalse(repo.isPokemonFavorited(10))
    }

    @Test
    fun toggleBattleFavorite_addsAndRemoves() {
        val repo = createRepo()
        repo.toggleBattleFavorite(99)
        assertTrue(repo.isBattleFavorited(99))
        assertEquals(setOf(99), repo.favoriteBattleIds.value)

        repo.toggleBattleFavorite(99)
        assertFalse(repo.isBattleFavorited(99))
        assertEquals(emptySet(), repo.favoriteBattleIds.value)
    }

    @Test
    fun togglePlayerFavorite_addsString() {
        val repo = createRepo()
        repo.togglePlayerFavorite("Ash")
        assertTrue(repo.isPlayerFavorited("Ash"))
        assertEquals(setOf("Ash"), repo.favoritePlayerNames.value)
    }

    @Test
    fun isPlayerFavorited_falseAfterDoubleToggle() {
        val repo = createRepo()
        repo.togglePlayerFavorite("Ash")
        repo.togglePlayerFavorite("Ash")
        assertFalse(repo.isPlayerFavorited("Ash"))
    }

    @Test
    fun clearAll_emptiesAllSets() {
        val repo = createRepo()
        repo.togglePokemonFavorite(1)
        repo.toggleBattleFavorite(2)
        repo.togglePlayerFavorite("Misty")

        repo.clearAll()

        assertEquals(emptySet(), repo.favoritePokemonIds.value)
        assertEquals(emptySet(), repo.favoriteBattleIds.value)
        assertEquals(emptySet(), repo.favoritePlayerNames.value)
    }

    @Test
    fun clearAll_persistsToStorage() {
        val storage = FakeFavoritesStorage()
        val repo = FavoritesRepository(storage)
        repo.togglePokemonFavorite(1)
        repo.toggleBattleFavorite(2)
        repo.togglePlayerFavorite("Brock")

        repo.clearAll()

        assertEquals(emptySet(), storage.loadIds("favorite_pokemon_ids"))
        assertEquals(emptySet(), storage.loadIds("favorite_battle_ids"))
        assertEquals(emptySet(), storage.loadStringSet("favorite_player_names"))
    }

    @Test
    fun initialState_loadsFromPrePopulatedStorage() {
        val storage = FakeFavoritesStorage()
        storage.saveIds("favorite_pokemon_ids", setOf(5, 10))
        storage.saveIds("favorite_battle_ids", setOf(20))
        storage.saveStringSet("favorite_player_names", setOf("Red"))

        val repo = FavoritesRepository(storage)

        assertEquals(setOf(5, 10), repo.favoritePokemonIds.value)
        assertEquals(setOf(20), repo.favoriteBattleIds.value)
        assertEquals(setOf("Red"), repo.favoritePlayerNames.value)
    }

    @Test
    fun multipleToggles_accumulateCorrectly() {
        val repo = createRepo()
        repo.togglePokemonFavorite(1)
        repo.togglePokemonFavorite(2)
        repo.togglePokemonFavorite(3)
        assertEquals(setOf(1, 2, 3), repo.favoritePokemonIds.value)

        repo.togglePokemonFavorite(2)
        assertEquals(setOf(1, 3), repo.favoritePokemonIds.value)
    }
}

package com.arcvgc.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FavoritesRepository(private val storage: FavoritesStorage) {

    private val _favoritePokemonIds = MutableStateFlow(storage.loadIds(KEY_POKEMON))
    val favoritePokemonIds: StateFlow<Set<Int>> = _favoritePokemonIds.asStateFlow()

    private val _favoriteBattleIds = MutableStateFlow(storage.loadIds(KEY_BATTLES))
    val favoriteBattleIds: StateFlow<Set<Int>> = _favoriteBattleIds.asStateFlow()

    private val _favoritePlayerNames = MutableStateFlow(storage.loadStringSet(KEY_PLAYERS))
    val favoritePlayerNames: StateFlow<Set<String>> = _favoritePlayerNames.asStateFlow()

    fun togglePokemonFavorite(id: Int) {
        val current = _favoritePokemonIds.value.toMutableSet()
        if (!current.add(id)) current.remove(id)
        _favoritePokemonIds.value = current
        storage.saveIds(KEY_POKEMON, current)
    }

    fun toggleBattleFavorite(id: Int) {
        val current = _favoriteBattleIds.value.toMutableSet()
        if (!current.add(id)) current.remove(id)
        _favoriteBattleIds.value = current
        storage.saveIds(KEY_BATTLES, current)
    }

    fun isPokemonFavorited(id: Int): Boolean = id in _favoritePokemonIds.value

    fun isBattleFavorited(id: Int): Boolean = id in _favoriteBattleIds.value

    fun togglePlayerFavorite(name: String) {
        val current = _favoritePlayerNames.value.toMutableSet()
        if (!current.add(name)) current.remove(name)
        _favoritePlayerNames.value = current
        storage.saveStringSet(KEY_PLAYERS, current)
    }

    fun isPlayerFavorited(name: String): Boolean = name in _favoritePlayerNames.value

    fun clearAll() {
        _favoritePokemonIds.value = emptySet()
        _favoriteBattleIds.value = emptySet()
        _favoritePlayerNames.value = emptySet()
        storage.saveIds(KEY_POKEMON, emptySet())
        storage.saveIds(KEY_BATTLES, emptySet())
        storage.saveStringSet(KEY_PLAYERS, emptySet())
    }

    /** Snapshot for iOS interop — List<Int> bridges to [KotlinInt] in Swift. */
    fun currentPokemonIds(): List<Int> = _favoritePokemonIds.value.toList()

    /** Snapshot for iOS interop — List<Int> bridges to [KotlinInt] in Swift. */
    fun currentBattleIds(): List<Int> = _favoriteBattleIds.value.toList()

    /** Snapshot for iOS interop — List<String> bridges cleanly to [String] in Swift. */
    fun currentPlayerNames(): List<String> = _favoritePlayerNames.value.toList()

    companion object {
        private const val KEY_POKEMON = "favorite_pokemon_ids"
        private const val KEY_BATTLES = "favorite_battle_ids"
        private const val KEY_PLAYERS = "favorite_player_names"
    }
}

package com.arcvgc.app.data.repository

import android.content.Context
import com.arcvgc.app.data.FavoritesStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.arcvgc.app.data.FavoritesRepository as SharedFavoritesRepository

interface FavoritesRepository {
    val favoritePokemonIds: StateFlow<Set<Int>>
    val favoriteBattleIds: StateFlow<Set<Int>>
    val favoritePlayerNames: StateFlow<Set<String>>
    fun togglePokemonFavorite(id: Int)
    fun toggleBattleFavorite(id: Int)
    fun togglePlayerFavorite(name: String)
    fun isPokemonFavorited(id: Int): Boolean
    fun isBattleFavorited(id: Int): Boolean
    fun isPlayerFavorited(name: String): Boolean
    fun clearAll()
}

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : FavoritesRepository {

    internal val shared = SharedFavoritesRepository(FavoritesStorage(context))

    override val favoritePokemonIds: StateFlow<Set<Int>> = shared.favoritePokemonIds
    override val favoriteBattleIds: StateFlow<Set<Int>> = shared.favoriteBattleIds
    override val favoritePlayerNames: StateFlow<Set<String>> = shared.favoritePlayerNames

    override fun togglePokemonFavorite(id: Int) = shared.togglePokemonFavorite(id)
    override fun toggleBattleFavorite(id: Int) = shared.toggleBattleFavorite(id)
    override fun togglePlayerFavorite(name: String) = shared.togglePlayerFavorite(name)
    override fun isPokemonFavorited(id: Int): Boolean = shared.isPokemonFavorited(id)
    override fun isBattleFavorited(id: Int): Boolean = shared.isBattleFavorited(id)
    override fun isPlayerFavorited(name: String): Boolean = shared.isPlayerFavorited(name)
    override fun clearAll() = shared.clearAll()
}

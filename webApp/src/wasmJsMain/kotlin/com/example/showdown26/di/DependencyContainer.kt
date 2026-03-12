package com.example.showdown26.di

import com.example.showdown26.data.BattleRepository
import com.example.showdown26.data.CatalogCacheStorage
import com.example.showdown26.data.FavoritesRepository
import com.example.showdown26.data.FavoritesStorage
import com.example.showdown26.data.FormatCatalogRepository
import com.example.showdown26.data.ItemCatalogRepository
import com.example.showdown26.data.PokemonCatalogRepository
import com.example.showdown26.data.SettingsRepository
import com.example.showdown26.data.SettingsStorage
import com.example.showdown26.data.TeraTypeCatalogRepository
import com.example.showdown26.network.ApiService

object DependencyContainer {
    val apiService: ApiService by lazy { ApiService() }
    val battleRepository: BattleRepository by lazy { BattleRepository(apiService) }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepository(FavoritesStorage()) }
    private val cacheStorage: CatalogCacheStorage by lazy { CatalogCacheStorage() }
    val pokemonCatalogRepository: PokemonCatalogRepository by lazy { PokemonCatalogRepository(apiService, cacheStorage) }
    val itemCatalogRepository: ItemCatalogRepository by lazy { ItemCatalogRepository(apiService, cacheStorage) }
    val teraTypeCatalogRepository: TeraTypeCatalogRepository by lazy { TeraTypeCatalogRepository(apiService, cacheStorage) }
    val formatCatalogRepository: FormatCatalogRepository by lazy { FormatCatalogRepository(apiService, cacheStorage) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(SettingsStorage(), cacheStorage, favoritesRepository) }
}

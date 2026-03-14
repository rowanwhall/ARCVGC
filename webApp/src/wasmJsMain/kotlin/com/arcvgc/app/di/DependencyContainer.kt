package com.arcvgc.app.di

import com.arcvgc.app.data.AppConfigRepository
import com.arcvgc.app.data.AppConfigStorage
import com.arcvgc.app.data.BattleRepository
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.data.FavoritesRepository
import com.arcvgc.app.data.FavoritesStorage
import com.arcvgc.app.data.FormatCatalogRepository
import com.arcvgc.app.data.ItemCatalogRepository
import com.arcvgc.app.data.PokemonCatalogRepository
import com.arcvgc.app.data.SettingsRepository
import com.arcvgc.app.data.SettingsStorage
import com.arcvgc.app.data.TeraTypeCatalogRepository
import com.arcvgc.app.network.ApiService

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
    val appConfigRepository: AppConfigRepository by lazy { AppConfigRepository(apiService, AppConfigStorage(), cacheStorage) }
}

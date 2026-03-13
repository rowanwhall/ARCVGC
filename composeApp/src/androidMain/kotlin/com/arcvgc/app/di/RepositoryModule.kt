package com.arcvgc.app.di

import com.arcvgc.app.data.repository.BattleRepository
import com.arcvgc.app.data.repository.BattleRepositoryImpl
import com.arcvgc.app.data.repository.FavoritesRepository
import com.arcvgc.app.data.repository.FavoritesRepositoryImpl
import com.arcvgc.app.data.repository.SettingsRepository
import com.arcvgc.app.data.repository.SettingsRepositoryImpl
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.FormatCatalogRepositoryImpl
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepositoryImpl
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepositoryImpl
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBattleRepository(impl: BattleRepositoryImpl): BattleRepository

    @Binds
    @Singleton
    abstract fun bindPokemonCatalogRepository(impl: PokemonCatalogRepositoryImpl): PokemonCatalogRepository

    @Binds
    @Singleton
    abstract fun bindItemCatalogRepository(impl: ItemCatalogRepositoryImpl): ItemCatalogRepository

    @Binds
    @Singleton
    abstract fun bindTeraTypeCatalogRepository(impl: TeraTypeCatalogRepositoryImpl): TeraTypeCatalogRepository

    @Binds
    @Singleton
    abstract fun bindFormatCatalogRepository(impl: FormatCatalogRepositoryImpl): FormatCatalogRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

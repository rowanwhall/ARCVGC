package com.example.showdown26.di

import com.example.showdown26.data.repository.BattleRepository
import com.example.showdown26.data.repository.BattleRepositoryImpl
import com.example.showdown26.data.repository.FavoritesRepository
import com.example.showdown26.data.repository.FavoritesRepositoryImpl
import com.example.showdown26.data.repository.SettingsRepository
import com.example.showdown26.data.repository.SettingsRepositoryImpl
import com.example.showdown26.data.repository.FormatCatalogRepository
import com.example.showdown26.data.repository.FormatCatalogRepositoryImpl
import com.example.showdown26.data.repository.ItemCatalogRepository
import com.example.showdown26.data.repository.ItemCatalogRepositoryImpl
import com.example.showdown26.data.repository.PokemonCatalogRepository
import com.example.showdown26.data.repository.PokemonCatalogRepositoryImpl
import com.example.showdown26.data.repository.TeraTypeCatalogRepository
import com.example.showdown26.data.repository.TeraTypeCatalogRepositoryImpl
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

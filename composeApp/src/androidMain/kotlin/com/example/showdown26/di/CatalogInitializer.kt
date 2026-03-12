package com.example.showdown26.di

import com.example.showdown26.data.repository.FormatCatalogRepository
import com.example.showdown26.data.repository.ItemCatalogRepository
import com.example.showdown26.data.repository.PokemonCatalogRepository
import com.example.showdown26.data.repository.TeraTypeCatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogInitializer @Inject constructor(
    pokemonCatalog: PokemonCatalogRepository,
    itemCatalog: ItemCatalogRepository,
    teraTypeCatalog: TeraTypeCatalogRepository,
    formatCatalog: FormatCatalogRepository
)

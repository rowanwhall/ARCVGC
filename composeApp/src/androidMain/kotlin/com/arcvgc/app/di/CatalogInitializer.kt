package com.arcvgc.app.di

import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogInitializer @Inject constructor(
    pokemonCatalog: PokemonCatalogRepository,
    itemCatalog: ItemCatalogRepository,
    teraTypeCatalog: TeraTypeCatalogRepository,
    formatCatalog: FormatCatalogRepository
)

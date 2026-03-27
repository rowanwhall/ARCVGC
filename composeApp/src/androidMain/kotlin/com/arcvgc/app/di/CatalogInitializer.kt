package com.arcvgc.app.di

import com.arcvgc.app.data.repository.AbilityCatalogRepository
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogInitializer @Inject constructor(
    appConfigRepository: AppConfigRepository,
    private val pokemonCatalog: PokemonCatalogRepository,
    private val itemCatalog: ItemCatalogRepository,
    private val teraTypeCatalog: TeraTypeCatalogRepository,
    private val formatCatalog: FormatCatalogRepository,
    private val abilityCatalog: AbilityCatalogRepository
) {
    init {
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            appConfigRepository.catalogVersionChanged
                .filter { it }
                .collect {
                    pokemonCatalog.reload()
                    itemCatalog.reload()
                    teraTypeCatalog.reload()
                    formatCatalog.reload()
                    abilityCatalog.reload()
                }
        }
    }
}

package com.arcvgc.app.data.repository

import android.content.Context
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface PokemonCatalogRepository {
    val state: StateFlow<CatalogState<PokemonPickerUiModel>>
    fun reload()
}

@Singleton
class PokemonCatalogRepositoryImpl @Inject constructor(
    apiService: ApiService,
    @ApplicationContext context: Context
) : PokemonCatalogRepository {
    private val shared = com.arcvgc.app.data.PokemonCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<PokemonPickerUiModel>> = shared.state
    override fun reload() = shared.reload()
}

package com.example.showdown26.data.repository

import android.content.Context
import com.example.showdown26.data.CatalogCacheStorage
import com.example.showdown26.data.CatalogState
import com.example.showdown26.network.ApiService
import com.example.showdown26.ui.model.PokemonPickerUiModel
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
    private val shared = com.example.showdown26.data.PokemonCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<PokemonPickerUiModel>> = shared.state
    override fun reload() = shared.reload()
}

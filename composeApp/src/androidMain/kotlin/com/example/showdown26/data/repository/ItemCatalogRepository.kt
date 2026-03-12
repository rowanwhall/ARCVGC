package com.example.showdown26.data.repository

import android.content.Context
import com.example.showdown26.data.CatalogCacheStorage
import com.example.showdown26.data.CatalogState
import com.example.showdown26.network.ApiService
import com.example.showdown26.ui.model.ItemUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface ItemCatalogRepository {
    val state: StateFlow<CatalogState<ItemUiModel>>
    fun reload()
}

@Singleton
class ItemCatalogRepositoryImpl @Inject constructor(
    apiService: ApiService,
    @ApplicationContext context: Context
) : ItemCatalogRepository {
    private val shared = com.example.showdown26.data.ItemCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<ItemUiModel>> = shared.state
    override fun reload() = shared.reload()
}

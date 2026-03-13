package com.arcvgc.app.data.repository

import android.content.Context
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.ItemUiModel
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
    private val shared = com.arcvgc.app.data.ItemCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<ItemUiModel>> = shared.state
    override fun reload() = shared.reload()
}

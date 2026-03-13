package com.arcvgc.app.data.repository

import android.content.Context
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.FormatUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface FormatCatalogRepository {
    val state: StateFlow<CatalogState<FormatUiModel>>
    fun reload()
}

@Singleton
class FormatCatalogRepositoryImpl @Inject constructor(
    apiService: ApiService,
    @ApplicationContext context: Context
) : FormatCatalogRepository {
    private val shared = com.arcvgc.app.data.FormatCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<FormatUiModel>> = shared.state
    override fun reload() = shared.reload()
}

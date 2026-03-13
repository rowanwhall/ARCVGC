package com.arcvgc.app.data.repository

import android.content.Context
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.TeraTypeUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface TeraTypeCatalogRepository {
    val state: StateFlow<CatalogState<TeraTypeUiModel>>
    fun reload()
}

@Singleton
class TeraTypeCatalogRepositoryImpl @Inject constructor(
    apiService: ApiService,
    @ApplicationContext context: Context
) : TeraTypeCatalogRepository {
    private val shared = com.arcvgc.app.data.TeraTypeCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<TeraTypeUiModel>> = shared.state
    override fun reload() = shared.reload()
}

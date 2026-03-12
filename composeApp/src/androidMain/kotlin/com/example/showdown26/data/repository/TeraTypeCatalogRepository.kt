package com.example.showdown26.data.repository

import android.content.Context
import com.example.showdown26.data.CatalogCacheStorage
import com.example.showdown26.data.CatalogState
import com.example.showdown26.network.ApiService
import com.example.showdown26.ui.model.TeraTypeUiModel
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
    private val shared = com.example.showdown26.data.TeraTypeCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<TeraTypeUiModel>> = shared.state
    override fun reload() = shared.reload()
}

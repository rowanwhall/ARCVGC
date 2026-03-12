package com.example.showdown26.data.repository

import android.content.Context
import com.example.showdown26.data.CatalogCacheStorage
import com.example.showdown26.data.CatalogState
import com.example.showdown26.network.ApiService
import com.example.showdown26.ui.model.FormatUiModel
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
    private val shared = com.example.showdown26.data.FormatCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<FormatUiModel>> = shared.state
    override fun reload() = shared.reload()
}

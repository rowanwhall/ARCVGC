package com.arcvgc.app.data.repository

import android.content.Context
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.ui.model.AbilityUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface AbilityCatalogRepository {
    val state: StateFlow<CatalogState<AbilityUiModel>>
    fun reload()
}

@Singleton
class AbilityCatalogRepositoryImpl @Inject constructor(
    apiService: ApiService,
    @ApplicationContext context: Context
) : AbilityCatalogRepository {
    private val shared = com.arcvgc.app.data.AbilityCatalogRepository(
        apiService, CatalogCacheStorage(context)
    )
    override val state: StateFlow<CatalogState<AbilityUiModel>> = shared.state
    override fun reload() = shared.reload()
}

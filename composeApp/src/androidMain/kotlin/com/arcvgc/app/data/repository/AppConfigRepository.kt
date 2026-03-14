package com.arcvgc.app.data.repository

import android.content.Context
import com.arcvgc.app.data.AppConfigStorage
import com.arcvgc.app.data.CatalogCacheStorage
import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.network.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.arcvgc.app.data.AppConfigRepository as SharedAppConfigRepository

interface AppConfigRepository {
    val config: StateFlow<AppConfig?>
    val catalogVersionChanged: StateFlow<Boolean>
}

@Singleton
class AppConfigRepositoryImpl @Inject constructor(
    apiService: ApiService,
    @ApplicationContext context: Context
) : AppConfigRepository {

    private val shared = SharedAppConfigRepository(
        apiService = apiService,
        storage = AppConfigStorage(context),
        catalogCacheStorage = CatalogCacheStorage(context)
    )

    override val config: StateFlow<AppConfig?> = shared.config
    override val catalogVersionChanged: StateFlow<Boolean> = shared.catalogVersionChanged
}

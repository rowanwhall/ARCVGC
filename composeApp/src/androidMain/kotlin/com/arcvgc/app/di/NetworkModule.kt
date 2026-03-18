package com.arcvgc.app.di

import com.arcvgc.app.data.DeepLinkResolver
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import com.arcvgc.app.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService = ApiService()

    @Provides
    @Singleton
    fun provideDeepLinkResolver(
        apiService: ApiService,
        itemCatalogRepository: ItemCatalogRepository,
        teraTypeCatalogRepository: TeraTypeCatalogRepository,
        formatCatalogRepository: FormatCatalogRepository
    ): DeepLinkResolver = DeepLinkResolver(
        apiService = apiService,
        itemCatalogProvider = { itemCatalogRepository.state.value.items },
        teraTypeCatalogProvider = { teraTypeCatalogRepository.state.value.items },
        formatCatalogProvider = { formatCatalogRepository.state.value.items }
    )
}

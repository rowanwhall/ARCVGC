package com.arcvgc.app.di

import com.arcvgc.app.data.DeepLinkResolver
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
    fun provideDeepLinkResolver(apiService: ApiService): DeepLinkResolver =
        DeepLinkResolver(apiService)
}

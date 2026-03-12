package com.example.showdown26.data.repository

import android.content.Context
import com.example.showdown26.data.CatalogCacheStorage
import com.example.showdown26.data.SettingsStorage
import com.example.showdown26.ui.model.SettingItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import com.example.showdown26.data.SettingsRepository as SharedSettingsRepository

interface SettingsRepository {
    val showWinnerHighlight: StateFlow<Boolean>
    val selectedThemeId: StateFlow<Int>
    val darkModeId: StateFlow<Int>
    val settingItems: StateFlow<List<SettingItem>>
    fun setBooleanSetting(key: String, value: Boolean)
    fun setIntSetting(key: String, value: Int)
    fun performAction(key: String)
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
    favoritesRepository: FavoritesRepository
) : SettingsRepository {

    private val shared = SharedSettingsRepository(
        storage = SettingsStorage(context),
        cacheStorage = CatalogCacheStorage(context),
        favoritesRepository = (favoritesRepository as FavoritesRepositoryImpl).shared
    )

    override val showWinnerHighlight: StateFlow<Boolean> = shared.showWinnerHighlight

    override val selectedThemeId: StateFlow<Int> = shared.selectedThemeId

    override val darkModeId: StateFlow<Int> = shared.darkModeId

    override val settingItems: StateFlow<List<SettingItem>> = shared.settingItems

    override fun setBooleanSetting(key: String, value: Boolean) = shared.setBooleanSetting(key, value)

    override fun setIntSetting(key: String, value: Int) = shared.setIntSetting(key, value)

    override fun performAction(key: String) = shared.performAction(key)
}

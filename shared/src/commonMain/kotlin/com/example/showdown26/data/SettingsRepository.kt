package com.example.showdown26.data

import com.example.showdown26.ui.model.AppTheme
import com.example.showdown26.ui.model.DarkModeOption
import com.example.showdown26.ui.model.SettingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val storage: SettingsStorage,
    private val cacheStorage: CatalogCacheStorage? = null,
    private val favoritesRepository: FavoritesRepository? = null
) {

    private val _showWinnerHighlight = MutableStateFlow(
        storage.getBoolean(KEY_SHOW_WINNER_HIGHLIGHT, true)
    )
    val showWinnerHighlight: StateFlow<Boolean> = _showWinnerHighlight.asStateFlow()

    private val _selectedThemeId = MutableStateFlow(
        storage.getInt(KEY_SELECTED_THEME, AppTheme.Red.id)
    )
    val selectedThemeId: StateFlow<Int> = _selectedThemeId.asStateFlow()

    private val _darkModeId = MutableStateFlow(
        storage.getInt(KEY_DARK_MODE, DarkModeOption.System.id)
    )
    val darkModeId: StateFlow<Int> = _darkModeId.asStateFlow()

    private val _settingItems = MutableStateFlow(buildSettingItems())
    val settingItems: StateFlow<List<SettingItem>> = _settingItems.asStateFlow()

    fun setShowWinnerHighlight(enabled: Boolean) {
        _showWinnerHighlight.value = enabled
        storage.putBoolean(KEY_SHOW_WINNER_HIGHLIGHT, enabled)
        _settingItems.value = buildSettingItems()
    }

    fun setSelectedThemeId(id: Int) {
        _selectedThemeId.value = id
        storage.putInt(KEY_SELECTED_THEME, id)
        _settingItems.value = buildSettingItems()
    }

    fun setDarkModeId(id: Int) {
        _darkModeId.value = id
        storage.putInt(KEY_DARK_MODE, id)
        _settingItems.value = buildSettingItems()
    }

    /** Snapshot getter for iOS interop. */
    fun isShowWinnerHighlightEnabled(): Boolean = _showWinnerHighlight.value

    /** Snapshot getter for iOS interop. */
    fun getSelectedThemeId(): Int = _selectedThemeId.value

    /** Snapshot getter for iOS interop. */
    fun getDarkModeId(): Int = _darkModeId.value

    /** Snapshot getter for iOS interop. */
    fun getSettingItems(): List<SettingItem> = _settingItems.value

    fun setBooleanSetting(key: String, value: Boolean) {
        when (key) {
            KEY_SHOW_WINNER_HIGHLIGHT -> setShowWinnerHighlight(value)
        }
    }

    fun setIntSetting(key: String, value: Int) {
        when (key) {
            KEY_SELECTED_THEME -> setSelectedThemeId(value)
            KEY_DARK_MODE -> setDarkModeId(value)
        }
    }

    fun performAction(key: String) {
        when (key) {
            KEY_CLEAR_CACHE -> {
                cacheStorage?.let { CatalogCache.clearAll(it) }
            }
            KEY_CLEAR_FAVORITES -> {
                favoritesRepository?.clearAll()
            }
        }
    }

    private fun buildSettingItems(): List<SettingItem> = listOf(
        SettingItem.DarkModeChoice(
            key = KEY_DARK_MODE,
            title = "Dark Mode",
            subtitle = "Choose between system, light, or dark appearance.",
            selectedModeId = _darkModeId.value
        ),
        SettingItem.ColorChoice(
            key = KEY_SELECTED_THEME,
            title = "Theme Color",
            subtitle = "Choose the app's accent color. We like red like our mascot, but maybe you're feeling Great, Ultra, or Master.",
            selectedThemeId = _selectedThemeId.value
        ),
        SettingItem.Toggle(
            key = KEY_SHOW_WINNER_HIGHLIGHT,
            title = "Winner Highlight",
            subtitle = "Highlight the winning team. Could save you time, but maybe you don't want spoilers.",
            isEnabled = _showWinnerHighlight.value
        ),
        SettingItem.Action(
            key = KEY_CLEAR_FAVORITES,
            title = "Clear Favorites",
            subtitle = "Clears all your favorites. Guess you couldn't choose just one?",
            confirmationMessage = "Are you sure? This can't be undone."
        ),
        SettingItem.Action(
            key = KEY_CLEAR_CACHE,
            title = "Invalidate Cache",
            subtitle = "Clears cached Pokémon, items, tera types, and formats. Use this if your search tab is missing options from one of those.",
            confirmationMessage = "Are you sure? This action is usually only done once a month and can use a lot of data."
        ),
        SettingItem.Link(
            key = KEY_PRIVACY_POLICY,
            title = "Privacy Policy",
            subtitle = "How ARC handles your data (spoiler: it doesn't).",
            url = URL_PRIVACY_POLICY
        ),
        SettingItem.Link(
            key = KEY_TERMS_OF_SERVICE,
            title = "Terms of Service",
            subtitle = "The boring-but-important stuff.",
            url = URL_TERMS_OF_SERVICE
        )
    )

    companion object {
        private const val KEY_SHOW_WINNER_HIGHLIGHT = "show_winner_highlight"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_CLEAR_FAVORITES = "clear_favorites"
        private const val KEY_CLEAR_CACHE = "clear_cache"
        private const val KEY_PRIVACY_POLICY = "privacy_policy"
        private const val KEY_TERMS_OF_SERVICE = "terms_of_service"

        const val URL_PRIVACY_POLICY = "https://arcvgc.com/privacy-policy"
        const val URL_TERMS_OF_SERVICE = "https://arcvgc.com/terms-of-service"

        const val DISCLAIMER_TEXT = "ARC is not affiliated with, endorsed by, or connected to Nintendo, The Pok\u00E9mon Company, or Pok\u00E9mon Showdown."
    }
}

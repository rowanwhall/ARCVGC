package com.arcvgc.app.data

import com.arcvgc.app.ui.model.AppTheme
import com.arcvgc.app.ui.model.DarkModeOption
import com.arcvgc.app.ui.model.SettingItem
import com.arcvgc.app.ui.model.SettingsSection
import com.arcvgc.app.util.createSafeScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsRepository(
    private val storage: SettingsStorageApi,
    private val cacheStorage: CatalogCacheStorageApi? = null,
    private val favoritesRepository: FavoritesRepository? = null,
    private val appConfigRepository: AppConfigRepository? = null
) {

    private val scope = createSafeScope()

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

    private val _preferredFormatId = MutableStateFlow(
        storage.getInt(KEY_PREFERRED_FORMAT, USE_DEFAULT_FORMAT)
    )
    val preferredFormatId: StateFlow<Int> = _preferredFormatId.asStateFlow()

    private val _settingSections = MutableStateFlow(buildSettingSections())
    val settingSections: StateFlow<List<SettingsSection>> = _settingSections.asStateFlow()

    init {
        if (appConfigRepository != null) {
            scope.launch {
                try {
                    appConfigRepository.config.collect {
                        _settingSections.value = buildSettingSections()
                    }
                } catch (_: Exception) {
                    // Ignore; keep last-built sections.
                }
            }
        }
    }

    fun setShowWinnerHighlight(enabled: Boolean) {
        _showWinnerHighlight.value = enabled
        storage.putBoolean(KEY_SHOW_WINNER_HIGHLIGHT, enabled)
        _settingSections.value = buildSettingSections()
    }

    fun setSelectedThemeId(id: Int) {
        _selectedThemeId.value = id
        storage.putInt(KEY_SELECTED_THEME, id)
        _settingSections.value = buildSettingSections()
    }

    fun setDarkModeId(id: Int) {
        _darkModeId.value = id
        storage.putInt(KEY_DARK_MODE, id)
        _settingSections.value = buildSettingSections()
    }

    fun setPreferredFormatId(id: Int) {
        _preferredFormatId.value = id
        storage.putInt(KEY_PREFERRED_FORMAT, id)
        _settingSections.value = buildSettingSections()
    }

    /** Snapshot getter for iOS interop. */
    fun isShowWinnerHighlightEnabled(): Boolean = _showWinnerHighlight.value

    /** Snapshot getter for iOS interop. */
    fun getSelectedThemeId(): Int = _selectedThemeId.value

    /** Snapshot getter for iOS interop. */
    fun getDarkModeId(): Int = _darkModeId.value

    /** Snapshot getter for iOS interop. */
    fun getPreferredFormatId(): Int = _preferredFormatId.value

    /**
     * Resolves the effective preferred format ID: the user's explicit selection, or
     * the current config default if the user hasn't overridden. Falls back to 1 if
     * neither is available.
     */
    fun getEffectivePreferredFormatId(): Int {
        val pref = _preferredFormatId.value
        if (pref != USE_DEFAULT_FORMAT) return pref
        return appConfigRepository?.getDefaultFormatId() ?: 1
    }

    /** Snapshot getter for iOS interop. */
    fun getSettingSections(): List<SettingsSection> = _settingSections.value

    fun setBooleanSetting(key: String, value: Boolean) {
        when (key) {
            KEY_SHOW_WINNER_HIGHLIGHT -> setShowWinnerHighlight(value)
        }
    }

    fun setIntSetting(key: String, value: Int) {
        when (key) {
            KEY_SELECTED_THEME -> setSelectedThemeId(value)
            KEY_DARK_MODE -> setDarkModeId(value)
            KEY_PREFERRED_FORMAT -> setPreferredFormatId(value)
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

    private fun buildSettingSections(): List<SettingsSection> = listOf(
        SettingsSection(
            title = "Appearance",
            items = listOf(
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
                )
            )
        ),
        SettingsSection(
            title = "Behavior",
            items = listOf(
                SettingItem.FormatChoice(
                    key = KEY_PREFERRED_FORMAT,
                    title = "Preferred Format",
                    subtitle = "What do you want to see first?",
                    selectedFormatId = _preferredFormatId.value,
                    defaultFormatId = appConfigRepository?.getConfig()?.defaultFormat?.id ?: 0
                ),
                SettingItem.Toggle(
                    key = KEY_SHOW_WINNER_HIGHLIGHT,
                    title = "Highlight Winner",
                    subtitle = "Could save you time, but maybe you don't want spoilers.",
                    isEnabled = _showWinnerHighlight.value
                )
            )
        ),
        SettingsSection(
            title = "Data",
            items = listOf(
                SettingItem.Action(
                    key = KEY_CLEAR_FAVORITES,
                    title = "Clear Favorites",
                    subtitle = "Guess you couldn't choose just one?",
                    confirmationMessage = "Are you sure? This can't be undone."
                ),
                SettingItem.Action(
                    key = KEY_CLEAR_CACHE,
                    title = "Invalidate Cache",
                    subtitle = "Use this if your search tab is missing options or you can't see the latest format.",
                    confirmationMessage = "Are you sure? This action is usually only done once a month and can use a lot of data."
                )
            )
        ),
        SettingsSection(
            title = "Links",
            items = listOf(
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
        )
    )

    companion object {
        private const val KEY_SHOW_WINNER_HIGHLIGHT = "show_winner_highlight"
        private const val KEY_SELECTED_THEME = "selected_theme"
        private const val KEY_DARK_MODE = "dark_mode"
        const val KEY_PREFERRED_FORMAT = "preferred_format"
        private const val KEY_CLEAR_FAVORITES = "clear_favorites"
        private const val KEY_CLEAR_CACHE = "clear_cache"
        private const val KEY_PRIVACY_POLICY = "privacy_policy"
        private const val KEY_TERMS_OF_SERVICE = "terms_of_service"

        /** Sentinel used for [KEY_PREFERRED_FORMAT] meaning "follow the config default". */
        const val USE_DEFAULT_FORMAT = 0

        const val URL_PRIVACY_POLICY = "https://arcvgc.com/privacy-policy"
        const val URL_TERMS_OF_SERVICE = "https://arcvgc.com/terms-of-service"

        const val DISCLAIMER_TEXT = "ARC is not affiliated with, endorsed by, or connected to Nintendo, The Pok\u00E9mon Company, or Pok\u00E9mon Showdown."
    }
}

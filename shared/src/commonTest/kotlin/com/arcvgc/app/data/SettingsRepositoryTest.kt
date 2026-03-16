package com.arcvgc.app.data

import com.arcvgc.app.testutil.FakeCatalogCacheStorage
import com.arcvgc.app.testutil.FakeFavoritesStorage
import com.arcvgc.app.testutil.FakeSettingsStorage
import com.arcvgc.app.ui.model.SettingItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SettingsRepositoryTest {

    private fun createRepo(
        storage: FakeSettingsStorage = FakeSettingsStorage(),
        cacheStorage: FakeCatalogCacheStorage? = null,
        favoritesRepo: FavoritesRepository? = null
    ) = SettingsRepository(storage, cacheStorage, favoritesRepo)

    // --- setShowWinnerHighlight ---

    @Test
    fun setShowWinnerHighlight_updatesFlowValue() {
        val repo = createRepo()
        repo.setShowWinnerHighlight(false)
        assertFalse(repo.showWinnerHighlight.value)
    }

    @Test
    fun setShowWinnerHighlight_persistsToStorage() {
        val storage = FakeSettingsStorage()
        val repo = SettingsRepository(storage)
        repo.setShowWinnerHighlight(false)
        assertFalse(storage.getBoolean("show_winner_highlight", true))
    }

    // --- setSelectedThemeId ---

    @Test
    fun setSelectedThemeId_updatesFlowAndPersists() {
        val storage = FakeSettingsStorage()
        val repo = SettingsRepository(storage)
        repo.setSelectedThemeId(2)
        assertEquals(2, repo.selectedThemeId.value)
        assertEquals(2, storage.getInt("selected_theme", 0))
    }

    // --- setDarkModeId ---

    @Test
    fun setDarkModeId_updatesFlowAndPersists() {
        val storage = FakeSettingsStorage()
        val repo = SettingsRepository(storage)
        repo.setDarkModeId(1)
        assertEquals(1, repo.darkModeId.value)
        assertEquals(1, storage.getInt("dark_mode", 0))
    }

    // --- setBooleanSetting ---

    @Test
    fun setBooleanSetting_dispatchesToSetShowWinnerHighlight() {
        val repo = createRepo()
        repo.setBooleanSetting("show_winner_highlight", false)
        assertFalse(repo.showWinnerHighlight.value)
    }

    // --- setIntSetting ---

    @Test
    fun setIntSetting_dispatchesToSetSelectedThemeId() {
        val repo = createRepo()
        repo.setIntSetting("selected_theme", 3)
        assertEquals(3, repo.selectedThemeId.value)
    }

    @Test
    fun setIntSetting_dispatchesToSetDarkModeId() {
        val repo = createRepo()
        repo.setIntSetting("dark_mode", 2)
        assertEquals(2, repo.darkModeId.value)
    }

    // --- performAction ---

    @Test
    fun performAction_clearFavorites_callsClearAll() {
        val favStorage = FakeFavoritesStorage()
        val favRepo = FavoritesRepository(favStorage)
        favRepo.togglePokemonFavorite(1)
        favRepo.toggleBattleFavorite(2)

        val repo = createRepo(favoritesRepo = favRepo)
        repo.performAction("clear_favorites")

        assertEquals(emptySet(), favRepo.favoritePokemonIds.value)
        assertEquals(emptySet(), favRepo.favoriteBattleIds.value)
    }

    @Test
    fun performAction_clearCache_clearsCatalogCache() {
        val cacheStorage = FakeCatalogCacheStorage()
        cacheStorage.putString("pokemon_catalog", "[\"data\"]")
        cacheStorage.putLong("pokemon_catalog_timestamp", 123L)

        val repo = createRepo(cacheStorage = cacheStorage)
        repo.performAction("clear_cache")

        assertEquals("", cacheStorage.getString("pokemon_catalog", "fallback"))
        assertEquals(0L, cacheStorage.getLong("pokemon_catalog_timestamp", 99L))
    }

    // --- settingItems ---

    @Test
    fun settingItems_returns7Items() {
        val repo = createRepo()
        val items = repo.settingItems.value
        assertEquals(7, items.size)
    }

    @Test
    fun settingItems_hasCorrectTypes() {
        val repo = createRepo()
        val items = repo.settingItems.value
        assertIs<SettingItem.DarkModeChoice>(items[0])
        assertIs<SettingItem.ColorChoice>(items[1])
        assertIs<SettingItem.Toggle>(items[2])
        assertIs<SettingItem.Action>(items[3])
        assertIs<SettingItem.Action>(items[4])
        assertIs<SettingItem.Link>(items[5])
        assertIs<SettingItem.Link>(items[6])
    }

    // --- Default values ---

    @Test
    fun defaultValues_fromEmptyStorage() {
        val repo = createRepo()
        assertTrue(repo.showWinnerHighlight.value)
        assertEquals(0, repo.selectedThemeId.value)
        assertEquals(0, repo.darkModeId.value)
    }

    // --- Initial values from pre-populated storage ---

    @Test
    fun initialValues_loadedFromPrePopulatedStorage() {
        val storage = FakeSettingsStorage()
        storage.putBoolean("show_winner_highlight", false)
        storage.putInt("selected_theme", 3)
        storage.putInt("dark_mode", 2)

        val repo = SettingsRepository(storage)

        assertFalse(repo.showWinnerHighlight.value)
        assertEquals(3, repo.selectedThemeId.value)
        assertEquals(2, repo.darkModeId.value)
    }
}

package com.arcvgc.app.ui.search

import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.data.SettingsStorageApi
import com.arcvgc.app.data.repository.AbilityCatalogRepository
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.SettingsRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.ui.model.AbilityUiModel
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.data.SettingsRepository as SharedSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val pokemonCatalogRepo = FakePokemonCatalogRepository()
    private val itemCatalogRepo = FakeItemCatalogRepository()
    private val teraTypeCatalogRepo = FakeTeraTypeCatalogRepository()
    private val formatCatalogRepo = FakeFormatCatalogRepository()
    private val abilityCatalogRepo = FakeAbilityCatalogRepository()
    private val appConfigRepo = FakeAppConfigRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val settingsRepo = FakeSettingsRepository()

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(
            pokemonCatalogRepository = pokemonCatalogRepo,
            itemCatalogRepository = itemCatalogRepo,
            teraTypeCatalogRepository = teraTypeCatalogRepo,
            formatCatalogRepository = formatCatalogRepo,
            abilityCatalogRepository = abilityCatalogRepo,
            appConfigRepository = appConfigRepo,
            settingsRepository = settingsRepo
        )
    }

    @Test
    fun init_setsDefaultFormatFromConfig() {
        appConfigRepo.configFlow.value = AppConfig(
            defaultFormat = Format(id = 5, name = "gen9vgc2024regg", formattedName = "Reg G"),
            minAndroidVersion = 1,
            minIosVersion = 1,
            minWebVersion = 1,
            minCatalogVersion = 1
        )
        val vm = createViewModel()
        val format = vm.uiState.value.selectedFormat
        assertEquals(5, format?.id)
        assertEquals("Reg G", format?.displayName)
    }

    @Test
    fun init_doesNotOverwriteManuallySetFormat() {
        val vm = createViewModel()
        vm.setFormat(FormatUiModel(id = 99, displayName = "Custom"))

        appConfigRepo.configFlow.value = AppConfig(
            defaultFormat = Format(id = 5, name = "gen9vgc2024regg", formattedName = "Reg G"),
            minAndroidVersion = 1,
            minIosVersion = 1,
            minWebVersion = 1,
            minCatalogVersion = 1
        )

        assertEquals(99, vm.uiState.value.selectedFormat?.id)
    }
}

// --- Fake Repositories ---

private class FakeAbilityCatalogRepository : AbilityCatalogRepository {
    override val state = MutableStateFlow(CatalogState<AbilityUiModel>())
    override fun reload() {}
}

private class FakePokemonCatalogRepository : PokemonCatalogRepository {
    override val state = MutableStateFlow(CatalogState<PokemonPickerUiModel>())
    override fun reload() {}
}

private class FakeItemCatalogRepository : ItemCatalogRepository {
    override val state = MutableStateFlow(CatalogState<ItemUiModel>())
    override fun reload() {}
}

private class FakeTeraTypeCatalogRepository : TeraTypeCatalogRepository {
    override val state = MutableStateFlow(CatalogState<TeraTypeUiModel>())
    override fun reload() {}
}

private class FakeFormatCatalogRepository : FormatCatalogRepository {
    override val state = MutableStateFlow(CatalogState<FormatUiModel>())
    override fun reload() {}
}

private class FakeAppConfigRepository : AppConfigRepository {
    val configFlow = MutableStateFlow<AppConfig?>(null)
    override val config: StateFlow<AppConfig?> = configFlow
    override val catalogVersionChanged = MutableStateFlow(false)
}

private class InMemorySettingsStorage : SettingsStorageApi {
    private val booleans = mutableMapOf<String, Boolean>()
    private val ints = mutableMapOf<String, Int>()
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = booleans[key] ?: defaultValue
    override fun putBoolean(key: String, value: Boolean) { booleans[key] = value }
    override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue
    override fun putInt(key: String, value: Int) { ints[key] = value }
}

private class FakeSettingsRepository : SettingsRepository {
    override val shared = SharedSettingsRepository(InMemorySettingsStorage())
    override val showWinnerHighlight = shared.showWinnerHighlight
    override val selectedThemeId = shared.selectedThemeId
    override val darkModeId = shared.darkModeId
    override val preferredFormatId = shared.preferredFormatId
    override val settingItems = shared.settingItems
    override fun setBooleanSetting(key: String, value: Boolean) { shared.setBooleanSetting(key, value) }
    override fun setIntSetting(key: String, value: Int) { shared.setIntSetting(key, value) }
    override fun performAction(key: String) { shared.performAction(key) }
}

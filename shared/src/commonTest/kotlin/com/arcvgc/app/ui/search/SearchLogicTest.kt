package com.arcvgc.app.ui.search

import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.domain.model.OrderBy
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.domain.model.WinnerFilter
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TypeUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchLogicTest {

    private fun testConfig(formatId: Int = 5, name: String = "gen9vgc2024regg", formattedName: String = "Reg G") = AppConfig(
        defaultFormat = Format(id = formatId, name = name, formattedName = formattedName),
        minAndroidVersion = 1,
        minIosVersion = 1,
        minWebVersion = 1,
        minCatalogVersion = 1
    )

    @Test
    fun init_setsDefaultFormatFromConfig() = runTest(UnconfinedTestDispatcher()) {
        val configFlow = MutableStateFlow<AppConfig?>(testConfig())
        val logic = SearchLogic(
            scope = backgroundScope,
            appConfigFlow = configFlow
        )

        val format = logic.uiState.value.selectedFormat
        assertEquals(5, format?.id)
        assertEquals("Reg G", format?.displayName)
    }

    @Test
    fun init_doesNotOverwriteManuallySetFormat() = runTest(UnconfinedTestDispatcher()) {
        val configFlow = MutableStateFlow<AppConfig?>(null)
        val logic = SearchLogic(
            scope = backgroundScope,
            appConfigFlow = configFlow
        )

        logic.setFormat(FormatUiModel(id = 99, displayName = "Custom"))
        configFlow.value = testConfig()

        assertEquals(99, logic.uiState.value.selectedFormat?.id)
    }

    @Test
    fun init_noScopeOrConfig_noDefaultFormat() {
        val logic = SearchLogic()
        assertNull(logic.uiState.value.selectedFormat)
    }

    @Test
    fun addPokemon_delegatesToReducer() {
        val logic = SearchLogic()
        val pokemon = PokemonPickerUiModel(
            id = 10, name = "Pikachu", imageUrl = null,
            types = listOf(TypeUiModel(name = "Electric", imageUrl = null))
        )
        logic.addPokemon(pokemon)

        assertEquals(1, logic.uiState.value.filterSlots.size)
        assertEquals(10, logic.uiState.value.filterSlots[0].pokemonId)
    }

    @Test
    fun init_preferredFormatOverridesConfigDefault() = runTest(UnconfinedTestDispatcher()) {
        val configFlow = MutableStateFlow<AppConfig?>(testConfig())
        val catalogFlow = MutableStateFlow(listOf(
            FormatUiModel(id = 5, displayName = "Reg G"),
            FormatUiModel(id = 42, displayName = "Reg M-A")
        ))
        val settingsStorage = com.arcvgc.app.testutil.FakeSettingsStorage().apply {
            putInt("preferred_format", 42)
        }
        val settings = com.arcvgc.app.data.SettingsRepository(settingsStorage)
        val logic = SearchLogic(
            scope = backgroundScope,
            appConfigFlow = configFlow,
            settingsRepository = settings,
            formatCatalogFlow = catalogFlow
        )

        val format = logic.uiState.value.selectedFormat
        assertEquals(42, format?.id)
        assertEquals("Reg M-A", format?.displayName)
    }

    @Test
    fun init_preferredFormat_prefersCatalogFlagsOverCachedConfig() = runTest(UnconfinedTestDispatcher()) {
        // Same as the config-default case but for the preferred-format path:
        // the catalog's isOpenTeamsheet flag should flow through, not whatever
        // the cached AppConfig held.
        val configFlow = MutableStateFlow<AppConfig?>(testConfig())
        val catalogFlow = MutableStateFlow(listOf(
            FormatUiModel(id = 42, displayName = "Reg M-A", isOpenTeamsheet = true)
        ))
        val settingsStorage = com.arcvgc.app.testutil.FakeSettingsStorage().apply {
            putInt("preferred_format", 42)
        }
        val settings = com.arcvgc.app.data.SettingsRepository(settingsStorage)
        val logic = SearchLogic(
            scope = backgroundScope,
            appConfigFlow = configFlow,
            settingsRepository = settings,
            formatCatalogFlow = catalogFlow
        )

        val format = logic.uiState.value.selectedFormat
        assertEquals(42, format?.id)
        assertEquals(true, format?.isOpenTeamsheet)
    }

    @Test
    fun init_preferredFormatUnset_usesConfigDefault() = runTest(UnconfinedTestDispatcher()) {
        val configFlow = MutableStateFlow<AppConfig?>(testConfig())
        val catalogFlow = MutableStateFlow(listOf(FormatUiModel(id = 5, displayName = "Reg G")))
        val settings = com.arcvgc.app.data.SettingsRepository(com.arcvgc.app.testutil.FakeSettingsStorage())
        val logic = SearchLogic(
            scope = backgroundScope,
            appConfigFlow = configFlow,
            settingsRepository = settings,
            formatCatalogFlow = catalogFlow
        )

        assertEquals(5, logic.uiState.value.selectedFormat?.id)
    }

    @Test
    fun init_defaultFormat_prefersCatalogFlagsOverCachedConfig() = runTest(UnconfinedTestDispatcher()) {
        // Simulates cached AppConfig: id/name persisted, isOpenTeamsheet stuck at false.
        // The catalog (loaded separately) has the correct flag. Default-format users
        // should still see isOpenTeamsheet = true, not the cached false.
        val configFlow = MutableStateFlow<AppConfig?>(testConfig())
        val catalogFlow = MutableStateFlow(listOf(
            FormatUiModel(id = 5, displayName = "Reg G", isOpenTeamsheet = true)
        ))
        val settings = com.arcvgc.app.data.SettingsRepository(com.arcvgc.app.testutil.FakeSettingsStorage())
        val logic = SearchLogic(
            scope = backgroundScope,
            appConfigFlow = configFlow,
            settingsRepository = settings,
            formatCatalogFlow = catalogFlow
        )

        val format = logic.uiState.value.selectedFormat
        assertEquals(5, format?.id)
        assertEquals(true, format?.isOpenTeamsheet)
    }

    @Test
    fun hydrate_appliesAllParamsAtomically() {
        val logic = SearchLogic()
        val params = SearchParams(
            filters = listOf(SearchFilterSlot(pokemonId = 25, pokemonName = "Pikachu")),
            team2Filters = listOf(SearchFilterSlot(pokemonId = 6, pokemonName = "Charizard")),
            formatId = 42,
            minimumRating = 1500,
            maximumRating = 1900,
            orderBy = OrderBy.Time,
            playerName = "Wolfey",
            formatName = "Reg G",
            winnerFilter = WinnerFilter.TEAM2
        )

        logic.hydrate(params)

        val state = logic.uiState.value
        assertEquals(1, state.filterSlots.size)
        assertEquals(25, state.filterSlots[0].pokemonId)
        assertEquals(1, state.team2FilterSlots.size)
        assertEquals(6, state.team2FilterSlots[0].pokemonId)
        assertEquals(42, state.selectedFormat?.id)
        assertEquals(1500, state.selectedMinRating)
        assertEquals(1900, state.selectedMaxRating)
        assertEquals(OrderBy.Time, state.selectedOrderBy)
        assertEquals("Wolfey", state.playerName)
        assertEquals(WinnerFilter.TEAM2, state.winnerFilter)
    }

    @Test
    fun hydrate_resolvesFormatFromCatalog() {
        val logic = SearchLogic()
        val params = SearchParams(
            filters = emptyList(),
            formatId = 42,
            orderBy = OrderBy.Rating,
            formatName = "Stale Name"
        )
        val catalog = listOf(
            FormatUiModel(id = 42, displayName = "VGC 2024 Reg G", isOpenTeamsheet = true)
        )

        logic.hydrate(params, catalog)

        val format = logic.uiState.value.selectedFormat
        assertEquals(42, format?.id)
        assertEquals("VGC 2024 Reg G", format?.displayName)
        assertEquals(true, format?.isOpenTeamsheet)
    }

    @Test
    fun hydrate_emptyCatalog_buildsMinimalFormatFromParams() {
        val logic = SearchLogic()
        val params = SearchParams(
            filters = emptyList(),
            formatId = 42,
            orderBy = OrderBy.Rating,
            formatName = "VGC 2024 Reg G"
        )

        logic.hydrate(params, formatCatalog = emptyList())

        val format = logic.uiState.value.selectedFormat
        assertEquals(42, format?.id)
        assertEquals("VGC 2024 Reg G", format?.displayName)
    }

    @Test
    fun hydrate_setsUserSelectedFormat_blocksDefaultFormatWatcher() = runTest(UnconfinedTestDispatcher()) {
        val configFlow = MutableStateFlow<AppConfig?>(null)
        val logic = SearchLogic(scope = backgroundScope, appConfigFlow = configFlow)
        val params = SearchParams(
            filters = emptyList(),
            formatId = 42,
            orderBy = OrderBy.Rating,
            formatName = "Hydrated"
        )

        logic.hydrate(params)
        // Later: config flow emits with a different default format.
        configFlow.value = testConfig(formatId = 99, formattedName = "Default")

        // Hydrated format must stick.
        assertEquals(42, logic.uiState.value.selectedFormat?.id)
        assertTrue(logic.uiState.value.userSelectedFormat)
    }

    @Test
    fun setUnratedOnly_clearsRatingsAndSwitchesSortOrder() {
        val logic = SearchLogic()
        logic.setMinRating(1500)
        logic.setMaxRating(1800)
        logic.setOrderBy(OrderBy.Rating)

        logic.setUnratedOnly(true)

        val state = logic.uiState.value
        assertNull(state.selectedMinRating)
        assertNull(state.selectedMaxRating)
        assertEquals(OrderBy.Time, state.selectedOrderBy)
    }
}

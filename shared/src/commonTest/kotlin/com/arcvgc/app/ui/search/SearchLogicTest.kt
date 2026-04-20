package com.arcvgc.app.ui.search

import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.Format
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
    fun setUnratedOnly_clearsRatingsAndSwitchesSortOrder() {
        val logic = SearchLogic()
        logic.setMinRating(1500)
        logic.setMaxRating(1800)
        logic.setOrderBy("rating")

        logic.setUnratedOnly(true)

        val state = logic.uiState.value
        assertNull(state.selectedMinRating)
        assertNull(state.selectedMaxRating)
        assertEquals("time", state.selectedOrderBy)
    }
}

package com.arcvgc.app.ui.search

import com.arcvgc.app.data.CatalogState
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.data.repository.FormatCatalogRepository
import com.arcvgc.app.data.repository.ItemCatalogRepository
import com.arcvgc.app.data.repository.PokemonCatalogRepository
import com.arcvgc.app.data.repository.TeraTypeCatalogRepository
import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.PokemonPickerUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import com.arcvgc.app.ui.model.TypeUiModel
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val pokemonCatalogRepo = FakePokemonCatalogRepository()
    private val itemCatalogRepo = FakeItemCatalogRepository()
    private val teraTypeCatalogRepo = FakeTeraTypeCatalogRepository()
    private val formatCatalogRepo = FakeFormatCatalogRepository()
    private val appConfigRepo = FakeAppConfigRepository()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SearchViewModel {
        return SearchViewModel(
            pokemonCatalogRepository = pokemonCatalogRepo,
            itemCatalogRepository = itemCatalogRepo,
            teraTypeCatalogRepository = teraTypeCatalogRepo,
            formatCatalogRepository = formatCatalogRepo,
            appConfigRepository = appConfigRepo
        )
    }

    // --- Initial State ---

    @Test
    fun initialState_hasEmptyFilterSlots() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.filterSlots.isEmpty())
    }

    @Test
    fun initialState_canAddMore() {
        val vm = createViewModel()
        assertTrue(vm.uiState.value.canAddMore)
    }

    @Test
    fun initialState_defaultOrderByIsRating() {
        val vm = createViewModel()
        assertEquals("rating", vm.uiState.value.selectedOrderBy)
    }

    @Test
    fun initialState_noRatingFilters() {
        val vm = createViewModel()
        assertNull(vm.uiState.value.selectedMinRating)
        assertNull(vm.uiState.value.selectedMaxRating)
        assertFalse(vm.uiState.value.unratedOnly)
    }

    // --- Config Init ---

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

    // --- addPokemon ---

    @Test
    fun addPokemon_addsToFilterSlots() {
        val vm = createViewModel()
        val pokemon = testPokemonPicker(id = 10, name = "Pikachu")

        vm.addPokemon(pokemon)

        assertEquals(1, vm.uiState.value.filterSlots.size)
        assertEquals(10, vm.uiState.value.filterSlots[0].pokemonId)
        assertEquals("Pikachu", vm.uiState.value.filterSlots[0].pokemonName)
    }

    @Test
    fun addPokemon_multipleAdds() {
        val vm = createViewModel()
        vm.addPokemon(testPokemonPicker(id = 1, name = "Bulbasaur"))
        vm.addPokemon(testPokemonPicker(id = 4, name = "Charmander"))
        vm.addPokemon(testPokemonPicker(id = 7, name = "Squirtle"))

        assertEquals(3, vm.uiState.value.filterSlots.size)
        assertEquals("Squirtle", vm.uiState.value.filterSlots[2].pokemonName)
    }

    @Test
    fun addPokemon_maxSixSlots() {
        val vm = createViewModel()
        repeat(6) { i -> vm.addPokemon(testPokemonPicker(id = i, name = "Mon$i")) }

        assertFalse(vm.uiState.value.canAddMore)
        assertEquals(6, vm.uiState.value.filterSlots.size)

        vm.addPokemon(testPokemonPicker(id = 99, name = "Extra"))
        assertEquals(6, vm.uiState.value.filterSlots.size)
    }

    // --- removePokemon ---

    @Test
    fun removePokemon_removesAtIndex() {
        val vm = createViewModel()
        vm.addPokemon(testPokemonPicker(id = 1, name = "Bulbasaur"))
        vm.addPokemon(testPokemonPicker(id = 4, name = "Charmander"))
        vm.addPokemon(testPokemonPicker(id = 7, name = "Squirtle"))

        vm.removePokemon(1)

        assertEquals(2, vm.uiState.value.filterSlots.size)
        assertEquals("Bulbasaur", vm.uiState.value.filterSlots[0].pokemonName)
        assertEquals("Squirtle", vm.uiState.value.filterSlots[1].pokemonName)
    }

    @Test
    fun removePokemon_restoresCanAddMore() {
        val vm = createViewModel()
        repeat(6) { i -> vm.addPokemon(testPokemonPicker(id = i, name = "Mon$i")) }
        assertFalse(vm.uiState.value.canAddMore)

        vm.removePokemon(0)
        assertTrue(vm.uiState.value.canAddMore)
    }

    // --- setItem ---

    @Test
    fun setItem_updatesSlot() {
        val vm = createViewModel()
        vm.addPokemon(testPokemonPicker(id = 1, name = "Pikachu"))
        assertNull(vm.uiState.value.filterSlots[0].item)

        val item = ItemUiModel(id = 5, name = "Choice Band", imageUrl = null)
        vm.setItem(0, item)

        assertEquals(5, vm.uiState.value.filterSlots[0].item?.id)
        assertEquals("Choice Band", vm.uiState.value.filterSlots[0].item?.name)
    }

    @Test
    fun setItem_doesNotAffectOtherSlots() {
        val vm = createViewModel()
        vm.addPokemon(testPokemonPicker(id = 1, name = "Pikachu"))
        vm.addPokemon(testPokemonPicker(id = 2, name = "Charizard"))

        vm.setItem(0, ItemUiModel(id = 5, name = "Choice Band", imageUrl = null))

        assertNull(vm.uiState.value.filterSlots[1].item)
    }

    // --- setTeraType ---

    @Test
    fun setTeraType_updatesSlot() {
        val vm = createViewModel()
        vm.addPokemon(testPokemonPicker(id = 1, name = "Pikachu"))

        val tera = TeraTypeUiModel(id = 3, name = "Water", imageUrl = null)
        vm.setTeraType(0, tera)

        assertEquals(3, vm.uiState.value.filterSlots[0].teraType?.id)
        assertEquals("Water", vm.uiState.value.filterSlots[0].teraType?.name)
    }

    // --- setFormat ---

    @Test
    fun setFormat_updatesSelectedFormat() {
        val vm = createViewModel()
        val format = FormatUiModel(id = 3, displayName = "Reg F")

        vm.setFormat(format)

        assertEquals(3, vm.uiState.value.selectedFormat?.id)
        assertEquals("Reg F", vm.uiState.value.selectedFormat?.displayName)
    }

    // --- Rating ---

    @Test
    fun setMinRating_updatesState() {
        val vm = createViewModel()
        vm.setMinRating(1500)
        assertEquals(1500, vm.uiState.value.selectedMinRating)
    }

    @Test
    fun setMaxRating_updatesState() {
        val vm = createViewModel()
        vm.setMaxRating(1800)
        assertEquals(1800, vm.uiState.value.selectedMaxRating)
    }

    @Test
    fun setMinRating_null_clearsRating() {
        val vm = createViewModel()
        vm.setMinRating(1500)
        vm.setMinRating(null)
        assertNull(vm.uiState.value.selectedMinRating)
    }

    // --- setUnratedOnly ---

    @Test
    fun setUnratedOnly_true_clearsRatingsAndSwitchesSortOrder() {
        val vm = createViewModel()
        vm.setMinRating(1500)
        vm.setMaxRating(1800)
        vm.setOrderBy("rating")

        vm.setUnratedOnly(true)

        assertTrue(vm.uiState.value.unratedOnly)
        assertNull(vm.uiState.value.selectedMinRating)
        assertNull(vm.uiState.value.selectedMaxRating)
        assertEquals("time", vm.uiState.value.selectedOrderBy)
    }

    @Test
    fun setUnratedOnly_true_doesNotSwitchNonRatingSortOrder() {
        val vm = createViewModel()
        vm.setOrderBy("time")

        vm.setUnratedOnly(true)

        assertEquals("time", vm.uiState.value.selectedOrderBy)
    }

    @Test
    fun setUnratedOnly_false_clearsUnratedFlag() {
        val vm = createViewModel()
        vm.setUnratedOnly(true)
        vm.setUnratedOnly(false)
        assertFalse(vm.uiState.value.unratedOnly)
    }

    // --- Time Range ---

    @Test
    fun setTimeRange_updatesState() {
        val vm = createViewModel()
        vm.setTimeRange(1000L, 2000L)
        assertEquals(1000L, vm.uiState.value.timeRangeStart)
        assertEquals(2000L, vm.uiState.value.timeRangeEnd)
    }

    @Test
    fun setTimeRange_nulls_clearsRange() {
        val vm = createViewModel()
        vm.setTimeRange(1000L, 2000L)
        vm.setTimeRange(null, null)
        assertNull(vm.uiState.value.timeRangeStart)
        assertNull(vm.uiState.value.timeRangeEnd)
    }

    // --- Player Name ---

    @Test
    fun setPlayerName_updatesState() {
        val vm = createViewModel()
        vm.setPlayerName("WolfeGlick")
        assertEquals("WolfeGlick", vm.uiState.value.playerName)
    }

    // --- Order By ---

    @Test
    fun setOrderBy_updatesState() {
        val vm = createViewModel()
        vm.setOrderBy("time")
        assertEquals("time", vm.uiState.value.selectedOrderBy)
    }

    // --- Helpers ---

    private fun testPokemonPicker(id: Int, name: String) = PokemonPickerUiModel(
        id = id,
        name = name,
        imageUrl = null,
        types = listOf(TypeUiModel(name = "Normal", imageUrl = null))
    )
}

// --- Fake Repositories ---

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

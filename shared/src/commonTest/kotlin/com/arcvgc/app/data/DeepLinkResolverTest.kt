package com.arcvgc.app.data

import com.arcvgc.app.domain.model.DeepLinkTarget
import com.arcvgc.app.domain.model.LookbackWindow
import com.arcvgc.app.domain.model.NetworkResult
import com.arcvgc.app.domain.model.OrderBy
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PokemonProfile
import com.arcvgc.app.domain.model.SearchQueryParams
import com.arcvgc.app.domain.model.WinnerFilter
import com.arcvgc.app.network.DeepLinkApi
import com.arcvgc.app.testutil.testPlayerListItem
import com.arcvgc.app.testutil.testPokemonProfile
import com.arcvgc.app.ui.model.AbilityUiModel
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.model.FormatUiModel
import com.arcvgc.app.ui.model.ItemUiModel
import com.arcvgc.app.ui.model.TeraTypeUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DeepLinkResolverTest {

    private class FakeDeepLinkApi(
        val pokemonById: Map<Int, PokemonProfile> = emptyMap(),
        val playersByName: Map<String, List<PlayerListItem>> = emptyMap()
    ) : DeepLinkApi {
        override suspend fun getPokemonById(
            id: Int,
            formatId: Int?,
            lookback: LookbackWindow?
        ): NetworkResult<PokemonProfile> =
            pokemonById[id]?.let { NetworkResult.Success(it) }
                ?: NetworkResult.Error("Pokémon not found")

        override suspend fun getPlayersByName(name: String): NetworkResult<List<PlayerListItem>> =
            playersByName[name]?.let { NetworkResult.Success(it) }
                ?: NetworkResult.Error("Player not found")
    }

    private val items = listOf(
        ItemUiModel(id = 10, name = "Booster Energy", imageUrl = "https://arcvgc.com/img/items/boosterenergy.png")
    )
    private val teraTypes = listOf(
        TeraTypeUiModel(id = 20, name = "Fire", imageUrl = "https://arcvgc.com/img/tera/fire.png")
    )
    private val formats = listOf(
        FormatUiModel(id = 5, displayName = "Reg H")
    )
    private val abilities = listOf(
        AbilityUiModel(id = 30, name = "Sand Stream")
    )

    private fun resolver(
        api: DeepLinkApi = FakeDeepLinkApi(),
        withCatalogs: Boolean = true
    ) = DeepLinkResolver(
        apiService = api,
        itemCatalogProvider = if (withCatalogs) ({ items }) else null,
        teraTypeCatalogProvider = if (withCatalogs) ({ teraTypes }) else null,
        formatCatalogProvider = if (withCatalogs) ({ formats }) else null,
        abilityCatalogProvider = if (withCatalogs) ({ abilities }) else null
    )

    // --- Simple targets ---

    @Test
    fun home_resolvesToHome() = runTest {
        assertEquals(DeepLinkResolver.ResolvedLink.Home, resolver().resolve(DeepLinkTarget.Home))
    }

    @Test
    fun searchTab_resolvesToSearchTab() = runTest {
        assertEquals(DeepLinkResolver.ResolvedLink.SearchTab, resolver().resolve(DeepLinkTarget.SearchTab))
    }

    @Test
    fun settingsTab_resolvesToSettingsTab() = runTest {
        assertEquals(DeepLinkResolver.ResolvedLink.SettingsTab, resolver().resolve(DeepLinkTarget.SettingsTab))
    }

    @Test
    fun favorites_resolvesKnownContentTypes() = runTest {
        val resolver = resolver()
        assertEquals(
            DeepLinkResolver.ResolvedLink.Favorites(FavoriteContentType.Battles),
            resolver.resolve(DeepLinkTarget.Favorites("battles"))
        )
        assertEquals(
            DeepLinkResolver.ResolvedLink.Favorites(FavoriteContentType.Pokemon),
            resolver.resolve(DeepLinkTarget.Favorites("pokemon"))
        )
        assertEquals(
            DeepLinkResolver.ResolvedLink.Favorites(FavoriteContentType.Players),
            resolver.resolve(DeepLinkTarget.Favorites("players"))
        )
    }

    @Test
    fun favorites_unknownContentType_returnsNull() = runTest {
        assertNull(resolver().resolve(DeepLinkTarget.Favorites("teams")))
    }

    // --- Pokemon ---

    @Test
    fun pokemon_success_mapsListItemAndLookback() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(150 to testPokemonProfile(id = 150, name = "Mewtwo")))
        val result = resolver(api).resolve(DeepLinkTarget.Pokemon(150, lookback = LookbackWindow.Week))

        val link = assertIs<DeepLinkResolver.ResolvedLink.Pokemon>(result)
        assertEquals(150, link.item.id)
        assertEquals("Mewtwo", link.item.name)
        assertEquals(LookbackWindow.Week, link.lookback)
    }

    @Test
    fun pokemon_apiError_returnsNull() = runTest {
        assertNull(resolver().resolve(DeepLinkTarget.Pokemon(999)))
    }

    // --- Player ---

    @Test
    fun player_returnsFirstMatch() = runTest {
        val api = FakeDeepLinkApi(
            playersByName = mapOf(
                "Wolfe" to listOf(testPlayerListItem(1, "Wolfe"), testPlayerListItem(2, "Wolfey"))
            )
        )
        val result = resolver(api).resolve(DeepLinkTarget.Player("Wolfe"))

        val link = assertIs<DeepLinkResolver.ResolvedLink.Player>(result)
        assertEquals(1, link.item.id)
    }

    @Test
    fun player_noMatches_returnsNull() = runTest {
        val api = FakeDeepLinkApi(playersByName = mapOf("Wolfe" to emptyList()))
        assertNull(resolver(api).resolve(DeepLinkTarget.Player("Wolfe")))
    }

    @Test
    fun player_apiError_returnsNull() = runTest {
        assertNull(resolver().resolve(DeepLinkTarget.Player("Unknown")))
    }

    // --- TopPokemon ---

    @Test
    fun topPokemon_withPokemonId_resolvesItem() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(150 to testPokemonProfile(id = 150, name = "Mewtwo")))
        val result = resolver(api).resolve(
            DeepLinkTarget.TopPokemon(formatId = 5, pokemonId = 150, lookback = LookbackWindow.Day)
        )

        val link = assertIs<DeepLinkResolver.ResolvedLink.TopPokemon>(result)
        assertEquals(5, link.formatId)
        assertEquals("Mewtwo", link.pokemonItem?.name)
        assertEquals(LookbackWindow.Day, link.lookback)
    }

    @Test
    fun topPokemon_pokemonFetchFails_stillResolvesWithoutItem() = runTest {
        val result = resolver().resolve(DeepLinkTarget.TopPokemon(formatId = 5, pokemonId = 999))

        val link = assertIs<DeepLinkResolver.ResolvedLink.TopPokemon>(result)
        assertEquals(5, link.formatId)
        assertNull(link.pokemonItem)
    }

    @Test
    fun topPokemon_withoutPokemonId_resolves() = runTest {
        val result = resolver().resolve(DeepLinkTarget.TopPokemon(formatId = null))

        val link = assertIs<DeepLinkResolver.ResolvedLink.TopPokemon>(result)
        assertNull(link.formatId)
        assertNull(link.pokemonItem)
    }

    // --- Search ---

    private fun searchQuery(
        pokemonIds: List<Int> = listOf(1),
        itemIds: List<Int?> = emptyList(),
        teraTypeIds: List<Int?> = emptyList(),
        abilityIds: List<Int?> = emptyList(),
        team2PokemonIds: List<Int> = emptyList(),
        formatId: Int = 5
    ) = SearchQueryParams(
        pokemonIds = pokemonIds,
        itemIds = itemIds,
        teraTypeIds = teraTypeIds,
        abilityIds = abilityIds,
        team2PokemonIds = team2PokemonIds,
        formatId = formatId
    )

    @Test
    fun search_populatesAllDisplayFieldsFromApiAndCatalogs() = runTest {
        val api = FakeDeepLinkApi(
            pokemonById = mapOf(
                1 to testPokemonProfile(id = 1, name = "Pikachu", imageUrl = "https://arcvgc.com/img/pokemon/pikachu.png")
            )
        )
        val result = resolver(api).resolve(
            DeepLinkTarget.Search(
                searchQuery(
                    pokemonIds = listOf(1),
                    itemIds = listOf(10),
                    teraTypeIds = listOf(20),
                    abilityIds = listOf(30)
                )
            )
        )

        val link = assertIs<DeepLinkResolver.ResolvedLink.Search>(result)
        val slot = link.params.filters.single()
        assertEquals(1, slot.pokemonId)
        assertEquals("Pikachu", slot.pokemonName)
        assertEquals("https://arcvgc.com/img/pokemon/pikachu.png", slot.pokemonImageUrl)
        assertEquals(10, slot.itemId)
        assertEquals("Booster Energy", slot.itemName)
        assertEquals("https://arcvgc.com/img/items/boosterenergy.png", slot.itemImageUrl)
        assertEquals(20, slot.teraTypeId)
        assertEquals("Fire", slot.teraTypeName)
        assertEquals("https://arcvgc.com/img/tera/fire.png", slot.teraTypeImageUrl)
        assertEquals(30, slot.abilityId)
        assertEquals("Sand Stream", slot.abilityName)
        assertEquals("Reg H", link.params.formatName)
    }

    @Test
    fun search_passesGeneralParamsThrough() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(1 to testPokemonProfile(id = 1)))
        val query = searchQuery(pokemonIds = listOf(1)).copy(
            winnerFilter = WinnerFilter.TEAM1,
            minimumRating = 1200,
            maximumRating = 1800,
            unratedOnly = true,
            orderBy = OrderBy.Time,
            timeRangeStart = 100L,
            timeRangeEnd = 200L,
            playerName = "Wolfe"
        )
        val result = resolver(api).resolve(DeepLinkTarget.Search(query))

        val params = assertIs<DeepLinkResolver.ResolvedLink.Search>(result).params
        assertEquals(WinnerFilter.TEAM1, params.winnerFilter)
        assertEquals(1200, params.minimumRating)
        assertEquals(1800, params.maximumRating)
        assertTrue(params.unratedOnly)
        assertEquals(OrderBy.Time, params.orderBy)
        assertEquals(100L, params.timeRangeStart)
        assertEquals(200L, params.timeRangeEnd)
        assertEquals("Wolfe", params.playerName)
        assertEquals(5, params.formatId)
    }

    @Test
    fun search_buildsTeam2Filters() = runTest {
        val api = FakeDeepLinkApi(
            pokemonById = mapOf(
                1 to testPokemonProfile(id = 1, name = "Pikachu"),
                2 to testPokemonProfile(id = 2, name = "Charizard")
            )
        )
        val query = searchQuery(pokemonIds = listOf(1), team2PokemonIds = listOf(2)).copy(
            team2ItemIds = listOf(10)
        )
        val result = resolver(api).resolve(DeepLinkTarget.Search(query))

        val params = assertIs<DeepLinkResolver.ResolvedLink.Search>(result).params
        assertEquals("Pikachu", params.filters.single().pokemonName)
        val team2Slot = params.team2Filters.single()
        assertEquals("Charizard", team2Slot.pokemonName)
        assertEquals("Booster Energy", team2Slot.itemName)
    }

    @Test
    fun search_failedPokemonFetch_dropsThatSlotOnly() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(1 to testPokemonProfile(id = 1, name = "Pikachu")))
        val result = resolver(api).resolve(
            DeepLinkTarget.Search(searchQuery(pokemonIds = listOf(1, 999)))
        )

        val params = assertIs<DeepLinkResolver.ResolvedLink.Search>(result).params
        assertEquals(listOf(1), params.filters.map { it.pokemonId })
    }

    @Test
    fun search_unknownCatalogIds_keepIdsButNullDisplayFields() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(1 to testPokemonProfile(id = 1)))
        val result = resolver(api).resolve(
            DeepLinkTarget.Search(
                searchQuery(pokemonIds = listOf(1), itemIds = listOf(77), teraTypeIds = listOf(88), abilityIds = listOf(99))
            )
        )

        val slot = assertIs<DeepLinkResolver.ResolvedLink.Search>(result).params.filters.single()
        assertEquals(77, slot.itemId)
        assertNull(slot.itemName)
        assertEquals(88, slot.teraTypeId)
        assertNull(slot.teraTypeName)
        assertEquals(99, slot.abilityId)
        assertNull(slot.abilityName)
    }

    @Test
    fun search_withoutCatalogProviders_resolvesWithNullDisplayFields() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(1 to testPokemonProfile(id = 1, name = "Pikachu")))
        val result = resolver(api, withCatalogs = false).resolve(
            DeepLinkTarget.Search(searchQuery(pokemonIds = listOf(1), itemIds = listOf(10)))
        )

        val params = assertIs<DeepLinkResolver.ResolvedLink.Search>(result).params
        val slot = params.filters.single()
        assertEquals("Pikachu", slot.pokemonName)
        assertEquals(10, slot.itemId)
        assertNull(slot.itemName)
        assertNull(params.formatName)
    }

    @Test
    fun search_waitsForLoadingCatalogs_thenTimesOutGracefully() = runTest {
        // A catalog stuck loading must not block resolution past the timeout
        val stuckCatalog = MutableStateFlow(CatalogState<ItemUiModel>(isLoading = true))
        val api = FakeDeepLinkApi(pokemonById = mapOf(1 to testPokemonProfile(id = 1)))
        val resolver = DeepLinkResolver(
            apiService = api,
            itemCatalogProvider = { emptyList() },
            catalogStateFlows = listOf(stuckCatalog)
        )

        val result = resolver.resolve(DeepLinkTarget.Search(searchQuery(pokemonIds = listOf(1))))

        assertIs<DeepLinkResolver.ResolvedLink.Search>(result)
    }

    @Test
    fun search_emptyFilters_resolvesToFilterlessSearch() = runTest {
        // The app emits pokemon-less search URLs (e.g. /search?p=&f=10&unrated&order=time);
        // reloading one must round-trip instead of falling back to Home
        val query = searchQuery(pokemonIds = emptyList()).copy(unratedOnly = true, orderBy = OrderBy.Time)
        val result = resolver().resolve(DeepLinkTarget.Search(query))

        val params = assertIs<DeepLinkResolver.ResolvedLink.Search>(result).params
        assertTrue(params.filters.isEmpty())
        assertTrue(params.unratedOnly)
        assertEquals(OrderBy.Time, params.orderBy)
        assertEquals("Reg H", params.formatName)
    }

    @Test
    fun search_allRequestedPokemonFailToResolve_returnsNull() = runTest {
        // Requested pokemon that all fail to fetch must NOT silently broaden the search
        val result = resolver().resolve(DeepLinkTarget.Search(searchQuery(pokemonIds = listOf(998, 999))))

        assertNull(result)
    }

    @Test
    fun search_team2Only_resolves() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(2 to testPokemonProfile(id = 2, name = "Charizard")))
        val result = resolver(api).resolve(
            DeepLinkTarget.Search(searchQuery(pokemonIds = emptyList(), team2PokemonIds = listOf(2)))
        )

        val params = assertIs<DeepLinkResolver.ResolvedLink.Search>(result).params
        assertTrue(params.filters.isEmpty())
        assertEquals("Charizard", params.team2Filters.single().pokemonName)
    }

    @Test
    fun search_team1ResolvesButAllTeam2Fail_returnsNull() = runTest {
        // Dropping an entire requested team silently broadens the search — reject instead
        val api = FakeDeepLinkApi(pokemonById = mapOf(1 to testPokemonProfile(id = 1)))
        val result = resolver(api).resolve(
            DeepLinkTarget.Search(searchQuery(pokemonIds = listOf(1), team2PokemonIds = listOf(999)))
        )

        assertNull(result)
    }

    @Test
    fun search_team2ResolvesButAllTeam1Fail_returnsNull() = runTest {
        val api = FakeDeepLinkApi(pokemonById = mapOf(2 to testPokemonProfile(id = 2)))
        val result = resolver(api).resolve(
            DeepLinkTarget.Search(searchQuery(pokemonIds = listOf(999), team2PokemonIds = listOf(2)))
        )

        assertNull(result)
    }
}

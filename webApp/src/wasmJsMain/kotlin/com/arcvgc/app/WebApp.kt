package com.arcvgc.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.arcvgc.app.data.SettingsRepository as SharedSettingsRepository
import com.arcvgc.app.di.DependencyContainer
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.BattleOverlayRequest
import com.arcvgc.app.ui.LocalBattleOverlay
import com.arcvgc.app.ui.LocalWindowSizeClass
import com.arcvgc.app.ui.ProvideViewModelStore
import com.arcvgc.app.ui.WindowSizeClass
import com.arcvgc.app.ui.historyGo
import com.arcvgc.app.ui.pushHistoryState
import com.arcvgc.app.ui.battledetail.BattleDetailPanel
import com.arcvgc.app.ui.contentlist.ContentListPage
import kotlinx.browser.window
import org.w3c.dom.events.Event
import com.arcvgc.app.ui.favorites.FavoritesPage
import com.arcvgc.app.ui.model.AppTheme
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.DarkModeOption
import com.arcvgc.app.ui.search.SearchPage
import com.arcvgc.app.ui.settings.SettingsPage

private val RedColorScheme = lightColorScheme(
    primary = Color(0xFFDC2F35),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410003),
    secondary = Color(0xFF775654),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF2C1514),
)

private val BlueColorScheme = lightColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF041E49),
    secondary = Color(0xFF5A5F71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E3FD),
    onSecondaryContainer = Color(0xFF171C28),
)

private val YellowColorScheme = lightColorScheme(
    primary = Color(0xFFE6A700),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE08D),
    onPrimaryContainer = Color(0xFF3D2E00),
    secondary = Color(0xFF6B5F3F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE08D),
    onSecondaryContainer = Color(0xFF241C04),
)

private val PurpleColorScheme = lightColorScheme(
    primary = Color(0xFF7B1FA2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3DAFF),
    onPrimaryContainer = Color(0xFF2C0042),
    secondary = Color(0xFF695768),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3DAFF),
    onSecondaryContainer = Color(0xFF231525),
)

private val RedDarkColorScheme = darkColorScheme(
    primary = Color(0xFFDC2F35),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF930012),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDBA),
    onSecondary = Color(0xFF442928),
    secondaryContainer = Color(0xFF5D3F3D),
    onSecondaryContainer = Color(0xFFFFDAD6),
)

private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF1A73E8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0842A0),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFBBC6DC),
    onSecondary = Color(0xFF263141),
    secondaryContainer = Color(0xFF3C4758),
    onSecondaryContainer = Color(0xFFD3E3FD),
)

private val YellowDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE6A700),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF7B5800),
    onPrimaryContainer = Color(0xFFFFE08D),
    secondary = Color(0xFFD3C4A0),
    onSecondary = Color(0xFF393017),
    secondaryContainer = Color(0xFF51472B),
    onSecondaryContainer = Color(0xFFFFE08D),
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7B1FA2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5B0080),
    onPrimaryContainer = Color(0xFFF3DAFF),
    secondary = Color(0xFFD7BDD5),
    onSecondary = Color(0xFF3B2839),
    secondaryContainer = Color(0xFF533E50),
    onSecondaryContainer = Color(0xFFF3DAFF),
)

private fun colorSchemeForTheme(themeId: Int, isDark: Boolean): ColorScheme = if (isDark) {
    when (themeId) {
        AppTheme.Blue.id -> BlueDarkColorScheme
        AppTheme.Yellow.id -> YellowDarkColorScheme
        AppTheme.Purple.id -> PurpleDarkColorScheme
        else -> RedDarkColorScheme
    }
} else {
    when (themeId) {
        AppTheme.Blue.id -> BlueColorScheme
        AppTheme.Yellow.id -> YellowColorScheme
        AppTheme.Purple.id -> PurpleColorScheme
        else -> RedColorScheme
    }
}

private enum class Tab(
    val label: String,
    val icon: ImageVector
) {
    Top("Top", Icons.Default.Star),
    Search("Search", Icons.Default.Search),
    Favorites("Favorites", Icons.Default.Favorite),
    Settings("Settings", Icons.Default.Settings)
}

@Composable
fun WebApp() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .crossfade(true)
            .build()
    }

    remember {
        DependencyContainer.pokemonCatalogRepository
        DependencyContainer.itemCatalogRepository
        DependencyContainer.teraTypeCatalogRepository
        DependencyContainer.formatCatalogRepository
        DependencyContainer.appConfigRepository
        Unit
    }

    val catalogVersionChanged by DependencyContainer.appConfigRepository.catalogVersionChanged.collectAsState()
    LaunchedEffect(catalogVersionChanged) {
        if (catalogVersionChanged) {
            DependencyContainer.pokemonCatalogRepository.reload()
            DependencyContainer.itemCatalogRepository.reload()
            DependencyContainer.teraTypeCatalogRepository.reload()
            DependencyContainer.formatCatalogRepository.reload()
        }
    }

    val themeId by DependencyContainer.settingsRepository.selectedThemeId.collectAsState()
    val darkModeId by DependencyContainer.settingsRepository.darkModeId.collectAsState()
    val isDark = when (DarkModeOption.fromId(darkModeId)) {
        DarkModeOption.System -> isSystemInDarkTheme()
        DarkModeOption.Light -> false
        DarkModeOption.Dark -> true
    }

    MaterialTheme(colorScheme = colorSchemeForTheme(themeId, isDark)) {
        var selectedTab by remember { mutableIntStateOf(0) }
        val searchOverlayState = remember { mutableStateOf<SearchParams?>(null) }
        var searchOverlayParams by searchOverlayState
        val navStackState = remember { mutableStateOf(listOf<MobileNavEntry>()) }
        var navStack by navStackState
        val historyDepthState = remember { mutableIntStateOf(0) }
        var historyDepth by historyDepthState
        val popStatesToIgnoreState = remember { mutableIntStateOf(0) }
        var popStatesToIgnore by popStatesToIgnoreState
        val tabs = Tab.entries

        // Browser back button handler
        DisposableEffect(Unit) {
            val listener: (Event) -> Unit = {
                if (popStatesToIgnoreState.intValue > 0) {
                    popStatesToIgnoreState.intValue--
                } else {
                    if (navStackState.value.isNotEmpty()) {
                        navStackState.value = navStackState.value.dropLast(1)
                    } else if (searchOverlayState.value != null) {
                        searchOverlayState.value = null
                    }
                    historyDepthState.intValue = maxOf(0, historyDepthState.intValue - 1)
                }
            }
            window.addEventListener("popstate", listener)
            onDispose { window.removeEventListener("popstate", listener) }
        }

        val handleSearch: (SearchParams) -> Unit = { params ->
            val isNewSearch = searchOverlayParams == null
            searchOverlayParams = params
            if (isNewSearch) {
                pushHistoryState()
                historyDepth++
            }
        }

        val handleSearchBack: () -> Unit = {
            val entriesToRemove = minOf(navStack.size + 1, historyDepth)
            searchOverlayParams = null
            navStack = emptyList()
            if (entriesToRemove > 0) {
                popStatesToIgnore++
                historyGo(-entriesToRemove)
                historyDepth -= entriesToRemove
            }
        }

        val handlePushEntry: (MobileNavEntry) -> Unit = { entry ->
            navStack = navStack + entry
            pushHistoryState()
            historyDepth++
        }

        val handlePopEntry: () -> Unit = {
            navStack = navStack.dropLast(1)
            if (historyDepth > 0) {
                popStatesToIgnore++
                historyGo(-1)
                historyDepth--
            }
        }

        val handleTabSelected: (Int) -> Unit = { index ->
            if (historyDepth > 0) {
                popStatesToIgnore++
                historyGo(-historyDepth)
                historyDepth = 0
            }
            selectedTab = index
            searchOverlayParams = null
            navStack = emptyList()
        }

        Surface(modifier = Modifier.fillMaxSize()) {
            ProvideViewModelStore {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val windowSizeClass = if (maxWidth < 600.dp) {
                        WindowSizeClass.Compact
                    } else {
                        WindowSizeClass.Expanded
                    }

                    CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                        if (windowSizeClass == WindowSizeClass.Compact) {
                            MobileLayout(
                                tabs = tabs,
                                selectedTab = selectedTab,
                                onTabSelected = handleTabSelected,
                                searchOverlayParams = searchOverlayParams,
                                onSearch = handleSearch,
                                onSearchBack = handleSearchBack,
                                navStack = navStack,
                                onPushEntry = handlePushEntry,
                                onPopEntry = handlePopEntry,
                                onReplaceNavStack = { entry ->
                                    navStack = listOf(entry)
                                    pushHistoryState()
                                    historyDepth++
                                }
                            )
                        } else {
                            DesktopLayout(
                                tabs = tabs,
                                selectedTab = selectedTab,
                                onTabSelected = handleTabSelected,
                                searchOverlayParams = searchOverlayParams,
                                onSearch = handleSearch,
                                onSearchBack = handleSearchBack
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopLayout(
    tabs: List<Tab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    searchOverlayParams: SearchParams?,
    onSearch: (SearchParams) -> Unit,
    onSearchBack: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            modifier = Modifier.fillMaxHeight().shadow(elevation = 8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                val tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    icon = { Icon(tab.icon, contentDescription = tab.label, tint = tint) },
                    label = { Text(tab.label, color = tint) },
                    colors = NavigationRailItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = SharedSettingsRepository.DISCLAIMER_TEXT,
                fontSize = 9.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 80.dp)
            )
        }

        val contentModifier = Modifier.weight(1f).fillMaxHeight()

        if (searchOverlayParams != null) {
            ContentListPage(
                mode = ContentListMode.Search(searchOverlayParams),
                onBack = onSearchBack,
                onSearchParamsChanged = onSearch,
                modifier = contentModifier
            )
        } else {
            when (tabs[selectedTab]) {
                Tab.Top -> ContentListPage(modifier = contentModifier)
                Tab.Search -> SearchPage(modifier = contentModifier, onSearch = onSearch)
                Tab.Favorites -> FavoritesPage(modifier = contentModifier)
                Tab.Settings -> SettingsPage(modifier = contentModifier)
            }
        }
    }
}

private sealed class MobileNavEntry {
    data class BattleDetail(val request: BattleOverlayRequest) : MobileNavEntry()
    data class Pokemon(
        val id: Int,
        val name: String,
        val imageUrl: String?,
        val typeImageUrls: List<String> = emptyList(),
        val formatId: Int? = null
    ) : MobileNavEntry()
    data class Player(val id: Int, val name: String, val formatId: Int? = null) : MobileNavEntry()
}

@Composable
private fun MobileLayout(
    tabs: List<Tab>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    searchOverlayParams: SearchParams?,
    onSearch: (SearchParams) -> Unit,
    onSearchBack: () -> Unit,
    navStack: List<MobileNavEntry>,
    onPushEntry: (MobileNavEntry) -> Unit,
    onPopEntry: () -> Unit,
    onReplaceNavStack: (MobileNavEntry) -> Unit
) {
    val favoriteBattleIds by DependencyContainer.favoritesRepository.favoriteBattleIds.collectAsState()
    val showWinnerHighlight by DependencyContainer.settingsRepository.showWinnerHighlight.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalBattleOverlay provides { request ->
                if (request != null) {
                    onReplaceNavStack(MobileNavEntry.BattleDetail(request))
                }
            }
        ) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTab == index
                            val tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { onTabSelected(index) },
                                icon = { Icon(tab.icon, contentDescription = tab.label, tint = tint) },
                                label = { Text(tab.label, color = tint) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                when (tabs[selectedTab]) {
                    Tab.Top -> ContentListPage(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                    Tab.Search -> SearchPage(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        onSearch = onSearch
                    )
                    Tab.Favorites -> FavoritesPage(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        onPokemonClick = { id, name, imageUrl, typeImageUrls, formatId ->
                            onPushEntry(MobileNavEntry.Pokemon(id, name, imageUrl, typeImageUrls, formatId))
                        },
                        onPlayerClick = { id, name, formatId ->
                            onPushEntry(MobileNavEntry.Player(id, name, formatId))
                        }
                    )
                    Tab.Settings -> SettingsPage(
                        modifier = Modifier.fillMaxSize().padding(innerPadding)
                    )
                }
            }
        }

        // Search overlay covers entire screen including bottom bar
        if (searchOverlayParams != null) {
            CompositionLocalProvider(
                LocalBattleOverlay provides { request ->
                    if (request != null) {
                        onReplaceNavStack(MobileNavEntry.BattleDetail(request))
                    }
                }
            ) {
                ContentListPage(
                    mode = ContentListMode.Search(searchOverlayParams),
                    onBack = { onSearchBack() },
                    onSearchParamsChanged = onSearch,
                    modifier = Modifier.fillMaxSize(),
                    onPokemonClick = { id, name, imageUrl, typeImageUrls, formatId ->
                        onPushEntry(MobileNavEntry.Pokemon(id, name, imageUrl, typeImageUrls, formatId))
                    },
                    onPlayerClick = { id, name, formatId ->
                        onPushEntry(MobileNavEntry.Player(id, name, formatId))
                    }
                )
            }
        }

        // Render navigation stack — each entry is a full-screen overlay
        navStack.forEachIndexed { index, entry ->
            when (entry) {
                is MobileNavEntry.BattleDetail -> {
                    val request = entry.request
                    val isFavorited = request.battleId in favoriteBattleIds
                    BattleDetailPanel(
                        battleId = request.battleId,
                        isFavorited = isFavorited,
                        onToggleFavorite = {
                            DependencyContainer.favoritesRepository.toggleBattleFavorite(request.battleId)
                        },
                        onClose = { onPopEntry() },
                        player1IsWinner = request.player1IsWinner,
                        player2IsWinner = request.player2IsWinner,
                        showWinnerHighlight = showWinnerHighlight,
                        onPokemonClick = { id, name, imageUrl, typeImageUrls, formatId ->
                            onPushEntry(MobileNavEntry.Pokemon(id, name, imageUrl, typeImageUrls, formatId))
                        },
                        onPlayerClick = { id, name, formatId ->
                            onPushEntry(MobileNavEntry.Player(id, name, formatId))
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
                is MobileNavEntry.Pokemon -> {
                    CompositionLocalProvider(
                        LocalBattleOverlay provides { request ->
                            if (request != null) onPushEntry(MobileNavEntry.BattleDetail(request))
                        }
                    ) {
                        ContentListPage(
                            mode = ContentListMode.Pokemon(
                                entry.id, entry.name, entry.imageUrl,
                                entry.typeImageUrls.getOrNull(0),
                                entry.typeImageUrls.getOrNull(1),
                                entry.formatId
                            ),
                            onBack = { onPopEntry() },
                            modifier = Modifier.fillMaxSize(),
                            onPlayerClick = { id, name, formatId ->
                                onPushEntry(MobileNavEntry.Player(id, name, formatId))
                            }
                        )
                    }
                }
                is MobileNavEntry.Player -> {
                    CompositionLocalProvider(
                        LocalBattleOverlay provides { request ->
                            if (request != null) onPushEntry(MobileNavEntry.BattleDetail(request))
                        }
                    ) {
                        ContentListPage(
                            mode = ContentListMode.Player(entry.id, entry.name, entry.formatId),
                            onBack = { onPopEntry() },
                            modifier = Modifier.fillMaxSize(),
                            onPokemonClick = { id, name, imageUrl, typeImageUrls, formatId ->
                                onPushEntry(MobileNavEntry.Pokemon(id, name, imageUrl, typeImageUrls, formatId))
                            }
                        )
                    }
                }
            }
        }
    }
}

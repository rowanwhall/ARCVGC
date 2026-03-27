package com.arcvgc.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcvgc.app.data.DeepLinkResolver
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.domain.model.DeepLink
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.battledetail.BattleDetailPage
import com.arcvgc.app.ui.battledetail.BattleDetailViewModel
import com.arcvgc.app.ui.battledetail.ReplayOverlay
import com.arcvgc.app.ui.components.ForceUpgradeOverlay
import com.arcvgc.app.ui.contentlist.ContentListPage
import com.arcvgc.app.ui.favorites.FavoritesPage
import com.arcvgc.app.ui.model.AppTheme
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.DarkModeOption
import com.arcvgc.app.ui.model.FavoriteContentType
import com.arcvgc.app.ui.search.SearchPage
import com.arcvgc.app.ui.settings.SettingsPage
import com.arcvgc.app.ui.settings.SettingsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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

@HiltViewModel
class DeepLinkViewModel @Inject constructor(
    val deepLinkResolver: DeepLinkResolver
) : ViewModel()

@Composable
fun App(deepLink: DeepLink? = null) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val themeId by settingsViewModel.settingsRepository.selectedThemeId.collectAsStateWithLifecycle()
    val darkModeId by settingsViewModel.settingsRepository.darkModeId.collectAsStateWithLifecycle()
    val isDark = when (DarkModeOption.fromId(darkModeId)) {
        DarkModeOption.System -> isSystemInDarkTheme()
        DarkModeOption.Light -> false
        DarkModeOption.Dark -> true
    }

    val activity = LocalActivity.current as androidx.activity.ComponentActivity
    DisposableEffect(isDark) {
        activity.enableEdgeToEdge(
            statusBarStyle = if (isDark) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            },
            navigationBarStyle = if (isDark) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
            }
        )
        onDispose {}
    }

    MaterialTheme(colorScheme = colorSchemeForTheme(themeId, isDark)) {
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        var searchOverlayParams by remember { mutableStateOf<SearchParams?>(null) }
        var deepLinkOverlay by remember { mutableStateOf<ContentListMode?>(null) }
        var deepLinkBattleDetailId by remember { mutableStateOf<Int?>(null) }
        var deepLinkReplayUrl by remember { mutableStateOf<String?>(null) }
        var deepLinkFavoritesType by remember { mutableStateOf<FavoriteContentType?>(null) }
        val tabs = Tab.entries

        if (deepLink != null) {
            val deepLinkViewModel: DeepLinkViewModel = hiltViewModel()
            LaunchedEffect(deepLink) {
                try {
                    // If any deep link has a battleId, navigate directly to battle detail
                    val battleId = deepLink.battleId
                    if (battleId != null) {
                        deepLinkBattleDetailId = battleId
                    }

                    val resolved = deepLinkViewModel.deepLinkResolver.resolve(deepLink)
                    // Only apply root target if no battleId (otherwise battle detail takes over)
                    if (battleId == null) {
                        when (resolved) {
                            is DeepLinkResolver.ResolvedLink.Home -> { /* default tab */ }
                            is DeepLinkResolver.ResolvedLink.Pokemon -> {
                                val item = resolved.item
                                deepLinkOverlay = ContentListMode.Pokemon(
                                    pokemonId = item.id,
                                    name = item.name,
                                    imageUrl = item.imageUrl,
                                    typeImageUrl1 = item.types.getOrNull(0)?.imageUrl,
                                    typeImageUrl2 = item.types.getOrNull(1)?.imageUrl
                                )
                            }
                            is DeepLinkResolver.ResolvedLink.Player -> {
                                deepLinkOverlay = ContentListMode.Player(
                                    playerId = resolved.item.id,
                                    playerName = resolved.item.name
                                )
                            }
                            is DeepLinkResolver.ResolvedLink.Favorites -> {
                                selectedTab = 2
                                deepLinkFavoritesType = resolved.contentType
                            }
                            is DeepLinkResolver.ResolvedLink.Search -> {
                                searchOverlayParams = resolved.params
                            }
                            is DeepLinkResolver.ResolvedLink.SearchTab -> {
                                selectedTab = 1
                            }
                            is DeepLinkResolver.ResolvedLink.SettingsTab -> {
                                selectedTab = 3
                            }
                            is DeepLinkResolver.ResolvedLink.TopPokemon -> {
                                deepLinkOverlay = ContentListMode.TopPokemon(
                                    formatId = resolved.formatId
                                )
                            }
                            null -> {}
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    Column {
                    HorizontalDivider()
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTab == index
                            val tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    selectedTab = index
                                },
                                icon = { Icon(tab.icon, contentDescription = tab.label, tint = tint) },
                                label = { Text(tab.label, color = tint) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                    }
                },

            ) { innerPadding ->
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_crossfade"
                ) { tab ->
                    when (tabs[tab]) {
                        Tab.Top -> ContentListPage(
                            modifier = Modifier.padding(innerPadding),
                            consumeTopInsets = false
                        )
                        Tab.Search -> SearchPage(
                            modifier = Modifier.padding(innerPadding),
                            onSearch = { searchOverlayParams = it }
                        )
                        Tab.Favorites -> FavoritesPage(
                            modifier = Modifier.padding(innerPadding),
                            consumeTopInsets = false,
                            initialSubTab = deepLinkFavoritesType?.let { type ->
                                when (type) {
                                    FavoriteContentType.Battles -> 0
                                    FavoriteContentType.Pokemon -> 1
                                    FavoriteContentType.Players -> 2
                                }
                            }
                        )
                        Tab.Settings -> SettingsPage(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }

            val lastSearchOverlayParams = rememberLastNonNull(searchOverlayParams)
            AnimatedVisibility(
                visible = searchOverlayParams != null,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                lastSearchOverlayParams?.let { params ->
                    ContentListPage(
                        mode = ContentListMode.Search(params),
                        onBack = { searchOverlayParams = null },
                        onSearchParamsChanged = { searchOverlayParams = it }
                    )
                }
            }

            val lastDeepLinkOverlay = rememberLastNonNull(deepLinkOverlay)
            AnimatedVisibility(
                visible = deepLinkOverlay != null,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                lastDeepLinkOverlay?.let { mode ->
                    ContentListPage(
                        mode = mode,
                        onBack = { deepLinkOverlay = null }
                    )
                }
            }

            val lastDeepLinkBattleDetailId = rememberLastNonNull(deepLinkBattleDetailId)
            AnimatedVisibility(
                visible = deepLinkBattleDetailId != null,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {
                lastDeepLinkBattleDetailId?.let { battleId ->
                    val battleDetailViewModel: BattleDetailViewModel = hiltViewModel(
                        key = "deep_link_battle_detail_$battleId"
                    )
                    val battleDetailState by battleDetailViewModel.state.collectAsStateWithLifecycle()

                    LaunchedEffect(battleId) {
                        battleDetailViewModel.loadBattleDetail(battleId)
                    }

                    BattleDetailPage(
                        state = battleDetailState,
                        onBack = { deepLinkBattleDetailId = null },
                        onRetry = { battleDetailViewModel.loadBattleDetail(battleId) },
                        onViewReplay = { url -> deepLinkReplayUrl = url },
                        modifier = Modifier.fillMaxSize(),
                        statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    )
                }
            }

            val lastDeepLinkReplayUrl = rememberLastNonNull(deepLinkReplayUrl)
            AnimatedVisibility(
                visible = deepLinkReplayUrl != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                lastDeepLinkReplayUrl?.let { url ->
                    ReplayOverlay(
                        replayUrl = url,
                        onDismiss = { deepLinkReplayUrl = null },
                        statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    )
                }
            }

            val config by settingsViewModel.appConfigRepository.config.collectAsStateWithLifecycle()
            val context = LocalContext.current
            val currentVersionCode = remember {
                try {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    packageInfo.longVersionCode.toInt()
                } catch (_: Exception) {
                    0
                }
            }
            config?.let {
                if (it.minAndroidVersion > currentVersionCode) {
                    ForceUpgradeOverlay()
                }
            }
        }
    }
}

@Composable
private fun <T> rememberLastNonNull(value: T?): T? {
    var last by remember { mutableStateOf(value) }
    if (value != null) last = value
    return last
}

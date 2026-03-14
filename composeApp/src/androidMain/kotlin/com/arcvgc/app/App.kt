package com.arcvgc.app

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcvgc.app.data.repository.AppConfigRepository
import com.arcvgc.app.domain.model.SearchParams
import com.arcvgc.app.ui.components.ForceUpgradeOverlay
import com.arcvgc.app.ui.contentlist.ContentListPage
import com.arcvgc.app.ui.favorites.FavoritesPage
import com.arcvgc.app.ui.model.AppTheme
import com.arcvgc.app.ui.model.ContentListMode
import com.arcvgc.app.ui.model.DarkModeOption
import com.arcvgc.app.ui.search.SearchPage
import com.arcvgc.app.ui.settings.SettingsPage
import com.arcvgc.app.ui.settings.SettingsViewModel

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
fun App() {
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
        val tabs = Tab.entries

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.shadow(elevation = 8.dp)
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
                                onClick = { selectedTab = index },
                                icon = { Icon(tab.icon, contentDescription = tab.label, tint = tint) },
                                label = { Text(tab.label, color = tint) },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                },

            ) { innerPadding ->
                when (tabs[selectedTab]) {
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
                        consumeTopInsets = false
                    )
                    Tab.Settings -> SettingsPage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            searchOverlayParams?.let { params ->
                ContentListPage(
                    mode = ContentListMode.Search(params),
                    onBack = { searchOverlayParams = null },
                    onSearchParamsChanged = { searchOverlayParams = it }
                )
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

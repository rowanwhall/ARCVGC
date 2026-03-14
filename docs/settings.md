# Settings Architecture

## Shared layer
- **`SettingsRepository`** (`shared/.../data/`): Manages all app settings. Constructor: `SettingsRepository(storage: SettingsStorage, cacheStorage: CatalogCacheStorage? = null)`. Each setting has its own `MutableStateFlow` (e.g., `_showWinnerHighlight`, `_selectedThemeId`, `_darkModeId`) plus a combined `settingItems: StateFlow<List<SettingItem>>` that auto-updates when any setting changes. Methods: `setBooleanSetting(key, value)` / `setIntSetting(key, value)` (generic dispatchers), `performAction(key)` (one-shot actions like cache clearing), `getSettingItems()` / `isShowWinnerHighlightEnabled()` / `getSelectedThemeId()` / `getDarkModeId()` (snapshots for iOS interop)
- **`SettingsStorage`** (`expect`/`actual`): `getBoolean`/`putBoolean`/`getInt`/`putInt` — Android uses `SharedPreferences`, iOS uses `NSUserDefaults`, Web uses `localStorage` via `@JsFun`
- **`SettingItem`** (`shared/.../ui/model/`): Sealed class with five subclasses: `DarkModeChoice` (key, title, subtitle, selectedModeId), `Toggle` (key, title, subtitle, isEnabled), `ColorChoice` (key, title, subtitle, selectedThemeId), `Action` (key, title, subtitle, confirmationMessage — triggers one-shot action), `Link` (key, title, subtitle, url — opens external URL in browser). Metadata defined once in shared `buildSettingItems()` — platforms never hardcode setting titles/subtitles
- **`DarkModeOption`** (`shared/.../ui/model/`): Enum with `System` (0), `Light` (1), `Dark` (2). `fromId()` companion for safe lookup.
- **Adding a new boolean setting**: Add a `MutableStateFlow` + setter in `SettingsRepository`, add to `buildSettingItems()`, add key routing in `setBooleanSetting()` — no platform UI changes needed. For new `SettingItem` subclass types, each platform's settings UI needs a new rendering branch.

## Android
- `SettingsRepository` interface + `SettingsRepositoryImpl` (Hilt-injectable, delegates to shared). Passes `CatalogCacheStorage(context)` to shared repo. Exposes `settingItems: StateFlow<List<SettingItem>>`, `showWinnerHighlight: StateFlow<Boolean>`, `selectedThemeId: StateFlow<Int>`, `darkModeId: StateFlow<Int>`, `performAction(key)`
- `SettingsViewModel` (`@HiltViewModel`): Injects `SettingsRepository` + all 4 catalog repos + `AppConfigRepository`. `performAction(key)` calls `settingsRepository.performAction()` then `reload()` on all 4 catalog repos
- `SettingsPage`: Collects `settingItems` flow, renders each via `when`: `DarkModeChoiceSettingRow` (tappable -> `DarkModePickerSheet`), `ToggleSettingRow` (switch), `ColorChoiceSettingRow` (tappable with color swatch -> `ThemePickerSheet`), `ActionSettingRow` (tappable -> confirmation `AlertDialog`), `LinkSettingRow` (tappable -> opens URL via `Intent(ACTION_VIEW)`)

## iOS
- `SettingsStore` (`@MainActor ObservableObject`): Wraps shared `SettingsRepository` (passes `CatalogCacheStorage()` to constructor), exposes `@Published settingItems`, `@Published showWinnerHighlight`, `@Published selectedThemeId`, `@Published darkModeId`, `@Published themeColor`, computed `colorSchemeOverride: ColorScheme?`. Methods: `setBooleanSetting()`, `setIntSetting()`, `performAction()`. `syncState()` refreshes all after mutations
- `SettingsView`: Takes `settingsStore` + optional `catalogStore`. Iterates `settingsStore.settingItems` with SKIE `switch onEnum(of:)` — `.darkModeChoice` renders tappable row -> `DarkModePickerSheet`, `.toggle` renders `Toggle`, `.colorChoice` renders tappable row -> `ThemePickerSheet`, `.action` renders tappable row -> confirmation `.alert()` that calls `performAction()` + `catalogStore?.reload()`, `.link` renders SwiftUI `Link` that opens URL in browser

## Web
- Uses shared `SettingsRepository` directly via `DependencyContainer` (passes `cacheStorage` to constructor)
- `SettingsPage`: Collects `settingItems` flow, same `when` pattern as Android — `DarkModeChoiceSettingRow` (-> `DarkModePickerDialog`), `ToggleSettingRow`, `ColorChoiceSettingRow` (-> `ThemePickerDialog`), `ActionSettingRow` (-> confirmation `AlertDialog` that calls `performAction()` + `reload()` on all 4 catalog repos from `DependencyContainer`), `LinkSettingRow` (opens URL via `window.open()`)

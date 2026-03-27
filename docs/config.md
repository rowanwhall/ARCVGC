# App Config

Remote config system fetched from the backend on app startup. Provides the default format for the Home tab, Search, and Pokemon content lists (when no format is threaded from battle detail), minimum app versions per platform (for forced upgrade on Android/iOS), and a minimum catalog version (for automatic cache invalidation).

## API

`GET /api/v1/config/` returns:
```json
{
  "success": true,
  "data": {
    "current_format": { "id": 1, "name": "gen9vgc2026regfbo3", "formatted_name": "[Gen 9] VGC 2026 Reg F (Bo3)" },
    "min_android_version": 1,
    "min_ios_version": 1,
    "min_web_version": 1,
    "min_catalog_version": 1
  }
}
```

## Shared layer
- **DTO**: `AppConfigResponseDto` / `AppConfigDataDto` (`shared/.../network/model/AppConfigResponseDto.kt`). Reuses existing `FormatListItemDto` for the `current_format` field.
- **Domain model**: `AppConfig` (`shared/.../domain/model/AppConfig.kt`) — `defaultFormat: Format`, `minAndroidVersion`, `minIosVersion`, `minWebVersion`, `minCatalogVersion`
- **Mapper**: `AppConfigDataDto.toDomain()` (`shared/.../network/mapper/AppConfigMapper.kt`)
- **ApiService**: `getConfig(): NetworkResult<AppConfig>` — standard `NetworkResult` pattern
- **`AppConfigStorage`** (`expect`/`actual`): `getString`/`putString`/`getInt`/`putInt` — Android uses `SharedPreferences("app_config")`, iOS uses `NSUserDefaults` with `"app_config_"` prefix, Web uses `localStorage` via `@JsFun` with `"app_config_"` prefix
- **`AppConfigRepository`** (`shared/.../data/AppConfigRepository.kt`): Constructor takes `apiService`, `storage: AppConfigStorage`, `catalogCacheStorage: CatalogCacheStorage`.
  - **State**: `config: StateFlow<AppConfig?>` (null = not yet loaded), `catalogVersionChanged: StateFlow<Boolean>` (signal for platforms to reload catalogs)
  - **Init flow**: Load cached config from storage -> emit if present -> fetch fresh config -> persist on success -> check catalog version (if `minCatalogVersion > localVersion`, clear `CatalogCache` and signal)
  - **Fail-open**: Network failure keeps cached config (or null on first launch). Forced upgrade only triggers if config was successfully fetched and version check fails.
  - **iOS interop**: `getConfig(): AppConfig?`, `getDefaultFormatId(): Int` (fallback `1`)

## Android
- **Hilt wrapper**: `AppConfigRepository` interface + `AppConfigRepositoryImpl` in `composeApp/.../data/repository/AppConfigRepository.kt`, bound in `RepositoryModule`
- **Eager init**: `CatalogInitializer` injects `AppConfigRepository` (triggers lazy init). Observes `catalogVersionChanged` to call `reload()` on all 4 catalog repos.
- **Home tab**: `ContentListViewModel.waitForConfigThenLoad()` — if config is null, waits for first non-null emission before calling `loadContent()`. `fetchContent()` Home branch uses `appConfigRepository.config.value?.defaultFormat?.id ?: 1`.
- **Search tab**: `SearchViewModel.init` observes config flow, pre-selects default format as `FormatUiModel` when `selectedFormat == null`.
- **Forced upgrade**: `ForceUpgradeOverlay` composable in `composeApp/.../ui/components/ForceUpgradeOverlay.kt`. Rendered in `App.kt` when `config.minAndroidVersion > currentVersionCode`. Full-screen surface, no dismissal.

## iOS
- **`AppConfigStore`** (`iosApp/iosApp/AppConfigStore.swift`): `@MainActor ObservableObject` wrapping shared `AppConfigRepository`. `@Published config: AppConfig?`, `@Published catalogVersionChanged: Bool`. Convenience `defaultFormatId: Int32` getter.
- **DependencyContainer**: `appConfigStore` created after `apiService` and `cacheStorage`.
- **Catalog reload**: `ContentView` observes `.onChange(of: appConfigStore.catalogVersionChanged)` -> `container.catalogStore.reload()`
- **Home tab**: `ContentListViewModel` takes optional `appConfigStore: AppConfigStore?`. `loadContent()` waits for config if nil. `fetchContent()` Home branch uses `appConfigStore?.defaultFormatId ?? 1`.
- **Search tab**: `SearchView` observes config via `.task` + `.onChange(of:)`, pre-selects default format when available.
- **Forced upgrade**: `ForceUpgradeView` in `iosApp/iosApp/ForceUpgradeView.swift`. Presented via `.fullScreenCover` with `.interactiveDismissDisabled(true)` when `config.minIosVersion > CFBundleVersion`.

## Web
- **DependencyContainer**: `appConfigRepository` added as lazy singleton.
- **Eager init + catalog reload**: `WebApp.kt` accesses `appConfigRepository` in `remember {}` block. `LaunchedEffect(catalogVersionChanged)` calls `reload()` on all 4 catalog repos.
- **Home tab**: `ContentListViewModel` takes optional `appConfigRepository`. Same wait-for-config pattern as Android.
- **Search tab**: `SearchViewModel` takes optional `appConfigRepository`, pre-selects default format.
- **No forced upgrade**: Web skips forced upgrade. `min_web_version` is in the model for future use.

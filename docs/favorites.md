# Favorites Architecture

## Shared layer
- **`FavoritesRepository`** (`shared/.../data/`): In-memory `StateFlow<Set<Int>>` for pokemon and battle IDs, `StateFlow<Set<String>>` for player names. Delegates persistence to `FavoritesStorage`. Methods: `togglePokemonFavorite()`, `toggleBattleFavorite()`, `togglePlayerFavorite()`, `isPokemonFavorited()`, `isBattleFavorited()`, `isPlayerFavorited()`, `currentPokemonIds()`, `currentBattleIds()`, `currentPlayerNames()` (list snapshots for iOS interop)
- **`FavoritesStorage`** (`expect`/`actual`): `loadIds(key)` / `saveIds(key, ids)` — Android uses `SharedPreferences`, iOS uses `NSUserDefaults`
- **`ContentListItem`** (`shared/.../ui/model/`): Sealed class enabling heterogeneous list rendering. Variants: `Battle(uiModel: BattleCardUiModel)`, `Pokemon(id, name, imageUrl, types: List<TypeUiModel>)`, `Player(id, name)`. Each has abstract `listKey: String` for stable list keys.
- **`FavoriteContentType`** (`shared/.../ui/model/`): Enum — `Battles`, `Pokemon`, `Players`. Parameterizes `ContentListMode.Favorites`.
- **`ContentListItemMapper`** (`shared/.../ui/mapper/`): Singleton object with `fromBattles()`, `fromPokemon()`, `fromPlayers()` — wraps domain models into `ContentListItem` variants. Used by all 3 platform ViewModels. Critical for iOS since constructing sealed class variants from Swift via this mapper is cleanest.

## Android
- `FavoritesRepository` interface + `FavoritesRepositoryImpl` (delegates to shared, injected via Hilt)
- `ContentListViewModel` handles all favorites modes: `Favorites(Battles)` loads battle IDs, `Favorites(Pokemon)` observes `favoritePokemonIds` flow and resolves Pokemon from the local catalog (no API calls — uses `pokemonCatalogState` combined with favorite IDs, shows loading if catalog is still loading), `Favorites(Players)` observes `favoritePlayerNames` flow and loads Player details via `BattleRepository.getPlayersByNames()`. Auto-refreshes when favorites change.
- `FavoritesPage`: Three sub-tabs (Battles, Pokemon, Players), all rendered via `ContentListPage(mode = ContentListMode.Favorites(contentType))`
- Heart buttons: `BattleDetailSheetWrapper` toggles battle favorites; `ContentListPage` toolbar toggles Pokemon favorites in Pokemon mode

## iOS
- `FavoritesStore` (`@MainActor ObservableObject`): Thin wrapper around shared `FavoritesRepository`, exposes `@Published` sets for SwiftUI reactivity, syncs state after each mutation
- `ContentListViewModel` handles all favorites modes (same pattern as Android). Takes optional `favoritesStore: FavoritesStore` param for reading favorite IDs.
- `FavoritesView`: Three sub-tabs (Battles, Pokemon, Players), all rendered via `ContentListView(mode: .favorites(contentType: ...))`. Auto-refresh via `.onChange(of: favoritesStore.favoritePokemonIds/favoritePlayerNames)`.
- Heart buttons: `BattleDetailSheet` toggles battle favorites; `ContentListView` toolbar toggles Pokemon favorites in Pokemon mode

## Web
- Uses shared `FavoritesRepository` directly via `DependencyContainer` singleton (same instance across all screens)
- `ContentListViewModel` handles all favorites modes (same pattern as Android but with try/catch instead of Result<T>)
- `FavoritesPage`: Three sub-tabs (Battles, Pokemon, Players), all rendered via `ContentListPage(mode = ContentListMode.Favorites(contentType))`
- Heart buttons: `BattleDetailPanel` toggles battle favorites; `ContentListPage` toolbar toggles Pokemon favorites in Pokemon mode
- Persistence via `FavoritesStorage` using browser `localStorage` (survives page refreshes)

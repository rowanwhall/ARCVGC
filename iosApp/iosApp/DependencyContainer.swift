import Foundation
import Shared

@MainActor
final class DependencyContainer: ObservableObject {
    let apiService: ApiService
    let battleRepository: BattleRepository
    let favoritesStore: FavoritesStore
    let catalogStore: CatalogStore
    let settingsStore: SettingsStore
    let appConfigStore: AppConfigStore

    init() {
        self.apiService = ApiService()
        self.battleRepository = BattleRepository(apiService: apiService)
        self.favoritesStore = FavoritesStore()
        let cacheStorage = CatalogCacheStorage()
        self.catalogStore = CatalogStore(apiService: apiService, cacheStorage: cacheStorage)
        self.settingsStore = SettingsStore(favoritesRepository: favoritesStore.repo)
        self.appConfigStore = AppConfigStore(apiService: apiService, cacheStorage: cacheStorage)
    }
}

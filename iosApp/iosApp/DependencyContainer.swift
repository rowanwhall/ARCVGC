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
        self.appConfigStore = AppConfigStore(apiService: apiService, cacheStorage: cacheStorage)
        let configStore = self.appConfigStore
        self.catalogStore = CatalogStore(
            apiService: apiService,
            cacheStorage: cacheStorage,
            defaultFormatIdProvider: { configStore.defaultFormatId }
        )
        self.settingsStore = SettingsStore(favoritesRepository: favoritesStore.repo)
    }
}

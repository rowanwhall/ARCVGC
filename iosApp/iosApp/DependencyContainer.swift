import Foundation
import Shared

enum ResolvedDeepLink: Equatable {
    case battle(id: Int32)
    case pokemon(target: PokemonNavTarget)
    case player(target: PlayerNavTarget)
}

@MainActor
final class DependencyContainer: ObservableObject {
    let apiService: ApiService
    let battleRepository: BattleRepository
    let deepLinkResolver: DeepLinkResolver
    let favoritesStore: FavoritesStore
    let catalogStore: CatalogStore
    let settingsStore: SettingsStore
    let appConfigStore: AppConfigStore

    @Published var pendingDeepLink: ResolvedDeepLink?

    init() {
        self.apiService = ApiService()
        self.battleRepository = BattleRepository(apiService: apiService)
        self.deepLinkResolver = DeepLinkResolver(apiService: apiService)
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

    func handleDeepLink(target: DeepLinkTarget) {
        Task {
            let resolved = try? await deepLinkResolver.resolve(target: target)
            guard let resolved else { return }
            switch onEnum(of: resolved) {
            case .battle(let battle):
                pendingDeepLink = .battle(id: battle.id)
            case .pokemon(let pokemon):
                let item = pokemon.item
                pendingDeepLink = .pokemon(target: PokemonNavTarget(
                    id: item.id,
                    name: item.name,
                    imageUrl: item.imageUrl,
                    typeImageUrl1: item.types.first?.imageUrl,
                    typeImageUrl2: item.types.count > 1 ? item.types[1].imageUrl : nil
                ))
            case .player(let player):
                pendingDeepLink = .player(target: PlayerNavTarget(
                    id: player.item.id,
                    name: player.item.name
                ))
            }
        }
    }
}

import Foundation
import Shared

@MainActor
final class FavoritesStore: ObservableObject {
    @Published private(set) var favoritePokemonIds: Set<Int32> = []
    @Published private(set) var favoriteBattleIds: Set<Int32> = []
    @Published private(set) var favoritePlayerNames: Set<String> = []

    let repo: FavoritesRepository

    init() {
        self.repo = FavoritesRepository(storage: FavoritesStorage())
        syncState()
    }

    func togglePokemonFavorite(id: Int32) {
        repo.togglePokemonFavorite(id: id)
        syncState()
    }

    func toggleBattleFavorite(id: Int32) {
        repo.toggleBattleFavorite(id: id)
        syncState()
    }

    func togglePlayerFavorite(name: String) {
        repo.togglePlayerFavorite(name: name)
        syncState()
    }

    func isPokemonFavorited(id: Int32) -> Bool {
        repo.isPokemonFavorited(id: id)
    }

    func isBattleFavorited(id: Int32) -> Bool {
        repo.isBattleFavorited(id: id)
    }

    func isPlayerFavorited(name: String) -> Bool {
        repo.isPlayerFavorited(name: name)
    }

    func syncState() {
        favoritePokemonIds = Set(repo.currentPokemonIds().map { $0.int32Value })
        favoriteBattleIds = Set(repo.currentBattleIds().map { $0.int32Value })
        favoritePlayerNames = Set(repo.currentPlayerNames().map { $0 as String })
    }
}

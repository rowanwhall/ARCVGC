import Foundation
import os
import Shared

@MainActor
final class BattleDetailViewModel: ObservableObject {
    @Published private(set) var state = BattleDetailState()

    private static let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.arcvgc.app", category: "BattleDetailViewModel")

    private let repository: BattleRepository
    private let battleId: Int32

    init(repository: BattleRepository, battleId: Int32) {
        self.repository = repository
        self.battleId = battleId
    }

    func loadBattleDetail() {
        guard !state.isLoading else { return }

        state.isLoading = true
        state.error = nil

        Task {
            do {
                let detail = try await repository.getMatchDetail(id: battleId)
                state.battleDetail = detail
                state.isLoading = false
            } catch {
                Self.logger.error("Failed to load battle detail (id=\(self.battleId)): \(error.localizedDescription)")
                state.error = error.localizedDescription
                state.isLoading = false
            }
        }
    }
}

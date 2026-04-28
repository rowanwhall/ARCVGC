import Foundation
import os
import Shared

@MainActor
final class SubmitReplayViewModel: ObservableObject {
    @Published private(set) var isSubmitting: Bool = false
    @Published var error: String? = nil

    private static let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.arcvgc.app", category: "SubmitReplayViewModel")

    private let repository: BattleRepository

    init(repository: BattleRepository) {
        self.repository = repository
    }

    func submit(replayUrl: String, onSuccess: @escaping () -> Void) {
        isSubmitting = true
        error = nil

        Task {
            let detail = await repository.submitReplayOrNull(replayUrl: replayUrl)
            if detail != nil {
                isSubmitting = false
                onSuccess()
            } else {
                Self.logger.error("Failed to submit replay")
                error = "Submission failed"
                isSubmitting = false
            }
        }
    }

    func reset() {
        isSubmitting = false
        error = nil
    }
}

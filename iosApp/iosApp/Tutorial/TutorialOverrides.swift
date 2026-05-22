import Foundation
import Shared

/// Per-platform string overrides for the tutorial. Empty by default — populate when
/// iOS needs different copy than the shared `TutorialContentProvider` provides.
enum TutorialOverrides {
    static let entries: [String: TutorialString] = [:]

    static func resolve(id: String) -> TutorialString? {
        entries[id] ?? TutorialContentProvider.shared.get(id: id)
    }
}

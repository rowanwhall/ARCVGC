import SwiftUI
import Shared

@MainActor
final class SettingsStore: ObservableObject {
    @Published private(set) var showWinnerHighlight: Bool = true
    @Published private(set) var selectedThemeId: Int32 = 0
    @Published private(set) var darkModeId: Int32 = 0
    @Published private(set) var themeColor: Color = SettingsStore.colorForThemeId(0)
    @Published private(set) var preferredFormatId: Int32 = 0
    @Published private(set) var settingSections: [SettingsSection] = []

    /// Snapshot of the user's effective preferred format (falls back to config default, then 1).
    var effectivePreferredFormatId: Int32 {
        Int32(repo.getEffectivePreferredFormatId())
    }

    var colorSchemeOverride: ColorScheme? {
        switch darkModeId {
        case 1: return .light
        case 2: return .dark
        default: return nil
        }
    }

    let repo: SettingsRepository

    init(favoritesRepository: Shared.FavoritesRepository? = nil, appConfigRepository: Shared.AppConfigRepository? = nil) {
        self.repo = SettingsRepository(
            storage: SettingsStorage(),
            cacheStorage: CatalogCacheStorage(),
            favoritesRepository: favoritesRepository,
            appConfigRepository: appConfigRepository
        )
        syncState()

        // SettingsStore lives for the process lifetime via DependencyContainer, so
        // we don't cancel this task or weakly capture self — matches AppConfigStore.
        Task {
            for await _ in repo.settingSections {
                self.syncState()
            }
        }
    }

    func setBooleanSetting(key: String, value: Bool) {
        repo.setBooleanSetting(key: key, value: value)
        syncState()
    }

    func setIntSetting(key: String, value: Int32) {
        repo.setIntSetting(key: key, value: value)
        syncState()
    }

    func performAction(key: String) {
        repo.performAction(key: key)
        syncState()
    }

    static func colorForThemeId(_ id: Int32) -> Color {
        switch id {
        case 1: return Color(red: 0.102, green: 0.451, blue: 0.910) // Blue
        case 2: return Color(red: 0.902, green: 0.655, blue: 0.0)   // Yellow
        case 3: return Color(red: 0.482, green: 0.122, blue: 0.635) // Purple
        default: return Color(red: 0.863, green: 0.184, blue: 0.208) // Red
        }
    }

    private func syncState() {
        showWinnerHighlight = repo.isShowWinnerHighlightEnabled()
        selectedThemeId = repo.getSelectedThemeId()
        darkModeId = repo.getDarkModeId()
        preferredFormatId = Int32(repo.getPreferredFormatId())
        themeColor = SettingsStore.colorForThemeId(selectedThemeId)
        settingSections = repo.getSettingSections()
    }
}

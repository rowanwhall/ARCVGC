import SwiftUI
import Shared

@MainActor
final class SettingsStore: ObservableObject {
    @Published private(set) var showWinnerHighlight: Bool = true
    @Published private(set) var selectedThemeId: Int32 = 0
    @Published private(set) var darkModeId: Int32 = 0
    @Published private(set) var themeColor: Color = SettingsStore.colorForThemeId(0)
    @Published private(set) var settingItems: [SettingItem] = []

    var colorSchemeOverride: ColorScheme? {
        switch darkModeId {
        case 1: return .light
        case 2: return .dark
        default: return nil
        }
    }

    private let repo: SettingsRepository

    init(favoritesRepository: Shared.FavoritesRepository? = nil) {
        self.repo = SettingsRepository(storage: SettingsStorage(), cacheStorage: CatalogCacheStorage(), favoritesRepository: favoritesRepository)
        syncState()
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
        themeColor = SettingsStore.colorForThemeId(selectedThemeId)
        settingItems = repo.getSettingItems()
    }
}

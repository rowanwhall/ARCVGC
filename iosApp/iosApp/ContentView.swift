import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var container: DependencyContainer

    var body: some View {
        ThemedContentView(settingsStore: container.settingsStore, appConfigStore: container.appConfigStore, container: container)
    }
}

private struct ThemedContentView: View {
    @ObservedObject var settingsStore: SettingsStore
    @ObservedObject var appConfigStore: AppConfigStore
    let container: DependencyContainer

    private var requiresUpgrade: Bool {
        guard let config = appConfigStore.config else { return false }
        let currentVersion = Int32(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "0") ?? 0
        return config.minIosVersion > currentVersion
    }

    var body: some View {
        TabView {
            NavigationStack {
                ContentListView(repository: container.battleRepository, favoritesStore: container.favoritesStore, settingsStore: container.settingsStore, appConfigStore: container.appConfigStore)
            }
                .tabItem {
                    Label("Top", systemImage: "star.fill")
                }

            SearchView(catalogStore: container.catalogStore, appConfigStore: container.appConfigStore)
                .tabItem {
                    Label("Search", systemImage: "magnifyingglass")
                }

            FavoritesView()
                .tabItem {
                    Label("Favorites", systemImage: "heart.fill")
                }

            NavigationStack {
                SettingsView(settingsStore: container.settingsStore, catalogStore: container.catalogStore, favoritesStore: container.favoritesStore)
            }
                .tabItem {
                    Label("Settings", systemImage: "gearshape.fill")
                }
        }
        .tint(settingsStore.themeColor)
        .environment(\.themeColor, settingsStore.themeColor)
        .preferredColorScheme(settingsStore.colorSchemeOverride)
        .onChange(of: appConfigStore.catalogVersionChanged) { _, changed in
            if changed {
                container.catalogStore.reload()
            }
        }
        .fullScreenCover(isPresented: Binding(
            get: { requiresUpgrade },
            set: { _ in }
        )) {
            ForceUpgradeView()
                .interactiveDismissDisabled(true)
        }
    }
}

#Preview {
    ContentView()
        .environmentObject(DependencyContainer())
}

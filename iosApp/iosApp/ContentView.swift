import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var container: DependencyContainer

    var body: some View {
        ThemedContentView(settingsStore: container.settingsStore, container: container)
    }
}

private struct ThemedContentView: View {
    @ObservedObject var settingsStore: SettingsStore
    let container: DependencyContainer

    var body: some View {
        TabView {
            NavigationStack {
                ContentListView(repository: container.battleRepository, favoritesStore: container.favoritesStore, settingsStore: container.settingsStore)
            }
                .tabItem {
                    Label("Top", systemImage: "star.fill")
                }

            SearchView(catalogStore: container.catalogStore)
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
    }
}

#Preview {
    ContentView()
        .environmentObject(DependencyContainer())
}

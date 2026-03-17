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
    @ObservedObject var container: DependencyContainer

    @State private var selectedTab = 0
    @State private var deepLinkPokemonTarget: PokemonNavTarget?
    @State private var deepLinkPlayerTarget: PlayerNavTarget?
    @State private var deepLinkBattleId: Int32?
    @State private var deepLinkFavoritesSubTab: Int?

    private var requiresUpgrade: Bool {
        guard let config = appConfigStore.config else { return false }
        let currentVersion = Int32(Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "0") ?? 0
        return config.minIosVersion > currentVersion
    }

    var body: some View {
        TabView(selection: $selectedTab) {
            NavigationStack {
                ContentListView(
                    repository: container.battleRepository,
                    favoritesStore: container.favoritesStore,
                    settingsStore: container.settingsStore,
                    appConfigStore: container.appConfigStore,
                    initialBattleId: deepLinkBattleId
                )
                .navigationDestination(isPresented: Binding(
                    get: { deepLinkPokemonTarget != nil },
                    set: { if !$0 { deepLinkPokemonTarget = nil } }
                )) {
                    if let target = deepLinkPokemonTarget {
                        ContentListView(
                            repository: container.battleRepository,
                            mode: .pokemon(id: target.id, name: target.name, imageUrl: target.imageUrl, typeImageUrl1: target.typeImageUrl1, typeImageUrl2: target.typeImageUrl2, formatId: target.formatId),
                            favoritesStore: container.favoritesStore,
                            settingsStore: container.settingsStore,
                            appConfigStore: container.appConfigStore
                        )
                    }
                }
                .navigationDestination(isPresented: Binding(
                    get: { deepLinkPlayerTarget != nil },
                    set: { if !$0 { deepLinkPlayerTarget = nil } }
                )) {
                    if let target = deepLinkPlayerTarget {
                        ContentListView(
                            repository: container.battleRepository,
                            mode: .player(id: target.id, name: target.name, formatId: target.formatId),
                            favoritesStore: container.favoritesStore,
                            settingsStore: container.settingsStore,
                            appConfigStore: container.appConfigStore
                        )
                    }
                }
            }
            .tabItem {
                Label("Top", systemImage: "star.fill")
            }
            .tag(0)

            SearchView(catalogStore: container.catalogStore, appConfigStore: container.appConfigStore)
                .tabItem {
                    Label("Search", systemImage: "magnifyingglass")
                }
                .tag(1)

            FavoritesView(initialSubTab: deepLinkFavoritesSubTab)
                .tabItem {
                    Label("Favorites", systemImage: "heart.fill")
                }
                .tag(2)

            NavigationStack {
                SettingsView(settingsStore: container.settingsStore, catalogStore: container.catalogStore, favoritesStore: container.favoritesStore)
            }
                .tabItem {
                    Label("Settings", systemImage: "gearshape.fill")
                }
                .tag(3)
        }
        .tint(settingsStore.themeColor)
        .environment(\.themeColor, settingsStore.themeColor)
        .preferredColorScheme(settingsStore.colorSchemeOverride)
        .onChange(of: appConfigStore.catalogVersionChanged) { _, changed in
            if changed {
                container.catalogStore.reload()
            }
        }
        .onChange(of: container.pendingDeepLink) { _, deepLink in
            guard let deepLink else { return }
            switch deepLink {
            case .battle(let id):
                selectedTab = 0
                deepLinkBattleId = id
            case .pokemon(let target):
                selectedTab = 0
                deepLinkPokemonTarget = target
            case .player(let target):
                selectedTab = 0
                deepLinkPlayerTarget = target
            case .favorites(let subTab):
                selectedTab = 2
                deepLinkFavoritesSubTab = subTab
            case .searchTab:
                selectedTab = 1
            case .settingsTab:
                selectedTab = 3
            }
            container.pendingDeepLink = nil
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

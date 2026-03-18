import SwiftUI
import Shared

struct ContentView: View {
    @EnvironmentObject var container: DependencyContainer

    var body: some View {
        ThemedContentView(settingsStore: container.settingsStore, appConfigStore: container.appConfigStore, container: container)
    }
}

private enum DeepLinkNavTarget: Hashable {
    case pokemon(PokemonNavTarget)
    case player(PlayerNavTarget)
}

private struct ThemedContentView: View {
    @ObservedObject var settingsStore: SettingsStore
    @ObservedObject var appConfigStore: AppConfigStore
    @ObservedObject var container: DependencyContainer

    @State private var selectedTab = 0
    @State private var deepLinkNavTarget: DeepLinkNavTarget?
    @State private var deepLinkBattleId: Int32?
    @State private var deepLinkFavoritesSubTab: Int?
    @State private var deepLinkSearchParams: SearchParams?

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
                .id(deepLinkBattleId ?? 0)
                .navigationDestination(isPresented: Binding(
                    get: { deepLinkNavTarget != nil },
                    set: { if !$0 { deepLinkNavTarget = nil } }
                )) {
                    Group {
                        switch deepLinkNavTarget {
                        case .pokemon(let target):
                            ContentListView(
                                repository: container.battleRepository,
                                mode: .pokemon(id: target.id, name: target.name, imageUrl: target.imageUrl, typeImageUrl1: target.typeImageUrl1, typeImageUrl2: target.typeImageUrl2, formatId: target.formatId),
                                favoritesStore: container.favoritesStore,
                                settingsStore: container.settingsStore,
                                appConfigStore: container.appConfigStore,
                                initialBattleId: deepLinkBattleId
                            )
                        case .player(let target):
                            ContentListView(
                                repository: container.battleRepository,
                                mode: .player(id: target.id, name: target.name, formatId: target.formatId),
                                favoritesStore: container.favoritesStore,
                                settingsStore: container.settingsStore,
                                appConfigStore: container.appConfigStore,
                                initialBattleId: deepLinkBattleId
                            )
                        case nil:
                            EmptyView()
                        }
                    }
                    .id(deepLinkNavTarget)
                }
            }
            .tabItem {
                Label("Top", systemImage: "star.fill")
            }
            .tag(0)

            SearchView(catalogStore: container.catalogStore, appConfigStore: container.appConfigStore, initialSearchParams: deepLinkSearchParams)
                .id(deepLinkSearchParams?.hashValue ?? 0)
                .tabItem {
                    Label("Search", systemImage: "magnifyingglass")
                }
                .tag(1)

            FavoritesView(initialSubTab: deepLinkFavoritesSubTab, initialBattleId: deepLinkBattleId)
                .id(deepLinkFavoritesSubTab ?? -1)
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
            let battleId = container.pendingBattleId
            container.pendingDeepLink = nil
            container.pendingBattleId = nil

            deepLinkFavoritesSubTab = nil
            deepLinkSearchParams = nil
            deepLinkBattleId = battleId

            switch deepLink {
            case .battle(let id):
                selectedTab = 0
                deepLinkNavTarget = nil
                deepLinkBattleId = id
            case .pokemon(let target):
                selectedTab = 0
                deepLinkNavTarget = .pokemon(target)
            case .player(let target):
                selectedTab = 0
                deepLinkNavTarget = .player(target)
            case .favorites(let subTab):
                selectedTab = 2
                deepLinkNavTarget = nil
                deepLinkFavoritesSubTab = subTab
            case .search(let params):
                selectedTab = 1
                deepLinkNavTarget = nil
                deepLinkSearchParams = params
            case .searchTab:
                selectedTab = 1
                deepLinkNavTarget = nil
            case .settingsTab:
                selectedTab = 3
                deepLinkNavTarget = nil
            }

            // Clear battleId after SwiftUI has created views with it,
            // preventing re-trigger on sheet dismissal
            DispatchQueue.main.async {
                deepLinkBattleId = nil
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

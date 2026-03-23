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
    @State private var deepLinkBattleDetailId: Int32?
    @State private var deepLinkReplayUrl: String?
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
                    appConfigStore: container.appConfigStore
                )
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
                                appConfigStore: container.appConfigStore
                            )
                        case .player(let target):
                            ContentListView(
                                repository: container.battleRepository,
                                mode: .player(id: target.id, name: target.name, formatId: target.formatId),
                                favoritesStore: container.favoritesStore,
                                settingsStore: container.settingsStore,
                                appConfigStore: container.appConfigStore
                            )
                        case nil:
                            EmptyView()
                        }
                    }
                    .id(deepLinkNavTarget)
                }
                .navigationDestination(isPresented: Binding(
                    get: { deepLinkBattleDetailId != nil },
                    set: { if !$0 { deepLinkBattleDetailId = nil } }
                )) {
                    if let battleId = deepLinkBattleDetailId {
                        BattleDetailNavWrapper(
                            repository: container.battleRepository,
                            battleId: battleId,
                            favoritesStore: container.favoritesStore,
                            settingsStore: container.settingsStore,
                            appConfigStore: container.appConfigStore,
                            onViewReplay: { url in
                                deepLinkReplayUrl = url
                            }
                        )
                    }
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

            FavoritesView(initialSubTab: deepLinkFavoritesSubTab)
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
            deepLinkNavTarget = nil

            // If any deep link has a battleId, navigate directly to battle detail
            if let battleId {
                selectedTab = 0
                deepLinkBattleDetailId = battleId
                return
            }

            switch deepLink {
            case .battle(let id):
                selectedTab = 0
                deepLinkBattleDetailId = id
            case .pokemon(let target):
                selectedTab = 0
                deepLinkNavTarget = .pokemon(target)
            case .player(let target):
                selectedTab = 0
                deepLinkNavTarget = .player(target)
            case .favorites(let subTab):
                selectedTab = 2
                deepLinkFavoritesSubTab = subTab
            case .search(let params):
                selectedTab = 1
                deepLinkSearchParams = params
            case .searchTab:
                selectedTab = 1
            case .settingsTab:
                selectedTab = 3
            case .topPokemon:
                selectedTab = 0
            }
        }
        .fullScreenCover(isPresented: Binding(
            get: { deepLinkReplayUrl != nil },
            set: { if !$0 { deepLinkReplayUrl = nil } }
        )) {
            if let url = deepLinkReplayUrl {
                ReplayOverlay(replayUrl: url, onDismiss: { deepLinkReplayUrl = nil })
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

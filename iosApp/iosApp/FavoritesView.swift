import SwiftUI
import Shared

struct FavoritesView: View {
    @EnvironmentObject var container: DependencyContainer
    @State private var selectedSubTab: Int
    private let initialSubTab: Int?
    private let initialBattleId: Int32?

    init(initialSubTab: Int? = nil, initialBattleId: Int32? = nil) {
        _selectedSubTab = State(initialValue: initialSubTab ?? 0)
        self.initialSubTab = initialSubTab
        self.initialBattleId = initialBattleId
    }

    private var battleIdForTab: Int32? {
        guard selectedSubTab == (initialSubTab ?? 0) else { return nil }
        return initialBattleId
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker("", selection: $selectedSubTab) {
                    Text("Battles").tag(0)
                    Text("Pokémon").tag(1)
                    Text("Players").tag(2)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)

                switch selectedSubTab {
                case 0:
                    ContentListView(
                        repository: container.battleRepository,
                        mode: .favorites(contentType: .battles),
                        favoritesStore: container.favoritesStore,
                        settingsStore: container.settingsStore,
                        initialBattleId: battleIdForTab
                    )
                case 1:
                    ContentListView(
                        repository: container.battleRepository,
                        mode: .favorites(contentType: .pokemon),
                        favoritesStore: container.favoritesStore,
                        settingsStore: container.settingsStore,
                        pokemonCatalogItems: container.catalogStore.pokemonItems,
                        appConfigStore: container.appConfigStore,
                        initialBattleId: battleIdForTab
                    )
                default:
                    ContentListView(
                        repository: container.battleRepository,
                        mode: .favorites(contentType: .players),
                        favoritesStore: container.favoritesStore,
                        settingsStore: container.settingsStore,
                        appConfigStore: container.appConfigStore,
                        initialBattleId: battleIdForTab
                    )
                }
            }
            .background(Color(.systemGroupedBackground))
        }
    }
}

#Preview {
    let container = DependencyContainer()
    return FavoritesView()
        .environmentObject(container)
}

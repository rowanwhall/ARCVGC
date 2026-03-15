import SwiftUI
import Shared

struct FavoritesView: View {
    @EnvironmentObject var container: DependencyContainer
    @State private var selectedSubTab = 0

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
                        settingsStore: container.settingsStore
                    )
                case 1:
                    ContentListView(
                        repository: container.battleRepository,
                        mode: .favorites(contentType: .pokemon),
                        favoritesStore: container.favoritesStore,
                        settingsStore: container.settingsStore,
                        appConfigStore: container.appConfigStore
                    )
                default:
                    ContentListView(
                        repository: container.battleRepository,
                        mode: .favorites(contentType: .players),
                        favoritesStore: container.favoritesStore,
                        settingsStore: container.settingsStore,
                        appConfigStore: container.appConfigStore
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

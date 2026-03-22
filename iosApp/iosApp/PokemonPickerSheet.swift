import SwiftUI
import Shared

struct PokemonPickerSheet: View {
    let items: [PokemonPickerUiModel]
    let isLoading: Bool
    let error: String?
    var excludeIds: Set<Int32> = []
    let onSelect: (PokemonPickerUiModel) -> Void

    @State private var query = ""
    @FocusState private var isSearchFocused: Bool
    @Environment(\.dismiss) private var dismiss

    private var filtered: [PokemonPickerUiModel] {
        let available = items.filter { !excludeIds.contains($0.id) }
        if query.isEmpty { return available }
        return available.filter { $0.name.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = error {
                    Text(error)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List(filtered, id: \.id) { pokemon in
                        Button {
                            onSelect(pokemon)
                            dismiss()
                        } label: {
                            PokemonPickerRow(pokemon: pokemon)
                        }
                        .listRowInsets(EdgeInsets(top: 2, leading: 16, bottom: 2, trailing: 16))
                    }
                    .listStyle(.plain)
                }

                TextField("", text: $query, prompt: Text("Search Pokémon").foregroundColor(Color(.secondaryLabel)))
                    .focused($isSearchFocused)
                    .outlinedTextFieldStyle(isFocused: isSearchFocused)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
            }
            .navigationTitle("Select Pokémon")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

private struct PokemonPickerRow: View {
    let pokemon: PokemonPickerUiModel

    var body: some View {
        SimplePokemonRow(
            imageUrl: pokemon.imageUrl,
            name: pokemon.name,
            types: pokemon.types.map { (name: $0.name, imageUrl: $0.imageUrl) },
            circleSize: 46,
            spriteSize: 64
        )
    }
}

#Preview {
    PokemonPickerSheet(
        items: [
            PokemonPickerUiModel(
                id: 149,
                name: "Dragonite",
                imageUrl: nil,
                types: [TypeUiModel(name: "Dragon", imageUrl: nil), TypeUiModel(name: "Flying", imageUrl: nil)]
            ),
            PokemonPickerUiModel(
                id: 6,
                name: "Charizard",
                imageUrl: nil,
                types: [TypeUiModel(name: "Fire", imageUrl: nil), TypeUiModel(name: "Flying", imageUrl: nil)]
            )
        ],
        isLoading: false,
        error: nil,
        onSelect: { _ in }
    )
}

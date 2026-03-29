import SwiftUI
import Shared

struct AbilityPickerSheet: View {
    let items: [AbilityUiModel]
    let isLoading: Bool
    let error: String?
    let onSelect: (AbilityUiModel?) -> Void

    @State private var query = ""
    @FocusState private var isSearchFocused: Bool
    @Environment(\.dismiss) private var dismiss

    private var filtered: [AbilityUiModel] {
        if query.isEmpty { return items }
        return items.filter { $0.name.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if isLoading {
                    LoadingIndicator()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = error {
                    Text(error)
                        .foregroundColor(.red)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        Button {
                            onSelect(nil)
                            dismiss()
                        } label: {
                            Text("None")
                                .foregroundColor(Color(.secondaryLabel))
                        }
                        ForEach(Array(filtered.enumerated()), id: \.element.name) { _, ability in
                            Button {
                                onSelect(ability)
                                dismiss()
                            } label: {
                                Text(ability.name)
                                    .foregroundColor(.primary)
                            }
                        }
                    }
                    .listStyle(.plain)
                }

                TextField("", text: $query, prompt: Text("Search Abilities").foregroundColor(Color(.secondaryLabel)))
                    .focused($isSearchFocused)
                    .outlinedTextFieldStyle(isFocused: isSearchFocused)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
            }
            .navigationTitle("Select Ability")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

#Preview {
    AbilityPickerSheet(
        items: [
            AbilityUiModel(id: 1, name: "Intimidate"),
            AbilityUiModel(id: 2, name: "Inner Focus"),
            AbilityUiModel(id: 3, name: "Multiscale")
        ],
        isLoading: false,
        error: nil,
        onSelect: { _ in }
    )
}

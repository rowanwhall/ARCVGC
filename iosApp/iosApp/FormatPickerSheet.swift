import SwiftUI
import Shared

struct FormatPickerSheet: View {
    let items: [FormatUiModel]
    let isLoading: Bool
    let error: String?
    let onSelect: (FormatUiModel) -> Void

    @State private var query = ""
    @FocusState private var isSearchFocused: Bool
    @Environment(\.dismiss) private var dismiss

    private var filtered: [FormatUiModel] {
        if query.isEmpty { return items }
        return items.filter { $0.displayName.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if isLoading {
                    LoadingIndicator()
                        .padding(.vertical, 24)
                } else if let error = error {
                    Text(error)
                        .foregroundColor(.red)
                        .padding(.vertical, 24)
                } else {
                    ForEach(filtered, id: \.id) { format in
                        Button {
                            onSelect(format)
                            dismiss()
                        } label: {
                            Text(format.displayName)
                                .foregroundColor(.primary)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.vertical, 12)
                                .padding(.horizontal, 16)
                        }
                        Divider()
                    }
                }

                TextField("", text: $query, prompt: Text("Search Formats").foregroundColor(Color(.secondaryLabel)))
                    .focused($isSearchFocused)
                    .outlinedTextFieldStyle(isFocused: isSearchFocused)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
            }
            .navigationTitle("Select Format")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

#Preview {
    FormatPickerSheet(
        items: [
            FormatUiModel(id: 1, displayName: "VGC 2026 Reg H", isHistoric: false, isOpenTeamsheet: true, isOfficial: true, hasSeries: true),
            FormatUiModel(id: 2, displayName: "OU", isHistoric: false, isOpenTeamsheet: false, isOfficial: false, hasSeries: false),
            FormatUiModel(id: 3, displayName: "Ubers", isHistoric: false, isOpenTeamsheet: false, isOfficial: false, hasSeries: false)
        ],
        isLoading: false,
        error: nil,
        onSelect: { _ in }
    )
}

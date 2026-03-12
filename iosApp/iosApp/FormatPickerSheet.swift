import SwiftUI
import Shared

struct FormatPickerSheet: View {
    let items: [FormatUiModel]
    let isLoading: Bool
    let error: String?
    let onSelect: (FormatUiModel) -> Void

    @State private var query = ""
    @Environment(\.dismiss) private var dismiss

    private var filtered: [FormatUiModel] {
        if query.isEmpty { return items }
        return items.filter { $0.displayName.localizedCaseInsensitiveContains(query) }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                if isLoading {
                    ProgressView()
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

                TextField("Search Formats", text: $query)
                    .textFieldStyle(.roundedBorder)
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
            FormatUiModel(id: 1, displayName: "VGC 2026 Reg H"),
            FormatUiModel(id: 2, displayName: "OU"),
            FormatUiModel(id: 3, displayName: "Ubers")
        ],
        isLoading: false,
        error: nil,
        onSelect: { _ in }
    )
}

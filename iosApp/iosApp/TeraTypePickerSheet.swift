import SwiftUI
import Shared

struct TeraTypePickerSheet: View {
    let items: [TeraTypeUiModel]
    let isLoading: Bool
    let error: String?
    let onSelect: (TeraTypeUiModel?) -> Void

    @State private var query = ""
    @FocusState private var isSearchFocused: Bool
    @Environment(\.dismiss) private var dismiss

    private var filtered: [TeraTypeUiModel] {
        if query.isEmpty { return items }
        return items.filter { $0.name.localizedCaseInsensitiveContains(query) }
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
                    List {
                        Button {
                            onSelect(nil)
                            dismiss()
                        } label: {
                            Text("None")
                                .foregroundColor(Color(.secondaryLabel))
                        }
                        ForEach(Array(filtered.enumerated()), id: \.element.name) { _, teraType in
                            Button {
                                onSelect(teraType)
                                dismiss()
                            } label: {
                                TeraTypePickerRow(teraType: teraType)
                            }
                        }
                    }
                    .listStyle(.plain)
                }

                TextField("", text: $query, prompt: Text("Search Tera Types").foregroundColor(Color(.secondaryLabel)))
                    .focused($isSearchFocused)
                    .outlinedTextFieldStyle(isFocused: isSearchFocused)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
            }
            .navigationTitle("Select Tera Type")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

private struct TeraTypePickerRow: View {
    let teraType: TeraTypeUiModel

    var body: some View {
        HStack(spacing: 12) {
            PreviewAsyncImage(url: teraType.imageUrl, previewAsset: "PreviewTera")
                .frame(width: 32, height: 32)

            Text(teraType.name)
                .foregroundColor(.primary)
        }
        .contentShape(Rectangle())
    }
}

#Preview {
    TeraTypePickerSheet(
        items: [
            TeraTypeUiModel(id: 1, name: "Normal", imageUrl: nil),
            TeraTypeUiModel(id: 2, name: "Fire", imageUrl: nil),
            TeraTypeUiModel(id: 3, name: "Water", imageUrl: nil)
        ],
        isLoading: false,
        error: nil,
        onSelect: { _ in }
    )
}

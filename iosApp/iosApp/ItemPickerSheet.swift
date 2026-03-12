import SwiftUI
import Shared

struct ItemPickerSheet: View {
    let items: [ItemUiModel]
    let isLoading: Bool
    let error: String?
    let onSelect: (ItemUiModel) -> Void

    @State private var query = ""
    @Environment(\.dismiss) private var dismiss

    private var filtered: [ItemUiModel] {
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
                    List(Array(filtered.enumerated()), id: \.element.name) { _, item in
                        Button {
                            onSelect(item)
                            dismiss()
                        } label: {
                            ItemPickerRow(item: item)
                        }
                    }
                    .listStyle(.plain)
                }

                TextField("Search Items", text: $query)
                    .textFieldStyle(.roundedBorder)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
            }
            .navigationTitle("Select Item")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
        }
    }
}

private struct ItemPickerRow: View {
    let item: ItemUiModel

    var body: some View {
        HStack(spacing: 12) {
            PreviewAsyncImage(url: item.imageUrl, previewAsset: "PreviewItem")
                .frame(width: 32, height: 32)

            Text(item.name)
                .foregroundColor(.primary)
        }
        .contentShape(Rectangle())
    }
}

#Preview {
    ItemPickerSheet(
        items: [
            ItemUiModel(id: 1, name: "Choice Band", imageUrl: nil),
            ItemUiModel(id: 2, name: "Choice Specs", imageUrl: nil),
            ItemUiModel(id: 3, name: "Life Orb", imageUrl: nil)
        ],
        isLoading: false,
        error: nil,
        onSelect: { _ in }
    )
}

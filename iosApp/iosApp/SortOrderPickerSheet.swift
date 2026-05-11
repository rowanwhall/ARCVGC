import Foundation
import Shared
import SwiftUI

struct SortOrderPickerSheet: View {
    let selectedOrderBy: OrderBy
    let onSelect: (OrderBy) -> Void

    @Environment(\.dismiss) private var dismiss

    private let options: [(value: OrderBy, label: String)] = [
        (OrderBy.rating, "Rating"),
        (OrderBy.time, "Time")
    ]

    var body: some View {
        NavigationStack {
            List(options, id: \.value) { option in
                Button {
                    onSelect(option.value)
                    dismiss()
                } label: {
                    HStack {
                        Text(option.label)
                            .foregroundColor(.primary)
                        Spacer()
                        if selectedOrderBy == option.value {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("Sort Order")
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
    SortOrderPickerSheet(
        selectedOrderBy: OrderBy.rating,
        onSelect: { _ in }
    )
}

import SwiftUI

struct SortOrderPickerSheet: View {
    let selectedOrderBy: String
    let onSelect: (String) -> Void

    @Environment(\.dismiss) private var dismiss

    private let options: [(value: String, label: String)] = [
        ("rating", "Rating"),
        ("time", "Time")
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
        selectedOrderBy: "rating",
        onSelect: { _ in }
    )
}

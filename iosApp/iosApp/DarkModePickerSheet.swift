import SwiftUI

struct DarkModePickerSheet: View {
    let selectedModeId: Int32
    let onSelect: (Int32) -> Void

    @Environment(\.dismiss) private var dismiss

    private let modes: [(id: Int32, name: String)] = [
        (0, "System"),
        (1, "Light"),
        (2, "Dark")
    ]

    var body: some View {
        NavigationStack {
            List(modes, id: \.id) { mode in
                Button {
                    onSelect(mode.id)
                    dismiss()
                } label: {
                    HStack {
                        Text(mode.name)
                            .foregroundColor(.primary)
                        Spacer()
                        if selectedModeId == mode.id {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("Dark Mode")
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
    DarkModePickerSheet(
        selectedModeId: 0,
        onSelect: { _ in }
    )
}

import SwiftUI

struct PlayerNamePickerSheet: View {
    let currentName: String
    let onConfirm: (String) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var text: String
    @FocusState private var isFocused: Bool

    init(currentName: String, onConfirm: @escaping (String) -> Void) {
        self.currentName = currentName
        self.onConfirm = onConfirm
        _text = State(initialValue: currentName)
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                TextField("Enter player name", text: $text)
                    .textFieldStyle(.roundedBorder)
                    .focused($isFocused)
                    .submitLabel(.done)
                    .onSubmit {
                        onConfirm(text.trimmingCharacters(in: .whitespaces))
                        dismiss()
                    }
                Spacer()
            }
            .padding()
            .navigationTitle("Showdown Username")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Done") {
                        onConfirm(text.trimmingCharacters(in: .whitespaces))
                        dismiss()
                    }
                }
            }
            .onAppear {
                isFocused = true
            }
        }
        .presentationDetents([.medium])
    }
}

#Preview {
    PlayerNamePickerSheet(
        currentName: "",
        onConfirm: { _ in }
    )
}

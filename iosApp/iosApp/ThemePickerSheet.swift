import SwiftUI

struct ThemePickerSheet: View {
    let selectedThemeId: Int32
    let onSelect: (Int32) -> Void

    @Environment(\.dismiss) private var dismiss

    private let themes: [(id: Int32, name: String, color: Color)] = [
        (0, "Red", SettingsStore.colorForThemeId(0)),
        (1, "Blue", SettingsStore.colorForThemeId(1)),
        (2, "Yellow", SettingsStore.colorForThemeId(2)),
        (3, "Purple", SettingsStore.colorForThemeId(3))
    ]

    var body: some View {
        NavigationStack {
            List(themes, id: \.id) { theme in
                Button {
                    onSelect(theme.id)
                    dismiss()
                } label: {
                    HStack {
                        RoundedRectangle(cornerRadius: AppTokens.colorSwatchCornerRadius)
                            .fill(theme.color)
                            .frame(width: AppTokens.colorSwatchSize, height: AppTokens.colorSwatchSize)
                        Text(theme.name)
                            .foregroundColor(.primary)
                        Spacer()
                        if selectedThemeId == theme.id {
                            Image(systemName: "checkmark")
                                .foregroundColor(.accentColor)
                        }
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("Theme Color")
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
    ThemePickerSheet(
        selectedThemeId: 0,
        onSelect: { _ in }
    )
}

import SwiftUI

struct OutlinedTextFieldModifier: ViewModifier {
    @Environment(\.themeColor) private var themeColor
    let isFocused: Bool

    func body(content: Content) -> some View {
        content
            .textFieldStyle(.plain)
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                    .stroke(isFocused ? themeColor : Color(.opaqueSeparator), lineWidth: 1.5)
            )
            .foregroundColor(Color(.label))
    }
}

extension View {
    func outlinedTextFieldStyle(isFocused: Bool) -> some View {
        modifier(OutlinedTextFieldModifier(isFocused: isFocused))
    }
}

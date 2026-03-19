import SwiftUI

struct OutlinedTextFieldModifier: ViewModifier {
    @Environment(\.themeColor) private var themeColor

    func body(content: Content) -> some View {
        content
            .textFieldStyle(.plain)
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(themeColor, lineWidth: 1.5)
            )
            .foregroundColor(Color(.label))
    }
}

extension View {
    func outlinedTextFieldStyle() -> some View {
        modifier(OutlinedTextFieldModifier())
    }
}

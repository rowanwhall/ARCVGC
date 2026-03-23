import SwiftUI

struct InfoButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "info.circle")
                .font(.system(size: 16))
                .foregroundColor(Color(.secondaryLabel))
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    InfoButton(action: {})
}

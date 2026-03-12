import SwiftUI

struct BattleEmptyView: View {
    var body: some View {
        Text("There's nothing here")
            .font(.body)
            .foregroundColor(.primary)
            .multilineTextAlignment(.center)
            .padding(.horizontal, 32)
    }
}

#Preview {
    BattleEmptyView()
}

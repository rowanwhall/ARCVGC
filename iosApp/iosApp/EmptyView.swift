import SwiftUI

struct BattleEmptyView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image("Error")
                .resizable()
                .scaledToFit()
                .frame(height: AppTokens.heroLogoHeight)

            Text("There's nothing here")
                .font(.body)
                .foregroundColor(.primary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
    }
}

#Preview {
    BattleEmptyView()
}

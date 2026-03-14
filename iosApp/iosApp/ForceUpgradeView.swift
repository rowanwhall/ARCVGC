import SwiftUI

struct ForceUpgradeView: View {
    var body: some View {
        VStack(spacing: 16) {
            Spacer()

            Text("Update Required")
                .font(.largeTitle)
                .fontWeight(.bold)
                .foregroundColor(Color(.label))

            Text("A new version of ARC is available. Please update to continue using the app.")
                .font(.body)
                .foregroundColor(Color(.secondaryLabel))
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button {
                if let url = URL(string: "https://apps.apple.com") {
                    UIApplication.shared.open(url)
                }
            } label: {
                Text("Update")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .padding(.horizontal, 32)
            .padding(.top, 16)

            Spacer()
        }
        .background(Color(.systemBackground))
    }
}

#Preview {
    ForceUpgradeView()
}

import SwiftUI

struct ErrorView: View {
    let onRetry: () -> Void

    var body: some View {
        VStack(spacing: 16) {
            Text("Oops! We ran into a problem, please try again")
                .font(.body)
                .foregroundColor(.primary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)

            Button("Retry", action: onRetry)
                .buttonStyle(.bordered)
        }
    }
}

#Preview {
    ErrorView(onRetry: {})
}

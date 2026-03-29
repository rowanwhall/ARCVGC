import SwiftUI

struct LoadingIndicator: View {
    private let frameNames = ["LoadingFrame0", "LoadingFrame1", "LoadingFrame2", "LoadingFrame3"]
    private let durations = [0.13, 0.10, 0.13, 0.10]

    @State private var frameIndex = 0

    var body: some View {
        Image(frameNames[frameIndex])
            .resizable()
            .scaledToFit()
            .frame(height: AppTokens.heroLogoHeight)
            .onAppear {
                animateFrames()
            }
    }

    private func animateFrames() {
        DispatchQueue.main.asyncAfter(deadline: .now() + durations[frameIndex]) {
            frameIndex = (frameIndex + 1) % frameNames.count
            animateFrames()
        }
    }
}

#Preview {
    LoadingIndicator()
}

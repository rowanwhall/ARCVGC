import SwiftUI

struct ReplayOverlay: View {
    let replayUrl: String
    var onDismiss: () -> Void

    @State private var isLoading = true

    var body: some View {
        NavigationStack {
            ZStack {
                if let url = URL(string: replayUrl) {
                    WebView(url: url, isLoading: $isLoading)
                        .opacity(isLoading ? 0 : 1)
                }

                if isLoading {
                    ProgressView()
                }
            }
            .navigationTitle("Replay")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        onDismiss()
                    } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 16, weight: .semibold))
                            .foregroundColor(Color(.label))
                    }
                }
            }
        }
    }
}

#Preview {
    ReplayOverlay(replayUrl: "https://replay.pokemonshowdown.com/example", onDismiss: {})
}

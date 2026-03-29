import SwiftUI
import Shared

struct ReplayOverlay: View {
    let navState: ReplayNavState
    let onDismiss: () -> Void

    @State private var currentIndex: Int
    @State private var isLoading = true

    init(navState: ReplayNavState, onDismiss: @escaping () -> Void) {
        self.navState = navState
        self.onDismiss = onDismiss
        _currentIndex = State(initialValue: Int(navState.initialIndex))
    }

    private var currentUrl: String {
        navState.games[currentIndex].replayUrl
    }

    private var hasMultipleGames: Bool {
        navState.games.count > 1
    }

    var body: some View {
        VStack(spacing: 0) {
            ZStack {
                if let url = URL(string: currentUrl) {
                    WebView(url: url, isLoading: $isLoading)
                        .opacity(isLoading ? 0 : 1)
                }

                if isLoading {
                    ProgressView()
                }
            }

            Divider()

            HStack {
                if hasMultipleGames {
                    Button {
                        currentIndex -= 1
                    } label: {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .semibold))
                    }
                    .disabled(currentIndex <= 0)

                    let currentGame = navState.games[currentIndex]
                    let positionLabel = currentGame.positionInSet.map { "Game \($0.intValue)" } ?? "Game \(currentIndex + 1)"
                    Text("\(positionLabel) of \(navState.games.count)")
                        .font(.subheadline)
                        .foregroundColor(Color(.label))

                    Button {
                        currentIndex += 1
                    } label: {
                        Image(systemName: "chevron.right")
                            .font(.system(size: 16, weight: .semibold))
                    }
                    .disabled(currentIndex >= navState.games.count - 1)
                }

                Spacer()

                Button {
                    onDismiss()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundColor(Color(.label))
                }
            }
            .padding(.horizontal, 32)
            .padding(.vertical, 12)
        }
        .onChange(of: currentIndex) {
            isLoading = true
        }
    }
}

#Preview {
    let navState = ReplayNavState(
        games: [
            ReplayGame(positionInSet: 1, replayUrl: "https://replay.pokemonshowdown.com/example1"),
            ReplayGame(positionInSet: 2, replayUrl: "https://replay.pokemonshowdown.com/example2"),
            ReplayGame(positionInSet: 3, replayUrl: "https://replay.pokemonshowdown.com/example3")
        ],
        initialIndex: 0
    )
    ReplayOverlay(navState: navState, onDismiss: {})
}

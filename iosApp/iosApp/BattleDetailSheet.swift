import SwiftUI
import WebKit
import Shared

struct BattleDetailPage: View {
    @StateObject private var viewModel: BattleDetailViewModel
    @ObservedObject private var favoritesStore: FavoritesStore
    @Environment(\.themeColor) private var themeColor
    var onPokemonClick: ((Int32, String, String?, [String], Int32?) -> Void)? = nil
    var onPlayerClick: ((Int32, String, Int32?) -> Void)? = nil
    var onViewReplay: ((String) -> Void)? = nil

    private let battleId: Int32
    private let player1IsWinner: KotlinBoolean?
    private let player2IsWinner: KotlinBoolean?
    private let showWinnerHighlight: Bool
    private let shareUrl: String?

    init(repository: BattleRepository, battleId: Int32, player1IsWinner: KotlinBoolean? = nil, player2IsWinner: KotlinBoolean? = nil, favoritesStore: FavoritesStore, showWinnerHighlight: Bool = true, shareUrl: String? = nil, onPokemonClick: ((Int32, String, String?, [String], Int32?) -> Void)? = nil, onPlayerClick: ((Int32, String, Int32?) -> Void)? = nil, onViewReplay: ((String) -> Void)? = nil) {
        self.battleId = battleId
        self.player1IsWinner = player1IsWinner
        self.player2IsWinner = player2IsWinner
        self.favoritesStore = favoritesStore
        self.showWinnerHighlight = showWinnerHighlight
        self.shareUrl = shareUrl
        self.onPokemonClick = onPokemonClick
        self.onPlayerClick = onPlayerClick
        self.onViewReplay = onViewReplay
        _viewModel = StateObject(wrappedValue: BattleDetailViewModel(
            repository: repository,
            battleId: battleId
        ))
    }

    var body: some View {
        Group {
            if viewModel.state.isLoading || (viewModel.state.battleDetail == nil && viewModel.state.error == nil) {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.state.error != nil {
                ErrorView {
                    viewModel.loadBattleDetail()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let battleDetail = viewModel.state.battleDetail {
                let wrappedOnPokemonClick: ((Int32, String, String?, [String]) -> Void)? = onPokemonClick.map { callback in
                    { id, name, imageUrl, typeImageUrls in
                        callback(id, name, imageUrl, typeImageUrls, battleDetail.formatId)
                    }
                }
                let wrappedOnPlayerClick: ((Int32, String) -> Void)? = onPlayerClick.map { callback in
                    { id, name in
                        callback(id, name, battleDetail.formatId)
                    }
                }
                ScrollView {
                    VStack(spacing: 16) {
                        Text(battleDetail.formatName)
                            .font(.subheadline)
                            .foregroundColor(Color(.secondaryLabel))

                        let setMatches = (battleDetail.setMatches as? [SetMatchUiModel]) ?? []
                        if setMatches.isEmpty {
                            Button {
                                onViewReplay?(battleDetail.replayUrl)
                            } label: {
                                Text("View Replay")
                                    .fontWeight(.semibold)
                            }
                            .buttonStyle(.borderedProminent)
                        } else {
                            HStack(spacing: 8) {
                                let currentPosition = battleDetail.positionInSet?.intValue ?? 1
                                let allGames: [(position: Int, url: String, isCurrent: Bool)] = (
                                    [(currentPosition, battleDetail.replayUrl, true)] +
                                    setMatches.map { (Int($0.positionInSet), $0.replayUrl, false) }
                                ).sorted { $0.position < $1.position }

                                ForEach(Array(allGames.enumerated()), id: \.offset) { _, game in
                                    if game.isCurrent {
                                        Button {
                                            onViewReplay?(game.url)
                                        } label: {
                                            Text("Game \(game.position)")
                                                .font(.subheadline)
                                        }
                                        .buttonStyle(.borderedProminent)
                                    } else {
                                        Button {
                                            onViewReplay?(game.url)
                                        } label: {
                                            Text("Game \(game.position)")
                                                .font(.subheadline)
                                        }
                                        .buttonStyle(.bordered)
                                    }
                                }
                            }
                        }

                        PlayerTeamDetailSection(player: battleDetail.player1, isWinnerOverride: player1IsWinner, showWinnerHighlight: showWinnerHighlight, onPokemonClick: wrappedOnPokemonClick, onPlayerClick: wrappedOnPlayerClick)

                        VsDivider()

                        PlayerTeamDetailSection(player: battleDetail.player2, isWinnerOverride: player2IsWinner, showWinnerHighlight: showWinnerHighlight, onPokemonClick: wrappedOnPokemonClick, onPlayerClick: wrappedOnPlayerClick)
                    }
                    .padding(.bottom, 16)
                }
                .background(Color(.systemBackground))
            }
        }
        .background(Color(.secondarySystemBackground))
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                HStack {
                    if let shareUrl = shareUrl, let url = URL(string: shareUrl) {
                        ShareLink(item: url) {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 18))
                                .foregroundColor(Color(.secondaryLabel))
                        }
                    }

                    Button {
                        favoritesStore.toggleBattleFavorite(id: battleId)
                    } label: {
                        Image(systemName: favoritesStore.isBattleFavorited(id: battleId) ? "heart.fill" : "heart")
                            .font(.system(size: 20))
                            .foregroundColor(favoritesStore.isBattleFavorited(id: battleId) ? themeColor : Color(.secondaryLabel))
                    }
                }
            }
        }
        .onAppear {
            viewModel.loadBattleDetail()
        }
    }
}

struct PlayerTeamDetailSection: View {
    let player: PlayerDetailUiModel
    var isWinnerOverride: KotlinBoolean? = nil
    var showWinnerHighlight: Bool = true
    var onPokemonClick: ((Int32, String, String?, [String]) -> Void)? = nil
    var onPlayerClick: ((Int32, String) -> Void)? = nil
    @Environment(\.themeColor) private var themeColor
    @State private var showCopied = false

    private var effectiveIsWinner: Bool {
        showWinnerHighlight && (isWinnerOverride ?? player.isWinner)?.boolValue == true
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Button {
                    onPlayerClick?(player.id, player.name)
                } label: {
                    HStack(spacing: 4) {
                        Text(player.name)
                            .font(.system(size: 18, weight: .bold))
                            .foregroundColor(Color(.label))
                        Image(systemName: "chevron.right")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(Color(.secondaryLabel))
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(Color(.systemBackground))
                    .cornerRadius(16)
                }
                .buttonStyle(.plain)

                Spacer()

                Button {
                    let text = ShowdownPasteFormatter.shared.format(team: player.team)
                    UIPasteboard.general.string = text
                    withAnimation {
                        showCopied = true
                    }
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                        withAnimation {
                            showCopied = false
                        }
                    }
                } label: {
                    Image(systemName: showCopied ? "checkmark" : "doc.on.doc")
                        .font(.system(size: 16))
                        .foregroundColor(showCopied ? themeColor : Color(.secondaryLabel))
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(Array(player.team.enumerated()), id: \.offset) { _, pokemon in
                        PokemonDetailCard(pokemon: pokemon, onPokemonClick: onPokemonClick)
                            .frame(width: UIScreen.main.bounds.width * 0.7)
                    }
                }
                .padding(.horizontal, 16)
            }

            Spacer().frame(height: 8)
        }
        .background(Color(.secondarySystemBackground))
        .overlay(
            effectiveIsWinner
                ? RoundedRectangle(cornerRadius: 0).stroke(themeColor, lineWidth: 2)
                : nil
        )
        .overlay(alignment: .bottom) {
            if showCopied {
                Text("Team copied to clipboard")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 8)
                    .background(Color(.label).opacity(0.8))
                    .cornerRadius(20)
                    .padding(.bottom, 8)
                    .transition(.opacity.combined(with: .move(edge: .bottom)))
            }
        }
    }
}

struct VsDivider: View {
    var body: some View {
        HStack {
            Rectangle()
                .fill(Color(.label).opacity(0.2))
                .frame(height: 1)
            Text("VS")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(Color(.label).opacity(0.4))
                .padding(.horizontal, 16)
            Rectangle()
                .fill(Color(.label).opacity(0.2))
                .frame(height: 1)
        }
        .padding(.horizontal, 16)
    }
}

struct WebView: UIViewRepresentable {
    let url: URL
    @Binding var isLoading: Bool

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    func makeUIView(context: Context) -> WKWebView {
        let webView = WKWebView()
        webView.navigationDelegate = context.coordinator
        webView.load(URLRequest(url: url))
        return webView
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        if uiView.url != url {
            uiView.load(URLRequest(url: url))
        }
    }

    class Coordinator: NSObject, WKNavigationDelegate {
        var parent: WebView

        init(_ parent: WebView) {
            self.parent = parent
        }

        func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
            parent.isLoading = true
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            parent.isLoading = false
        }

        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            parent.isLoading = false
        }
    }
}

#Preview("VsDivider") {
    VsDivider()
        .padding()
}

#Preview("MoveChip") {
    MoveChip(moveName: "Extreme Speed")
        .padding()
}

#Preview {
    let container = DependencyContainer()
    return NavigationStack {
        BattleDetailPage(repository: container.battleRepository, battleId: 1, favoritesStore: container.favoritesStore)
    }
}

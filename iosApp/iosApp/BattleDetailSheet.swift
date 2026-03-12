import SwiftUI
import WebKit
import Shared

struct BattleDetailSheet: View {
    @StateObject private var viewModel: BattleDetailViewModel
    @ObservedObject private var favoritesStore: FavoritesStore
    @Environment(\.themeColor) private var themeColor
    @State private var selectedTab = 0
    var onDismiss: (() -> Void)? = nil
    var onPokemonClick: ((Int32, String, String?, [String]) -> Void)? = nil
    var onPlayerClick: ((Int32, String) -> Void)? = nil

    private let battleId: Int32
    private let player1IsWinner: KotlinBoolean?
    private let player2IsWinner: KotlinBoolean?
    private let showWinnerHighlight: Bool

    init(repository: BattleRepository, battleId: Int32, player1IsWinner: KotlinBoolean? = nil, player2IsWinner: KotlinBoolean? = nil, favoritesStore: FavoritesStore, showWinnerHighlight: Bool = true, onDismiss: (() -> Void)? = nil, onPokemonClick: ((Int32, String, String?, [String]) -> Void)? = nil, onPlayerClick: ((Int32, String) -> Void)? = nil) {
        self.battleId = battleId
        self.player1IsWinner = player1IsWinner
        self.player2IsWinner = player2IsWinner
        self.favoritesStore = favoritesStore
        self.showWinnerHighlight = showWinnerHighlight
        self.onDismiss = onDismiss
        self.onPokemonClick = onPokemonClick
        self.onPlayerClick = onPlayerClick
        _viewModel = StateObject(wrappedValue: BattleDetailViewModel(
            repository: repository,
            battleId: battleId
        ))
    }

    var body: some View {
        VStack(spacing: 0) {
            // Drag handle
            Capsule()
                .fill(Color(.systemGray3))
                .frame(width: 36, height: 5)
                .padding(.top, 8)
                .padding(.bottom, 8)

            // Header row with X button and heart
            HStack {
                Button {
                    onDismiss?()
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Color(.secondaryLabel))
                }

                Spacer()

                Button {
                    favoritesStore.toggleBattleFavorite(id: battleId)
                } label: {
                    Image(systemName: favoritesStore.isBattleFavorited(id: battleId) ? "heart.fill" : "heart")
                        .font(.system(size: 20))
                        .foregroundColor(favoritesStore.isBattleFavorited(id: battleId) ? themeColor : Color(.secondaryLabel))
                }
            }
            .padding(.horizontal, 30)
            .padding(.bottom, 18)

            if viewModel.state.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if viewModel.state.error != nil {
                ErrorView {
                    viewModel.loadBattleDetail()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let battleDetail = viewModel.state.battleDetail {
                // Tab picker
                Picker("", selection: $selectedTab) {
                    Text("Team Preview").tag(0)
                    Text("Replay").tag(1)
                }
                .pickerStyle(.segmented)
                .padding(.horizontal)
                .padding(.bottom, 8)

                // Tab content
                if selectedTab == 0 {
                    TeamPreviewTab(battleDetail: battleDetail, player1IsWinner: player1IsWinner, player2IsWinner: player2IsWinner, showWinnerHighlight: showWinnerHighlight, onPokemonClick: onPokemonClick, onPlayerClick: onPlayerClick)
                } else {
                    ReplayTab(replayUrl: battleDetail.replayUrl)
                }
            }
        }
        .background(Color(.secondarySystemBackground))
        .onAppear {
            viewModel.loadBattleDetail()
        }
    }
}

struct TeamPreviewTab: View {
    let battleDetail: BattleDetailUiModel
    var player1IsWinner: KotlinBoolean? = nil
    var player2IsWinner: KotlinBoolean? = nil
    var showWinnerHighlight: Bool = true
    var onPokemonClick: ((Int32, String, String?, [String]) -> Void)? = nil
    var onPlayerClick: ((Int32, String) -> Void)? = nil

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                PlayerTeamDetailSection(player: battleDetail.player1, isWinnerOverride: player1IsWinner, showWinnerHighlight: showWinnerHighlight, onPokemonClick: onPokemonClick, onPlayerClick: onPlayerClick)

                VsDivider()

                PlayerTeamDetailSection(player: battleDetail.player2, isWinnerOverride: player2IsWinner, showWinnerHighlight: showWinnerHighlight, onPokemonClick: onPokemonClick, onPlayerClick: onPlayerClick)
            }
            .padding(.vertical, 16)
        }
        .background(Color(.systemBackground))
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
                    showCopied = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
                        showCopied = false
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

struct ReplayTab: View {
    let replayUrl: String
    @State private var isLoading = true

    var body: some View {
        ZStack {
            if let url = URL(string: replayUrl) {
                WebView(url: url, isLoading: $isLoading)
                    .opacity(isLoading ? 0 : 1)
            }

            if isLoading {
                ProgressView()
            }
        }
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
    return BattleDetailSheet(repository: container.battleRepository, battleId: 1, favoritesStore: container.favoritesStore)
}

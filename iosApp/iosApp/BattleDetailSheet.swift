import SwiftUI
import WebKit
import Shared

struct BattleDetailPage: View {
    @ObservedObject private var viewModel: BattleDetailViewModel
    @ObservedObject private var favoritesStore: FavoritesStore
    @Environment(\.themeColor) private var themeColor
    var onPokemonClick: ((Int32, String, String?, [String], Int32?) -> Void)? = nil
    var onPlayerClick: ((Int32, String, Int32?) -> Void)? = nil
    var onViewReplay: ((ReplayNavState) -> Void)? = nil
    @State private var showReplayInfo = false
    @State private var showUnratedInfo = false

    private let battleId: Int32
    private let player1IsWinner: KotlinBoolean?
    private let player2IsWinner: KotlinBoolean?
    private let showWinnerHighlight: Bool
    private let shareUrl: String?

    init(viewModel: BattleDetailViewModel, battleId: Int32, player1IsWinner: KotlinBoolean? = nil, player2IsWinner: KotlinBoolean? = nil, favoritesStore: FavoritesStore, showWinnerHighlight: Bool = true, shareUrl: String? = nil, onPokemonClick: ((Int32, String, String?, [String], Int32?) -> Void)? = nil, onPlayerClick: ((Int32, String, Int32?) -> Void)? = nil, onViewReplay: ((ReplayNavState) -> Void)? = nil) {
        self.viewModel = viewModel
        self.battleId = battleId
        self.player1IsWinner = player1IsWinner
        self.player2IsWinner = player2IsWinner
        self.favoritesStore = favoritesStore
        self.showWinnerHighlight = showWinnerHighlight
        self.shareUrl = shareUrl
        self.onPokemonClick = onPokemonClick
        self.onPlayerClick = onPlayerClick
        self.onViewReplay = onViewReplay
    }

    var body: some View {
        Group {
            if viewModel.state.isLoading || (viewModel.state.battleDetail == nil && viewModel.state.error == nil) {
                LoadingIndicator()
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
                        HStack(spacing: 0) {
                            if let rating = battleDetail.rating?.intValue {
                                Text("\(battleDetail.formatName) \(AppTokens.bulletSeparator) \(String(rating))")
                                    .font(.subheadline)
                                    .foregroundColor(Color(.secondaryLabel))
                            } else {
                                Color.clear.frame(width: AppTokens.infoButtonSize, height: AppTokens.infoButtonSize)
                                Text("\(battleDetail.formatName) \(AppTokens.bulletSeparator) Unrated")
                                    .font(.subheadline)
                                    .foregroundColor(Color(.secondaryLabel))

                                InfoButton { showUnratedInfo = true }
                            }
                        }

                        let setMatches = (battleDetail.setMatches as? [SetMatchUiModel]) ?? []
                        HStack(spacing: 0) {
                            let currentPosition = battleDetail.positionInSet?.intValue
                            let allGames: [(position: Int?, url: String, isCurrent: Bool)] = (
                                [(currentPosition, battleDetail.replayUrl, true)] +
                                setMatches.map { ($0.positionInSet?.intValue, $0.replayUrl, false) }
                            ).sorted { ($0.position ?? Int.max) < ($1.position ?? Int.max) }

                            Color.clear.frame(width: AppTokens.infoButtonSize, height: AppTokens.infoButtonSize)
                            HStack(spacing: 8) {
                                ForEach(Array(allGames.enumerated()), id: \.offset) { _, game in
                                    let label = game.position.map { "Game \($0)" } ?? "View Replay"
                                    let buttonLabel = HStack(spacing: 4) {
                                        Text(label)
                                            .font(.subheadline)
                                        Image(systemName: "chevron.right")
                                            .font(.system(size: 10, weight: .semibold))
                                    }
                                    if game.isCurrent {
                                        Button {
                                            onViewReplay?(battleDetail.toReplayNavState(tappedUrl: game.url))
                                        } label: {
                                            buttonLabel
                                        }
                                        .buttonStyle(.borderedProminent)
                                        .controlSize(.small)
                                    } else {
                                        Button {
                                            onViewReplay?(battleDetail.toReplayNavState(tappedUrl: game.url))
                                        } label: {
                                            buttonLabel
                                        }
                                        .buttonStyle(.bordered)
                                        .controlSize(.small)
                                    }
                                }
                            }

                            InfoButton { showReplayInfo = true }
                        }

                        PlayerTeamDetailSection(player: battleDetail.player1, isWinnerOverride: player1IsWinner, showWinnerHighlight: showWinnerHighlight, onPokemonClick: wrappedOnPokemonClick, onPlayerClick: wrappedOnPlayerClick)

                        VsDivider()

                        PlayerTeamDetailSection(player: battleDetail.player2, isWinnerOverride: player2IsWinner, showWinnerHighlight: showWinnerHighlight, onPokemonClick: wrappedOnPokemonClick, onPlayerClick: wrappedOnPlayerClick)
                    }
                    .padding(.bottom, 16)
                }
                .background(Color(.systemBackground))
                .sheet(isPresented: $showReplayInfo) {
                    if let content = InfoContentProvider.shared.get(key: "replay") {
                        InfoSheet(content: content)
                    }
                }
                .sheet(isPresented: $showUnratedInfo) {
                    if let content = InfoContentProvider.shared.get(key: "unrated") {
                        InfoSheet(content: content)
                    }
                }
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
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @State private var showCopied = false

    private static let cardWidth: CGFloat = 280
    private static let cardSpacing: CGFloat = 12
    private static let innerPadding: CGFloat = 16

    private var effectiveIsWinner: Bool {
        showWinnerHighlight && (isWinnerOverride ?? player.isWinner)?.boolValue == true
    }

    private var playerHeader: some View {
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
                .padding(.horizontal, AppTokens.playerChipHorizontalPadding)
                .padding(.vertical, AppTokens.playerChipVerticalPadding)
                .background(Color(.systemBackground))
                .cornerRadius(AppTokens.playerChipCornerRadius)
                .overlay(
                    RoundedRectangle(cornerRadius: AppTokens.playerChipCornerRadius)
                        .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
                )
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
    }

    private var copiedToast: some View {
        Group {
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

    var body: some View {
        if horizontalSizeClass == .regular {
            expandedLayout
        } else {
            compactLayout
        }
    }

    private var compactLayout: some View {
        VStack(alignment: .leading, spacing: 8) {
            playerHeader
                .padding(.horizontal, 16)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: Self.cardSpacing) {
                    ForEach(Array(player.team.enumerated()), id: \.offset) { _, pokemon in
                        PokemonDetailCard(pokemon: pokemon, onPokemonClick: onPokemonClick)
                            .frame(width: min(UIScreen.main.bounds.width * 0.7, 320))
                    }
                }
                .padding(.horizontal, Self.innerPadding)
            }
        }
        .padding(.vertical, 8)
        .background(Color(.secondarySystemBackground))
        .overlay(
            effectiveIsWinner
                ? RoundedRectangle(cornerRadius: 0).stroke(themeColor, lineWidth: 2)
                : nil
        )
        .overlay(alignment: .bottom) { copiedToast }
    }

    private var expandedColumns: Int {
        let availableWidth = UIScreen.main.bounds.width - Self.innerPadding * 4
        let fitted = Int((availableWidth + Self.cardSpacing) / (Self.cardWidth + Self.cardSpacing))
        return max(1, min(fitted, 3, player.team.count))
    }

    private var expandedLayout: some View {
        let columns = expandedColumns
        let gridWidth = Self.cardWidth * CGFloat(columns) + Self.cardSpacing * CGFloat(columns - 1)
        let containerWidth = gridWidth + Self.innerPadding * 2
        let rows = stride(from: 0, to: player.team.count, by: columns).map { startIndex in
            Array(player.team[startIndex..<min(startIndex + columns, player.team.count)])
        }

        return VStack(alignment: .leading, spacing: 8) {
            playerHeader

            VStack(spacing: Self.cardSpacing) {
                ForEach(Array(rows.enumerated()), id: \.offset) { _, row in
                    HStack(spacing: Self.cardSpacing) {
                        ForEach(Array(row.enumerated()), id: \.offset) { _, pokemon in
                            PokemonDetailCard(pokemon: pokemon, onPokemonClick: onPokemonClick)
                                .frame(width: Self.cardWidth)
                        }
                        if row.count < columns {
                            ForEach(0..<(columns - row.count), id: \.self) { _ in
                                Color.clear.frame(width: Self.cardWidth)
                            }
                        }
                    }
                }
            }
        }
        .padding(Self.innerPadding)
        .frame(width: containerWidth)
        .frame(maxWidth: .infinity)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(AppTokens.cardCornerRadius)
        .overlay(
            effectiveIsWinner
                ? RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius).stroke(themeColor, lineWidth: 2)
                : nil
        )
        .overlay(alignment: .bottom) { copiedToast }
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

/// Wrapper that adds pokemon/player drill-down navigation from battle detail.
/// Attaches `navigationDestination` at the battle-detail level so pushes/pops
/// happen from the correct position in the NavigationStack.
struct BattleDetailNavWrapper: View {
    @StateObject private var viewModel: BattleDetailViewModel
    let repository: BattleRepository
    let battleId: Int32
    var player1IsWinner: KotlinBoolean? = nil
    var player2IsWinner: KotlinBoolean? = nil
    let favoritesStore: FavoritesStore
    let settingsStore: SettingsStore
    var showWinnerHighlight: Bool = true
    var shareUrl: String? = nil
    var appConfigStore: AppConfigStore? = nil
    var onViewReplay: ((ReplayNavState) -> Void)? = nil

    @State private var pokemonNavTarget: PokemonNavTarget? = nil
    @State private var playerNavTarget: PlayerNavTarget? = nil

    init(repository: BattleRepository, battleId: Int32, player1IsWinner: KotlinBoolean? = nil, player2IsWinner: KotlinBoolean? = nil, favoritesStore: FavoritesStore, settingsStore: SettingsStore, showWinnerHighlight: Bool = true, shareUrl: String? = nil, appConfigStore: AppConfigStore? = nil, onViewReplay: ((ReplayNavState) -> Void)? = nil) {
        self.repository = repository
        self.battleId = battleId
        self.player1IsWinner = player1IsWinner
        self.player2IsWinner = player2IsWinner
        self.favoritesStore = favoritesStore
        self.settingsStore = settingsStore
        self.showWinnerHighlight = showWinnerHighlight
        self.shareUrl = shareUrl
        self.appConfigStore = appConfigStore
        self.onViewReplay = onViewReplay
        _viewModel = StateObject(wrappedValue: BattleDetailViewModel(
            repository: repository,
            battleId: battleId
        ))
    }

    var body: some View {
        BattleDetailPage(
            viewModel: viewModel,
            battleId: battleId,
            player1IsWinner: player1IsWinner,
            player2IsWinner: player2IsWinner,
            favoritesStore: favoritesStore,
            showWinnerHighlight: showWinnerHighlight,
            shareUrl: shareUrl,
            onPokemonClick: { id, name, imageUrl, typeImageUrls, formatId in
                pokemonNavTarget = PokemonNavTarget(id: id, name: name, imageUrl: imageUrl, typeImageUrl1: typeImageUrls.first, typeImageUrl2: typeImageUrls.count > 1 ? typeImageUrls[1] : nil, formatId: formatId)
            },
            onPlayerClick: { id, name, formatId in
                playerNavTarget = PlayerNavTarget(id: id, name: name, formatId: formatId)
            },
            onViewReplay: onViewReplay
        )
        .navigationDestination(isPresented: Binding(
            get: { pokemonNavTarget != nil },
            set: { if !$0 { pokemonNavTarget = nil } }
        )) {
            if let target = pokemonNavTarget {
                ContentListView(
                    repository: repository,
                    mode: .pokemon(id: target.id, name: target.name, imageUrl: target.imageUrl, typeImageUrl1: target.typeImageUrl1, typeImageUrl2: target.typeImageUrl2, formatId: target.formatId),
                    favoritesStore: favoritesStore,
                    settingsStore: settingsStore,
                    appConfigStore: appConfigStore
                )
            }
        }
        .navigationDestination(isPresented: Binding(
            get: { playerNavTarget != nil },
            set: { if !$0 { playerNavTarget = nil } }
        )) {
            if let target = playerNavTarget {
                ContentListView(
                    repository: repository,
                    mode: .player(id: target.id, name: target.name, formatId: target.formatId),
                    favoritesStore: favoritesStore,
                    settingsStore: settingsStore,
                    appConfigStore: appConfigStore
                )
            }
        }
    }
}

#Preview {
    let container = DependencyContainer()
    return NavigationStack {
        BattleDetailPage(viewModel: BattleDetailViewModel(repository: container.battleRepository, battleId: 1), battleId: 1, favoritesStore: container.favoritesStore)
    }
}

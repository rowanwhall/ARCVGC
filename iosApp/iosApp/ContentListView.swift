import SwiftUI
import Shared

struct ContentListView: View {
    @StateObject private var viewModel: ContentListViewModel
    @State private var selectedBattleId: Int32? = nil
    @State private var replayNavState: ReplayNavState? = nil
    @State private var pokemonNavTarget: PokemonNavTarget? = nil
    @State private var playerNavTarget: PlayerNavTarget? = nil
    @State private var topPokemonFormatId: Int32? = nil
    @FocusState private var isSearchFieldFocused: Bool
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @EnvironmentObject private var container: DependencyContainer

    private let repository: BattleRepository
    private let mode: ContentListMode
    @ObservedObject private var favoritesStore: FavoritesStore
    @ObservedObject private var settingsStore: SettingsStore
    private let onSearchParamsChanged: ((SearchParams) -> Void)?

    private var hasSortToggle: Bool {
        switch mode {
        case .search, .pokemon, .player: return true
        default: return false
        }
    }

    private let appConfigStore: AppConfigStore?

    /// Live format items from the catalog store, sorted with the user's preferred format pinned
    /// to the top (falling back to the app-config default when no preference is set).
    private var formatItems: [FormatUiModel] {
        let preferred = settingsStore.preferredFormatId
        let pinned: Int32? = preferred != 0 ? preferred : appConfigStore?.config?.defaultFormat.id
        return FormatSorter.shared.sorted(
            formats: container.catalogStore.formatItems,
            defaultFormatId: pinned.map { KotlinInt(int: $0) }
        )
    }

    init(repository: BattleRepository, mode: ContentListMode = .home, favoritesStore: FavoritesStore, settingsStore: SettingsStore, pokemonCatalogItems: [PokemonPickerUiModel] = [], appConfigStore: AppConfigStore? = nil, formatItems: [FormatUiModel] = [], onSearchParamsChanged: ((SearchParams) -> Void)? = nil) {
        self.repository = repository
        self.mode = mode
        self.favoritesStore = favoritesStore
        self.settingsStore = settingsStore
        self.appConfigStore = appConfigStore
        self.onSearchParamsChanged = onSearchParamsChanged
        _viewModel = StateObject(wrappedValue: ContentListViewModel(repository: repository, mode: mode, favoritesStore: favoritesStore, pokemonCatalogItems: pokemonCatalogItems, appConfigStore: appConfigStore, formatItems: formatItems, settingsStore: settingsStore))
    }

    private func buildShareUrl(battleId: Int32? = nil) -> String {
        let bid: KotlinInt? = battleId.map { KotlinInt(int: $0) }
        return ShareUrlBuilderKt.shareUrlForMode(mode: viewModel.mode.toSharedMode(), battleId: bid)
    }

    private var showShareButton: Bool {
        switch mode {
        case .home, .favorites, .topPokemon: return false
        default: return true
        }
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        if showShareButton {
            ToolbarItem(placement: .navigationBarTrailing) {
                if let url = URL(string: buildShareUrl()) {
                    ShareLink(item: url) {
                        Image(systemName: "square.and.arrow.up")
                            .foregroundColor(Color(.secondaryLabel))
                    }
                }
            }
        }
        if case .pokemon(let pId, _, _, _, _, _) = mode {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    favoritesStore.togglePokemonFavorite(id: pId)
                } label: {
                    Image(systemName: favoritesStore.isPokemonFavorited(id: pId) ? "heart.fill" : "heart")
                        .foregroundColor(favoritesStore.isPokemonFavorited(id: pId) ? settingsStore.themeColor : .gray)
                }
            }
        }
        if case .player(_, let pName, _) = mode {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    favoritesStore.togglePlayerFavorite(name: pName)
                } label: {
                    Image(systemName: favoritesStore.isPlayerFavorited(name: pName) ? "heart.fill" : "heart")
                        .foregroundColor(favoritesStore.isPlayerFavorited(name: pName) ? settingsStore.themeColor : .gray)
                }
            }
        }
    }

    @ViewBuilder
    private func battleDetailPageContent(battleId: Int32) -> some View {
        BattleDetailNavWrapper(
            repository: repository,
            battleId: battleId,
            favoritesStore: favoritesStore,
            settingsStore: settingsStore,
            showWinnerHighlight: settingsStore.showWinnerHighlight,
            shareUrl: ShareUrlBuilderKt.shareBattleUrl(battleId: battleId),
            appConfigStore: appConfigStore,
            onViewReplay: { navState in
                replayNavState = navState
            }
        )
    }

    private var isTopPokemonMode: Bool {
        if case .topPokemon = mode { return true }
        return false
    }

    var body: some View {
        let header = ContentListHeader(mode: viewModel.mode)

        GeometryReader { geometry in
            ScrollView {
                LazyVStack(spacing: AppTokens.contentListItemSpacing) {
                    if case .homeHero = header {
                        VStack(spacing: 0) {
                            Image("Logo")
                                .resizable()
                                .scaledToFit()
                                .frame(height: AppTokens.heroLogoHeight)
                                .ifSilhouette()
                            Text("ARC")
                                .font(.custom("Orbitron-Regular", size: 24, relativeTo: .title))
                                .fontWeight(.semibold)
                                .foregroundColor(Color(.label))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 16)
                        .padding(.top, 24)
                    }

                    // TODO: Replace with branded favorites asset when ready

                    if case .pokemonHero(let name, let imageUrl, let typeImageUrls) = header {
                        VStack(spacing: 0) {
                            if imageUrl != nil {
                                PokemonAvatar(
                                    imageUrl: imageUrl,
                                    circleSize: 132,
                                    spriteSize: 184
                                )
                            }
                            Text(name)
                                .font(.title2)
                                .fontWeight(.semibold)
                            if !typeImageUrls.isEmpty {
                                TypeIconRow(
                                    types: typeImageUrls.map { (name: "Type", imageUrl: $0 as String?) },
                                    iconSize: 24
                                )
                                .padding(.top, 4)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 16)
                    }

                    if case .playerHero(let name) = header {
                        VStack(spacing: 0) {
                            Text(name)
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color(.label))
                                .padding(.horizontal, AppTokens.playerChipHorizontalPadding)
                                .padding(.vertical, AppTokens.playerChipVerticalPadding)
                                .background(Color(.systemBackground))
                                .cornerRadius(AppTokens.playerChipCornerRadius)
                                .overlay(
                                    RoundedRectangle(cornerRadius: AppTokens.playerChipCornerRadius)
                                        .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
                                )
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 16)
                    }

                    if case .searchFilters(let data) = header {
                        let searchParams: SearchParams? = {
                            if case .search(let params) = viewModel.mode { return params }
                            return nil
                        }()
                        SearchFilterChipsView(
                            filters: data,
                            searchParams: searchParams,
                            onSearchParamsChanged: { newParams in
                                viewModel.updateSearchParams(newParams)
                                onSearchParamsChanged?(newParams)
                            }
                        )
                        .padding(.horizontal, 16)
                    }

                    let hasContent = (viewModel.state.items as! [ContentListItem]).contains { $0.isContentItem }
                    if viewModel.state.isLoading {
                        LoadingIndicator()
                            .frame(maxWidth: .infinity)
                            .frame(height: geometry.size.height * 0.6)
                    } else if viewModel.state.error != nil, !hasContent {
                        ErrorView {
                            viewModel.loadContent()
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: geometry.size.height * 0.6)
                    } else if !hasContent {
                        // Render non-content items (e.g. FormatSelector) before the empty view
                        ForEach(Array(viewModel.state.items.enumerated()), id: \.element.listKey) { _, item in
                            if !item.isContentItem && !isTopPokemonMode {
                                switch onEnum(of: item) {
                                case .formatSelector:
                                    if !formatItems.isEmpty {
                                        let isLoadingFormat = viewModel.state.loadingSections.contains("format_selector")
                                        HStack(spacing: 8) {
                                            Spacer()
                                            FormatDropdown(
                                                formats: formatItems,
                                                selectedFormatId: viewModel.selectedFormatId,
                                                onFormatSelected: { viewModel.selectFormat($0) }
                                            )
                                            if isLoadingFormat {
                                                ProgressView()
                                                    .scaleEffect(0.7)
                                            }
                                            Spacer()
                                        }
                                        .padding(.horizontal, 16)
                                    }
                                case .searchField:
                                    TextField("", text: Binding(
                                        get: { viewModel.searchQuery },
                                        set: { viewModel.setSearchQuery($0) }
                                    ), prompt: Text("Search Pok\u{00E9}mon").foregroundColor(Color(.secondaryLabel)))
                                        .focused($isSearchFieldFocused)
                                        .outlinedTextFieldStyle(isFocused: isSearchFieldFocused)
                                        .overlay(alignment: .trailing) {
                                            if !viewModel.searchQuery.isEmpty {
                                                Button { viewModel.setSearchQuery("") } label: {
                                                    Image(systemName: "xmark.circle.fill")
                                                        .foregroundColor(Color(.tertiaryLabel))
                                                }
                                                .padding(.trailing, 8)
                                            }
                                        }
                                        .padding(.horizontal, 16)
                                default:
                                    EmptyView()
                                }
                            }
                        }
                        BattleEmptyView()
                            .frame(maxWidth: .infinity)
                            .frame(height: geometry.size.height * 0.6)
                    } else {
                        // iOS flattens SectionGroups into their inner Sections so the existing
                        // per-Section rendering path stays unchanged. iPad multi-column layout
                        // may opt in to rendering the group directly in the future.
                        let unwrappedItems = unwrapSectionGroups(viewModel.state.items as! [ContentListItem])
                        ForEach(Array(unwrappedItems.enumerated()), id: \.element.listKey) { index, item in
                            switch onEnum(of: item) {
                            case .section(let section):
                                let showSort = section.header == "Battles" && hasSortToggle
                                let isLoadingSection = viewModel.state.loadingSections.contains(section.header)
                                let hasSeeMore = section.trailingAction is ContentListItem.SectionActionSeeMore
                                if !section.header.isEmpty {
                                    SectionHeaderView(
                                        title: section.header,
                                        isLoading: isLoadingSection,
                                        sortOrder: showSort ? viewModel.sortOrder : nil,
                                        onToggleSortOrder: showSort ? { viewModel.toggleSortOrder() } : nil,
                                        onSeeMore: hasSeeMore ? { topPokemonFormatId = viewModel.selectedFormatId } : nil
                                    )
                                    .padding(.horizontal, 16)
                                }
                                if section.items.isEmpty && !isLoadingSection {
                                    BattleEmptyView()
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 32)
                                        .padding(.horizontal, 16)
                                }
                                ForEach(Array((section.items as! [ContentListItem]).enumerated()), id: \.element.listKey) { _, child in
                                    contentItemView(child)
                                        .opacity(isLoadingSection ? 0.5 : 1.0)
                                        .padding(.horizontal, child.edgeToEdge ? 0 : 16)
                                }
                            case .formatSelector:
                                if !formatItems.isEmpty && !isTopPokemonMode {
                                    let isLoadingFormat = viewModel.state.loadingSections.contains("format_selector")
                                    HStack(spacing: 8) {
                                        Spacer()
                                        FormatDropdown(
                                            formats: formatItems,
                                            selectedFormatId: viewModel.selectedFormatId,
                                            onFormatSelected: { viewModel.selectFormat($0) }
                                        )
                                        if isLoadingFormat {
                                            ProgressView()
                                                .scaleEffect(0.7)
                                        }
                                        Spacer()
                                    }
                                    .padding(.horizontal, 16)
                                }
                            case .searchField:
                                if !isTopPokemonMode {
                                    TextField("", text: Binding(
                                        get: { viewModel.searchQuery },
                                        set: { viewModel.setSearchQuery($0) }
                                    ), prompt: Text("Search Pok\u{00E9}mon").foregroundColor(Color(.secondaryLabel)))
                                        .focused($isSearchFieldFocused)
                                        .outlinedTextFieldStyle(isFocused: isSearchFieldFocused)
                                        .overlay(alignment: .trailing) {
                                            if !viewModel.searchQuery.isEmpty {
                                                Button { viewModel.setSearchQuery("") } label: {
                                                    Image(systemName: "xmark.circle.fill")
                                                        .foregroundColor(Color(.tertiaryLabel))
                                                }
                                                .padding(.trailing, 8)
                                            }
                                        }
                                        .padding(.horizontal, 16)
                                }
                            default:
                                contentItemView(item)
                                    .padding(.horizontal, item.edgeToEdge ? 0 : 16)
                                    .onAppear {
                                        if index >= unwrappedItems.count - paginationThreshold {
                                            viewModel.paginate()
                                        }
                                    }
                            }
                        }

                        if viewModel.state.isPaginating {
                            ProgressView()
                                .padding()
                        }
                    }
                }
                .padding(.top, header.hasPokemonHero ? 4 : 0)
                .padding(.bottom, isTopPokemonMode ? AppTokens.usageBottomBarReservedHeight : 16)
            }
            .refreshable {
                await refreshContent()
            }
            .overlay(alignment: .bottom) {
                if isTopPokemonMode {
                    UsageBottomBar(
                        formats: formatItems,
                        selectedFormatId: viewModel.selectedFormatId,
                        onFormatSelected: { viewModel.selectFormat($0) },
                        isLoadingFormat: viewModel.state.loadingSections.contains("format_selector"),
                        searchQuery: Binding(
                            get: { viewModel.searchQuery },
                            set: { viewModel.setSearchQuery($0) }
                        )
                    )
                    .padding(.horizontal, 8)
                    .padding(.bottom, 4)
                }
            }
        }
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { toolbarContent }
        .background(Color(.systemBackground))
        .navigationDestination(isPresented: Binding(
            get: { selectedBattleId != nil },
            set: { if !$0 { selectedBattleId = nil } }
        )) {
            if let battleId = selectedBattleId {
                battleDetailPageContent(battleId: battleId)
            }
        }
        .fullScreenCover(isPresented: Binding(
            get: { replayNavState != nil },
            set: { if !$0 { replayNavState = nil } }
        )) {
            if let navState = replayNavState {
                ReplayOverlay(navState: navState, onDismiss: { replayNavState = nil })
            }
        }
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
        .navigationDestination(isPresented: Binding(
            get: { topPokemonFormatId != nil },
            set: { if !$0 { topPokemonFormatId = nil } }
        )) {
            if let formatId = topPokemonFormatId {
                ContentListView(
                    repository: repository,
                    mode: .topPokemon(formatId: formatId),
                    favoritesStore: favoritesStore,
                    settingsStore: settingsStore,
                    appConfigStore: appConfigStore
                )
            }
        }
        .onAppear {
            if viewModel.state.items.isEmpty {
                viewModel.loadContent()
            }
        }
        .onChange(of: favoritesStore.favoritePokemonIds) {
            if case .favorites(contentType: .pokemon) = mode {
                viewModel.loadContent()
            }
        }
        .onChange(of: favoritesStore.favoritePlayerNames) {
            if case .favorites(contentType: .players) = mode {
                viewModel.loadContent()
            }
        }
    }

    @ViewBuilder
    private func contentItemView(_ item: ContentListItem) -> some View {
        switch onEnum(of: item) {
        case .battle(let battleItem):
            BattleCardView(uiModel: battleItem.uiModel, showWinnerHighlight: settingsStore.showWinnerHighlight) {
                selectedBattleId = battleItem.uiModel.id
            }
        case .pokemon(let pokemonItem):
            Button {
                let typeUrls = pokemonItem.types.compactMap { $0.imageUrl }
                let derivedFormatId: Int32? = {
                    switch mode {
                    case .home: return viewModel.selectedFormatId
                    case .topPokemon: return viewModel.selectedFormatId
                    case .search(let params): return params.formatId
                    case .pokemon: return viewModel.selectedFormatId
                    case .player: return viewModel.selectedFormatId
                    default: return nil
                    }
                }()
                pokemonNavTarget = PokemonNavTarget(
                    id: pokemonItem.id,
                    name: pokemonItem.name,
                    imageUrl: pokemonItem.imageUrl,
                    typeImageUrl1: typeUrls.first,
                    typeImageUrl2: typeUrls.count > 1 ? typeUrls[1] : nil,
                    formatId: derivedFormatId
                )
            } label: {
                HStack(spacing: 12) {
                    PokemonAvatar(
                        imageUrl: pokemonItem.imageUrl,
                        circleSize: 40,
                        spriteSize: 56
                    )
                    VStack(alignment: .leading, spacing: 0) {
                        Text(pokemonItem.name)
                            .font(.body)
                            .fontWeight(.medium)
                            .foregroundColor(Color(.label))
                        if let pct = pokemonItem.usagePercent {
                            Text(pct)
                                .font(.subheadline)
                                .foregroundColor(Color(.label).opacity(0.75))
                        }
                    }
                    Spacer()
                    TypeIconRow(types: pokemonItem.types.map { (name: $0.name, imageUrl: $0.imageUrl) })
                }
                .contentShape(Rectangle())
                .padding(.vertical, 4)
                .padding(.horizontal, 12)
                .background(Color(.systemBackground))
                .cornerRadius(AppTokens.cardCornerRadius)
                .overlay(
                    RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                        .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
                )
            }
            .buttonStyle(PressableButtonStyle())
        case .player(let playerItem):
            Button {
                let derivedFormatId: Int32? = {
                    switch mode {
                    case .home: return viewModel.selectedFormatId
                    case .topPokemon: return viewModel.selectedFormatId
                    case .search(let params): return params.formatId
                    case .pokemon: return viewModel.selectedFormatId
                    case .player: return viewModel.selectedFormatId
                    default: return nil
                    }
                }()
                playerNavTarget = PlayerNavTarget(id: playerItem.id, name: playerItem.name, formatId: derivedFormatId)
            } label: {
                HStack {
                    Text(playerItem.name)
                        .font(.body)
                        .fontWeight(.medium)
                    Spacer()
                }
                .padding(16)
                .background(Color(.systemBackground))
                .cornerRadius(AppTokens.cardCornerRadius)
                .overlay(
                    RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                        .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
                )
            }
            .buttonStyle(PressableButtonStyle())
        case .highlightButtons(let buttonsItem):
            let buttons = buttonsItem.buttons as! [ContentListItem.HighlightButton]
            HStack(spacing: 8) {
                ForEach(Array(buttons.enumerated()), id: \.offset) { _, button in
                    Button {
                        selectedBattleId = button.battleId
                    } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(button.label)
                                    .font(.subheadline)
                                    .fontWeight(.semibold)
                                    .foregroundColor(Color(.label))
                                Text(String(button.rating))
                                    .font(.subheadline)
                                    .fontWeight(.regular)
                                    .foregroundColor(Color(.label))
                            }
                            Spacer()
                            Image(systemName: "chevron.right")
                                .font(.subheadline)
                                .foregroundColor(Color(.secondaryLabel))
                        }
                        .padding(12)
                    }
                    .background(Color(.systemBackground))
                    .cornerRadius(AppTokens.cardCornerRadius)
                    .overlay(
                        RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                            .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
                    )
                }
            }
        case .pokemonGrid(let gridItem):
            let allPokemon = gridItem.pokemon as! [ContentListItem.PokemonGridItem]
            let columns = horizontalSizeClass == .regular ? max(min(allPokemon.count, 6), 1) : 3
            VStack(spacing: 8) {
                ForEach(Array(stride(from: 0, to: allPokemon.count, by: columns).enumerated()), id: \.offset) { _, startIndex in
                    let endIndex = min(startIndex + columns, allPokemon.count)
                    let rowItems = Array(allPokemon[startIndex..<endIndex])
                    HStack(spacing: 8) {
                        ForEach(Array(rowItems.enumerated()), id: \.element.id) { _, pokemon in
                            Button {
                                pokemonNavTarget = PokemonNavTarget(
                                    id: pokemon.id,
                                    name: pokemon.name,
                                    imageUrl: pokemon.imageUrl,
                                    typeImageUrl1: nil,
                                    typeImageUrl2: nil,
                                    formatId: viewModel.selectedFormatId
                                )
                            } label: {
                                VStack(spacing: 0) {
                                    FillPokemonAvatar(imageUrl: pokemon.imageUrl)
                                    Text(pokemon.name)
                                        .font(.caption2)
                                        .fontWeight(.medium)
                                        .foregroundColor(Color(.label))
                                        .lineLimit(1)
                                        .truncationMode(.tail)
                                    if let pct = pokemon.usagePercent {
                                        Text(pct)
                                            .font(.caption2)
                                            .foregroundColor(Color(.label).opacity(0.75))
                                            .lineLimit(1)
                                    }
                                }
                                .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.plain)
                        }
                        ForEach(0..<(columns - rowItems.count), id: \.self) { _ in
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
            .padding(12)
            .background(Color(.systemBackground))
            .cornerRadius(AppTokens.cardCornerRadius)
            .overlay(
                RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                    .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
            )
        case .statChipRow(let chipRow):
            let allChips = chipRow.chips as! [ContentListItem.StatChipItem]
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(Array(allChips.enumerated()), id: \.element.name) { _, chip in
                        if let pokemonId = chip.pokemonId {
                            Button {
                                pokemonNavTarget = PokemonNavTarget(
                                    id: pokemonId.int32Value,
                                    name: chip.name,
                                    imageUrl: chip.imageUrl,
                                    typeImageUrl1: nil,
                                    typeImageUrl2: nil,
                                    formatId: viewModel.selectedFormatId
                                )
                            } label: {
                                statChipBody(chip: chip)
                            }
                            .buttonStyle(.plain)
                        } else {
                            statChipBody(chip: chip)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        case .section:
            EmptyView()
        case .sectionGroup:
            EmptyView()
        case .formatSelector:
            EmptyView()
        case .searchField:
            EmptyView()
        }
    }

    @ViewBuilder
    private func statChipBody(chip: ContentListItem.StatChipItem) -> some View {
        HStack(spacing: 8) {
            if chip.pokemonId != nil {
                PokemonAvatar(imageUrl: chip.imageUrl, circleSize: 20, spriteSize: 32)
            } else if let imageUrl = chip.imageUrl, let url = URL(string: imageUrl) {
                AsyncImage(url: url) { image in
                    image.resizable().scaledToFit()
                } placeholder: {
                    Color.clear
                }
                .frame(width: 24, height: 24)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(chip.name)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(Color(.label))
                    .lineLimit(1)
                if let pct = chip.usagePercent {
                    Text(pct)
                        .font(.caption2)
                        .foregroundColor(Color(.label).opacity(0.75))
                        .lineLimit(1)
                }
            }
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(Color(.systemBackground))
        .cornerRadius(AppTokens.cardCornerRadius)
        .overlay(
            RoundedRectangle(cornerRadius: AppTokens.cardCornerRadius)
                .stroke(Color(.opaqueSeparator), lineWidth: AppTokens.standardBorderWidth)
        )
    }

    private func refreshContent() async {
        viewModel.refresh()
        // Wait for refresh to complete
        while viewModel.state.isRefreshing {
            try? await Task.sleep(nanoseconds: 100_000_000)
        }
    }
}

#Preview {
    let container = DependencyContainer()
    return NavigationStack {
        ContentListView(repository: container.battleRepository, favoritesStore: container.favoritesStore, settingsStore: container.settingsStore)
    }
}

/// Replaces each `ContentListItem.SectionGroup` with its inner sections in place.
/// iPhone and current iPad layouts render each section vertically as before; a
/// future iPad multi-column layout can call the grouped path directly instead.
private func unwrapSectionGroups(_ items: [ContentListItem]) -> [ContentListItem] {
    items.flatMap { item -> [ContentListItem] in
        if let group = item as? ContentListItem.SectionGroup {
            return group.sections as! [ContentListItem]
        }
        return [item]
    }
}

import SwiftUI
import Shared

struct PokemonNavTarget: Hashable {
    let id: Int32
    let name: String
    let imageUrl: String?
    let typeImageUrl1: String?
    let typeImageUrl2: String?
    let formatId: Int32?

    init(id: Int32, name: String, imageUrl: String?, typeImageUrl1: String?, typeImageUrl2: String?, formatId: Int32? = nil) {
        self.id = id
        self.name = name
        self.imageUrl = imageUrl
        self.typeImageUrl1 = typeImageUrl1
        self.typeImageUrl2 = typeImageUrl2
        self.formatId = formatId
    }
}

struct PlayerNavTarget: Hashable {
    let id: Int32
    let name: String
    let formatId: Int32?

    init(id: Int32, name: String, formatId: Int32? = nil) {
        self.id = id
        self.name = name
        self.formatId = formatId
    }
}

private let paginationThreshold = 5

private struct SearchFilterChipData {
    let index: Int
    let name: String
    let imageUrl: String?
    let itemName: String?
    let teraTypeImageUrl: String?
}

private struct SearchFiltersData {
    let pokemonChips: [SearchFilterChipData]
    let formatName: String?
    let minimumRating: Int32?
    let maximumRating: Int32?
    let unratedOnly: Bool
    let playerName: String?
    let timeRangeStart: Int64?
    let timeRangeEnd: Int64?
}

private enum ContentListHeader {
    case none
    case homeHero
    case favoritesHero
    case pokemonHero(name: String, imageUrl: String?, typeImageUrls: [String])
    case playerHero(name: String)
    case searchFilters(SearchFiltersData)

    init(mode: ContentListMode) {
        switch mode {
        case .home:
            self = .homeHero
        case .favorites:
            self = .favoritesHero
        case .pokemon(_, let name, let imageUrl, let typeImageUrl1, let typeImageUrl2, _):
            let typeUrls = [typeImageUrl1, typeImageUrl2].compactMap { $0 }
            self = .pokemonHero(name: name, imageUrl: imageUrl, typeImageUrls: typeUrls)
        case .player(_, let name, _):
            self = .playerHero(name: name)
        case .search(let params):
            let chips = params.filters.enumerated().map { index, slot in
                SearchFilterChipData(
                    index: index,
                    name: slot.pokemonName,
                    imageUrl: slot.pokemonImageUrl,
                    itemName: slot.itemName,
                    teraTypeImageUrl: slot.teraTypeImageUrl
                )
            }
            self = .searchFilters(SearchFiltersData(
                pokemonChips: chips,
                formatName: params.formatName,
                minimumRating: params.minimumRating.map { $0.int32Value > 0 ? $0.int32Value : nil } ?? nil,
                maximumRating: params.maximumRating.map { $0.int32Value > 0 ? $0.int32Value : nil } ?? nil,
                unratedOnly: params.unratedOnly,
                playerName: params.playerName,
                timeRangeStart: params.timeRangeStart?.int64Value,
                timeRangeEnd: params.timeRangeEnd?.int64Value
            ))
        }
    }

    var hasPokemonHero: Bool {
        if case .pokemonHero = self { return true }
        return false
    }

    var hasSearchFilters: Bool {
        if case .searchFilters = self { return true }
        return false
    }
}

struct ContentListView: View {
    @StateObject private var viewModel: ContentListViewModel
    @State private var selectedBattleId: Int32? = nil
    @State private var pokemonNavTarget: PokemonNavTarget? = nil
    @State private var playerNavTarget: PlayerNavTarget? = nil
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

    /// Live format items from the catalog store, available for the dropdown and child navigation.
    private var formatItems: [FormatUiModel] {
        container.catalogStore.formatItems
    }

    init(repository: BattleRepository, mode: ContentListMode = .home, favoritesStore: FavoritesStore, settingsStore: SettingsStore, pokemonCatalogItems: [PokemonPickerUiModel] = [], appConfigStore: AppConfigStore? = nil, formatItems: [FormatUiModel] = [], onSearchParamsChanged: ((SearchParams) -> Void)? = nil, initialBattleId: Int32? = nil) {
        self.repository = repository
        self.mode = mode
        self.favoritesStore = favoritesStore
        self.settingsStore = settingsStore
        self.appConfigStore = appConfigStore
        self.onSearchParamsChanged = onSearchParamsChanged
        _viewModel = StateObject(wrappedValue: ContentListViewModel(repository: repository, mode: mode, favoritesStore: favoritesStore, pokemonCatalogItems: pokemonCatalogItems, appConfigStore: appConfigStore, formatItems: formatItems))
        _selectedBattleId = State(initialValue: initialBattleId)
    }

    private func buildShareUrl(battleId: Int32? = nil) -> String {
        let bid: KotlinInt? = battleId.map { KotlinInt(int: $0) }
        return ShareUrlBuilderKt.shareUrlForMode(mode: viewModel.mode.toSharedMode(), battleId: bid)
    }

    private var showShareButton: Bool {
        switch mode {
        case .home, .favorites: return false
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
    private func battleDetailSheetContent(battleId: Int32) -> some View {
        let selectedBattle: BattleCardUiModel? = findBattle(in: viewModel.state.items, id: battleId)
        BattleDetailSheet(
            repository: repository,
            battleId: battleId,
            player1IsWinner: selectedBattle?.player1.isWinner,
            player2IsWinner: selectedBattle?.player2.isWinner,
            favoritesStore: favoritesStore,
            showWinnerHighlight: settingsStore.showWinnerHighlight,
            shareUrl: buildShareUrl(battleId: battleId),
            onDismiss: { selectedBattleId = nil },
            onPokemonClick: { id, name, imageUrl, typeImageUrls, formatId in
                pokemonNavTarget = PokemonNavTarget(id: id, name: name, imageUrl: imageUrl, typeImageUrl1: typeImageUrls.first, typeImageUrl2: typeImageUrls.count > 1 ? typeImageUrls[1] : nil, formatId: formatId)
            },
            onPlayerClick: { id, name, formatId in
                playerNavTarget = PlayerNavTarget(id: id, name: name, formatId: formatId)
            }
        )
    }

    var body: some View {
        let header = ContentListHeader(mode: viewModel.mode)

        GeometryReader { geometry in
            ScrollView {
                LazyVStack(spacing: 12) {
                    if case .homeHero = header {
                        VStack(spacing: 0) {
                            Text("ARC")
                                .font(.system(size: 34, weight: .bold))
                            Text("Today's Top Battles")
                                .font(.system(size: 20, weight: .semibold))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 24)
                        .padding(.bottom, 8)
                    }

                    if case .favoritesHero = header {
                        VStack(spacing: 4) {
                            Image(systemName: "heart.fill")
                                .font(.system(size: 40))
                                .foregroundColor(Color(.label))
                            Text("Favorites")
                                .font(.system(size: 20, weight: .semibold))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 24)
                        .padding(.bottom, 8)
                    }

                    if case .pokemonHero(let name, let imageUrl, let typeImageUrls) = header {
                        VStack(spacing: 0) {
                            if imageUrl != nil {
                                PokemonAvatar(
                                    imageUrl: imageUrl,
                                    circleSize: 158,
                                    spriteSize: 227
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
                    }

                    if case .playerHero(let name) = header {
                        VStack(spacing: 0) {
                            Text(name)
                                .font(.system(size: 18, weight: .bold))
                                .foregroundColor(Color(.label))
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(Color(.systemBackground))
                                .cornerRadius(16)
                        }
                        .frame(maxWidth: .infinity)
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
                    }

                    if viewModel.state.isLoading {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                            .frame(height: geometry.size.height * 0.6)
                    } else if viewModel.state.error != nil, viewModel.state.items.isEmpty {
                        ErrorView {
                            viewModel.loadContent()
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: geometry.size.height * 0.6)
                    } else if viewModel.state.items.isEmpty {
                        BattleEmptyView()
                            .frame(maxWidth: .infinity)
                            .frame(height: geometry.size.height * 0.6)
                    } else {
                        ForEach(Array(viewModel.state.items.enumerated()), id: \.element.listKey) { index, item in
                            switch onEnum(of: item) {
                            case .section(let section):
                                let showSort = section.header == "Battles" && hasSortToggle
                                let isLoadingSection = viewModel.state.loadingSections.contains(section.header)
                                SectionHeaderView(
                                    title: section.header,
                                    isLoading: isLoadingSection,
                                    sortOrder: showSort ? viewModel.sortOrder : nil,
                                    onToggleSortOrder: showSort ? { viewModel.toggleSortOrder() } : nil
                                )
                                if section.items.isEmpty && !isLoadingSection {
                                    BattleEmptyView()
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 32)
                                }
                                ForEach(Array((section.items as! [ContentListItem]).enumerated()), id: \.element.listKey) { _, child in
                                    contentItemView(child)
                                        .opacity(isLoadingSection ? 0.5 : 1.0)
                                }
                            case .formatSelector:
                                if !formatItems.isEmpty {
                                    HStack {
                                        Spacer()
                                        FormatDropdown(
                                            formats: formatItems,
                                            selectedFormatId: viewModel.selectedFormatId,
                                            onFormatSelected: { viewModel.selectFormat($0) }
                                        )
                                        Spacer()
                                    }
                                }
                            default:
                                contentItemView(item)
                                    .onAppear {
                                        if index >= viewModel.state.items.count - paginationThreshold {
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
                .padding(.horizontal, 16)
                .padding(.top, header.hasPokemonHero ? 4 : 0)
                .padding(.bottom, 16)
            }
            .refreshable {
                await refreshContent()
            }
        }
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { toolbarContent }
        .background(Color(.secondarySystemBackground))
        .sheet(isPresented: Binding(
            get: { selectedBattleId != nil && pokemonNavTarget == nil && playerNavTarget == nil },
            set: { if !$0 && pokemonNavTarget == nil && playerNavTarget == nil { selectedBattleId = nil } }
        )) {
            if let battleId = selectedBattleId {
                battleDetailSheetContent(battleId: battleId)
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
            SimplePokemonRow(
                imageUrl: pokemonItem.imageUrl,
                name: pokemonItem.name,
                types: pokemonItem.types.map { (name: $0.name, imageUrl: $0.imageUrl) },
                circleSize: 40,
                spriteSize: 56,
                fontWeight: .medium
            )
            .padding(.vertical, 4)
            .padding(.horizontal, 12)
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .onTapGesture {
                let typeUrls = pokemonItem.types.compactMap { $0.imageUrl }
                let derivedFormatId: Int32? = {
                    switch mode {
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
            }
        case .player(let playerItem):
            HStack {
                Text(playerItem.name)
                    .font(.body)
                    .fontWeight(.medium)
                Spacer()
            }
            .padding(16)
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .onTapGesture {
                let derivedFormatId: Int32? = {
                    switch mode {
                    case .search(let params): return params.formatId
                    case .pokemon: return viewModel.selectedFormatId
                    case .player: return viewModel.selectedFormatId
                    default: return nil
                    }
                }()
                playerNavTarget = PlayerNavTarget(id: playerItem.id, name: playerItem.name, formatId: derivedFormatId)
            }
        case .highlightButtons(let buttonsItem):
            let buttons = buttonsItem.buttons as! [ContentListItem.HighlightButton]
            HStack(spacing: 8) {
                ForEach(Array(buttons.enumerated()), id: \.offset) { _, button in
                    Button {
                        selectedBattleId = button.battleId
                    } label: {
                        VStack(spacing: 2) {
                            Text(button.label)
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(Color(.label))
                            Text(String(button.rating))
                                .font(.subheadline)
                                .fontWeight(.regular)
                                .foregroundColor(Color(.label))
                        }
                        .frame(maxWidth: .infinity)
                        .padding(12)
                    }
                    .background(Color(.systemGray5))
                    .cornerRadius(12)
                }
            }
        case .pokemonGrid(let gridItem):
            let allPokemon = gridItem.pokemon as! [ContentListItem.PokemonGridItem]
            VStack(spacing: 8) {
                ForEach(Array(stride(from: 0, to: allPokemon.count, by: 3).enumerated()), id: \.offset) { _, startIndex in
                    let endIndex = min(startIndex + 3, allPokemon.count)
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
                                        .foregroundColor(Color(.label))
                                        .lineLimit(1)
                                        .truncationMode(.tail)
                                    if let pct = pokemon.usagePercent {
                                        Text(pct)
                                            .font(.caption2)
                                            .foregroundColor(Color(.secondaryLabel))
                                            .lineLimit(1)
                                    }
                                }
                                .frame(maxWidth: .infinity)
                            }
                            .buttonStyle(.plain)
                        }
                        ForEach(0..<(3 - rowItems.count), id: \.self) { _ in
                            Color.clear.frame(maxWidth: .infinity)
                        }
                    }
                }
            }
            .padding(12)
            .background(Color(.systemBackground))
            .cornerRadius(12)
        case .statChipRow(let chipRow):
            let allChips = chipRow.chips as! [ContentListItem.StatChipItem]
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(Array(allChips.enumerated()), id: \.element.name) { _, chip in
                        HStack(spacing: 8) {
                            if let imageUrl = chip.imageUrl, let url = URL(string: imageUrl) {
                                AsyncImage(url: url) { image in
                                    image.resizable().scaledToFit()
                                } placeholder: {
                                    Color.clear
                                }
                                .frame(width: 24, height: 24)
                            }
                            VStack(alignment: chip.imageUrl != nil ? .leading : .center, spacing: 2) {
                                Text(chip.name)
                                    .font(.subheadline)
                                    .foregroundColor(Color(.label))
                                    .lineLimit(1)
                                if let pct = chip.usagePercent {
                                    Text(pct)
                                        .font(.caption2)
                                        .foregroundColor(Color(.secondaryLabel))
                                        .lineLimit(1)
                                }
                            }
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 8)
                        .background(Color(.systemBackground))
                        .cornerRadius(12)
                    }
                }
            }
        case .section:
            EmptyView()
        case .formatSelector:
            EmptyView()
        }
    }

    private func refreshContent() async {
        viewModel.refresh()
        // Wait for refresh to complete
        while viewModel.state.isRefreshing {
            try? await Task.sleep(nanoseconds: 100_000_000)
        }
    }
}

private let filterChipHeight: CGFloat = 44

private struct SearchFilterChipsView: View {
    let filters: SearchFiltersData
    let searchParams: SearchParams?
    let onSearchParamsChanged: ((SearchParams) -> Void)?

    var body: some View {
        WrappingHStack(horizontalSpacing: 4, verticalSpacing: 4) {
            if let formatName = filters.formatName {
                Text(formatName)
                    .font(.system(size: 14))
                    .foregroundColor(Color(.label))
                    .padding(.horizontal, 6)
                    .frame(height: filterChipHeight)
                    .background(Color(.systemGray5))
                    .cornerRadius(4)
            }

            ForEach(Array(filters.pokemonChips.enumerated()), id: \.offset) { _, chip in
                let label = {
                    var text = chip.name
                    if let itemName = chip.itemName { text += " @ \(itemName)" }
                    return text
                }()
                let canRemove = searchParams?.canRemovePokemonAt(index: Int32(chip.index)) ?? false
                HStack(spacing: 2) {
                    if let url = chip.teraTypeImageUrl {
                        AsyncImage(url: URL(string: url)) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                            default:
                                Color.clear
                            }
                        }
                        .frame(width: 27, height: 27)
                    }
                    if let url = chip.imageUrl {
                        AsyncImage(url: URL(string: url)) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                            default:
                                Color.clear
                            }
                        }
                        .frame(width: 40, height: 40)
                    }
                    Text(label)
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removePokemonAt(index: Int32(chip.index)))
                        }
                    }
                }
                .padding(.leading, 4)
                .padding(.trailing, canRemove ? 0 : 4)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(4)
            }

            if let rating = filters.minimumRating {
                let canRemove = searchParams?.canRemoveMinRating() ?? false
                HStack(spacing: 0) {
                    Text("\(String(rating))+")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeMinRating())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(4)
            }

            if let rating = filters.maximumRating {
                let canRemove = searchParams?.canRemoveMaxRating() ?? false
                HStack(spacing: 0) {
                    Text("\(String(rating))-")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeMaxRating())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(4)
            }

            if filters.unratedOnly {
                let canRemove = searchParams?.canRemoveUnrated() ?? false
                HStack(spacing: 0) {
                    Text("Unrated")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeUnrated())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(4)
            }

            if let name = filters.playerName {
                let canRemove = searchParams?.canRemovePlayerName() ?? false
                HStack(spacing: 0) {
                    Text(name)
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removePlayerName())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(4)
            }

            if filters.timeRangeStart != nil || filters.timeRangeEnd != nil {
                let formatter = DateFormatter()
                let _ = formatter.dateFormat = "MM/dd/yy"
                let startStr = filters.timeRangeStart.map { formatter.string(from: Date(timeIntervalSince1970: Double($0))) } ?? "..."
                let endStr = filters.timeRangeEnd.map { formatter.string(from: Date(timeIntervalSince1970: Double($0))) } ?? "..."
                let canRemove = searchParams?.canRemoveTimeRange() ?? false
                HStack(spacing: 0) {
                    Text("\(startStr) – \(endStr)")
                        .font(.system(size: 14))
                        .foregroundColor(Color(.label))
                    if canRemove, let params = searchParams, let callback = onSearchParamsChanged {
                        FilterChipCloseButton {
                            callback(params.removeTimeRange())
                        }
                    }
                }
                .padding(.leading, 6)
                .padding(.trailing, canRemove ? 0 : 6)
                .frame(height: filterChipHeight)
                .background(Color(.systemGray5))
                .cornerRadius(4)
            }

        }
    }
}

private struct FilterChipCloseButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "xmark")
                .font(.system(size: 10, weight: .semibold))
                .foregroundColor(Color(.secondaryLabel))
                .frame(width: 28, height: 28)
        }
    }
}

private struct WrappingHStack: Layout {
    var horizontalSpacing: CGFloat
    var verticalSpacing: CGFloat

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let result = arrangeSubviews(proposal: proposal, subviews: subviews)
        return result.size
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let result = arrangeSubviews(proposal: proposal, subviews: subviews)
        for (index, position) in result.positions.enumerated() {
            subviews[index].place(
                at: CGPoint(x: bounds.minX + position.x, y: bounds.minY + position.y),
                proposal: .unspecified
            )
        }
    }

    private struct ArrangementResult {
        var size: CGSize
        var positions: [CGPoint]
    }

    private func arrangeSubviews(proposal: ProposedViewSize, subviews: Subviews) -> ArrangementResult {
        let maxWidth = proposal.width ?? .infinity
        var positions: [CGPoint] = []
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        var totalHeight: CGFloat = 0
        var totalWidth: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth && x > 0 {
                y += rowHeight + verticalSpacing
                x = 0
                rowHeight = 0
            }
            positions.append(CGPoint(x: x, y: y))
            rowHeight = max(rowHeight, size.height)
            x += size.width + horizontalSpacing
            totalWidth = max(totalWidth, x - horizontalSpacing)
            totalHeight = y + rowHeight
        }

        return ArrangementResult(
            size: CGSize(width: totalWidth, height: totalHeight),
            positions: positions
        )
    }
}

private struct SectionHeaderView: View {
    let title: String
    var isLoading: Bool = false
    var sortOrder: String? = nil
    var onToggleSortOrder: (() -> Void)? = nil

    var body: some View {
        HStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(Color(.secondaryLabel))
            Spacer()
            if let sortOrder = sortOrder, let toggle = onToggleSortOrder {
                SortToggleButton(sortOrder: sortOrder, isLoading: isLoading, action: toggle)
            }
        }
    }
}

private struct SortToggleButton: View {
    let sortOrder: String
    var isLoading: Bool = false
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                if isLoading {
                    ProgressView()
                        .scaleEffect(0.6)
                        .frame(width: 14, height: 14)
                } else {
                    Image(systemName: "arrow.up.arrow.down")
                        .font(.system(size: 10, weight: .semibold))
                }
                Text(sortOrder == "rating" ? "Rating" : "Time")
                    .font(.system(size: 12))
            }
            .foregroundColor(Color(.secondaryLabel))
            .padding(.horizontal, 8)
            .frame(height: 28)
            .background(Color(.systemGray5))
            .cornerRadius(4)
        }
        .disabled(isLoading)
    }
}

private struct FormatDropdown: View {
    let formats: [FormatUiModel]
    let selectedFormatId: Int32
    let onFormatSelected: (Int32) -> Void

    var body: some View {
        let selectedFormat = formats.first { $0.id == selectedFormatId }
        Menu {
            ForEach(Array(formats.enumerated()), id: \.element.id) { _, format in
                Button {
                    onFormatSelected(format.id)
                } label: {
                    if format.id == selectedFormatId {
                        Label(format.displayName, systemImage: "checkmark")
                    } else {
                        Text(format.displayName)
                    }
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(selectedFormat?.displayName ?? "Format")
                    .font(.system(size: 14))
                    .foregroundColor(Color(.secondaryLabel))
                Image(systemName: "chevron.up.chevron.down")
                    .font(.system(size: 10))
                    .foregroundColor(Color(.secondaryLabel))
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(Color(.systemGray6))
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color(.systemGray4), lineWidth: 1)
            )
        }
    }
}

private func findBattle(in items: [ContentListItem], id: Int32) -> BattleCardUiModel? {
    for item in items {
        if let battleItem = item as? ContentListItem.Battle, battleItem.uiModel.id == id {
            return battleItem.uiModel
        }
        if let section = item as? ContentListItem.Section,
           let found = findBattle(in: section.items as! [ContentListItem], id: id) {
            return found
        }
    }
    return nil
}

#Preview {
    let container = DependencyContainer()
    return NavigationStack {
        ContentListView(repository: container.battleRepository, favoritesStore: container.favoritesStore, settingsStore: container.settingsStore)
    }
}

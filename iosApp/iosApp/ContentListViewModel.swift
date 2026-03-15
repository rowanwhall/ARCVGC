import Foundation
import os
import Shared

enum FavoriteContentType {
    case battles, pokemon, players
}

enum ContentListMode {
    case home
    case favorites(contentType: FavoriteContentType)
    case search(params: SearchParams)
    case pokemon(id: Int32, name: String, imageUrl: String?, typeImageUrl1: String?, typeImageUrl2: String?, formatId: Int32? = nil)
    case player(id: Int32, name: String)
}

@MainActor
final class ContentListViewModel: ObservableObject {
    @Published private(set) var state = ContentListState()
    @Published private(set) var sortOrder: String
    @Published private(set) var selectedFormatId: Int32

    private static let logger = Logger(subsystem: Bundle.main.bundleIdentifier ?? "com.arcvgc.app", category: "ContentListViewModel")

    private let repository: BattleRepository
    @Published private(set) var mode: ContentListMode
    private let favoritesStore: FavoritesStore?
    private let pokemonCatalogItems: [PokemonPickerUiModel]
    private let appConfigStore: AppConfigStore?
    let formatItems: [FormatUiModel]

    init(repository: BattleRepository, mode: ContentListMode = .home, favoritesStore: FavoritesStore? = nil, pokemonCatalogItems: [PokemonPickerUiModel] = [], appConfigStore: AppConfigStore? = nil, formatItems: [FormatUiModel] = []) {
        self.repository = repository
        self.mode = mode
        self.favoritesStore = favoritesStore
        self.pokemonCatalogItems = pokemonCatalogItems
        self.appConfigStore = appConfigStore
        self.formatItems = formatItems
        if case .search(let params) = mode {
            self.sortOrder = params.orderBy
        } else {
            self.sortOrder = "time"
        }
        if case .pokemon(_, _, _, _, _, let formatId) = mode {
            self.selectedFormatId = formatId ?? appConfigStore?.defaultFormatId ?? 1
        } else {
            self.selectedFormatId = 0
        }
    }

    private static let defaultPageSize: Int32 = 10

    private func fetchContent(page: Int32 = 1) async throws -> (items: [ContentListItem], pagination: Pagination) {
        switch mode {
        case .home:
            let nowSeconds = Int64(Date().timeIntervalSince1970)
            let formatId = appConfigStore?.defaultFormatId ?? 1
            let result = try await repository.searchMatches(
                filters: [],
                formatId: formatId,
                minimumRating: nil,
                maximumRating: nil,
                unratedOnly: false,
                orderBy: "rating",
                limit: Self.defaultPageSize,
                page: page,
                timeRangeStart: KotlinLong(value: nowSeconds - 86400),
                timeRangeEnd: KotlinLong(value: nowSeconds),
                playerName: nil
            )
            let items = ContentListItemMapper.shared.fromBattles(battles: result.battles)
            return (items: items as! [ContentListItem], pagination: result.pagination)

        case .favorites(let contentType):
            switch contentType {
            case .battles:
                guard let store = favoritesStore else {
                    return (items: [], pagination: Pagination(page: 1, itemsPerPage: 0, totalItems: 0, totalPages: 1))
                }
                let ids = Array(store.favoriteBattleIds)
                if ids.isEmpty {
                    return (items: [], pagination: Pagination(page: 1, itemsPerPage: 0, totalItems: 0, totalPages: 1))
                }
                let battles = try await repository.getMatchesByIds(ids: ids.map { KotlinInt(int: $0) })
                let items = ContentListItemMapper.shared.fromBattles(battles: battles)
                return (items: items as! [ContentListItem], pagination: Pagination(page: 1, itemsPerPage: Int32(battles.count), totalItems: Int32(battles.count), totalPages: 1))

            case .pokemon:
                guard let store = favoritesStore else {
                    return (items: [], pagination: Pagination(page: 1, itemsPerPage: 0, totalItems: 0, totalPages: 1))
                }
                let ids = Array(store.favoritePokemonIds)
                if ids.isEmpty {
                    return (items: [], pagination: Pagination(page: 1, itemsPerPage: 0, totalItems: 0, totalPages: 1))
                }
                let pokemon = try await repository.getPokemonByIds(ids: ids.map { KotlinInt(int: $0) })
                let items = ContentListItemMapper.shared.fromPokemon(pokemon: pokemon)
                return (items: items as! [ContentListItem], pagination: Pagination(page: 1, itemsPerPage: Int32(pokemon.count), totalItems: Int32(pokemon.count), totalPages: 1))

            case .players:
                guard let store = favoritesStore else {
                    return (items: [], pagination: Pagination(page: 1, itemsPerPage: 0, totalItems: 0, totalPages: 1))
                }
                let names = Array(store.favoritePlayerNames)
                if names.isEmpty {
                    return (items: [], pagination: Pagination(page: 1, itemsPerPage: 0, totalItems: 0, totalPages: 1))
                }
                let players = try await repository.getPlayersByNames(names: names)
                let items = ContentListItemMapper.shared.fromPlayers(players: players)
                return (items: items as! [ContentListItem], pagination: Pagination(page: 1, itemsPerPage: Int32(players.count), totalItems: Int32(players.count), totalPages: 1))
            }

        case .search(let params):
            let playerTask: Task<PlayerListItem?, Never>? = {
                guard page == 1, let playerName = params.playerName else { return nil }
                return Task { try? await repository.getPlayersByNames(names: [playerName]).first }
            }()

            let result = try await repository.searchMatches(
                filters: params.filters,
                formatId: params.formatId,
                minimumRating: params.minimumRating,
                maximumRating: params.maximumRating,
                unratedOnly: params.unratedOnly,
                orderBy: sortOrder,
                limit: Self.defaultPageSize,
                page: page,
                timeRangeStart: params.timeRangeStart,
                timeRangeEnd: params.timeRangeEnd,
                playerName: params.playerName
            )
            let battleItems = ContentListItemMapper.shared.fromBattles(battles: result.battles) as! [ContentListItem]

            if page == 1 {
                let pokemonIds = params.filters.map { KotlinInt(int: $0.pokemonId) }
                let pinnedPokemon = ContentListItemMapper.shared.fromPokemonCatalog(
                    pokemonIds: pokemonIds, catalog: pokemonCatalogItems
                ) as! [ContentListItem]
                var pinnedPlayer: [ContentListItem] = []
                if let task = playerTask, let player = await task.value {
                    pinnedPlayer = [ContentListItem.Player(id: player.id, name: player.name)]
                }
                var sections: [ContentListItem] = []
                if !pinnedPokemon.isEmpty {
                    sections.append(ContentListItem.Section(header: "Pokémon", items: pinnedPokemon))
                }
                if !pinnedPlayer.isEmpty {
                    sections.append(ContentListItem.Section(header: "Players", items: pinnedPlayer))
                }
                if !battleItems.isEmpty {
                    sections.append(ContentListItem.Section(header: "Battles", items: battleItems))
                }
                return (items: sections, pagination: result.pagination)
            }
            return (items: battleItems, pagination: result.pagination)

        case .pokemon(let id, _, _, _, _, _):
            let result = try await repository.searchMatches(
                filters: [SearchFilterSlot(pokemonId: id, itemId: nil, teraTypeId: nil, pokemonName: "", pokemonImageUrl: nil, itemName: nil, teraTypeImageUrl: nil)],
                formatId: selectedFormatId,
                minimumRating: nil,
                maximumRating: nil,
                unratedOnly: false,
                orderBy: sortOrder,
                limit: Self.defaultPageSize,
                page: page,
                timeRangeStart: nil,
                timeRangeEnd: nil,
                playerName: nil
            )
            let battleItems = ContentListItemMapper.shared.fromBattles(battles: result.battles) as! [ContentListItem]
            if page == 1 {
                let sections: [ContentListItem] = battleItems.isEmpty ? [] : [ContentListItem.Section(header: "Battles", items: battleItems)]
                return (items: sections, pagination: result.pagination)
            }
            return (items: battleItems, pagination: result.pagination)

        case .player(let id, let name):
            if page == 1 {
                async let profileResult = { try? await repository.getPlayerProfile(id: id) }()
                async let battlesResult = repository.searchMatches(
                    filters: [],
                    formatId: 1,
                    minimumRating: nil,
                    maximumRating: nil,
                    unratedOnly: false,
                    orderBy: sortOrder,
                    limit: Self.defaultPageSize,
                    page: page,
                    timeRangeStart: nil,
                    timeRangeEnd: nil,
                    playerName: name
                )

                let result = try await battlesResult
                let profile = await profileResult
                let battleItems = ContentListItemMapper.shared.fromBattles(battles: result.battles) as! [ContentListItem]

                var sections: [ContentListItem] = []

                if let profile = profile {
                    var highlightButtons: [ContentListItem.HighlightButton] = []
                    if let topRated = profile.topRatedMatch {
                        highlightButtons.append(ContentListItem.HighlightButton(label: "Top Rated Battle", rating: topRated.rating, battleId: topRated.id))
                    }
                    if let mostRecent = profile.mostRecentRatedMatch {
                        highlightButtons.append(ContentListItem.HighlightButton(label: "Latest Rated Battle", rating: mostRecent.rating, battleId: mostRecent.id))
                    }
                    if !highlightButtons.isEmpty {
                        sections.append(ContentListItem.HighlightButtons(buttons: highlightButtons))
                    }

                    if !profile.mostUsedPokemon.isEmpty {
                        let gridItems = profile.mostUsedPokemon.map {
                            ContentListItem.PokemonGridItem(id: $0.id, name: $0.name, imageUrl: $0.imageUrl)
                        }
                        sections.append(ContentListItem.Section(header: "Favorite Pokémon", items: [ContentListItem.PokemonGrid(pokemon: gridItems)]))
                    }
                }

                if !battleItems.isEmpty {
                    sections.append(ContentListItem.Section(header: "Battles", items: battleItems))
                }

                return (items: sections, pagination: result.pagination)
            } else {
                let result = try await repository.searchMatches(
                    filters: [],
                    formatId: 1,
                    minimumRating: nil,
                    maximumRating: nil,
                    unratedOnly: false,
                    orderBy: sortOrder,
                    limit: Self.defaultPageSize,
                    page: page,
                    timeRangeStart: nil,
                    timeRangeEnd: nil,
                    playerName: name
                )
                let items = ContentListItemMapper.shared.fromBattles(battles: result.battles)
                return (items: items as! [ContentListItem], pagination: result.pagination)
            }
        }
    }

    func selectFormat(_ formatId: Int32) {
        guard selectedFormatId != formatId else { return }
        selectedFormatId = formatId
        state.loadingSections = Set(["Battles"])
        state.currentPage = 1
        state.canPaginate = false

        Task {
            do {
                let result = try await fetchContent()
                state.items = result.items
                state.currentPage = result.pagination.page
                state.canPaginate = result.pagination.page < result.pagination.totalPages
            } catch {
                Self.logger.error("Failed to select format: \(error.localizedDescription)")
            }
            state.loadingSections = []
        }
    }

    func updateSearchParams(_ params: SearchParams) {
        mode = .search(params: params)
        sortOrder = params.orderBy
        state = ContentListState()
        loadContent()
    }

    func loadContent() {
        guard !state.isLoading else { return }

        state.isLoading = true
        state.error = nil

        Task {
            if case .home = mode, let store = appConfigStore, store.config == nil {
                // Wait for config to load before fetching Home content
                for await config in store.$config.values {
                    if config != nil { break }
                }
            }

            do {
                let result = try await fetchContent()
                state.items = result.items
                state.currentPage = result.pagination.page
                state.canPaginate = result.pagination.page < result.pagination.totalPages
                state.error = nil
            } catch {
                Self.logger.error("Failed to load content: \(error.localizedDescription)")
                state.error = error.localizedDescription
            }
            state.isLoading = false
        }
    }

    func refresh() {
        guard !state.isRefreshing else { return }

        state.isRefreshing = true
        state.error = nil

        Task {
            do {
                let result = try await fetchContent(page: 1)
                state.items = result.items
                state.currentPage = result.pagination.page
                state.canPaginate = result.pagination.page < result.pagination.totalPages
                state.error = nil
            } catch {
                Self.logger.error("Failed to refresh content: \(error.localizedDescription)")
                state.error = error.localizedDescription
            }
            state.isRefreshing = false
        }
    }

    func toggleSortOrder() {
        sortOrder = sortOrder == "time" ? "rating" : "time"
        state.loadingSections = Set(["Battles"])
        state.currentPage = 1
        state.canPaginate = false

        Task {
            do {
                let result = try await fetchContent()
                state.items = result.items
                state.currentPage = result.pagination.page
                state.canPaginate = result.pagination.page < result.pagination.totalPages
            } catch {
                Self.logger.error("Failed to toggle sort order: \(error.localizedDescription)")
            }
            state.loadingSections = []
        }
    }

    func paginate() {
        guard !state.isPaginating, state.canPaginate, state.loadingSections.isEmpty else { return }

        state.isPaginating = true

        let nextPage = state.currentPage + 1
        Task {
            do {
                let result = try await fetchContent(page: nextPage)
                let existingKeys = Set(state.items.map { $0.listKey })
                let newItems = result.items.filter { !existingKeys.contains($0.listKey) }
                state.items.append(contentsOf: newItems)
                state.currentPage = result.pagination.page
                state.canPaginate = result.pagination.page < result.pagination.totalPages
            } catch {
                Self.logger.error("Failed to paginate content (page \(nextPage)): \(error.localizedDescription)")
            }
            state.isPaginating = false
        }
    }
}

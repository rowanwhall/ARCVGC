import Foundation
import Shared

enum FavoriteContentType {
    case battles, pokemon, players
}

enum ContentListMode {
    case home
    case favorites(contentType: FavoriteContentType)
    case search(params: SearchParams)
    case pokemon(id: Int32, name: String, imageUrl: String?, typeImageUrl1: String?, typeImageUrl2: String?, formatId: Int32? = nil)
    case player(id: Int32, name: String, formatId: Int32? = nil)
    case topPokemon(formatId: Int32? = nil)

    func toSharedMode() -> Shared.ContentListMode {
        switch self {
        case .home:
            return Shared.ContentListMode.Home.shared
        case .favorites(let contentType):
            let sharedType: Shared.FavoriteContentType = {
                switch contentType {
                case .battles: return .battles
                case .pokemon: return .pokemon
                case .players: return .players
                }
            }()
            return Shared.ContentListMode.Favorites(contentType: sharedType)
        case .search(let params):
            return Shared.ContentListMode.Search(params: params)
        case .pokemon(let id, let name, let imageUrl, let typeImageUrl1, let typeImageUrl2, let formatId):
            return Shared.ContentListMode.Pokemon(
                pokemonId: id,
                name: name,
                imageUrl: imageUrl,
                typeImageUrl1: typeImageUrl1,
                typeImageUrl2: typeImageUrl2,
                formatId: formatId.map { KotlinInt(int: $0) }
            )
        case .player(let id, let name, let formatId):
            return Shared.ContentListMode.Player(
                playerId: id,
                playerName: name,
                formatId: formatId.map { KotlinInt(int: $0) }
            )
        case .topPokemon(let formatId):
            return Shared.ContentListMode.TopPokemon(
                formatId: formatId.map { KotlinInt(int: $0) }
            )
        }
    }
}

@MainActor
final class ContentListViewModel: ObservableObject {
    @Published private(set) var state: ContentListUiState
    @Published private(set) var sortOrder: String
    @Published private(set) var selectedFormatId: Int32
    @Published private(set) var searchQuery: String
    @Published private(set) var mode: ContentListMode

    let formatItems: [FormatUiModel]

    private let logic: ContentListLogic
    private let logicScope: Kotlinx_coroutines_coreCoroutineScope
    private var observationTasks: [Task<Void, Never>] = []

    init(repository: BattleRepository, mode: ContentListMode = .home, favoritesStore: FavoritesStore? = nil, pokemonCatalogItems: [PokemonPickerUiModel] = [], appConfigStore: AppConfigStore? = nil, formatItems: [FormatUiModel] = []) {
        self.mode = mode
        self.formatItems = formatItems

        let scope = CoroutineScopeFactory.shared.createMainScope()
        self.logicScope = scope

        let sharedMode = mode.toSharedMode()
        self.logic = ContentListLogic(
            scope: scope,
            repository: repository,
            favoritesRepository: favoritesStore?.repo ?? FavoritesRepository(storage: FavoritesStorage()),
            appConfigRepository: appConfigStore?.repo ?? AppConfigRepository(
                apiService: ApiService(),
                storage: AppConfigStorage(),
                catalogCacheStorage: CatalogCacheStorage()
            ),
            mode: sharedMode,
            pokemonCatalogItems: pokemonCatalogItems,
            pokemonCatalogState: nil,
            initialTopPokemonFetchCount: 6
        )

        self.state = logic.uiState.value
        self.sortOrder = logic.sortOrder.value as String
        self.selectedFormatId = (logic.selectedFormatId.value as! KotlinInt).int32Value
        self.searchQuery = logic.searchQuery.value as String

        observationTasks.append(Task { [weak self] in
            for await uiState in logic.uiState {
                self?.state = uiState
            }
        })
        observationTasks.append(Task { [weak self] in
            for await order in logic.sortOrder {
                self?.sortOrder = order as String
            }
        })
        observationTasks.append(Task { [weak self] in
            for await id in logic.selectedFormatId {
                self?.selectedFormatId = (id as! KotlinInt).int32Value
            }
        })
        observationTasks.append(Task { [weak self] in
            for await query in logic.searchQuery {
                self?.searchQuery = query as String
            }
        })

        logic.initialize()
    }

    func loadContent() { logic.loadContent() }
    func refresh() { logic.refresh() }
    func paginate() { logic.paginate() }
    func selectFormat(_ formatId: Int32) { logic.selectFormat(formatId: formatId) }
    func toggleSortOrder() { logic.toggleSortOrder() }
    func setSearchQuery(_ query: String) { logic.setSearchQuery(query: query) }

    func updateSearchParams(_ params: SearchParams) {
        mode = .search(params: params)
        logic.updateSearchParams(params: params)
    }

    deinit {
        observationTasks.forEach { $0.cancel() }
        CoroutineScopeFactory.shared.cancel(scope: logicScope)
    }
}

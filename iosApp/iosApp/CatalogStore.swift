import Foundation
import Shared

@MainActor
final class CatalogStore: ObservableObject {
    @Published private(set) var pokemonLoading = true
    @Published private(set) var pokemonItems: [PokemonPickerUiModel] = []
    @Published private(set) var pokemonError: String? = nil

    @Published private(set) var itemLoading = true
    @Published private(set) var itemItems: [ItemUiModel] = []
    @Published private(set) var itemError: String? = nil

    @Published private(set) var teraTypeLoading = true
    @Published private(set) var teraTypeItems: [TeraTypeUiModel] = []
    @Published private(set) var teraTypeError: String? = nil

    @Published private(set) var formatLoading = true
    @Published private(set) var formatItems: [FormatUiModel] = []
    @Published private(set) var formatError: String? = nil

    private let apiService: ApiService
    private let cacheStorage: CatalogCacheStorage
    private let defaultFormatIdProvider: () -> Int32?

    init(apiService: ApiService, cacheStorage: CatalogCacheStorage, defaultFormatIdProvider: @escaping () -> Int32? = { nil }) {
        self.apiService = apiService
        self.cacheStorage = cacheStorage
        self.defaultFormatIdProvider = defaultFormatIdProvider
        Task { await loadPokemonCatalog() }
        Task { await loadItemCatalog() }
        Task { await loadTeraTypeCatalog() }
        Task { await loadFormatCatalog() }
    }

    func reload() {
        pokemonLoading = true
        pokemonItems = []
        pokemonError = nil
        itemLoading = true
        itemItems = []
        itemError = nil
        teraTypeLoading = true
        teraTypeItems = []
        teraTypeError = nil
        formatLoading = true
        formatItems = []
        formatError = nil
        Task { await loadPokemonCatalog() }
        Task { await loadItemCatalog() }
        Task { await loadTeraTypeCatalog() }
        Task { await loadFormatCatalog() }
    }

    private func loadPokemonCatalog() async {
        if let result = try? await CatalogLoaderKt.loadPokemonCatalog(apiService: apiService, cacheStorage: cacheStorage),
           let items = result.items as? [PokemonPickerUiModel] {
            pokemonItems = items
        } else {
            pokemonError = "Failed to load Pokémon"
        }
        pokemonLoading = false
    }

    private func loadItemCatalog() async {
        if let result = try? await CatalogLoaderKt.loadItemCatalog(apiService: apiService, cacheStorage: cacheStorage),
           let items = result.items as? [ItemUiModel] {
            itemItems = items
        } else {
            itemError = "Failed to load items"
        }
        itemLoading = false
    }

    private func loadTeraTypeCatalog() async {
        if let result = try? await CatalogLoaderKt.loadTeraTypeCatalog(apiService: apiService, cacheStorage: cacheStorage),
           let items = result.items as? [TeraTypeUiModel] {
            teraTypeItems = items
        } else {
            teraTypeError = "Failed to load tera types"
        }
        teraTypeLoading = false
    }

    private func loadFormatCatalog() async {
        if let result = try? await CatalogLoaderKt.loadFormatCatalog(apiService: apiService, cacheStorage: cacheStorage),
           let items = result.items as? [FormatUiModel] {
            formatItems = sortFormats(items)
        } else {
            formatError = "Failed to load formats"
        }
        formatLoading = false
    }

    private func sortFormats(_ formats: [FormatUiModel]) -> [FormatUiModel] {
        let sorted = formats.sorted { $0.id > $1.id }
        guard let defaultId = defaultFormatIdProvider(),
              let defaultFormat = sorted.first(where: { $0.id == defaultId }) else {
            return sorted
        }
        return [defaultFormat] + sorted.filter { $0.id != defaultId }
    }
}

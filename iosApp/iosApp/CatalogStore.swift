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

    init(apiService: ApiService, cacheStorage: CatalogCacheStorage) {
        self.apiService = apiService
        self.cacheStorage = cacheStorage
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
        do {
            let result = try await CatalogLoaderKt.loadPokemonCatalog(apiService: apiService, cacheStorage: cacheStorage)
            if let items = result.items as? [PokemonPickerUiModel] {
                pokemonItems = items
            } else {
                pokemonError = result.error ?? "Failed to load Pokémon"
            }
        } catch {
            pokemonError = error.localizedDescription
        }
        pokemonLoading = false
    }

    private func loadItemCatalog() async {
        do {
            let result = try await CatalogLoaderKt.loadItemCatalog(apiService: apiService, cacheStorage: cacheStorage)
            if let items = result.items as? [ItemUiModel] {
                itemItems = items
            } else {
                itemError = result.error ?? "Failed to load items"
            }
        } catch {
            itemError = error.localizedDescription
        }
        itemLoading = false
    }

    private func loadTeraTypeCatalog() async {
        do {
            let result = try await CatalogLoaderKt.loadTeraTypeCatalog(apiService: apiService, cacheStorage: cacheStorage)
            if let items = result.items as? [TeraTypeUiModel] {
                teraTypeItems = items
            } else {
                teraTypeError = result.error ?? "Failed to load tera types"
            }
        } catch {
            teraTypeError = error.localizedDescription
        }
        teraTypeLoading = false
    }

    private func loadFormatCatalog() async {
        do {
            let result = try await CatalogLoaderKt.loadFormatCatalog(apiService: apiService, cacheStorage: cacheStorage)
            if let items = result.items as? [FormatUiModel] {
                formatItems = items
            } else {
                formatError = result.error ?? "Failed to load formats"
            }
        } catch {
            formatError = error.localizedDescription
        }
        formatLoading = false
    }
}

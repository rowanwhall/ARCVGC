import Foundation
import Shared

@MainActor
final class AppConfigStore: ObservableObject {
    @Published private(set) var config: AppConfig? = nil
    @Published private(set) var catalogVersionChanged: Bool = false

    let repo: AppConfigRepository

    init(apiService: ApiService, cacheStorage: CatalogCacheStorage) {
        self.repo = AppConfigRepository(
            apiService: apiService,
            storage: AppConfigStorage(),
            catalogCacheStorage: cacheStorage
        )

        Task {
            for await value in repo.config {
                self.config = value
            }
        }

        Task {
            for await value in repo.catalogVersionChanged {
                self.catalogVersionChanged = value.boolValue
            }
        }
    }

    var defaultFormatId: Int32 {
        Int32(repo.getDefaultFormatId())
    }
}

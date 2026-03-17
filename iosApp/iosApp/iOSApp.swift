import SwiftUI
import Shared

@main
struct iOSApp: App {
    @StateObject private var container = DependencyContainer()

    init() {
        SentryInit_iosKt.initializeSentry()

        URLCache.shared = URLCache(
            memoryCapacity: 50 * 1024 * 1024,
            diskCapacity: 100 * 1024 * 1024,
            diskPath: "image_cache"
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(container)
                .onOpenURL { url in
                    let pathAndQuery: String
                    if url.scheme == "arcvgc" {
                        // Custom URL scheme: arcvgc://battle/42 or arcvgc://pokemon/150?battle=42
                        var path = "/\(url.host ?? "")/\(url.path.trimmingCharacters(in: CharacterSet(charactersIn: "/")))"
                            .replacingOccurrences(of: "//", with: "/")
                        if let query = url.query {
                            path += "?\(query)"
                        }
                        pathAndQuery = path
                    } else {
                        // Universal link: https://arcvgc.com/pokemon/150?battle=42
                        var path = url.path
                        if let query = url.query {
                            path += "?\(query)"
                        }
                        pathAndQuery = path
                    }
                    if let deepLink = DeepLinkTargetKt.parseDeepLink(path: pathAndQuery) {
                        container.handleDeepLink(deepLink: deepLink)
                    }
                }
        }
    }
}

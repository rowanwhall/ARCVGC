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
                    let path: String
                    if url.scheme == "arcvgc" {
                        // Custom URL scheme: arcvgc://battle/42
                        path = "/\(url.host ?? "")/\(url.path.trimmingCharacters(in: CharacterSet(charactersIn: "/")))"
                            .replacingOccurrences(of: "//", with: "/")
                    } else {
                        // Universal link: https://arcvgc.com/battle/42
                        path = url.path
                    }
                    if let target = DeepLinkTargetKt.parseDeepLink(path: path) {
                        container.handleDeepLink(target: target)
                    }
                }
        }
    }
}

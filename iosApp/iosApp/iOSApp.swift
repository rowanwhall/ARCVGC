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
        }
    }
}

import SwiftUI
import Shared

@main
struct iOSApp: App {
    @StateObject private var container = DependencyContainer()

    init() {
        // DEBUG: Font registration
        for family in UIFont.familyNames.sorted() {
            let names = UIFont.fontNames(forFamilyName: family)
            if family.lowercased().contains("orbit") || names.contains(where: { $0.lowercased().contains("orbit") }) {
                print("DEBUG FONT: family='\(family)' names=\(names)")
            }
        }
        // Try common name variations
        for name in ["Orbitron", "Orbitron-Regular", "Orbitron-Bold", "OrbitronRegular", "OrbitronBold"] {
            let found = UIFont(name: name, size: 12) != nil
            print("DEBUG FONT: UIFont(name: '\(name)') -> \(found)")
        }
        // Check bundle
        print("DEBUG FONT: file in bundle = \(Bundle.main.path(forResource: "Orbitron-Bold", ofType: "ttf") ?? "NOT FOUND")")
        // Check Info.plist UIAppFonts
        if let fonts = Bundle.main.object(forInfoDictionaryKey: "UIAppFonts") as? [String] {
            print("DEBUG FONT: UIAppFonts = \(fonts)")
        } else {
            print("DEBUG FONT: UIAppFonts NOT SET in Info.plist")
        }

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
                        let trimmedPath = url.path.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
                        var path = trimmedPath.isEmpty
                            ? "/\(url.host ?? "")"
                            : "/\(url.host ?? "")/\(trimmedPath)"
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

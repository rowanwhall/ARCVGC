import Foundation

/// Maps a tutorial image ID to an iOS asset-catalog name.
/// Returns nil if the page should render without an image.
///
/// NOTE: the screenshot assets (Battles/Pokemon/Players/Usage/Search/Favorites)
/// are temporary copies of the web versions — replace with iOS-specific assets in
/// a follow-up. See docs/tutorial.md.
enum TutorialImages {
    static func assetName(for imageId: String?) -> String? {
        guard let imageId else { return nil }
        switch imageId {
        case "info": return "Info"
        case "favorite": return "Favorite"
        case "battles": return "Battles"
        case "pokemon": return "Pokemon"
        case "players": return "Players"
        case "usage": return "Usage"
        case "search": return "Search"
        case "favorites": return "Favorites"
        default: return nil
        }
    }
}

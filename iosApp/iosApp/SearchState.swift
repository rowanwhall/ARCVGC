import Foundation
import Shared

struct SearchState {
    var filterSlots: [SearchFilterSlotUiModel] = []
    var selectedFormat: FormatUiModel? = nil
    var selectedMinRating: Int32? = nil
    var selectedMaxRating: Int32? = nil
    var unratedOnly: Bool = false
    var selectedOrderBy: String = "rating"
    var timeRangeStart: Date? = nil
    var timeRangeEnd: Date? = nil
    var playerName: String = ""

    var canAddMore: Bool { filterSlots.count < 6 }
}

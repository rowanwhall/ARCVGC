import Foundation
import Shared

struct ContentListState {
    var isLoading: Bool = false
    var items: [ContentListItem] = []
    var error: String? = nil
    var isRefreshing: Bool = false
    var isPaginating: Bool = false
    var currentPage: Int32 = 1
    var canPaginate: Bool = true
    var loadingSections: Set<String> = []
}

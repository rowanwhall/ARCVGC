package com.arcvgc.app.ui.model

sealed class ContentListItem {
    abstract val listKey: String
    open val edgeToEdge: Boolean get() = false
    open val isContentItem: Boolean get() = true

    /**
     * Whether this item must be emitted as an individual cell in a cell-packed grid
     * (e.g. web's `LazyVerticalGrid` with `GridCells.FixedSize`). Items that are
     * `true` cannot be bundled with siblings into a single full-span grid slot
     * because they rely on the grid's column layout for placement, animated reflow,
     * and scroll-to-item behavior. Used by the web desktop layout to decide whether
     * a section emits its content as individual grid items or as a single combined
     * grid item whose width is measured from the content.
     */
    open val requiresIndividualGridCells: Boolean get() = false

    data class Battle(val uiModel: BattleCardUiModel) : ContentListItem() {
        override val listKey get() = "battle_${uiModel.id}"
        override val requiresIndividualGridCells: Boolean get() = true
    }

    data class Pokemon(
        val id: Int,
        val name: String,
        val imageUrl: String?,
        val types: List<TypeUiModel>,
        val usagePercent: String? = null
    ) : ContentListItem() {
        override val listKey get() = "pokemon_$id"
    }

    data class Player(val id: Int, val name: String) : ContentListItem() {
        override val listKey get() = "player_$id"
    }

    sealed class SectionAction {
        data object SeeMore : SectionAction()
    }

    data class Section(
        val header: String,
        val items: List<ContentListItem>,
        val trailingAction: SectionAction? = null
    ) : ContentListItem() {
        override val listKey get() = "section_$header"
    }

    /**
     * Groups multiple [Section]s that should be rendered together. Desktop web lays
     * them out as a responsive 1/2/3-column row; every other platform flattens the
     * group back to top-level sections via [unwrapSectionGroups] at the rendering
     * boundary and sees no visual change.
     */
    data class SectionGroup(val sections: List<Section>) : ContentListItem() {
        override val listKey get() = "section_group_${sections.joinToString("_") { it.header }}"
    }

    data class HighlightButton(val label: String, val rating: Int, val battleId: Int)

    data class HighlightButtons(val buttons: List<HighlightButton>) : ContentListItem() {
        override val listKey get() = "highlight_buttons"
    }

    data class PokemonGridItem(val id: Int, val name: String, val imageUrl: String?, val usagePercent: String? = null)

    data class PokemonGrid(val pokemon: List<PokemonGridItem>) : ContentListItem() {
        override val listKey get() = "pokemon_grid"
    }

    data class StatChipItem(
        val name: String,
        val usagePercent: String?,
        val imageUrl: String? = null,
        val pokemonId: Int? = null
    )

    data class StatChipRow(val chips: List<StatChipItem>, val id: String = "") : ContentListItem() {
        override val listKey get() = "stat_chip_row_$id"
        override val edgeToEdge: Boolean get() = true
    }

    data object FormatSelector : ContentListItem() {
        override val listKey get() = "format_selector"
        override val isContentItem: Boolean get() = false
    }

    data class SearchField(val query: String) : ContentListItem() {
        override val listKey get() = "search_field"
        override val isContentItem: Boolean get() = false
    }
}

/**
 * Replaces each [ContentListItem.SectionGroup] with its inner sections in place,
 * preserving order. Platforms that don't render groups specially (Android, iOS,
 * mobile web) call this once at the rendering boundary so the existing per-section
 * code paths stay unchanged. Desktop web skips the unwrap and handles groups in
 * its own emission loop.
 */
fun List<ContentListItem>.unwrapSectionGroups(): List<ContentListItem> =
    flatMap { item ->
        if (item is ContentListItem.SectionGroup) item.sections else listOf(item)
    }

/**
 * Collects every `listKey` reachable from this list, recursing through
 * [ContentListItem.Section] children and [ContentListItem.SectionGroup] sections
 * so nested items contribute their own keys. Used by pagination dedup to avoid
 * re-appending items that are already shown under a wrapping section or group.
 */
fun List<ContentListItem>.collectListKeys(): Set<String> = buildSet {
    fun visit(item: ContentListItem) {
        add(item.listKey)
        when (item) {
            is ContentListItem.Section -> item.items.forEach(::visit)
            is ContentListItem.SectionGroup -> item.sections.forEach(::visit)
            else -> {}
        }
    }
    this@collectListKeys.forEach(::visit)
}

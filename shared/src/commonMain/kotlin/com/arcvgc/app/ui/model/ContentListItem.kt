package com.arcvgc.app.ui.model

sealed class ContentListItem {
    abstract val listKey: String
    open val edgeToEdge: Boolean get() = false

    data class Battle(val uiModel: BattleCardUiModel) : ContentListItem() {
        override val listKey get() = "battle_${uiModel.id}"
    }

    data class Pokemon(
        val id: Int,
        val name: String,
        val imageUrl: String?,
        val types: List<TypeUiModel>
    ) : ContentListItem() {
        override val listKey get() = "pokemon_$id"
    }

    data class Player(val id: Int, val name: String) : ContentListItem() {
        override val listKey get() = "player_$id"
    }

    data class Section(val header: String, val items: List<ContentListItem>) : ContentListItem() {
        override val listKey get() = "section_$header"
    }

    data class HighlightButton(val label: String, val rating: Int, val battleId: Int)

    data class HighlightButtons(val buttons: List<HighlightButton>) : ContentListItem() {
        override val listKey get() = "highlight_buttons"
    }

    data class PokemonGridItem(val id: Int, val name: String, val imageUrl: String?, val usagePercent: String? = null)

    data class PokemonGrid(val pokemon: List<PokemonGridItem>) : ContentListItem() {
        override val listKey get() = "pokemon_grid"
    }

    data class StatChipItem(val name: String, val usagePercent: String?, val imageUrl: String? = null)

    data class StatChipRow(val chips: List<StatChipItem>, val id: String = "") : ContentListItem() {
        override val listKey get() = "stat_chip_row_$id"
        override val edgeToEdge: Boolean get() = true
    }

    data object FormatSelector : ContentListItem() {
        override val listKey get() = "format_selector"
    }
}

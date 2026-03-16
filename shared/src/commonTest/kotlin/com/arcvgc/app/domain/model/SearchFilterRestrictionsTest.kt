package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchFilterRestrictionsTest {

    // --- canFilterByTeraType ---

    @Test
    fun canFilterByTeraType_ogerpon_false() {
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("Ogerpon"))
    }

    @Test
    fun canFilterByTeraType_ogerponCornerstone_false() {
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("Ogerpon-Cornerstone"))
    }

    @Test
    fun canFilterByTeraType_ogerponHearthflame_false() {
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("Ogerpon-Hearthflame"))
    }

    @Test
    fun canFilterByTeraType_ogerponWellspring_false() {
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("Ogerpon-Wellspring"))
    }

    @Test
    fun canFilterByTeraType_terapagos_false() {
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("Terapagos"))
    }

    @Test
    fun canFilterByTeraType_terapagosStellar_false() {
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("Terapagos-Stellar"))
    }

    @Test
    fun canFilterByTeraType_pikachu_true() {
        assertTrue(SearchFilterRestrictions.canFilterByTeraType("Pikachu"))
    }

    @Test
    fun canFilterByTeraType_caseInsensitive() {
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("OGERPON"))
        assertFalse(SearchFilterRestrictions.canFilterByTeraType("terapagos"))
    }

    // --- canFilterByItem ---

    @Test
    fun canFilterByItem_ogerponCornerstone_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Ogerpon-Cornerstone"))
    }

    @Test
    fun canFilterByItem_ogerponHearthflame_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Ogerpon-Hearthflame"))
    }

    @Test
    fun canFilterByItem_ogerponWellspring_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Ogerpon-Wellspring"))
    }

    @Test
    fun canFilterByItem_zacianCrowned_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Zacian-Crowned"))
    }

    @Test
    fun canFilterByItem_zamazentaCrowned_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Zamazenta-Crowned"))
    }

    @Test
    fun canFilterByItem_giratinaOrigin_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Giratina-Origin"))
    }

    @Test
    fun canFilterByItem_palkiaOrigin_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Palkia-Origin"))
    }

    @Test
    fun canFilterByItem_dialgaOrigin_false() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("Dialga-Origin"))
    }

    @Test
    fun canFilterByItem_pikachu_true() {
        assertTrue(SearchFilterRestrictions.canFilterByItem("Pikachu"))
    }

    @Test
    fun canFilterByItem_caseInsensitive() {
        assertFalse(SearchFilterRestrictions.canFilterByItem("ZACIAN-CROWNED"))
        assertFalse(SearchFilterRestrictions.canFilterByItem("dialga-origin"))
    }
}

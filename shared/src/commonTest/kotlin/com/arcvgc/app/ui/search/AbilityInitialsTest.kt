package com.arcvgc.app.ui.search

import kotlin.test.Test
import kotlin.test.assertEquals

class AbilityInitialsTest {

    @Test
    fun singleWord() {
        assertEquals("I", abilityInitials("Intimidate"))
    }

    @Test
    fun twoWords() {
        assertEquals("SS", abilityInitials("Sand Stream"))
    }

    @Test
    fun threeWords_takeFirstTwo() {
        assertEquals("CB", abilityInitials("Clear Body Armor"))
    }

    @Test
    fun parenthetical_ignored() {
        assertEquals("AO", abilityInitials("As One (Glastrier)"))
    }

    @Test
    fun singleLetter_uppercase() {
        assertEquals("S", abilityInitials("static"))
    }
}

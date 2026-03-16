package com.arcvgc.app.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DomainItemTest {

    // --- DomainItem.displayName ---

    @Test
    fun displayName_camelCase_insertsSpaces() {
        assertEquals("Booster Energy", DomainItem(1, "BoosterEnergy").displayName)
    }

    @Test
    fun displayName_multiWord_insertsSpaces() {
        assertEquals("Choice Scarf", DomainItem(1, "ChoiceScarf").displayName)
    }

    @Test
    fun displayName_alreadySpaced_unchanged() {
        assertEquals("Choice Scarf", DomainItem(1, "Choice Scarf").displayName)
    }

    @Test
    fun displayName_singleWord_unchanged() {
        assertEquals("Leftovers", DomainItem(1, "Leftovers").displayName)
    }

    // --- DomainItem.spriteIconName ---

    @Test
    fun spriteIconName_camelCase_lowercaseNoSpecialChars() {
        assertEquals("boosterenergy", DomainItem(1, "BoosterEnergy").spriteIconName)
    }

    @Test
    fun spriteIconName_withSpaces_removesSpaces() {
        assertEquals("hearthflamemask", DomainItem(1, "Hearthflame Mask").spriteIconName)
    }

    @Test
    fun spriteIconName_choiceScarf() {
        assertEquals("choicescarf", DomainItem(1, "Choice Scarf").spriteIconName)
    }

    // --- Ability.displayName ---

    @Test
    fun abilityDisplayName_camelCase_insertsSpaces() {
        assertEquals("Sand Stream", Ability(1, "SandStream").displayName)
    }

    @Test
    fun abilityDisplayName_singleWord_unchanged() {
        assertEquals("Intimidate", Ability(1, "Intimidate").displayName)
    }

    // --- Move.displayName ---

    @Test
    fun moveDisplayName_camelCase_insertsSpaces() {
        assertEquals("Shadow Ball", Move(1, "ShadowBall").displayName)
    }

    @Test
    fun moveDisplayName_singleWord_unchanged() {
        assertEquals("Protect", Move(1, "Protect").displayName)
    }
}

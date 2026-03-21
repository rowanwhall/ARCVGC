package com.arcvgc.app.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals

class FormatSorterTest {

    @Test
    fun emptyListReturnsEmpty() {
        val result = FormatSorter.sorted(emptyList(), defaultFormatId = null)
        assertEquals(emptyList(), result)
    }

    @Test
    fun sortsDescendingById() {
        val formats = listOf(
            FormatUiModel(id = 1, displayName = "Reg F"),
            FormatUiModel(id = 3, displayName = "Reg H"),
            FormatUiModel(id = 2, displayName = "Reg I")
        )
        val result = FormatSorter.sorted(formats, defaultFormatId = null)
        assertEquals(listOf(3, 2, 1), result.map { it.id })
    }

    @Test
    fun promotesDefaultFormatToFront() {
        val formats = listOf(
            FormatUiModel(id = 1, displayName = "Reg F"),
            FormatUiModel(id = 3, displayName = "Reg H"),
            FormatUiModel(id = 2, displayName = "Reg I")
        )
        val result = FormatSorter.sorted(formats, defaultFormatId = 2)
        assertEquals(listOf(2, 3, 1), result.map { it.id })
    }

    @Test
    fun defaultFormatAlreadyFirstIsNoOp() {
        val formats = listOf(
            FormatUiModel(id = 3, displayName = "Reg H"),
            FormatUiModel(id = 2, displayName = "Reg I"),
            FormatUiModel(id = 1, displayName = "Reg F")
        )
        val result = FormatSorter.sorted(formats, defaultFormatId = 3)
        assertEquals(listOf(3, 2, 1), result.map { it.id })
    }

    @Test
    fun defaultFormatIdNotFoundReturnsSortedOnly() {
        val formats = listOf(
            FormatUiModel(id = 1, displayName = "Reg F"),
            FormatUiModel(id = 2, displayName = "Reg I")
        )
        val result = FormatSorter.sorted(formats, defaultFormatId = 99)
        assertEquals(listOf(2, 1), result.map { it.id })
    }

    @Test
    fun singleItemWithMatchingDefault() {
        val formats = listOf(FormatUiModel(id = 5, displayName = "Only"))
        val result = FormatSorter.sorted(formats, defaultFormatId = 5)
        assertEquals(listOf(5), result.map { it.id })
    }

    @Test
    fun twoFormatsWithLowerIdAsDefaultPromotesCorrectly() {
        val formats = listOf(
            FormatUiModel(id = 1, displayName = "Reg F"),
            FormatUiModel(id = 2, displayName = "Reg I")
        )
        // Default is Reg I (id=2), should be first
        val result = FormatSorter.sorted(formats, defaultFormatId = 2)
        assertEquals(listOf(2, 1), result.map { it.id })
        assertEquals("Reg I", result.first().displayName)
    }
}

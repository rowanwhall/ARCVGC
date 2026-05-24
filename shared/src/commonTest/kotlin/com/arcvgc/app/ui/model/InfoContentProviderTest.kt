package com.arcvgc.app.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InfoContentProviderTest {

    @Test
    fun replayKeyReturnsContent() {
        val content = InfoContentProvider.get("replay")
        assertNotNull(content)
        assertEquals("Replays and Sets", content.title)
        assertTrue(content.body.isNotBlank())
    }

    @Test
    fun unratedKeyReturnsContent() {
        val content = InfoContentProvider.get("unrated")
        assertNotNull(content)
        assertEquals("Unrated Battle", content.title)
        assertTrue(content.body.isNotBlank())
    }

    @Test
    fun trendingLookbackKeyReturnsContent() {
        val content = InfoContentProvider.get("trending_lookback")
        assertNotNull(content)
        assertEquals("Trending Lookback Window", content.title)
        assertTrue(content.body.isNotBlank())
    }

    @Test
    fun topPlayersKeyReturnsContent() {
        val content = InfoContentProvider.get("top_players")
        assertNotNull(content)
        assertEquals("Top Players", content.title)
        assertTrue(content.body.isNotBlank())
    }

    @Test
    fun unknownKeyReturnsNull() {
        assertNull(InfoContentProvider.get("nonexistent"))
    }

    @Test
    fun replayContentHasNoImage() {
        val content = InfoContentProvider.get("replay")
        assertNotNull(content)
        assertNull(content.imageUrl)
    }
}

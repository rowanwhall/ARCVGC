package com.arcvgc.app.network.mapper

import com.arcvgc.app.network.model.RatedMatchDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PlayerProfileMapperTest {

    @Test
    fun ratedMatchDto_withValidFields_returnsDomain() {
        val dto = RatedMatchDto(id = 42, rating = 1800)

        val result = dto.toDomain()

        assertNotNull(result)
        assertEquals(42, result.id)
        assertEquals(1800, result.rating)
    }

    @Test
    fun ratedMatchDto_withNullId_returnsNull() {
        val dto = RatedMatchDto(id = null, rating = 1800)

        val result = dto.toDomain()

        assertNull(result)
    }

    @Test
    fun ratedMatchDto_withNullRating_returnsNull() {
        val dto = RatedMatchDto(id = 42, rating = null)

        val result = dto.toDomain()

        assertNull(result)
    }

    @Test
    fun ratedMatchDto_withBothNull_returnsNull() {
        val dto = RatedMatchDto(id = null, rating = null)

        val result = dto.toDomain()

        assertNull(result)
    }
}

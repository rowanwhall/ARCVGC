package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.DomainItem
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.domain.model.PlayerPreview
import com.arcvgc.app.domain.model.PokemonPreview
import com.arcvgc.app.domain.model.TeraType
import com.arcvgc.app.network.model.FormatDto
import com.arcvgc.app.network.model.MatchPreviewDto
import com.arcvgc.app.network.model.NetworkItemDto
import com.arcvgc.app.network.model.PlayerPreviewDto
import com.arcvgc.app.network.model.PokemonPreviewDto
import com.arcvgc.app.network.model.TeraTypeDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MatchPreviewMapperTest {

    // region NetworkItemDto.toDomain()

    @Test
    fun networkItemDto_withValidIdAndName_returnsDomainItem() {
        val dto = NetworkItemDto(id = 1, name = "BoosterEnergy", imageUrl = "https://example.com/item.png")

        val result = dto.toDomain()

        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("BoosterEnergy", result.name)
        assertEquals("https://example.com/item.png", result.imageUrl)
    }

    @Test
    fun networkItemDto_withNullId_returnsNull() {
        val dto = NetworkItemDto(id = null, name = "BoosterEnergy")

        val result = dto.toDomain()

        assertNull(result)
    }

    @Test
    fun networkItemDto_withNullName_returnsNull() {
        val dto = NetworkItemDto(id = 1, name = null)

        val result = dto.toDomain()

        assertNull(result)
    }

    // endregion

    // region TeraTypeDto.toTeraType()

    @Test
    fun teraTypeDto_withValidIdAndName_returnsTeraType() {
        val dto = TeraTypeDto(id = 5, name = "Fire", imageUrl = "https://example.com/fire.png")

        val result = dto.toTeraType()

        assertNotNull(result)
        assertEquals(5, result.id)
        assertEquals("Fire", result.name)
        assertEquals("https://example.com/fire.png", result.imageUrl)
    }

    @Test
    fun teraTypeDto_withNullId_returnsNull() {
        val dto = TeraTypeDto(id = null, name = "Fire")

        val result = dto.toTeraType()

        assertNull(result)
    }

    @Test
    fun teraTypeDto_withNullName_returnsNull() {
        val dto = TeraTypeDto(id = 5, name = null)

        val result = dto.toTeraType()

        assertNull(result)
    }

    // endregion

    // region PokemonPreviewDto.toDomain()

    @Test
    fun pokemonPreviewDto_mapsAllFieldsCorrectly() {
        val itemDto = NetworkItemDto(id = 10, name = "ChoiceBand", imageUrl = "https://example.com/band.png")
        val teraDto = TeraTypeDto(id = 3, name = "Water", imageUrl = "https://example.com/water.png")
        val dto = PokemonPreviewDto(
            id = 25,
            name = "Pikachu",
            pokedexNumber = 25,
            item = itemDto,
            teraType = teraDto,
            imageUrl = "https://example.com/pikachu.png"
        )

        val result = dto.toDomain()

        assertEquals(25, result.id)
        assertEquals("Pikachu", result.name)
        assertEquals(25, result.pokedexNumber)
        val item = result.item
        assertNotNull(item)
        assertEquals(10, item.id)
        assertEquals("ChoiceBand", item.name)
        val teraType = result.teraType
        assertNotNull(teraType)
        assertEquals(3, teraType.id)
        assertEquals("Water", teraType.name)
        assertEquals("https://example.com/pikachu.png", result.imageUrl)
    }

    @Test
    fun pokemonPreviewDto_withNullItem_returnsNullItemInDomain() {
        val dto = PokemonPreviewDto(
            id = 6,
            name = "Charizard",
            pokedexNumber = 6,
            item = null
        )

        val result = dto.toDomain()

        assertNull(result.item)
    }

    // endregion

    // region PlayerPreviewDto.toDomain()

    @Test
    fun playerPreviewDto_withEmptyTeam_returnsNull() {
        val dto = PlayerPreviewDto(
            id = 1,
            name = "Ash",
            winner = true,
            team = emptyList()
        )

        val result = dto.toDomain()

        assertNull(result)
    }

    @Test
    fun playerPreviewDto_withNonEmptyTeam_returnsPlayerPreview() {
        val pokemon = PokemonPreviewDto(
            id = 25,
            name = "Pikachu",
            pokedexNumber = 25,
            item = null
        )
        val dto = PlayerPreviewDto(
            id = 1,
            name = "Ash",
            winner = true,
            team = listOf(pokemon)
        )

        val result = dto.toDomain()

        assertNotNull(result)
        assertEquals(1, result.id)
        assertEquals("Ash", result.name)
        assertEquals(true, result.isWinner)
        assertEquals(1, result.team.size)
        assertEquals("Pikachu", result.team[0].name)
    }

    // endregion

    // region FormatDto.toDomain()

    @Test
    fun formatDto_mapsCorrectly() {
        val dto = FormatDto(id = 1, name = "gen9vgc2024regg", formattedName = "Gen 9 VGC 2024 Reg G")

        val result = dto.toDomain()

        assertEquals(1, result.id)
        assertEquals("gen9vgc2024regg", result.name)
        assertEquals("Gen 9 VGC 2024 Reg G", result.formattedName)
    }

    // endregion

    // region MatchPreviewDto.toDomain()

    @Test
    fun matchPreviewDto_mapsAllFields() {
        val pokemon = PokemonPreviewDto(id = 25, name = "Pikachu", pokedexNumber = 25, item = null)
        val player1 = PlayerPreviewDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val player2 = PlayerPreviewDto(id = 2, name = "Gary", winner = false, team = listOf(pokemon))
        val format = FormatDto(id = 1, name = "gen9vgc2024regg", formattedName = "Gen 9 VGC 2024 Reg G")
        val dto = MatchPreviewDto(
            id = 100,
            showdownId = "gen9vgc2024regg-12345",
            uploadTime = "2024-01-15T10:30:00Z",
            rating = 1500,
            private = false,
            format = format,
            players = listOf(player1, player2)
        )

        val result = dto.toDomain()

        assertEquals(100, result.id)
        assertEquals("gen9vgc2024regg-12345", result.showdownId)
        assertEquals("2024-01-15T10:30:00Z", result.uploadTime)
        assertEquals(1500, result.rating)
        assertEquals(false, result.isPrivate)
        assertEquals("gen9vgc2024regg", result.format.name)
        assertEquals(2, result.players.size)
    }

    // endregion

    // region List<MatchPreviewDto>.toDomain()

    @Test
    fun listMatchPreviewDto_filtersOutMatchesWithFewerThan2ValidPlayers() {
        val pokemon = PokemonPreviewDto(id = 25, name = "Pikachu", pokedexNumber = 25, item = null)
        val validPlayer = PlayerPreviewDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val emptyTeamPlayer = PlayerPreviewDto(id = 2, name = "Gary", winner = false, team = emptyList())
        val format = FormatDto(id = 1, name = "gen9vgc2024regg", formattedName = null)

        // Match with only 1 valid player (second player has empty team -> filtered by PlayerPreviewDto.toDomain())
        val matchWith1Player = MatchPreviewDto(
            id = 1,
            showdownId = "match-1",
            uploadTime = "2024-01-15T10:30:00Z",
            rating = null,
            private = false,
            format = format,
            players = listOf(validPlayer, emptyTeamPlayer)
        )

        val result = listOf(matchWith1Player).toDomain()

        assertEquals(0, result.size)
    }

    @Test
    fun listMatchPreviewDto_keepsMatchesWith2OrMoreValidPlayers() {
        val pokemon = PokemonPreviewDto(id = 25, name = "Pikachu", pokedexNumber = 25, item = null)
        val player1 = PlayerPreviewDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val player2 = PlayerPreviewDto(id = 2, name = "Gary", winner = false, team = listOf(pokemon))
        val format = FormatDto(id = 1, name = "gen9vgc2024regg", formattedName = null)

        val validMatch = MatchPreviewDto(
            id = 1,
            showdownId = "match-1",
            uploadTime = "2024-01-15T10:30:00Z",
            rating = 1600,
            private = false,
            format = format,
            players = listOf(player1, player2)
        )

        val result = listOf(validMatch).toDomain()

        assertEquals(1, result.size)
        assertEquals(1, result[0].id)
    }

    // endregion
}

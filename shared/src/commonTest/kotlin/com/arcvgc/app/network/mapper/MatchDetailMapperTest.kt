package com.arcvgc.app.network.mapper

import com.arcvgc.app.network.model.AbilityDto
import com.arcvgc.app.network.model.BaseSpeciesDto
import com.arcvgc.app.network.model.FormatDto
import com.arcvgc.app.network.model.MatchDetailDto
import com.arcvgc.app.network.model.MoveDto
import com.arcvgc.app.network.model.NetworkItemDto
import com.arcvgc.app.network.model.PlayerDetailDto
import com.arcvgc.app.network.model.PokemonDetailDto
import com.arcvgc.app.network.model.SetMatchDto
import com.arcvgc.app.network.model.TeraTypeDto
import com.arcvgc.app.network.model.TypeDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MatchDetailMapperTest {

    // region AbilityDto.toDomain()

    @Test
    fun abilityDto_mapsCorrectly() {
        val dto = AbilityDto(id = 1, name = "Intimidate")

        val result = dto.toDomain()

        assertEquals(1, result.id)
        assertEquals("Intimidate", result.name)
    }

    // endregion

    // region MoveDto.toDomain()

    @Test
    fun moveDto_mapsCorrectly() {
        val dto = MoveDto(id = 10, name = "Thunderbolt")

        val result = dto.toDomain()

        assertEquals(10, result.id)
        assertEquals("Thunderbolt", result.name)
    }

    // endregion

    // region TypeDto.toDomain()

    @Test
    fun typeDto_mapsCorrectly() {
        val dto = TypeDto(id = 13, name = "Electric", imageUrl = "https://example.com/electric.png")

        val result = dto.toDomain()

        assertEquals(13, result.id)
        assertEquals("Electric", result.name)
        assertEquals("https://example.com/electric.png", result.imageUrl)
    }

    // endregion

    // region BaseSpeciesDto.toDomain()

    @Test
    fun baseSpeciesDto_mapsCorrectly() {
        val dto = BaseSpeciesDto(id = 6, name = "Charizard", pokedexNumber = 6)

        val result = dto.toDomain()

        assertEquals(6, result.id)
        assertEquals("Charizard", result.name)
        assertEquals(6, result.pokedexNumber)
    }

    // endregion

    // region NetworkItemDto.toDomainItem()

    @Test
    fun networkItemDto_toDomainItem_withValidData_returnsDomainItem() {
        val dto = NetworkItemDto(id = 5, name = "LifeOrb", imageUrl = "https://example.com/orb.png")

        val result = dto.toDomainItem()

        assertNotNull(result)
        assertEquals(5, result.id)
        assertEquals("LifeOrb", result.name)
        assertEquals("https://example.com/orb.png", result.imageUrl)
    }

    @Test
    fun networkItemDto_toDomainItem_withNullId_returnsNull() {
        val dto = NetworkItemDto(id = null, name = "LifeOrb")

        val result = dto.toDomainItem()

        assertNull(result)
    }

    // endregion

    // region PokemonDetailDto.toDomain()

    @Test
    fun pokemonDetailDto_mapsAllNestedFields() {
        val ability = AbilityDto(id = 1, name = "Intimidate")
        val item = NetworkItemDto(id = 5, name = "LifeOrb", imageUrl = "https://example.com/orb.png")
        val move1 = MoveDto(id = 10, name = "Thunderbolt")
        val move2 = MoveDto(id = 20, name = "Protect")
        val type1 = TypeDto(id = 13, name = "Electric", imageUrl = "https://example.com/electric.png")
        val baseSpecies = BaseSpeciesDto(id = 25, name = "Pikachu", pokedexNumber = 25)
        val teraType = TeraTypeDto(id = 3, name = "Water", imageUrl = "https://example.com/water.png")

        val dto = PokemonDetailDto(
            id = 25,
            name = "Pikachu",
            pokedexNumber = 25,
            tier = "OU",
            ability = ability,
            item = item,
            moves = listOf(move1, move2),
            types = listOf(type1),
            baseSpecies = baseSpecies,
            teraType = teraType,
            imageUrl = "https://example.com/pikachu.png"
        )

        val result = dto.toDomain()

        assertEquals(25, result.id)
        assertEquals("Pikachu", result.name)
        assertEquals(25, result.pokedexNumber)
        assertEquals("OU", result.tier)
        assertEquals(1, result.ability.id)
        assertEquals("Intimidate", result.ability.name)
        val resultItem = result.item
        assertNotNull(resultItem)
        assertEquals(5, resultItem.id)
        assertEquals(2, result.moves.size)
        assertEquals("Thunderbolt", result.moves[0].name)
        assertEquals("Protect", result.moves[1].name)
        assertEquals(1, result.types.size)
        assertEquals("Electric", result.types[0].name)
        val resultBaseSpecies = result.baseSpecies
        assertNotNull(resultBaseSpecies)
        assertEquals("Pikachu", resultBaseSpecies.name)
        val resultTeraType = result.teraType
        assertNotNull(resultTeraType)
        assertEquals("Water", resultTeraType.name)
        assertEquals("https://example.com/pikachu.png", result.imageUrl)
    }

    @Test
    fun pokemonDetailDto_withNullTier_mapsToEmptyString() {
        val dto = PokemonDetailDto(
            id = 6,
            name = "Charizard",
            pokedexNumber = 6,
            tier = null,
            ability = AbilityDto(id = 1, name = "Blaze"),
            item = null,
            moves = emptyList(),
            types = emptyList(),
            baseSpecies = null,
            teraType = null,
            imageUrl = null
        )

        val result = dto.toDomain()

        assertEquals("", result.tier)
    }

    @Test
    fun pokemonDetailDto_withNullItem_returnsNullItem() {
        val dto = PokemonDetailDto(
            id = 6,
            name = "Charizard",
            pokedexNumber = 6,
            tier = "OU",
            ability = AbilityDto(id = 1, name = "Blaze"),
            item = null,
            moves = emptyList(),
            types = emptyList(),
            baseSpecies = null,
            teraType = null,
            imageUrl = null
        )

        val result = dto.toDomain()

        assertNull(result.item)
    }

    @Test
    fun pokemonDetailDto_withNullBaseSpecies_returnsNull() {
        val dto = PokemonDetailDto(
            id = 6,
            name = "Charizard",
            pokedexNumber = 6,
            tier = "OU",
            ability = AbilityDto(id = 1, name = "Blaze"),
            item = null,
            moves = emptyList(),
            types = emptyList(),
            baseSpecies = null,
            teraType = null,
            imageUrl = null
        )

        val result = dto.toDomain()

        assertNull(result.baseSpecies)
    }

    @Test
    fun pokemonDetailDto_withNullTeraType_returnsNull() {
        val dto = PokemonDetailDto(
            id = 6,
            name = "Charizard",
            pokedexNumber = 6,
            tier = "OU",
            ability = AbilityDto(id = 1, name = "Blaze"),
            item = null,
            moves = emptyList(),
            types = emptyList(),
            baseSpecies = null,
            teraType = null,
            imageUrl = null
        )

        val result = dto.toDomain()

        assertNull(result.teraType)
    }

    // endregion

    // region PlayerDetailDto.toDomain()

    @Test
    fun playerDetailDto_mapsCorrectly() {
        val pokemon = PokemonDetailDto(
            id = 25,
            name = "Pikachu",
            pokedexNumber = 25,
            tier = "OU",
            ability = AbilityDto(id = 1, name = "Static"),
            item = null,
            moves = listOf(MoveDto(id = 10, name = "Thunderbolt")),
            types = listOf(TypeDto(id = 13, name = "Electric")),
            baseSpecies = null,
            teraType = null,
            imageUrl = null
        )
        val dto = PlayerDetailDto(
            id = 1,
            name = "Ash",
            winner = true,
            team = listOf(pokemon)
        )

        val result = dto.toDomain()

        assertEquals(1, result.id)
        assertEquals("Ash", result.name)
        assertEquals(true, result.isWinner)
        assertEquals(1, result.team.size)
        assertEquals("Pikachu", result.team[0].name)
    }

    // endregion

    // region MatchDetailDto.toDomain()

    @Test
    fun matchDetailDto_mapsAllFields() {
        val pokemon = PokemonDetailDto(
            id = 25,
            name = "Pikachu",
            pokedexNumber = 25,
            tier = "OU",
            ability = AbilityDto(id = 1, name = "Static"),
            item = null,
            moves = emptyList(),
            types = emptyList(),
            baseSpecies = null,
            teraType = null,
            imageUrl = null
        )
        val player1 = PlayerDetailDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val player2 = PlayerDetailDto(id = 2, name = "Gary", winner = false, team = listOf(pokemon))
        val format = FormatDto(id = 1, name = "gen9vgc2024regg", formattedName = "Gen 9 VGC 2024 Reg G")

        val dto = MatchDetailDto(
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
        assertEquals("Gen 9 VGC 2024 Reg G", result.format.formattedName)
        assertEquals(2, result.players.size)
        assertEquals("Ash", result.players[0].name)
        assertEquals("Gary", result.players[1].name)
    }

    // endregion

    @Test
    fun matchDetailDto_mapsSetFields() {
        val pokemon = PokemonDetailDto(
            id = 25, name = "Pikachu", pokedexNumber = 25, tier = "OU",
            ability = AbilityDto(id = 1, name = "Static"),
            item = null, moves = emptyList(), types = emptyList(),
            baseSpecies = null, teraType = null, imageUrl = null
        )
        val player = PlayerDetailDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val format = FormatDto(id = 1, name = "gen9vgc2026regfbo3", formattedName = null)
        val setMatches = listOf(
            SetMatchDto(id = 94, showdownId = "gen9vgc2026regfbo3-111", positionInSet = 3),
            SetMatchDto(id = 95, showdownId = "gen9vgc2026regfbo3-222", positionInSet = 2),
            SetMatchDto(id = 100, showdownId = "gen9vgc2026regfbo3-333", positionInSet = 1)
        )

        val dto = MatchDetailDto(
            id = 100,
            showdownId = "gen9vgc2026regfbo3-333",
            uploadTime = "2026-02-23T02:15:59",
            rating = null,
            private = false,
            format = format,
            players = listOf(player),
            setId = "1049",
            positionInSet = 1,
            setMatches = setMatches
        )

        val result = dto.toDomain()

        assertEquals("1049", result.setId)
        assertEquals(1, result.positionInSet)
        assertEquals(3, result.setMatches.size)
        assertEquals(94, result.setMatches[0].id)
        assertEquals("gen9vgc2026regfbo3-111", result.setMatches[0].showdownId)
        assertEquals(3, result.setMatches[0].positionInSet)
    }

    @Test
    fun matchDetailDto_withNullSetFields_mapsToDefaults() {
        val pokemon = PokemonDetailDto(
            id = 25, name = "Pikachu", pokedexNumber = 25, tier = "OU",
            ability = AbilityDto(id = 1, name = "Static"),
            item = null, moves = emptyList(), types = emptyList(),
            baseSpecies = null, teraType = null, imageUrl = null
        )
        val player = PlayerDetailDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val format = FormatDto(id = 1, name = "gen9vgc2024regg", formattedName = null)

        val dto = MatchDetailDto(
            id = 100,
            showdownId = "gen9vgc2024regg-12345",
            uploadTime = "2024-01-15T10:30:00Z",
            rating = null,
            private = false,
            format = format,
            players = listOf(player)
        )

        val result = dto.toDomain()

        assertNull(result.setId)
        assertNull(result.positionInSet)
        assertEquals(0, result.setMatches.size)
    }

    @Test
    fun matchDetailDto_withNullSetId_ignoresSetMatches() {
        val pokemon = PokemonDetailDto(
            id = 25, name = "Pikachu", pokedexNumber = 25, tier = "OU",
            ability = AbilityDto(id = 1, name = "Static"),
            item = null, moves = emptyList(), types = emptyList(),
            baseSpecies = null, teraType = null, imageUrl = null
        )
        val player = PlayerDetailDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val format = FormatDto(id = 1, name = "gen9vgc2026regibo3", formattedName = null)

        val dto = MatchDetailDto(
            id = 100,
            showdownId = "gen9vgc2026regibo3-12345",
            uploadTime = "2024-01-15T10:30:00Z",
            rating = null,
            private = false,
            format = format,
            players = listOf(player),
            setId = null,
            positionInSet = 1,
            setMatches = listOf(
                SetMatchDto(id = 99, showdownId = "gen9vgc2026regibo3-111", positionInSet = 1),
                SetMatchDto(id = 100, showdownId = "gen9vgc2026regibo3-222", positionInSet = 1),
                SetMatchDto(id = 101, showdownId = "gen9vgc2026regibo3-333", positionInSet = 2)
            )
        )

        val result = dto.toDomain()

        assertNull(result.setId)
        assertEquals(0, result.setMatches.size)
    }

    // endregion

    // region SetMatchDto.toDomain()

    @Test
    fun setMatchDto_mapsCorrectly() {
        val dto = SetMatchDto(id = 94, showdownId = "gen9vgc2026regfbo3-111", positionInSet = 3)

        val result = dto.toDomain()

        assertEquals(94, result.id)
        assertEquals("gen9vgc2026regfbo3-111", result.showdownId)
        assertEquals(3, result.positionInSet)
    }

    // endregion

    // region MatchDetail.replayUrl

    @Test
    fun matchDetail_replayUrl_computedCorrectly() {
        val pokemon = PokemonDetailDto(
            id = 25,
            name = "Pikachu",
            pokedexNumber = 25,
            tier = null,
            ability = AbilityDto(id = 1, name = "Static"),
            item = null,
            moves = emptyList(),
            types = emptyList(),
            baseSpecies = null,
            teraType = null,
            imageUrl = null
        )
        val player = PlayerDetailDto(id = 1, name = "Ash", winner = true, team = listOf(pokemon))
        val format = FormatDto(id = 1, name = "gen9vgc2024regg", formattedName = null)

        val dto = MatchDetailDto(
            id = 100,
            showdownId = "gen9vgc2024regg-12345",
            uploadTime = "2024-01-15T10:30:00Z",
            rating = null,
            private = false,
            format = format,
            players = listOf(player)
        )

        val result = dto.toDomain()

        assertEquals("https://replay.pokemonshowdown.com/gen9vgc2024regg-12345", result.replayUrl)
    }

    // endregion
}

package com.arcvgc.app.testutil

import com.arcvgc.app.domain.model.Ability
import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.BaseSpecies
import com.arcvgc.app.domain.model.DomainItem
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.domain.model.MatchDetail
import com.arcvgc.app.domain.model.MatchPreview
import com.arcvgc.app.domain.model.Move
import com.arcvgc.app.domain.model.PlayerDetail
import com.arcvgc.app.domain.model.PlayerListItem
import com.arcvgc.app.domain.model.PlayerPreview
import com.arcvgc.app.domain.model.PokemonDetail
import com.arcvgc.app.domain.model.PokemonListItem
import com.arcvgc.app.domain.model.PokemonPreview
import com.arcvgc.app.domain.model.PokemonType
import com.arcvgc.app.domain.model.SearchFilterSlot
import com.arcvgc.app.domain.model.TeraType

fun testFormat(
    id: Int = 1,
    name: String = "gen9vgc2024regh",
    formattedName: String? = "Reg H"
) = Format(id = id, name = name, formattedName = formattedName)

fun testTeraType(
    id: Int = 1,
    name: String = "Fire",
    imageUrl: String? = "https://arcvgc.com/img/tera/fire.png"
) = TeraType(id = id, name = name, imageUrl = imageUrl)

fun testDomainItem(
    id: Int = 1,
    name: String = "BoosterEnergy",
    imageUrl: String? = "https://arcvgc.com/img/items/boosterenergy.png"
) = DomainItem(id = id, name = name, imageUrl = imageUrl)

fun testAbility(
    id: Int = 1,
    name: String = "SandStream"
) = Ability(id = id, name = name)

fun testMove(
    id: Int = 1,
    name: String = "ShadowBall"
) = Move(id = id, name = name)

fun testPokemonType(
    id: Int = 1,
    name: String = "Fire",
    imageUrl: String? = "https://arcvgc.com/img/types/fire.png"
) = PokemonType(id = id, name = name, imageUrl = imageUrl)

fun testPokemonPreview(
    id: Int = 1,
    name: String = "Pikachu",
    pokedexNumber: Int? = 25,
    item: DomainItem? = testDomainItem(),
    teraType: TeraType? = testTeraType(),
    imageUrl: String? = "https://arcvgc.com/img/pokemon/pikachu.png"
) = PokemonPreview(
    id = id,
    name = name,
    pokedexNumber = pokedexNumber,
    item = item,
    teraType = teraType,
    imageUrl = imageUrl
)

fun testPokemonDetail(
    id: Int = 1,
    name: String = "Pikachu",
    pokedexNumber: Int? = 25,
    tier: String = "OU",
    ability: Ability = testAbility(),
    item: DomainItem? = testDomainItem(),
    moves: List<Move> = listOf(
        testMove(1, "Thunderbolt"),
        testMove(2, "VoltSwitch"),
        testMove(3, "SurfingThunderbolt"),
        testMove(4, "Protect")
    ),
    types: List<PokemonType> = listOf(testPokemonType(1, "Electric", "https://arcvgc.com/img/types/electric.png")),
    baseSpecies: BaseSpecies? = null,
    teraType: TeraType? = testTeraType(),
    imageUrl: String? = "https://arcvgc.com/img/pokemon/pikachu.png"
) = PokemonDetail(
    id = id,
    name = name,
    pokedexNumber = pokedexNumber,
    tier = tier,
    ability = ability,
    item = item,
    moves = moves,
    types = types,
    baseSpecies = baseSpecies,
    teraType = teraType,
    imageUrl = imageUrl
)

fun testPlayerPreview(
    id: Int = 1,
    name: String = "PlayerOne",
    isWinner: Boolean? = true,
    team: List<PokemonPreview> = listOf(testPokemonPreview())
) = PlayerPreview(id = id, name = name, isWinner = isWinner, team = team)

fun testPlayerDetail(
    id: Int = 1,
    name: String = "PlayerOne",
    isWinner: Boolean? = true,
    team: List<PokemonDetail> = listOf(testPokemonDetail())
) = PlayerDetail(id = id, name = name, isWinner = isWinner, team = team)

fun testMatchPreview(
    id: Int = 1,
    showdownId: String = "gen9vgc2024regh-12345",
    uploadTime: String = "2026-02-08T17:03:32",
    rating: Int? = 1500,
    isPrivate: Boolean = false,
    format: Format = testFormat(),
    players: List<PlayerPreview> = listOf(
        testPlayerPreview(1, "PlayerOne", true),
        testPlayerPreview(2, "PlayerTwo", false)
    )
) = MatchPreview(
    id = id,
    showdownId = showdownId,
    uploadTime = uploadTime,
    rating = rating,
    isPrivate = isPrivate,
    format = format,
    players = players
)

fun testMatchDetail(
    id: Int = 1,
    showdownId: String = "gen9vgc2024regh-12345",
    uploadTime: String = "2026-02-08T17:03:32",
    rating: Int? = 1500,
    isPrivate: Boolean = false,
    format: Format = testFormat(),
    players: List<PlayerDetail> = listOf(
        testPlayerDetail(1, "PlayerOne", true),
        testPlayerDetail(2, "PlayerTwo", false)
    )
) = MatchDetail(
    id = id,
    showdownId = showdownId,
    uploadTime = uploadTime,
    rating = rating,
    isPrivate = isPrivate,
    format = format,
    players = players
)

fun testSearchFilterSlot(
    pokemonId: Int = 1,
    itemId: Int? = null,
    teraTypeId: Int? = null,
    pokemonName: String = "Pikachu",
    pokemonImageUrl: String? = null,
    itemName: String? = null,
    teraTypeImageUrl: String? = null
) = SearchFilterSlot(
    pokemonId = pokemonId,
    itemId = itemId,
    teraTypeId = teraTypeId,
    pokemonName = pokemonName,
    pokemonImageUrl = pokemonImageUrl,
    itemName = itemName,
    teraTypeImageUrl = teraTypeImageUrl
)

fun testPokemonListItem(
    id: Int = 1,
    name: String = "Pikachu",
    pokedexNumber: Int? = 25,
    tier: String = "OU",
    types: List<PokemonType> = listOf(testPokemonType()),
    imageUrl: String? = "https://arcvgc.com/img/pokemon/pikachu.png"
) = PokemonListItem(
    id = id,
    name = name,
    pokedexNumber = pokedexNumber,
    tier = tier,
    types = types,
    imageUrl = imageUrl
)

fun testPlayerListItem(
    id: Int = 1,
    name: String = "PlayerOne"
) = PlayerListItem(id = id, name = name)

fun testAppConfig(
    defaultFormat: Format = testFormat(),
    minAndroidVersion: Int = 1,
    minIosVersion: Int = 1,
    minWebVersion: Int = 1,
    minCatalogVersion: Int = 1
) = AppConfig(
    defaultFormat = defaultFormat,
    minAndroidVersion = minAndroidVersion,
    minIosVersion = minIosVersion,
    minWebVersion = minWebVersion,
    minCatalogVersion = minCatalogVersion
)

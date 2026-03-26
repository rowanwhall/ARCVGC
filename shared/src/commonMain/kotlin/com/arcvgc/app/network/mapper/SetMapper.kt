package com.arcvgc.app.network.mapper

import com.arcvgc.app.domain.model.MatchSet
import com.arcvgc.app.domain.model.SetDetail
import com.arcvgc.app.domain.model.SetMatchInfo
import com.arcvgc.app.domain.model.SetPlayer
import com.arcvgc.app.domain.model.SetPlayerDetail
import com.arcvgc.app.network.model.SetDetailDto
import com.arcvgc.app.network.model.SetDto
import com.arcvgc.app.network.model.SetMatchModelDto
import com.arcvgc.app.network.model.SetPlayerDetailDto
import com.arcvgc.app.network.model.SetPlayerDto

fun SetDto.toDomain(): MatchSet {
    return MatchSet(
        id = id,
        maxRating = maxRating,
        matchCount = matchCount,
        format = format.toDomain(),
        matches = matches.map { it.toDomain() },
        players = players.map { it.toDomain() }
    )
}

fun SetMatchModelDto.toDomain(): SetMatchInfo {
    return SetMatchInfo(
        positionInSet = positionInSet,
        id = id,
        showdownId = showdownId,
        uploadTime = uploadTime,
        rating = rating,
        isPrivate = private,
        winnerId = winnerId
    )
}

fun SetPlayerDto.toDomain(): SetPlayer {
    return SetPlayer(
        id = id,
        name = name,
        winCount = winCount
    )
}

fun SetDetailDto.toDomain(): SetDetail {
    return SetDetail(
        id = id,
        maxRating = maxRating,
        matchCount = matchCount,
        format = format.toDomain(),
        matches = matches.map { it.toDomain() },
        players = players.map { it.toDomain() }
    )
}

fun SetPlayerDetailDto.toDomain(): SetPlayerDetail {
    return SetPlayerDetail(
        id = id,
        name = name,
        winCount = winCount,
        team = team.map { it.toDomain() }
    )
}

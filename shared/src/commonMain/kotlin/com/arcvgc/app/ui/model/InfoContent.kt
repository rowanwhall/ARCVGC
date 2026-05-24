package com.arcvgc.app.ui.model

data class InfoContent(
    val title: String,
    val body: String,
    val imageUrl: String? = null
)

object InfoContentProvider {
    private val content = mapOf(
        "replay" to InfoContent(
            title = "Replays and Sets",
            body = "Replays open on Pok\u00e9mon Showdown.\n\nBest-of-3 sets may show multiple game buttons - the highlighted button is the game you navigated from.\n\nShowdown doesn't tell us what games go together so we have to give it our best guess, and sometimes quick consecutive sets between the same players/teams can fool us.\n\nSets might also be incomplete if the players only upload some of the games."
        ),
        "unrated" to InfoContent(
            title = "Unrated Battle",
            body = "Showdown did not associate a rating with this battle. This usually happens when the battle was played outside the ladder or was an early game in a best-of-3. Try watching the replay or checking later games in the set if you think this is incorrect."
        ),
        "trending_lookback" to InfoContent(
            title = "Trending Lookback Window",
            body = "For the \"Trending\" sections, this is the period of time used to determine the percent rise/drop. For example, \"7 days\" will compare the last week to the week before it. We default to 7 days since it best reflects current meta shifts, but month-to-month and day-to-day can still be valuable!"
        ),
        "top_players" to InfoContent(
            title = "Top Players",
            body = "The players who have won the highest rated games/sets with this Pok\u00e9mon. If you want to be on the leaderboard but we don't have your replay because it's private, you can submit it from the top right of the home page!"
        )
    )

    fun get(key: String): InfoContent? = content[key]
}

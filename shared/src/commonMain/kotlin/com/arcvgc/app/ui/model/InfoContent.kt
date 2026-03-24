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
        )
    )

    fun get(key: String): InfoContent? = content[key]
}

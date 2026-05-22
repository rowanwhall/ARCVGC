package com.arcvgc.app.ui.tutorial

data class TutorialString(
    val title: String,
    val body: String
)

object TutorialContentProvider {
    private val content: Map<String, TutorialString> = mapOf(
        "welcome" to TutorialString(
            title = "Welcome to ARC - Automatic Replay Compiler",
            body = "Data here is sourced entirely from replays on play.pokemonshowdown.com. From the home tab, you can select any active format and see the top Pokémon and battles from the last 24 hours. Click any Pokémon or battle to see more about them, or use the button in the top right to submit private replays you'd like to share on ARC."
        ),
        "battles" to TutorialString(
            title = "Battles and Replays",
            body = "When you open a battle, you'll see a link to the replay along with both players and their teams. Depending on the format, we'll also show you open teamsheet data and links to other replays in the set. Click on the replay links to view them or any player name/Pokémon to see more about them!"
        ),
        "pokemon" to TutorialString(
            title = "Pokémon",
            body = "When you select a Pokémon, you can see data about how it's been used, the players who have done the best with it, and battles that include it. You can change the format and the window of time you want to see data from, as well as whether to sort the battles by rating or time. Just like the home and battle pages, every Pokémon, battle, and player name are clickable!"
        ),
        "players" to TutorialString(
            title = "Players",
            body = "Every Showdown user with a battle on ARC has their own page you can reach by clicking on it, usually from a battle. You can see their favorite six Pokémon and their top battles from across all formats, as well as battles from the currently selected format. Like the Pokémon page, you can also change the format and sort battles by rating or time."
        ),
        "usage" to TutorialString(
            title = "Usage",
            body = "The usage tab contains a list of the top 100 Pokémon ordered by how often they're used. You can change the format and window of time you'd like this data to be based on, then select any Pokémon to see their page - but you know about those already!"
        ),
        "search" to TutorialString(
            title = "Search",
            body = "On the search tab you can construct full matchups with up to 6 on each side, each with tera/item/ability for open teamsheet formats, and select the winning side. Add filters for date range, rating, and SD username too. Solve your problem MUs or find your favorite winning!"
        ),
        "favorites" to TutorialString(
            title = "Favorites",
            body = "You can favorite any battle, Pokémon, or player to check up on them later here on the Favorites tab. Everything is stored locally - zero tracking of you or your personal data."
        ),
        "settings" to TutorialString(
            title = "Settings",
            body = "Finally, the settings tab lets you change how the app behaves. Select dark mode, accent color, default format, and more. Note that your default format will determine whether other VGC or Smogon formats show up first in selectors throughout the app. And again, everything is stored locally, but your choices will persist across app sessions."
        )
    )

    fun get(id: String): TutorialString? = content[id]

    fun resolve(id: String): TutorialString? = tutorialOverride(id) ?: get(id)
}

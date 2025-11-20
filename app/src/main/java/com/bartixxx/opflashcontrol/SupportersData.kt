package com.bartixxx.opflashcontrol

/**
 * A data class that represents a supporter.
 *
 * @param name The name of the supporter.
 * @param info Information about the supporter.
 * @param avatarUrl The URL of the supporter's avatar.
 */
data class Supporter(val name: String, val info: String, val avatarUrl: String)

/**
 * An object that contains a list of supporters.
 */
object SupportersData {
    /**
     * A list of supporters.
     */
    val supporters = listOf(
        Supporter(
            "Bartixxx",
            "App Developer",
            "https://ik.imagekit.io/bartixxx32/github/tr:r-20/u/16412925"
        ),
    )
}
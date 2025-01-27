package com.bartixxx.opflashcontrol

data class Supporter(val name: String, val info: String, val avatarUrl: String)

object SupportersData {
    val supporters = listOf(
        Supporter(
            "Bartixxx",
            "App Developer",
            "https://ik.imagekit.io/bartixxx32/github/tr:r-20/u/16412925"
        ),
    )
}
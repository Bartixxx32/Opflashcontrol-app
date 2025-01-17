package com.bartixxx.opflashcontrol

data class Supporter(val name: String, val info: String, val avatarUrl: String)

object SupportersData {
    val supporters = listOf(
        Supporter("Bartixxx", "App Developer", "https://avatars.githubusercontent.com/u/16412925?v=4"),
    )
}
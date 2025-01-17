package com.bartixxx.opflashcontrol

data class Supporter(val name: String, val info: String, val avatarUrl: String)

object SupportersData {
    val supporters = listOf(
        Supporter("Bartixxx", "Creator", "https://ik.imagekit.io/bartixxx32/NeedRP/LOGO.jpg"),

    )
}
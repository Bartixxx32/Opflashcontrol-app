package com.bartixxx.opflashcontrol

data class Supporter(val name: String, val info: String)

object SupportersData {
    val supporters = listOf(
        Supporter("Bartixxx", "Creator"),
    )
}

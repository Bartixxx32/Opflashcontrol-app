package com.bartixxx.opflashcontrol

object LedPaths {
    const val WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
    const val YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
    const val WHITE2_LED_PATH = "/sys/class/leds/led:torch_2/brightness"
    const val YELLOW2_LED_PATH = "/sys/class/leds/led:torch_3/brightness"
    const val FLASH_WHITE_LED_PATH = "/sys/class/leds/led:flash_0/brightness"
    const val FLASH_YELLOW_LED_PATH = "/sys/class/leds/led:flash_1/brightness"
    const val FLASH_WHITE2_LED_PATH = "/sys/class/leds/led:flash_2/brightness"
    const val FLASH_YELLOW2_LED_PATH = "/sys/class/leds/led:flash_3/brightness"
    val TOGGLE_PATHS = listOf("/sys/class/leds/led:switch_2/brightness")
}
package com.bartixxx.opflashcontrol

object LedPaths {
    var WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
    var YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
    var WHITE2_LED_PATH = "/sys/class/leds/led:torch_2/brightness"
    var YELLOW2_LED_PATH = "/sys/class/leds/led:torch_3/brightness"
    var FLASH_WHITE_LED_PATH = "/sys/class/leds/led:flash_0/brightness"
    var FLASH_YELLOW_LED_PATH = "/sys/class/leds/led:flash_1/brightness"
    var FLASH_WHITE2_LED_PATH = "/sys/class/leds/led:flash_2/brightness"
    var FLASH_YELLOW2_LED_PATH = "/sys/class/leds/led:flash_3/brightness"
    var TOGGLE_PATHS = listOf("/sys/class/leds/led:switch_2/brightness")
}
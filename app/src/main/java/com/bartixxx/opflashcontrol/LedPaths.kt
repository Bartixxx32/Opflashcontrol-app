package com.bartixxx.opflashcontrol

/**
 * An object that contains the paths to the LED brightness files.
 */
object LedPaths {
    /**
     * The path to the white LED brightness file.
     */
    var WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
    /**
     * The path to the yellow LED brightness file.
     */
    var YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
    /**
     * The path to the second white LED brightness file.
     */
    var WHITE2_LED_PATH = "/sys/class/leds/led:torch_2/brightness"
    /**
     * The path to the second yellow LED brightness file.
     */
    var YELLOW2_LED_PATH = "/sys/class/leds/led:torch_3/brightness"
    /**
     * The path to the white LED flash brightness file.
     */
    var FLASH_WHITE_LED_PATH = "/sys/class/leds/led:flash_0/brightness"
    /**
     * The path to the yellow LED flash brightness file.
     */
    var FLASH_YELLOW_LED_PATH = "/sys/class/leds/led:flash_1/brightness"
    /**
     * The path to the second white LED flash brightness file.
     */
    var FLASH_WHITE2_LED_PATH = "/sys/class/leds/led:flash_2/brightness"
    /**
     * The path to the second yellow LED flash brightness file.
     */
    var FLASH_YELLOW2_LED_PATH = "/sys/class/leds/led:flash_3/brightness"
    /**
     * A list of paths to the LED toggle files.
     */
    var TOGGLE_PATHS = listOf("/sys/class/leds/led:switch_2/brightness")
}

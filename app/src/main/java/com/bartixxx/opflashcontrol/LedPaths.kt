package com.bartixxx.opflashcontrol

import android.util.Log

/**
 * An object that contains the paths to the LED brightness files.
 */
object LedPaths {
    private const val TAG = "LedPaths"

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

    /**
     * Logs the currently configured LED paths for diagnostic purposes.
     */
    fun logDiagnostics() {
        Log.d(TAG, "=== LED Paths Diagnostic ===")
        Log.d(TAG, "WHITE_LED_PATH: $WHITE_LED_PATH")
        Log.d(TAG, "YELLOW_LED_PATH: $YELLOW_LED_PATH")
        Log.d(TAG, "WHITE2_LED_PATH: $WHITE2_LED_PATH")
        Log.d(TAG, "YELLOW2_LED_PATH: $YELLOW2_LED_PATH")
        Log.d(TAG, "FLASH_WHITE_LED_PATH: $FLASH_WHITE_LED_PATH")
        Log.d(TAG, "FLASH_YELLOW_LED_PATH: $FLASH_YELLOW_LED_PATH")
        Log.d(TAG, "FLASH_WHITE2_LED_PATH: $FLASH_WHITE2_LED_PATH")
        Log.d(TAG, "FLASH_YELLOW2_LED_PATH: $FLASH_YELLOW2_LED_PATH")
        Log.d(TAG, "TOGGLE_PATHS: $TOGGLE_PATHS")
        Log.d(TAG, "=============================")
    }
}

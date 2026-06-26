package com.bartixxx.opflashcontrol

import android.util.Log
import java.io.File

/**
 * A utility object for finding the paths to the LED brightness files.
 */
object LedPathUtil {
    private const val TAG = "LedPathUtil"

    private val TORCH_PATTERNS = listOf(
        "led:torch", "led:torch_", "torch", "led:led_torch",
        "white", "led:white", "led:led_white",
        "yellow", "led:yellow", "led:led_yellow"
    )
    private val FLASH_PATTERNS = listOf(
        "led:flash", "led:flash_", "flash", "led:led_flash"
    )
    private val SWITCH_PATTERNS = listOf(
        "led:switch", "led:switch_", "switch", "led:led_switch"
    )

    /**
     * Finds the paths to the LED brightness files.
     *
     * @param baseDir The base directory to search for LED brightness files.
     * @return The paths to the LED brightness files.
     */
    fun findLedPaths(baseDir: File = File("/sys/class/leds/")): LedPaths {
        val torchPaths = mutableListOf<String>()
        val flashPaths = mutableListOf<String>()
        val switchPaths = mutableListOf<String>()
        val otherPaths = mutableListOf<String>()

        try {
            val ledDirs = baseDir.listFiles()
            ledDirs?.forEach { dir ->
                val dirName = dir.name
                val path = dir.absolutePath + "/brightness"

                when {
                    TORCH_PATTERNS.any { dirName.startsWith(it) } -> torchPaths.add(path)
                    FLASH_PATTERNS.any { dirName.startsWith(it) } -> flashPaths.add(path)
                    SWITCH_PATTERNS.any { dirName.startsWith(it) } -> switchPaths.add(path)
                    else -> otherPaths.add("$dirName -> $path")
                }
            }

            Log.d(TAG, "Found torch paths: $torchPaths")
            Log.d(TAG, "Found flash paths: $flashPaths")
            Log.d(TAG, "Found switch paths: $switchPaths")
            if (otherPaths.isNotEmpty()) {
                Log.d(TAG, "Other LED directories: $otherPaths")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding LED paths", e)
        }

        return LedPaths.apply {
            if (torchPaths.size >= 2) {
                WHITE_LED_PATH = torchPaths[0]
                YELLOW_LED_PATH = torchPaths[1]
                if (torchPaths.size >= 4) {
                    WHITE2_LED_PATH = torchPaths[2]
                    YELLOW2_LED_PATH = torchPaths[3]
                }
            } else if (torchPaths.size == 1) {
                // Single torch LED - use it as both white and yellow
                WHITE_LED_PATH = torchPaths[0]
                YELLOW_LED_PATH = torchPaths[0]
            }

            if (flashPaths.size >= 2) {
                FLASH_WHITE_LED_PATH = flashPaths[0]
                FLASH_YELLOW_LED_PATH = flashPaths[1]
                if (flashPaths.size >= 4) {
                    FLASH_WHITE2_LED_PATH = flashPaths[2]
                    FLASH_YELLOW2_LED_PATH = flashPaths[3]
                }
            } else if (flashPaths.size == 1) {
                // Single flash LED - use it as both white and yellow
                FLASH_WHITE_LED_PATH = flashPaths[0]
                FLASH_YELLOW_LED_PATH = flashPaths[0]
            }

            if (switchPaths.isNotEmpty()) {
                TOGGLE_PATHS = switchPaths
            }
        }
    }
}
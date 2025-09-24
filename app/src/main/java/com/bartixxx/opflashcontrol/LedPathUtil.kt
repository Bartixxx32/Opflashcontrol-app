package com.bartixxx.opflashcontrol

import android.util.Log
import java.io.File

object LedPathUtil {
    private const val TAG = "LedPathUtil"

    fun findLedPaths(): LedPaths {
        val basePath = "/sys/class/leds/"
        val torchPaths = mutableListOf<String>()
        val flashPaths = mutableListOf<String>()
        val switchPaths = mutableListOf<String>()

        try {
            val ledDirs = File(basePath).listFiles()
            ledDirs?.forEach { dir ->
                val dirName = dir.name
                when {
                    dirName.startsWith("led:torch") -> torchPaths.add(dir.absolutePath + "/brightness")
                    dirName.startsWith("led:flash") -> flashPaths.add(dir.absolutePath + "/brightness")
                    dirName.startsWith("led:switch") -> switchPaths.add(dir.absolutePath + "/brightness")
                }
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
            }
            if (flashPaths.size >= 2) {
                FLASH_WHITE_LED_PATH = flashPaths[0]
                FLASH_YELLOW_LED_PATH = flashPaths[1]
                if (flashPaths.size >= 4) {
                    FLASH_WHITE2_LED_PATH = flashPaths[2]
                    FLASH_YELLOW2_LED_PATH = flashPaths[3]
                }
            }
            if (switchPaths.isNotEmpty()) {
                TOGGLE_PATHS = switchPaths
            }
        }
    }
}
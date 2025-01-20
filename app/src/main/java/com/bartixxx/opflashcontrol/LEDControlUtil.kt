package com.bartixxx.opflashcontrol

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.DataOutputStream
import java.io.IOException

class LedController(private val context: Context) {

    companion object {
        const val WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
        const val YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
        const val WHITE2_LED_PATH = "/sys/class/leds/led:torch_2/brightness"
        const val YELLOW2_LED_PATH = "/sys/class/leds/led:torch_3/brightness"
        val TOGGLE_PATHS = listOf("/sys/class/leds/led:switch_2/brightness")
    }

    private fun sanitizeBrightness(brightness: Int): Int {
        return if (brightness == 0) 1 else brightness
    }

    fun controlLeds(
        action: String,
        whiteLedPath: String = WHITE_LED_PATH,
        yellowLedPath: String = YELLOW_LED_PATH,
        white2LedPath: String? = WHITE2_LED_PATH,
        yellow2LedPath: String? = YELLOW2_LED_PATH,
        whiteBrightness: Int,
        yellowBrightness: Int,
        white2Brightness: Int = 0,
        yellow2Brightness: Int = 0,
        showToast: Boolean = true
    ) {
        val sanitizedWhiteBrightness = sanitizeBrightness(whiteBrightness)
        val sanitizedYellowBrightness = sanitizeBrightness(yellowBrightness)
        val sanitizedWhite2Brightness = sanitizeBrightness(white2Brightness)
        val sanitizedYellow2Brightness = sanitizeBrightness(yellow2Brightness)

        val commands = mutableListOf<String>()

        if (action == "on") {
            commands.addAll(commonOnCommands(whiteLedPath, yellowLedPath, white2LedPath, yellow2LedPath))
            commands.addAll(listOf(
                "echo $sanitizedWhiteBrightness > $whiteLedPath",
                "echo $sanitizedYellowBrightness > $yellowLedPath",
                white2LedPath?.let { "echo $sanitizedWhite2Brightness > $it" },
                yellow2LedPath?.let { "echo $sanitizedYellow2Brightness > $it" }
            ).filterNotNull())
            TOGGLE_PATHS.forEach { commands.add("echo 255 > $it") }
        } else if (action == "off") {
            commands.addAll(commonOffCommands(whiteLedPath, yellowLedPath, white2LedPath, yellow2LedPath))
        }

        executeRootCommands(commands, showToast)
    }

    private fun commonOnCommands(
        white: String, yellow: String, white2: String?, yellow2: String?
    ): List<String> {
        return listOf(
            "echo 80 > $white",
            "echo 80 > $yellow",
            white2?.let { "echo 80 > $it" },
            yellow2?.let { "echo 80 > $it" }
        ).filterNotNull() + TOGGLE_PATHS.map { "echo 0 > $it" }
    }

    private fun commonOffCommands(
        white: String, yellow: String, white2: String?, yellow2: String?
    ): List<String> {
        return listOf(
            "echo 80 > $white",
            "echo 80 > $yellow",
            white2?.let { "echo 80 > $it" },
            yellow2?.let { "echo 80 > $it" }
        ).filterNotNull() + TOGGLE_PATHS.map { "echo 0 > $it" }
    }

    private fun executeRootCommands(commands: List<String>, showToast: Boolean = true) {
        val maxRetries = 3 // Maximum number of retries
        val initialDelay = 1000L // Initial delay in milliseconds
        val maxDelay = 8000L // Maximum delay (8 seconds) for exponential backoff
        var attempt = 0
        var delay = initialDelay

        while (attempt < maxRetries) {
            try {
                commands.forEach { Log.d("LEDControlApp", "Executing command: $it") }

                val process = Runtime.getRuntime().exec("su")
                process.outputStream.use { outputStream ->
                    DataOutputStream(outputStream).use { dataOutputStream ->
                        val batchCommands = commands.joinToString("\n") + "\nexit\n"
                        dataOutputStream.writeBytes(batchCommands)
                        dataOutputStream.flush()
                    }
                }
                process.waitFor()

                // Show toast on the main thread if allowed
                if (showToast) {
                    Toast.makeText(context, "Command executed", Toast.LENGTH_SHORT).show()
                }
                return // Exit the method if the command was successfully executed
            } catch (e: IOException) {
                e.printStackTrace()
                if (showToast) {
                    Toast.makeText(context, "IO Error", Toast.LENGTH_LONG).show()
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                if (showToast) {
                    Toast.makeText(context, "Permission Error", Toast.LENGTH_LONG).show()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                if (showToast) {
                    Toast.makeText(context, "Interrupted Error", Toast.LENGTH_LONG).show()
                }
            }

            // Retry mechanism with exponential backoff
            attempt++
            if (attempt < maxRetries) {
                Log.d("LEDControlApp", "Retrying... attempt #$attempt")
                Thread.sleep(delay)
                delay = (delay * 2).coerceAtMost(maxDelay) // Exponential backoff
            }
        }

        // If we've exhausted all retries, show toast on the main thread if allowed
        if (showToast) {
            Toast.makeText(context, "Retry Failed", Toast.LENGTH_LONG).show()
        }
    }
}

package com.bartixxx.opflashcontrol

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.DataOutputStream
import java.io.IOException

class LedController {

    companion object {
        const val WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
        const val YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
        val TOGGLE_PATHS = listOf("/sys/class/leds/led:switch_2/brightness")
    }

    private val context: Context

    constructor(context: Context) {
        this.context = context
    }

    private fun sanitizeBrightness(brightness: Int): Int {
        return if (brightness == 0) 1 else brightness
    }

    fun controlLeds(
        action: String,
        whiteLedPath: String = WHITE_LED_PATH,
        yellowLedPath: String = YELLOW_LED_PATH,
        white2LedPath: String? = null,
        yellow2LedPath: String? = null,
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
            commands.addAll(
                commonOnCommands(
                    whiteLedPath,
                    yellowLedPath,
                    white2LedPath,
                    yellow2LedPath,
                    sanitizedWhiteBrightness,
                    sanitizedYellowBrightness,
                    sanitizedWhite2Brightness,
                    sanitizedYellow2Brightness
                )
            )
        } else if (action == "off") {
            commands.addAll(
                commonOffCommands(
                    whiteLedPath,
                    yellowLedPath,
                    white2LedPath,
                    yellow2LedPath
                )
            )
        }

        executeRootCommands(commands, showToast)
    }

    private fun commonOnCommands(
        white: String, yellow: String, white2: String?, yellow2: String?,
        whiteBrightness: Int, yellowBrightness: Int, white2Brightness: Int, yellow2Brightness: Int
    ): List<String> {
        val commands = mutableListOf<String>()

        // Reset toggle paths to 0, then back to 255 to ensure proper refresh
        TOGGLE_PATHS.forEach {
            commands.add("echo 0 > $it")
        }

        commands.add("echo $whiteBrightness > $white")
        commands.add("echo $yellowBrightness > $yellow")

        white2?.let {
            commands.add("echo $white2Brightness > $it")
        }

        yellow2?.let {
            commands.add("echo $yellow2Brightness > $it")
        }

        TOGGLE_PATHS.forEach {
            commands.add("echo 255 > $it")
        }

        return commands
    }

    private fun commonOffCommands(
        white: String, yellow: String, white2: String?, yellow2: String?
    ): List<String> {
        val commands = mutableListOf<String>()

        // Set brightness to 80 instead of 0 for "off" action
        commands.add("echo 80 > $white")
        commands.add("echo 80 > $yellow")

        white2?.let {
            commands.add("echo 80 > $it")
        }

        yellow2?.let {
            commands.add("echo 80 > $it")
        }

        // Reset toggle paths to 0
        TOGGLE_PATHS.forEach {
            commands.add("echo 0 > $it")
        }

        return commands
    }


    protected fun executeRootCommands(commands: List<String>, showToast: Boolean = true) {
        val maxRetries = 3 // Maximum number of retries
        val initialDelay = 1000L // Initial delay in milliseconds
        val maxDelay = 8000L // Maximum delay (8 seconds) for exponential backoff
        var attempt = 0
        var delay = initialDelay


        // Create a Handler for the main thread
        val mainHandler = Handler(Looper.getMainLooper())

        // Check SELinux status
        // val selinuxProcess = Runtime.getRuntime().exec("getenforce")
        // val selinuxStatus = selinuxProcess.inputStream.bufferedReader().readText().trim()
        // if (selinuxStatus == "Enforcing") {
        //     Log.d("LEDControlApp", "SELinux is in Enforcing mode.")
        // } else {
        //     Log.d("LEDControlApp", "SELinux is in Permissive mode.")
        // }

        while (attempt < maxRetries) {
            try {
                commands.forEach { Log.d("LEDControlApp", "Executing command: $it") }

                // Use ProcessBuilder to execute the root commands
                val process = ProcessBuilder("su").redirectErrorStream(true).start()
                val outputStream = DataOutputStream(process.outputStream)
                commands.forEach { outputStream.writeBytes("$it\n") }
                outputStream.writeBytes("exit\n")
                outputStream.flush()
                outputStream.close()
                process.waitFor()

                // Show toast on the main thread if allowed
                if (showToast) {
                    mainHandler.post {
                        Toast.makeText(context, "Command executed successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                return // Exit the method if the command was successfully executed
            } catch (e: IOException) {
                e.printStackTrace()
                if (showToast) {
                    mainHandler.post {
                        Toast.makeText(context, "IO error occurred", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                if (showToast) {
                    mainHandler.post {
                        Toast.makeText(context, "Permission denied", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                if (showToast) {
                    mainHandler.post {
                        Toast.makeText(context, "Operation interrupted", Toast.LENGTH_LONG).show()
                    }
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

        // If we've exhausted all retries, show toast on the main thread
        if (showToast) {
            mainHandler.post {
                Toast.makeText(context, "Retry failed", Toast.LENGTH_LONG).show()
            }
        }
    }


}

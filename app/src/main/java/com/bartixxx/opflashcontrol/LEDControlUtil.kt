package com.bartixxx.opflashcontrol

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import java.io.DataOutputStream
import java.io.IOException

/**
 * A controller for the flashlight LEDs.
 *
 * This class provides methods for controlling the flashlight LEDs.
 *
 * @param context The context.
 */
class LedController(private val context: Context) {
    
    val isRootAvailable: Boolean = RootDetector.isRootAvailable()
    private val fallbackController: FlashlightFallbackController? = 
        if (!isRootAvailable && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            FlashlightFallbackController(context)
        } else null

    init {
        Log.d("LedController", "Root available: $isRootAvailable")
        if (!isRootAvailable && fallbackController != null) {
            Log.d("LedController", "Using fallback controller with ${fallbackController.getMaxTorchLevel()} torch levels")
        }
    }

    /**
     * Sanitizes a brightness value to ensure it is not 0.
     *
     * @param brightness The brightness value to sanitize.
     * @return The sanitized brightness value.
     */
    private fun sanitizeBrightness(brightness: Int): Int {
        return if (brightness == 0) 1 else brightness
    }
    
    /**
     * Gets the maximum torch level available for non-root devices.
     * Returns 1 if root is available or fallback is not supported.
     */
    fun getMaxTorchLevel(): Int {
        return fallbackController?.getMaxTorchLevel() ?: 1
    }

    /**
     * Controls the flashlight LEDs.
     *
     * @param action The action to perform. Can be "on" or "off".
     * @param whiteLedPath The path to the white LED brightness file.
     * @param yellowLedPath The path to the yellow LED brightness file.
     * @param togglePaths A list of paths to the LED toggle files.
     * @param white2LedPath The path to the second white LED brightness file.
     * @param yellow2LedPath The path to the second yellow LED brightness file.
     * @param whiteBrightness The brightness of the white LED.
     * @param yellowBrightness The brightness of the yellow LED.
     * @param white2Brightness The brightness of the second white LED.
     * @param yellow2Brightness The brightness of the second yellow LED.
     * @param showToast Whether to show a toast message when the command is executed.
     */
    fun controlLeds(
        action: String,
        whiteLedPath: String,
        yellowLedPath: String,
        togglePaths: List<String>,
        white2LedPath: String? = null,
        yellow2LedPath: String? = null,
        whiteBrightness: Int,
        yellowBrightness: Int,
        white2Brightness: Int = 0,
        yellow2Brightness: Int = 0,
        showToast: Boolean = true
    ) {
        // Use fallback controller if root is not available
        if (!isRootAvailable && fallbackController != null) {
            if (action == "on") {
                // Use the maximum of white and yellow brightness for the torch level
                val maxBrightness = maxOf(whiteBrightness, yellowBrightness)
                val torchLevel = fallbackController.mapBrightnessToTorchLevel(maxBrightness)
                fallbackController.turnOnTorch(torchLevel)
            } else {
                fallbackController.turnOffTorch()
            }
            return
        }
        
        // Root path: use original implementation
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
                    togglePaths,
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
                    yellow2LedPath,
                    togglePaths
                )
            )
        }

        executeRootCommands(commands, showToast)
    }

    /**
     * Returns a list of commands to turn on the flashlight LEDs.
     *
     * @param white The path to the white LED brightness file.
     * @param yellow The path to the yellow LED brightness file.
     * @param white2 The path to the second white LED brightness file.
     * @param yellow2 The path to the second yellow LED brightness file.
     * @param togglePaths A list of paths to the LED toggle files.
     * @param whiteBrightness The brightness of the white LED.
     * @param yellowBrightness The brightness of the yellow LED.
     * @param white2Brightness The brightness of the second white LED.
     * @param yellow2Brightness The brightness of the second yellow LED.
     * @return A list of commands to turn on the flashlight LEDs.
     */
    private fun commonOnCommands(
        white: String, yellow: String, white2: String?, yellow2: String?,
        togglePaths: List<String>,
        whiteBrightness: Int, yellowBrightness: Int, white2Brightness: Int, yellow2Brightness: Int
    ): List<String> {
        val commands = mutableListOf<String>()

        // Reset toggle paths to 0, then back to 255 to ensure proper refresh
        togglePaths.forEach {
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

        togglePaths.forEach {
            commands.add("echo 255 > $it")
        }

        return commands
    }

    /**
     * Returns a list of commands to turn off the flashlight LEDs.
     *
     * @param white The path to the white LED brightness file.
     * @param yellow The path to the yellow LED brightness file.
     * @param white2 The path to the second white LED brightness file.
     * @param yellow2 The path to the second yellow LED brightness file.
     * @param togglePaths A list of paths to the LED toggle files.
     * @return A list of commands to turn off the flashlight LEDs.
     */
    private fun commonOffCommands(
        white: String, yellow: String, white2: String?, yellow2: String?,
        togglePaths: List<String>
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
        togglePaths.forEach {
            commands.add("echo 0 > $it")
        }

        return commands
    }


    /**
     * Executes a list of commands as root.
     *
     * @param commands The commands to execute.
     * @param showToast Whether to show a toast message when the command is executed.
     */
    internal fun executeRootCommands(commands: List<String>, showToast: Boolean = true) {
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
                Log.d("LedController", "Executing commands: $commands")
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

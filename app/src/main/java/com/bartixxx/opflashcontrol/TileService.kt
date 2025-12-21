package com.bartixxx.opflashcontrol

import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.bartixxx.opflashcontrol.LedPaths.TOGGLE_PATHS
import com.bartixxx.opflashcontrol.LedPaths.WHITE_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.YELLOW_LED_PATH
import kotlinx.coroutines.*

/**
 * A Quick Settings tile service for controlling the flashlight LEDs.
 *
 * This service provides a Quick Settings tile that allows the user to control the flashlight LEDs.
 */
class LEDControlTileService : TileService() {

    // currentBrightnessState: 0 means off; otherwise, 1-indexed into brightnessSteps.
    private var currentBrightnessState = 0
    private lateinit var ledController: LedController
    private var brightnessExceededTime: Long = 0L
    private var safetyTriggered = false
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 500L // Check every 500 milliseconds

    // Define brightness steps as absolute values.
    // We now include a low step of 20, along with 50, 80, 150, 250, and 500.
    private val brightnessSteps = listOf(10, 20, 50, 80, 150, 250, 500)
    private val maxBrightness = 500

    private var lastTapTime: Long = 0L // For double-tap detection.
    private val doubleTapInterval = 300L // Time window for detecting double-tap (in milliseconds)

    // Create a coroutine scope for offloading blocking operations.
    // This scope will be cancelled when the service stops.
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    /**
     * Called when the tile is added to the Quick Settings panel.
     */
    override fun onStartListening() {
        super.onStartListening()
        try {
            ledController = LedController(this)
            updateTileDescription()
            startSafetyCheck()
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during onStartListening", e)
        }
    }

    /**
     * Called when the tile is removed from the Quick Settings panel.
     */
    override fun onStopListening() {
        super.onStopListening()
        stopSafetyCheck()
        // Cancel any ongoing coroutines when the service stops.
        serviceJob.cancel()
    }

    /**
     * Called when the user clicks the tile.
     */
    override fun onClick() {
        super.onClick()
        val currentTime = System.currentTimeMillis()

        try {
            if (currentTime - lastTapTime < doubleTapInterval) {
                // Double-tap detected.
                if (currentBrightnessState == 0 || currentBrightnessState == 1) {
                    // When off (or at the very first step), double-tap sets brightness to 80.
                    Log.d("LEDControlTileService", "Double-tap detected while off, turning LEDs on at 80.")
                    currentBrightnessState = brightnessSteps.indexOf(80) + 1
                    controlAllLeds(80)
                    updateTileDescription()
                } else {
                    // Otherwise, double-tap turns the LEDs off.
                    Log.d("LEDControlTileService", "Double-tap detected, turning off LEDs.")
                    turnOffLeds()
                }
            } else {
                // Single tap: cycle through brightness states.
                VibrationUtil.vibrate(this, 50L)

                if (currentBrightnessState == 0) {
                    // If currently off, turn on at default brightness
                    val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
                    val defaultBrightness = prefs.getInt(Constants.KEY_DEFAULT_BRIGHTNESS, 80) // Default to 80 if not set

                    // Find the index of the closest step in brightnessSteps
                    // Find closest value
                    val closestStep = brightnessSteps.minByOrNull { kotlin.math.abs(it - defaultBrightness) } ?: 80
                    val closestIndex = brightnessSteps.indexOf(closestStep)

                    currentBrightnessState = closestIndex + 1
                    // Turn on at the explicit default brightness, but track state as the closest step
                    Log.d("LEDControlTileService", "Turning on at default brightness: $defaultBrightness (closest step: $closestStep)")

                    controlAllLeds(defaultBrightness)
                } else {
                    // Cycle through states (0 = off, then each brightness step).
                    currentBrightnessState = (currentBrightnessState + 1) % (brightnessSteps.size + 1)
                    val brightnessValue = if (currentBrightnessState == 0) 0
                    else brightnessSteps[currentBrightnessState - 1]
                    controlAllLeds(brightnessValue)
                }

                updateTileDescription()
                Log.d("LEDControlTileService", "Brightness state changed to $currentBrightnessState")
            }
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during onClick", e)
            // Turn off LEDs if something goes wrong.
            turnOffLeds()
        }

        lastTapTime = currentTime // Update the last tap time.
    }

    /**
     * Turns off the flashlight LEDs.
     */
    private fun turnOffLeds() {
        try {
            currentBrightnessState = 0
            controlAllLeds(0) // Turn off LEDs.
            updateTileDescription()
            VibrationUtil.vibrate(this, 100L) // Provide haptic feedback.
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error turning off LEDs", e)
        }
    }

    /**
     * Starts the brightness safety check.
     */
    private fun startSafetyCheck() {
        handler.post(object : Runnable {
            override fun run() {
                try {
                    checkBrightnessSafety()
                } catch (e: Exception) {
                    Log.e("LEDControlTileService", "Error during safety check", e)
                }
                handler.postDelayed(this, checkInterval)
            }
        })
    }

    /**
     * Stops the brightness safety check.
     */
    private fun stopSafetyCheck() {
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Checks if the brightness of the flashlight LEDs is within a safe range.
     */
    private fun checkBrightnessSafety() {
        try {
            val prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
            if (prefs.getBoolean(Constants.KEY_SAFETY_AWARE, false)) {
                brightnessExceededTime = 0L
                return
            }

            val currentTime = System.currentTimeMillis()
            val brightnessValue = if (currentBrightnessState == 0) 0
            else brightnessSteps[currentBrightnessState - 1]

            // Trigger safety if the brightness (absolute value) is more than 120.
            if (brightnessValue > 120) {
                if (brightnessExceededTime == 0L) {
                    brightnessExceededTime = currentTime
                    Log.d("LEDControlTileService", "Brightness exceeded safe limit, timer started.")
                } else if (currentTime - brightnessExceededTime > 20000) { // More than 20 seconds.
                    if (!safetyTriggered) {
                        Log.d("LEDControlTileService", "Safety triggered! Reducing brightness to 80.")
                        safetyTriggered = true
                        currentBrightnessState = brightnessSteps.indexOf(80) + 1
                        controlAllLeds(80)
                        updateTileDescription()
                    }
                }
            } else {
                // Reset if brightness is within safe range.
                brightnessExceededTime = 0L
                safetyTriggered = false
            }
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during brightness safety check", e)
        }
    }

    /**
     * Updates the tile's description.
     */
    private fun updateTileDescription() {
        try {
            val stateText = when (currentBrightnessState) {
                0 -> getString(R.string.double_tap_to_turn_off)
                else -> {
                    val brightnessValue = brightnessSteps[currentBrightnessState - 1]
                    // Calculate percentage relative to the maximum brightness.
                    val brightnessPercentage = brightnessValue * 100 / maxBrightness
                    "${getString(R.string.brightness)}: $brightnessPercentage%"
                }
            }

            qsTile.label = if (currentBrightnessState == 0)
                getString(R.string.flashlight_off)
            else
                getString(R.string.flashlight_on)

            qsTile.icon = Icon.createWithResource(
                this,
                if (currentBrightnessState == 0) R.drawable.floff else R.drawable.flon
            )

            qsTile.subtitle = stateText
            qsTile.state = if (currentBrightnessState == 0) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            qsTile.updateTile()
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error updating tile description", e)
        }
    }

    /**
     * Controls the flashlight LEDs.
     *
     * @param brightness The brightness to set the LEDs to.
     */
    private fun controlAllLeds(brightness: Int) {
        // Offload LED control operations to the IO dispatcher so that blocking operations do not freeze the UI.
        serviceScope.launch(Dispatchers.IO) {
            try {
                if (brightness == 0) {
                    ledController.controlLeds(
                        "off",
                        WHITE_LED_PATH,
                        YELLOW_LED_PATH,
                        TOGGLE_PATHS,
                        whiteBrightness = 1,
                        yellowBrightness = 1,
                        showToast = false
                    )
                } else {
                    ledController.controlLeds(
                        "on",
                        WHITE_LED_PATH,
                        YELLOW_LED_PATH,
                        TOGGLE_PATHS,
                        whiteBrightness = brightness,
                        yellowBrightness = brightness,
                        showToast = false
                    )
                }
            } catch (e: Exception) {
                Log.e("LEDControlTileService", "Error controlling LEDs", e)
            }
        }
    }
}

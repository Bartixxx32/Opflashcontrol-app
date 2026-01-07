package com.bartixxx.opflashcontrol

import android.content.Context
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
    private val brightnessSteps = listOf(10, 20, 50, 80, 150, 250, 500)
    private val maxBrightness = 500

    private var lastTapTime: Long = 0L // For double-tap detection.
    private val doubleTapInterval = 300L // Time window for detecting double-tap (in milliseconds)

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var isBurnAware = false
    private var defaultBrightness = 80

    override fun onStartListening() {
        super.onStartListening()
        try {
            val prefs = getSharedPreferences("OpFlashControlPrefs", Context.MODE_PRIVATE)
            isBurnAware = prefs.getBoolean("burn_aware", false)
            defaultBrightness = prefs.getInt("default_brightness", 80)

            ledController = LedController(this)
            updateTileDescription()
            startSafetyCheck()
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during onStartListening", e)
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        stopSafetyCheck()
        serviceJob.cancel()
    }

    override fun onClick() {
        super.onClick()
        val currentTime = System.currentTimeMillis()

        try {
            if (currentTime - lastTapTime < doubleTapInterval) {
                // Double-tap detected.
                if (currentBrightnessState == 0) {
                    // When off, double-tap sets brightness to default.
                    Log.d("LEDControlTileService", "Double-tap detected while off, turning LEDs on at $defaultBrightness.")
                    controlAllLeds(defaultBrightness)
                    // Find closest step for state tracking or just set to a state
                    currentBrightnessState = (brightnessSteps.indexOfFirst { it >= defaultBrightness }.takeIf { it != -1 } ?: (brightnessSteps.size - 1)) + 1
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
                    // If turning on from OFF, use default brightness
                    currentBrightnessState = (brightnessSteps.indexOfFirst { it >= defaultBrightness }.takeIf { it != -1 } ?: (brightnessSteps.size - 1)) + 1
                    controlAllLeds(defaultBrightness)
                } else {
                    // Cycle through states
                    currentBrightnessState = (currentBrightnessState + 1) % (brightnessSteps.size + 1)
                    val brightnessValue = if (currentBrightnessState == 0) 0
                    else brightnessSteps[currentBrightnessState - 1]
                    controlAllLeds(brightnessValue)
                }
                updateTileDescription()
            }
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during onClick", e)
            turnOffLeds()
        }

        lastTapTime = currentTime
    }

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

    private fun stopSafetyCheck() {
        handler.removeCallbacksAndMessages(null)
    }

    private fun checkBrightnessSafety() {
        if (isBurnAware) return

        try {
            val currentTime = System.currentTimeMillis()
            val brightnessValue = if (currentBrightnessState == 0) 0
            else brightnessSteps[currentBrightnessState - 1]

            if (brightnessValue > 120) {
                if (brightnessExceededTime == 0L) {
                    brightnessExceededTime = currentTime
                } else if (currentTime - brightnessExceededTime > 20000) { // More than 20 seconds.
                    if (!safetyTriggered) {
                        safetyTriggered = true
                        // Revert to 80
                        currentBrightnessState = brightnessSteps.indexOf(80) + 1
                        controlAllLeds(80)
                        updateTileDescription()
                    }
                }
            } else {
                brightnessExceededTime = 0L
                safetyTriggered = false
            }
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during brightness safety check", e)
        }
    }

    private fun updateTileDescription() {
        try {
            val stateText = when (currentBrightnessState) {
                0 -> getString(R.string.double_tap_to_turn_off)
                else -> {
                    val brightnessValue = brightnessSteps[currentBrightnessState - 1]
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

    private fun controlAllLeds(brightness: Int) {
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

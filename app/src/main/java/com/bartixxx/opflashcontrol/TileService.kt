package com.bartixxx.opflashcontrol

import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.bartixxx.opflashcontrol.BaseActivity.Companion.WHITE_LED_PATH
import com.bartixxx.opflashcontrol.BaseActivity.Companion.YELLOW_LED_PATH

class LEDControlTileService : TileService() {
    private var currentBrightnessState = 0 // 0 = off, 1 = 10, 2 = 30, ... up to 255
    private lateinit var ledController: LedController
    private var brightnessExceededTime: Long = 0L
    private var safetyTriggered = false
    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 500L // Check every 500 milliseconds
    private val brightnessSteps =
        listOf(10, 30, 50, 80, 100, 120, 150, 180, 210, 240, 255) // Defined brightness steps

    private var lastTapTime: Long = 0L // Track time of last tap for double-tap detection
    private val doubleTapInterval = 300L // Time window for detecting double-tap (in milliseconds)

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

    override fun onStopListening() {
        super.onStopListening()
        stopSafetyCheck()
    }

    override fun onClick() {
        super.onClick()
        val currentTime = System.currentTimeMillis()

        try {
            if (currentTime - lastTapTime < doubleTapInterval) {
                // Double-tap detected
                if (currentBrightnessState == 0 || currentBrightnessState == 1) {
                    // Double-tap while off: Turn LEDs on at 80% brightness
                    Log.d(
                        "LEDControlTileService",
                        "Double-tap detected while off, turning LEDs on at 80%."
                    )
                    currentBrightnessState =
                        brightnessSteps.indexOf(80) + 1 // Set to 80% brightness
                    controlAllLeds(80)
                    updateTileDescription()
                } else {
                    // Double-tap while on: Turn off LEDs
                    Log.d("LEDControlTileService", "Double-tap detected, turning off LEDs.")
                    turnOffLeds()
                }
            } else {
                // Single tap logic (cycle through brightness levels)
                VibrationUtil.vibrate(this, 50L)
                // Ensure we cycle through brightness states
                currentBrightnessState =
                    (currentBrightnessState + 1) % (brightnessSteps.size + 1) // Includes 0 (off)
                updateTileDescription()

                // Log the current state
                Log.d(
                    "LEDControlTileService",
                    "Brightness state changed to $currentBrightnessState"
                )

                // Set brightness based on the current state
                val brightnessLevel =
                    if (currentBrightnessState == 0) 0 else brightnessSteps[currentBrightnessState - 1]
                controlAllLeds(brightnessLevel)
            }
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during onClick", e)
            // Handle exception gracefully by turning off the LEDs to avoid leaving them in an inconsistent state
            turnOffLeds()
        }

        lastTapTime = currentTime // Update the time of the last tap
    }

    private fun turnOffLeds() {
        try {
            currentBrightnessState = 0
            controlAllLeds(0) // Turn off LEDs
            updateTileDescription()
            VibrationUtil.vibrate(this, 100L) // Provide haptic feedback for turning off
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
        try {
            val currentTime = System.currentTimeMillis()
            val brightnessLevel =
                if (currentBrightnessState == 0) 0 else brightnessSteps[currentBrightnessState - 1]

            if (brightnessLevel >= 120) {
                if (brightnessExceededTime == 0L) {
                    brightnessExceededTime = currentTime
                    Log.d("LEDControlTileService", "Brightness exceeded safe limit, timer started.")
                } else if (currentTime - brightnessExceededTime > 20000) { // More than 20 seconds
                    if (!safetyTriggered) {
                        Log.d(
                            "LEDControlTileService",
                            "Safety triggered! Reducing brightness to 80."
                        )
                        safetyTriggered = true
                        currentBrightnessState = brightnessSteps.indexOf(80) + 1 // Set to 80%
                        controlAllLeds(80)
                        updateTileDescription()
                    }
                }
            } else {
                brightnessExceededTime = 0L // Reset the timer if within safe range
                safetyTriggered = false
            }
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error during brightness safety check", e)
        }
    }

    private fun updateTileDescription() {
        try {
            val stateText = when (currentBrightnessState) {
                0 -> getString(R.string.double_tap_to_turn_off) // Message when LEDs are off
                else -> (getString(R.string.brightness) + ": " + brightnessSteps[currentBrightnessState - 1]) + "%" // Brightness state
            }

            // Change the tile label to Flashlight ON/OFF
            qsTile.label =
                if (currentBrightnessState == 0) getString(R.string.flashlight_off) else getString(R.string.flashlight_on)

            // Set the icon based on the current state (ON or OFF)
            qsTile.icon = Icon.createWithResource(
                this,
                if (currentBrightnessState == 0) R.drawable.floff else R.drawable.flon
            )

            // Update tile subtitle and state
            qsTile.subtitle = stateText
            qsTile.state =
                if (currentBrightnessState == 0) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            qsTile.updateTile()
        } catch (e: Exception) {
            Log.e("LEDControlTileService", "Error updating tile description", e)
        }
    }

    private fun controlAllLeds(brightness: Int) {
        try {
            if (brightness == 0) {
                ledController.controlLeds(
                    "off",
                    WHITE_LED_PATH,
                    YELLOW_LED_PATH,
                    whiteBrightness = 1,
                    yellowBrightness = 1,
                    showToast = false
                )
            } else {
                ledController.controlLeds(
                    "on",
                    WHITE_LED_PATH,
                    YELLOW_LED_PATH,
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

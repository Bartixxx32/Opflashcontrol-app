package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bartixxx.opflashcontrol.LedPaths.TOGGLE_PATHS
import com.bartixxx.opflashcontrol.LedPaths.WHITE_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.YELLOW_LED_PATH
import com.bartixxx.opflashcontrol.databinding.ActivityExperimentalBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * An experimental activity that provides additional flashlight controls.
 *
 * This activity allows the user to experiment with different light cycles and brightness cycles.
 */
class ExperimentalActivity : BaseActivity() {

    private lateinit var binding: ActivityExperimentalBinding
    private val scope = CoroutineScope(Dispatchers.IO)
    private var lightJob: Job? = null
    private var brightnessJob: Job? = null

    private var lightCycleDelay: Long = 500  // Default delay
    private var lightCycleBrightness: Int = 500  // Default brightness
    private lateinit var ledController: LedController

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LedPathUtil.findLedPaths()
        ledController = LedController(this)
        binding = ActivityExperimentalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()

        // Set up slider for delay control with vibration feedback
        binding.delaySeekBar1.valueFrom = 10f
        binding.delaySeekBar1.value = 500f
        binding.delaySeekBar1.valueTo = 3000f // Delay range in milliseconds

        // Add the listener to update delay and TextView
        binding.delaySeekBar1.addOnChangeListener { _, value, _ ->
            lightCycleDelay = value.toLong()  // Set delay based on slider value
            // Vibrate on slider change
            VibrationUtil.vibrate(this, 50L)
            // Update the delayTextView with the current delay value
            binding.delayTextView2.text = "Delay: ${lightCycleDelay} ms"
        }

        // Initialize the TextView with the current delay
        binding.delayTextView2.text = "Delay: $lightCycleDelay ms"

        // Set up second slider for brightness control with vibration feedback
        binding.brightnessSeekBar.valueFrom = 0f
        binding.brightnessSeekBar.value = 80f
        binding.brightnessSeekBar.valueTo = 500f // Brightness range from 0 to 500

        // Add the listener to update brightness and TextView
        binding.brightnessSeekBar.addOnChangeListener { _, value, _ ->
            lightCycleBrightness = value.toInt()  // Set brightness based on slider value
            // Vibrate on slider change
            VibrationUtil.vibrate(this, 50L)
            // Update the brightnessTextView with the current brightness value
            binding.brightnessTextView.text = "Brightness: $lightCycleBrightness"
        }

        // Initialize the TextView with the current brightness
        binding.brightnessTextView.text = "Brightness: $lightCycleBrightness"
    }

    /**
     * Sets up the buttons in the activity.
     */
    private fun setupButtons() {
        binding.button1.setOnClickListener {
            Toast.makeText(this, getString(R.string.command_executed), Toast.LENGTH_SHORT).show()
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)

            if (!isLedOn) {
                isLedOn = true
                // Stop any active cycle before starting a new one
                stopBrightnessCycle()
                stopLightCycle()
                startLightCycle()  // Start the light cycle
            }
        }

        binding.button2.setOnClickListener {
            Toast.makeText(this, getString(R.string.command_executed), Toast.LENGTH_SHORT).show()
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)
            if (isLedOn) {
                isLedOn = false
                stopLightCycle()  // Stop light cycle
            }
        }

        binding.button3.setOnClickListener {
            Toast.makeText(this, getString(R.string.command_executed), Toast.LENGTH_SHORT).show()
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)

            if (!isLedOn) {
                isLedOn = true
                // Stop any active cycle before starting a new one
                stopLightCycle()
                stopBrightnessCycle()
                startBrightnessCycle()  // Start the brightness cycle
            }
        }

        binding.button4.setOnClickListener {
            Toast.makeText(this, getString(R.string.command_executed), Toast.LENGTH_SHORT).show()
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)
            if (isLedOn) {
                isLedOn = false
                stopBrightnessCycle()  // Stop brightness cycle
            }
        }

        binding.buttonBack.setOnClickListener {
            VibrationUtil.vibrate(this, 100L)
            navigateBackToMain()
        }
    }

    /**
     * Starts a light cycle that alternates between the white and yellow LEDs.
     */
    private fun startLightCycle() {
        stopLightCycle()
        lightJob = scope.launch {
            while (isActive) {
                ledController.controlLeds(
                    "on",
                    WHITE_LED_PATH,
                    YELLOW_LED_PATH,
                    TOGGLE_PATHS,
                    whiteBrightness = lightCycleBrightness,
                    yellowBrightness = 0,
                    showToast = false
                )
                delay(lightCycleDelay)
                ledController.controlLeds(
                    "on",
                    WHITE_LED_PATH,
                    YELLOW_LED_PATH,
                    TOGGLE_PATHS,
                    whiteBrightness = 0,
                    yellowBrightness = lightCycleBrightness,
                    showToast = false
                )
                delay(lightCycleDelay)
            }
        }
    }

    /**
     * Stops the light cycle.
     */
    private fun stopLightCycle() {
        lightJob?.cancel()
        ledController.controlLeds(
            "off",
            WHITE_LED_PATH,
            YELLOW_LED_PATH,
            TOGGLE_PATHS,
            whiteBrightness = 0,
            yellowBrightness = 0,
            showToast = false
        )
    }

    /**
     * Starts a brightness cycle that gradually increases and decreases the brightness of the LEDs.
     */
    private fun startBrightnessCycle() {
        stopBrightnessCycle()
        brightnessJob = scope.launch {
            var brightness = 1
            var increment = 5
            while (isActive) {
                ledController.controlLeds(
                    "on",
                    WHITE_LED_PATH,
                    YELLOW_LED_PATH,
                    TOGGLE_PATHS,
                    whiteBrightness = brightness,
                    yellowBrightness = brightness,
                    showToast = false
                )
                delay(50)
                brightness += increment
                if (brightness > 500 || brightness < 1) {
                    increment *= -1
                    brightness += increment * 2
                }
            }
        }
    }

    /**
     * Stops the brightness cycle.
     */
    private fun stopBrightnessCycle() {
        brightnessJob?.cancel()
        ledController.controlLeds(
            "off",
            WHITE_LED_PATH,
            YELLOW_LED_PATH,
            TOGGLE_PATHS,
            whiteBrightness = 0,
            yellowBrightness = 0,
            showToast = false
        )
    }

    /**
     * Navigates back to the main activity.
     */
    private fun navigateBackToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Called when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

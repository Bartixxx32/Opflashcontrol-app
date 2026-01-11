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

        // Detect root availability and adapt UI
        val isRootAvailable = ledController.isRootAvailable
        val maxTorchLevel = if (isRootAvailable) 500 else ledController.getMaxTorchLevel()

        setupButtons()

        with(binding) {
            if (!isRootAvailable && maxTorchLevel > 1) {
                // Non-root mode: hide alternate ON/OFF (requires white/yellow control)
                button1.visibility = android.view.View.GONE
                button2.visibility = android.view.View.GONE
                
                // Hide brightness slider - brightness cycle uses full torch range
                brightnessSeekBar.visibility = android.view.View.GONE
                brightnessTextView.visibility = android.view.View.GONE
                
                // Hide brightness label (since brightness slider is hidden)
                findViewById<android.widget.TextView>(R.id.brightnessLabel).visibility = android.view.View.GONE
                
                // Show delay slider to control pulsing speed
                delaySeekBar1.valueFrom = 10f
                delaySeekBar1.value = 500f
                delaySeekBar1.valueTo = 3000f
                lightCycleDelay = 500
                delayTextView2.text = "Delay: ${lightCycleDelay} ms"
            } else {
                // Root mode: standard configuration
                // Set up slider for delay control with vibration feedback
                delaySeekBar1.valueFrom = 10f
                delaySeekBar1.value = 500f
                delaySeekBar1.valueTo = 3000f // Delay range in milliseconds
                
                // Set up second slider for brightness control with vibration feedback
                brightnessSeekBar.valueFrom = 0f
                brightnessSeekBar.value = 80f
                brightnessSeekBar.valueTo = 500f // Brightness range from 0 to 500
                lightCycleBrightness = 80
            }

            // Add the listener to update delay and TextView
            delaySeekBar1.addOnChangeListener { _, value, _ ->
                lightCycleDelay = value.toLong()  // Set delay based on slider value
                // Vibrate on slider change
                VibrationUtil.vibrate(this@ExperimentalActivity, 50L)
                // Update the delayTextView with the current delay value
                delayTextView2.text = "Delay: ${lightCycleDelay} ms"
            }

            // Initialize the TextView with the current delay
            delayTextView2.text = "Delay: $lightCycleDelay ms"

            // Add the listener to update brightness and TextView
            brightnessSeekBar.addOnChangeListener { _, value, _ ->
                lightCycleBrightness = value.toInt()  // Set brightness based on slider value
                // Vibrate on slider change
                VibrationUtil.vibrate(this@ExperimentalActivity, 50L)
                // Update the brightnessTextView with the current brightness value
                brightnessTextView.text = "Brightness: $lightCycleBrightness"
            }

            // Initialize the TextView with the current brightness
            brightnessTextView.text = "Brightness: $lightCycleBrightness"
        }
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
            val isRootAvailable = ledController.isRootAvailable
            val maxBrightness = if (isRootAvailable) 500 else ledController.getMaxTorchLevel()
            val minBrightness = 1
            
            var brightness = minBrightness
            var increment = if (isRootAvailable) 5 else 1
            
            while (isActive) {
                ledController.controlLeds(
                    "on",
                    WHITE_LED_PATH,
                    YELLOW_LED_PATH,
                    TOGGLE_PATHS,
                    whiteBrightness = if (isRootAvailable) brightness else (brightness * 500 / maxBrightness),
                    yellowBrightness = if (isRootAvailable) brightness else (brightness * 500 / maxBrightness),
                    showToast = false
                )
                // Read delay from slider in each iteration so changes apply immediately
                delay(if (isRootAvailable) 50L else lightCycleDelay)
                brightness += increment
                if (brightness > maxBrightness || brightness < minBrightness) {
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

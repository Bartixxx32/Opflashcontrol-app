package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bartixxx.opflashcontrol.databinding.ActivityExperimentalBinding
import java.lang.Thread.sleep

class ExperimentalActivity : BaseActivity() {

    private lateinit var binding: ActivityExperimentalBinding
    private var lightThread: Thread? = null
    private var brightnessThread: Thread? = null

    private var lightCycleDelay: Long = 500  // Default delay
    private var lightCycleBrightness: Int = 255  // Default brightness
    private lateinit var ledController: LedController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.brightnessSeekBar.value = 255f
        binding.brightnessSeekBar.valueTo = 255f // Brightness range from 0 to 255

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

    private fun startLightCycle() {
        Log.d("ExperimentalActivity", "Starting light cycle")
        // Ensure previous thread is stopped and interrupted
        stopLightCycle()

        lightThread = Thread {
            while (isLedOn) {
                try {
                    Log.d("ExperimentalActivity", "Light cycle: white LED on")
                    // Using the brightness from the slider
                    ledController.controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = lightCycleBrightness, yellowBrightness = 0, showToast = false)
                    sleep(lightCycleDelay)  // Use slider value for delay
                    Log.d("ExperimentalActivity", "Light cycle: yellow LED on")
                    ledController.controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = 0, yellowBrightness = lightCycleBrightness, showToast = false)
                    sleep(lightCycleDelay)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    Log.d("ExperimentalActivity", "Light thread was interrupted")
                    break
                }
            }
        }
        lightThread?.start()
    }

    private fun stopLightCycle() {
        Log.d("ExperimentalActivity", "Stopping light cycle")
        // Interrupt the current light cycle thread, if any
        if (lightThread?.isAlive == true) {
            lightThread?.interrupt()
            Log.d("ExperimentalActivity", "Light thread interrupted")
        }
        lightThread = null
        ledController.controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = 0, yellowBrightness = 0, showToast = false)  // Turn LEDs off
    }

    private fun startBrightnessCycle() {
        Log.d("ExperimentalActivity", "Starting brightness cycle")
        // Ensure previous thread is stopped and interrupted
        stopBrightnessCycle()

        brightnessThread = Thread {
            var brightness = 1
            var increment = 5
            while (isLedOn) {
                try {
                    Log.d("ExperimentalActivity", "Brightness cycle: setting brightness to $brightness")
                    // Use the brightness from the slider
                    ledController.controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = brightness, yellowBrightness = brightness, showToast = false)
                    sleep(50) // Small delay for smooth transition
                    brightness += increment
                    if (brightness > 255 || brightness < 1) {
                        increment *= -1
                        brightness += increment * 2
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    Log.d("ExperimentalActivity", "Brightness thread was interrupted")
                    break
                }
            }
        }
        brightnessThread?.start()
    }

    private fun stopBrightnessCycle() {
        Log.d("ExperimentalActivity", "Stopping brightness cycle")
        // Interrupt the current brightness cycle thread, if any
        if (brightnessThread?.isAlive == true) {
            brightnessThread?.interrupt()
            Log.d("ExperimentalActivity", "Brightness thread interrupted")
        }
        brightnessThread = null
        ledController.controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = 0, yellowBrightness = 0, showToast = false)  // Turn LEDs off
    }

    private fun navigateBackToMain() {
        Log.d("ExperimentalActivity", "Navigating back to main activity")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

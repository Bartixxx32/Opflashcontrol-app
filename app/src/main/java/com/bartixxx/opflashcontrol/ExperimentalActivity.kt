package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import android.util.Log

import com.bartixxx.opflashcontrol.databinding.ActivityExperimentalBinding
import java.lang.Thread.sleep

class ExperimentalActivity : BaseActivity() {

    private lateinit var binding: ActivityExperimentalBinding
    private var lightThread: Thread? = null
    private var brightnessThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExperimentalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.button1.setOnClickListener {
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)
            if (!isLedOn) {
                isLedOn = true
                startLightCycle()
            }
        }

        binding.button2.setOnClickListener {
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)
            if (isLedOn) {
                isLedOn = false
                stopLightCycle()
            }
        }

        binding.button3.setOnClickListener {
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)
            if (!isLedOn) {
                isLedOn = true
                startBrightnessCycle()
            }
        }

        binding.button4.setOnClickListener {
            VibrationUtil.vibrate(this@ExperimentalActivity, 100L)
            if (isLedOn) {
                isLedOn = false
                stopBrightnessCycle()
            }
        }

        binding.buttonBack.setOnClickListener {
            VibrationUtil.vibrate(this, 100L)
            navigateBackToMain()
        }
    }

    private fun startLightCycle() {
        lightThread = Thread {
            val maxBrightness = 255  // Assuming 255 is max brightness, adjust if different
            while (isLedOn) {
                try {
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = maxBrightness, yellowBrightness = 0)
                    sleep(500)  // Half-second flash duration
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = 0, yellowBrightness = maxBrightness)
                    sleep(500)
                } catch (e: InterruptedException) {
                    // Thread was interrupted, so we exit the loop
                    Thread.currentThread().interrupt() // Restore the interrupted status
                    Log.d("ExperimentalActivity", "Light thread was interrupted")
                    break
                }
            }
        }
        lightThread?.start()
    }

    private fun stopLightCycle() {
        isLedOn = false
        lightThread?.interrupt()
        lightThread = null
        controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = 0, yellowBrightness = 0)  // Turn LEDs off
    }

    private fun startBrightnessCycle() {
        brightnessThread = Thread {
            var brightness = 1
            var increment = 5
            while (isLedOn) {
                try {
                    // Here we're not turning the LED off, just changing its brightness
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = brightness, yellowBrightness = brightness)
                    sleep(50) // Small delay for smooth transition

                    brightness += increment
                    if (brightness > 255 || brightness < 1) {
                        increment *= -1 // Change direction when brightness hits limits
                        brightness += increment * 2 // Adjust to skip 256 or -1
                    }
                } catch (e: InterruptedException) {
                    // Thread was interrupted, so we exit the loop
                    Thread.currentThread().interrupt() // Restore the interrupted status
                    Log.d("ExperimentalActivity", "Brightness thread was interrupted")
                    break
                }
            }
        }
        brightnessThread?.start()
    }

    private fun stopBrightnessCycle() {
        isLedOn = false
        brightnessThread?.interrupt()
        brightnessThread = null
        controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = 0, yellowBrightness = 0)  // Turn LEDs off
    }

    private fun navigateBackToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
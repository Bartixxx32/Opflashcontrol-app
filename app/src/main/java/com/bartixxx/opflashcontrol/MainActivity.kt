package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bartixxx.opflashcontrol.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private var clickCount = 0
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the range of the sliders
        with(binding) {
            masterSeekBar.valueFrom = 0f
            masterSeekBar.value = 80f
            masterSeekBar.valueTo = 255f

            whiteSeekBar.valueFrom = 0f
            whiteSeekBar.valueTo = 255f

            yellowSeekBar.valueFrom = 0f
            yellowSeekBar.valueTo = 255f

            setupSlider(masterSeekBar, masterTextView, "Master Brightness") { progress ->
                masterBrightness = progress
                if (isLedOn && whiteBrightness <= 1 && yellowBrightness <= 1) {
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = progress, yellowBrightness = progress)
                }
            }

            setupSlider(whiteSeekBar, whiteTextView, "White Brightness") { progress ->
                whiteBrightness = progress
                if (isLedOn) {
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = progress, yellowBrightness = yellowBrightness)
                }
            }

            setupSlider(yellowSeekBar, yellowTextView, "Yellow Brightness") { progress ->
                yellowBrightness = progress
                if (isLedOn) {
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = whiteBrightness, yellowBrightness = progress)
                }
            }

            on.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                toggleLEDs(true)
            }
            off.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                toggleLEDs(false)
            }
            destroyer.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                executeExtraFunction()
            }
            navigateToMainActivity2.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                navigateToMainActivity2()
            }

            // Set up click listener for the title which requires 5 clicks within 5 seconds
            flashbrightness.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                VibrationUtil.vibrate(this@MainActivity, 100L)
                if (currentTime - lastClickTime > 5000) {
                    clickCount = 1
                } else {
                    clickCount++
                }

                lastClickTime = currentTime

                if (clickCount == 5) {
                    // Perform the action after 3 clicks within 5 seconds
                    performSecretAction()
                    // Reset counter
                    clickCount = 0
                } else {
                    // Optionally, you can show feedback for each click if desired
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (clickCount < 5) {
                            clickCount = 0
                        }
                    }, 5000)
                }
            }
        }
    }

    private fun toggleLEDs(on: Boolean) {
        isLedOn = on
        if (on) {
            controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH,
                whiteBrightness = if (whiteBrightness == 0) masterBrightness else whiteBrightness,
                yellowBrightness = if (yellowBrightness == 0) masterBrightness else yellowBrightness
            )
        } else {
            controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = 1, yellowBrightness = 1)
        }
    }

    private fun executeExtraFunction() {
        controlLeds("off", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, whiteBrightness = 1000, yellowBrightness = 1000)
        controlLeds("on", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, whiteBrightness = 1500, yellowBrightness = 1500)
        isLedOn = true
    }

    private fun navigateToMainActivity2() {
        startActivity(Intent(this, MainActivity2::class.java))
    }

    private fun performSecretAction() {
        VibrationUtil.vibrate(this, 200L)
        Toast.makeText(this, getString(R.string.experimental), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ExperimentalActivity::class.java))
    }
}
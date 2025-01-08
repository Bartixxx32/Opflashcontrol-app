package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import com.bartixxx.opflashcontrol.databinding.ActivityMainBinding
import com.google.android.material.slider.Slider
import android.widget.TextView


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the range of the sliders
        with(binding) {
            masterSeekBar.valueFrom = 0f
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
                VibrationUtil.vibrate100(this@MainActivity)
                toggleLEDs(true)
            }
            off.setOnClickListener {
                VibrationUtil.vibrate100(this@MainActivity)
                toggleLEDs(false)
            }
            destroyer.setOnClickListener {
                VibrationUtil.vibrate100(this@MainActivity)
                executeExtraFunction()
            }
            navigateToMainActivity2.setOnClickListener {
                VibrationUtil.vibrate100(this@MainActivity)
                navigateToMainActivity2()
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
}

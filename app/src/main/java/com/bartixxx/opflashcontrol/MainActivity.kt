package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import com.bartixxx.opflashcontrol.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            setupSeekBar(masterSeekBar, masterTextView, "Master Brightness") { progress ->
                masterBrightness = progress
                if (isLedOn && whiteBrightness <= 1 && yellowBrightness <= 1) {
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = progress, yellowBrightness = progress)
                }
            }

            setupSeekBar(whiteSeekBar, whiteTextView, "White Brightness") { progress ->
                whiteBrightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = progress, yellowBrightness = yellowBrightness)
            }

            setupSeekBar(yellowSeekBar, yellowTextView, "Yellow Brightness") { progress ->
                yellowBrightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, whiteBrightness = whiteBrightness, yellowBrightness = progress)
            }

            on.setOnClickListener { toggleLEDs(true) }
            off.setOnClickListener { toggleLEDs(false) }
            destroyer.setOnClickListener { executeExtraFunction() }
            navigateToMainActivity2.setOnClickListener { navigateToMainActivity2() }
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
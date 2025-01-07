package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import com.bartixxx.opflashcontrol.databinding.ActivityMain2Binding

class MainActivity2 : BaseActivity() {

    private lateinit var binding: ActivityMain2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            setupSeekBar(masterSeekBar, masterTextView, "Master Brightness") { progress ->
                masterBrightness = progress
                if (isLedOn && allBrightnessAtOne()) {
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, progress, progress, progress, progress)
                }
            }

            setupSeekBar(whiteSeekBar, whiteTextView, "White Brightness") { progress ->
                whiteBrightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, progress, yellowBrightness, white2Brightness, yellow2Brightness)
            }

            setupSeekBar(yellowSeekBar, yellowTextView, "Yellow Brightness") { progress ->
                yellowBrightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, whiteBrightness, progress, white2Brightness, yellow2Brightness)
            }

            setupSeekBar(white2SeekBar2, white2TextView3, "White2 Brightness") { progress ->
                white2Brightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, whiteBrightness, yellowBrightness, progress, yellow2Brightness)
            }

            setupSeekBar(yellow2SeekBar3, yellow2TextView2, "Yellow2 Brightness") { progress ->
                yellow2Brightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, whiteBrightness, yellowBrightness, white2Brightness, progress)
            }

            on.setOnClickListener { toggleLEDs(true) }
            off.setOnClickListener { toggleLEDs(false) }
            destroyer.setOnClickListener { executeExtraFunction() }
            navigateBackToMainActivity.setOnClickListener { navigateBackToMainActivity() }
        }
    }

    private fun allBrightnessAtOne() = whiteBrightness <= 1 && yellowBrightness <= 1 && white2Brightness <= 1 && yellow2Brightness <= 1

    private fun toggleLEDs(on: Boolean) {
        isLedOn = on
        if (on) {
            controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH,
                if (whiteBrightness == 0) masterBrightness else whiteBrightness,
                if (yellowBrightness == 0) masterBrightness else yellowBrightness,
                if (white2Brightness == 0) masterBrightness else white2Brightness,
                if (yellow2Brightness == 0) masterBrightness else yellow2Brightness
            )
        } else {
            controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, 1, 1, 1, 1)
        }
    }

    private fun executeExtraFunction() {
        controlLeds("off", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, FLASH_WHITE2_LED_PATH, FLASH_YELLOW2_LED_PATH, 1000, 1000, 1000, 1000)
        controlLeds("on", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, FLASH_WHITE2_LED_PATH, FLASH_YELLOW2_LED_PATH, 1500, 1500, 1500, 1500)
        isLedOn = true
    }

    private fun navigateBackToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}
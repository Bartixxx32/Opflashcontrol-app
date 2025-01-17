package com.bartixxx.opflashcontrol

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bartixxx.opflashcontrol.MainActivity
import com.bartixxx.opflashcontrol.databinding.ActivityMain2Binding

class MainActivity2 : BaseActivity() {

    private lateinit var binding: ActivityMain2Binding
    private var clickCount = 0
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
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

            white2SeekBar2.valueFrom = 0f
            white2SeekBar2.valueTo = 255f

            yellow2SeekBar3.valueFrom = 0f
            yellow2SeekBar3.valueTo = 255f

            setupSlider(masterSeekBar, masterTextView, "Master Brightness") { progress ->
                masterBrightness = progress
                if (isLedOn && allBrightnessAtOne()) {
                    controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, progress, progress, progress, progress)
                }
            }

            setupSlider(whiteSeekBar, whiteTextView, "White Brightness") { progress ->
                whiteBrightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, progress, yellowBrightness, white2Brightness, yellow2Brightness)
            }

            setupSlider(yellowSeekBar, yellowTextView, "Yellow Brightness") { progress ->
                yellowBrightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, whiteBrightness, progress, white2Brightness, yellow2Brightness)
            }

            setupSlider(white2SeekBar2, white2TextView3, "White2 Brightness") { progress ->
                white2Brightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, whiteBrightness, yellowBrightness, progress, yellow2Brightness)
            }

            setupSlider(yellow2SeekBar3, yellow2TextView2, "Yellow2 Brightness") { progress ->
                yellow2Brightness = progress
                if (isLedOn) controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, whiteBrightness, yellowBrightness, white2Brightness, progress)
            }

            on.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity2, 100L)
                toggleLEDs(true)
            }
            off.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity2, 100L)
                toggleLEDs(false)
            }
            destroyer.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity2, 100L)
                executeExtraFunction()
            }
            navigateBackToMainActivity.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity2, 100L)
                navigateBackToMainActivity()
            }
            // Set up click listener for the title which requires 5 clicks within 5 seconds
            flashbrightness.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                VibrationUtil.vibrate(this@MainActivity2, 100L)

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
            // Add clickable and holdable behavior for the MaterialTextView
            buymecoffe.setOnClickListener {
                val delayBetweenVibrations = 100L // Delay between each vibration in milliseconds
                val vibrationstrenght = 100L
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/bartixxx32"))
                startActivity(browserIntent)

                // First vibration
                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity2, vibrationstrenght)
                }, 0)

                // Second vibration
                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity2, vibrationstrenght)
                }, delayBetweenVibrations)

                // Third vibration
                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity2, vibrationstrenght)
                }, delayBetweenVibrations * 2)

                // Redirect to the webpage after all vibrations
                Handler(Looper.getMainLooper()).postDelayed({
                }, delayBetweenVibrations * 3)
            }

            buymecoffe.setOnLongClickListener {
                // Navigate to another activity
                val intent = Intent(this@MainActivity2, SupportersActivity::class.java)
                startActivity(intent)
                true // Consume the long-click event
            }
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
    private fun performSecretAction() {
        VibrationUtil.vibrate(this, 200L)
        Toast.makeText(this, getString(R.string.experimental), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ExperimentalActivity::class.java))
    }
}

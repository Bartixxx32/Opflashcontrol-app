package com.bartixxx.opflashcontrol

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bartixxx.opflashcontrol.LedPaths.FLASH_WHITE_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.FLASH_YELLOW_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.TOGGLE_PATHS
import com.bartixxx.opflashcontrol.LedPaths.WHITE_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.YELLOW_LED_PATH
import com.bartixxx.opflashcontrol.databinding.ActivityMainBinding

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private var clickCount = 0
    private var lastClickTime: Long = 0
    private var brightnessCheckHandler: Handler? = null
    private var brightnessCheckRunnable: Runnable? = null
    private var brightnessExceededTime: Long = 0
    private var safetyTriggered = false
    private val MAX_BRIGHTNESS = 120
    private val SAFE_BRIGHTNESS = 80
    private val CHECK_INTERVAL = 200L // 200 ms
    private var eyeDestroyerCooldown = false // Flag to track cooldown
    private lateinit var ledController: LedController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LedPathUtil.findLedPaths()
        ledController = LedController(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the brightness check handler
        brightnessCheckHandler = Handler(Looper.getMainLooper())
        brightnessCheckRunnable = Runnable {
            checkBrightnessSafety()
            brightnessCheckHandler?.postDelayed(brightnessCheckRunnable!!, CHECK_INTERVAL)
        }
        brightnessCheckHandler?.postDelayed(brightnessCheckRunnable!!, CHECK_INTERVAL)

        // Set the range of the sliders
        with(binding) {
            masterSeekBar.valueFrom = 0f
            masterSeekBar.value = 80f
            masterSeekBar.valueTo = 500f

            whiteSeekBar.valueFrom = 0f
            whiteSeekBar.valueTo = 500f

            yellowSeekBar.valueFrom = 0f
            yellowSeekBar.valueTo = 500f

            setupSlider(masterSeekBar, masterTextView, "Master Brightness") { progress ->
                if (!safetyTriggered) {
                    masterBrightness = progress
                    if (isLedOn && whiteBrightness <= 1 && yellowBrightness <= 1) {
                        ledController.controlLeds(
                            "on",
                            LedPaths.WHITE_LED_PATH,
                            LedPaths.YELLOW_LED_PATH,
                            LedPaths.TOGGLE_PATHS,
                            whiteBrightness = progress,
                            yellowBrightness = progress
                        )
                    }
                }
            }

            setupSlider(whiteSeekBar, whiteTextView, "White Brightness") { progress ->
                if (!safetyTriggered) {
                    whiteBrightness = progress
                    if (isLedOn) {
                        ledController.controlLeds(
                            "on",
                            LedPaths.WHITE_LED_PATH,
                            LedPaths.YELLOW_LED_PATH,
                            LedPaths.TOGGLE_PATHS,
                            whiteBrightness = progress,
                            yellowBrightness = yellowBrightness
                        )
                    }
                }
            }

            setupSlider(yellowSeekBar, yellowTextView, "Yellow Brightness") { progress ->
                if (!safetyTriggered) {
                    yellowBrightness = progress
                    if (isLedOn) {
                        ledController.controlLeds(
                            "on",
                            LedPaths.WHITE_LED_PATH,
                            LedPaths.YELLOW_LED_PATH,
                            LedPaths.TOGGLE_PATHS,
                            whiteBrightness = whiteBrightness,
                            yellowBrightness = progress
                        )
                    }
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
            // Add clickable and holdable behavior for the MaterialTextView
            buymecoffe.setOnClickListener {
                val delayBetweenVibrations = 100L // Delay between each vibration in milliseconds
                val vibrationstrenght = 100L
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/bartixxx32"))
                startActivity(browserIntent)

                // First vibration
                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity, vibrationstrenght)
                }, 0)

                // Second vibration
                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity, vibrationstrenght)
                }, delayBetweenVibrations)

                // Third vibration
                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity, vibrationstrenght)
                }, delayBetweenVibrations * 2)

                // Redirect to the webpage after all vibrations
                Handler(Looper.getMainLooper()).postDelayed({
                }, delayBetweenVibrations * 3)
            }

            buymecoffe.setOnLongClickListener {
                // Navigate to another activity
                val intent = Intent(this@MainActivity, SupportersActivity::class.java)
                startActivity(intent)
                true // Consume the long-click event
            }
        }
    }

    private fun toggleLEDs(on: Boolean) {
        isLedOn = on
        if (on) {
            ledController.controlLeds(
                "on",
                LedPaths.WHITE_LED_PATH,
                LedPaths.YELLOW_LED_PATH,
                LedPaths.TOGGLE_PATHS,
                whiteBrightness = if (whiteBrightness == 0) masterBrightness else whiteBrightness,
                yellowBrightness = if (yellowBrightness == 0) masterBrightness else yellowBrightness
            )
        } else {
            ledController.controlLeds(
                "off",
                LedPaths.WHITE_LED_PATH,
                LedPaths.YELLOW_LED_PATH,
                LedPaths.TOGGLE_PATHS,
                whiteBrightness = 1,
                yellowBrightness = 1
            )
        }
    }

    private fun executeExtraFunction() {
        if (eyeDestroyerCooldown) {
            Toast.makeText(this, "Please wait before using this feature again.", Toast.LENGTH_SHORT).show()
            return
        }
        android.util.Log.d("MainActivity", "Executing Eye Destroyer function")

        // After cycling, execute the eye destroyer functionality
        ledController.controlLeds(
            "off",
            LedPaths.FLASH_WHITE_LED_PATH,
            LedPaths.FLASH_YELLOW_LED_PATH,
            LedPaths.TOGGLE_PATHS,
            whiteBrightness = 1000,
            yellowBrightness = 1000
        )
        ledController.controlLeds(
            "on",
            LedPaths.FLASH_WHITE_LED_PATH,
            LedPaths.FLASH_YELLOW_LED_PATH,
            LedPaths.TOGGLE_PATHS,
            whiteBrightness = 1500,
            yellowBrightness = 1500
        )
        isLedOn = true
        startEyeDestroyerCooldown()
    }


    private fun startEyeDestroyerCooldown() {
        eyeDestroyerCooldown = true
        binding.destroyer.isEnabled = false // Disable the button

        // Re-enable after 5 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            eyeDestroyerCooldown = false
            binding.destroyer.isEnabled = true // Enable the button
        }, 5000) // 5 seconds delay
    }

    private fun navigateToMainActivity2() {
        startActivity(Intent(this, MainActivity2::class.java))
    }

    private fun checkBrightnessSafety() {
        val currentTime = System.currentTimeMillis()

        // Check if any brightness exceeds the limit
        if (masterBrightness > MAX_BRIGHTNESS || whiteBrightness > MAX_BRIGHTNESS || yellowBrightness > MAX_BRIGHTNESS) {
            if (brightnessExceededTime == 0L) {
                brightnessExceededTime = currentTime
            } else if (currentTime - brightnessExceededTime > 20000) { // More than 20 seconds
                revertExceedingBrightnessToSafeLevel()
            }
        } else {
            brightnessExceededTime = 0L // Reset the timer if brightness is within safe range
            safetyTriggered = false // Allow user to adjust brightness again
        }
    }

    private fun revertExceedingBrightnessToSafeLevel() {
        // Check and revert only the brightness value that exceeds the limit
        if (masterBrightness > MAX_BRIGHTNESS) {
            masterBrightness = SAFE_BRIGHTNESS
            binding.masterSeekBar.value = SAFE_BRIGHTNESS.toFloat()
        }

        if (whiteBrightness > MAX_BRIGHTNESS) {
            whiteBrightness = SAFE_BRIGHTNESS
            binding.whiteSeekBar.value = SAFE_BRIGHTNESS.toFloat()
        }

        if (yellowBrightness > MAX_BRIGHTNESS) {
            yellowBrightness = SAFE_BRIGHTNESS
            binding.yellowSeekBar.value = SAFE_BRIGHTNESS.toFloat()
        }

        // Apply the changes to the LEDs
        if (isLedOn) {
            ledController.controlLeds(
                "on",
                LedPaths.WHITE_LED_PATH,
                LedPaths.YELLOW_LED_PATH,
                LedPaths.TOGGLE_PATHS,
                whiteBrightness = if (whiteBrightness > MAX_BRIGHTNESS) SAFE_BRIGHTNESS else whiteBrightness,
                yellowBrightness = if (yellowBrightness > MAX_BRIGHTNESS) SAFE_BRIGHTNESS else yellowBrightness
            )
        }

        safetyTriggered = true
        Toast.makeText(
            this,
            "Brightness exceeded limit! Adjusted to safe levels.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun performSecretAction() {
        VibrationUtil.vibrate(this, 200L)
        Toast.makeText(this, getString(R.string.experimental), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ExperimentalActivity::class.java))
    }
}

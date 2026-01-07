package com.bartixxx.opflashcontrol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bartixxx.opflashcontrol.LedPaths.FLASH_WHITE2_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.FLASH_WHITE_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.FLASH_YELLOW2_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.FLASH_YELLOW_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.TOGGLE_PATHS
import com.bartixxx.opflashcontrol.LedPaths.WHITE2_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.WHITE_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.YELLOW2_LED_PATH
import com.bartixxx.opflashcontrol.LedPaths.YELLOW_LED_PATH
import com.bartixxx.opflashcontrol.databinding.ActivityMain2Binding

/**
 * The second main activity of the application.
 *
 * This activity allows the user to control the flashlight LEDs on devices with four LEDs.
 */
class MainActivity2 : BaseActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var prefs: SharedPreferences

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

    private var isBurnAware = false
    private var defaultBrightness = 80

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LedPathUtil.findLedPaths()
        ledController = LedController(this)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("OpFlashControlPrefs", Context.MODE_PRIVATE)
        loadPreferences()

        // Initialize the brightness check handler
        brightnessCheckHandler = Handler(Looper.getMainLooper())
        brightnessCheckRunnable = Runnable {
            checkBrightnessSafety()
            brightnessCheckHandler?.postDelayed(brightnessCheckRunnable!!, CHECK_INTERVAL)
        }
        brightnessCheckHandler?.postDelayed(brightnessCheckRunnable!!, CHECK_INTERVAL)

        with(binding) {
            masterSeekBar.valueFrom = 0f
            masterSeekBar.value = masterBrightness.toFloat()
            masterSeekBar.valueTo = 500f

            whiteSeekBar.valueFrom = 0f
            whiteSeekBar.value = whiteBrightness.toFloat()
            whiteSeekBar.valueTo = 500f

            yellowSeekBar.valueFrom = 0f
            yellowSeekBar.value = yellowBrightness.toFloat()
            yellowSeekBar.valueTo = 500f

            white2SeekBar2.valueFrom = 0f
            white2SeekBar2.value = white2Brightness.toFloat()
            white2SeekBar2.valueTo = 500f

            yellow2SeekBar3.valueFrom = 0f
            yellow2SeekBar3.value = yellow2Brightness.toFloat()
            yellow2SeekBar3.valueTo = 500f

            burnAwareCheckbox.isChecked = isBurnAware
            burnAwareCheckbox.setOnCheckedChangeListener { _, isChecked ->
                isBurnAware = isChecked
                prefs.edit().putBoolean("burn_aware", isChecked).apply()
            }

            defaultBrightnessSeekBar.valueFrom = 1f
            defaultBrightnessSeekBar.valueTo = 500f
            defaultBrightnessSeekBar.value = defaultBrightness.toFloat()
            defaultBrightnessTextView.text = "Default Brightness: $defaultBrightness"

            setupSlider(defaultBrightnessSeekBar, defaultBrightnessTextView, "Default Brightness") { progress ->
                defaultBrightness = progress
                prefs.edit().putInt("default_brightness", progress).apply()
            }

            setupSlider(masterSeekBar, masterTextView, "Master Brightness") { progress ->
                if (!safetyTriggered || isBurnAware) {
                    masterBrightness = progress
                    if (isLedOn && allBrightnessAtOne()) {
                        ledController.controlLeds(
                            "on",
                            WHITE_LED_PATH,
                            YELLOW_LED_PATH,
                            TOGGLE_PATHS,
                            WHITE2_LED_PATH,
                            YELLOW2_LED_PATH,
                            progress,
                            progress,
                            progress,
                            progress
                        )
                    }
                }
            }

            setupSlider(whiteSeekBar, whiteTextView, "White Brightness") { progress ->
                if (!safetyTriggered || isBurnAware) {
                    whiteBrightness = progress
                    if (isLedOn) ledController.controlLeds(
                        "on",
                        WHITE_LED_PATH,
                        YELLOW_LED_PATH,
                        TOGGLE_PATHS,
                        WHITE2_LED_PATH,
                        YELLOW2_LED_PATH,
                        progress,
                        yellowBrightness,
                        white2Brightness,
                        yellow2Brightness
                    )
                }
            }

            setupSlider(yellowSeekBar, yellowTextView, "Yellow Brightness") { progress ->
                if (!safetyTriggered || isBurnAware) {
                    yellowBrightness = progress
                    if (isLedOn) ledController.controlLeds(
                        "on",
                        WHITE_LED_PATH,
                        YELLOW_LED_PATH,
                        TOGGLE_PATHS,
                        WHITE2_LED_PATH,
                        YELLOW2_LED_PATH,
                        whiteBrightness,
                        progress,
                        white2Brightness,
                        yellow2Brightness
                    )
                }
            }

            setupSlider(white2SeekBar2, white2TextView3, "White2 Brightness") { progress ->
                if (!safetyTriggered || isBurnAware) {
                    white2Brightness = progress
                    if (isLedOn) ledController.controlLeds(
                        "on",
                        WHITE_LED_PATH,
                        YELLOW_LED_PATH,
                        TOGGLE_PATHS,
                        WHITE2_LED_PATH,
                        YELLOW2_LED_PATH,
                        whiteBrightness,
                        yellowBrightness,
                        progress,
                        yellow2Brightness
                    )
                }
            }

            setupSlider(yellow2SeekBar3, yellow2TextView2, "Yellow2 Brightness") { progress ->
                if (!safetyTriggered || isBurnAware) {
                    yellow2Brightness = progress
                    if (isLedOn) ledController.controlLeds(
                        "on",
                        WHITE_LED_PATH,
                        YELLOW_LED_PATH,
                        TOGGLE_PATHS,
                        WHITE2_LED_PATH,
                        YELLOW2_LED_PATH,
                        whiteBrightness,
                        yellowBrightness,
                        white2Brightness,
                        progress
                    )
                }
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
                    performSecretAction()
                    clickCount = 0
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (clickCount < 5) {
                            clickCount = 0
                        }
                    }, 5000)
                }
            }

            buymecoffe.setOnClickListener {
                val delayBetweenVibrations = 100L
                val vibrationStrength = 100L
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/bartixxx32"))
                startActivity(browserIntent)

                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity2, vibrationStrength)
                }, 0)

                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity2, vibrationStrength)
                }, delayBetweenVibrations)

                Handler(Looper.getMainLooper()).postDelayed({
                    VibrationUtil.vibrate(this@MainActivity2, vibrationStrength)
                }, delayBetweenVibrations * 2)
            }

            buymecoffe.setOnLongClickListener {
                val intent = Intent(this@MainActivity2, SupportersActivity::class.java)
                startActivity(intent)
                true
            }
        }
    }

    private fun loadPreferences() {
        isBurnAware = prefs.getBoolean("burn_aware", false)
        defaultBrightness = prefs.getInt("default_brightness", 80)
    }

    private fun allBrightnessAtOne() =
        whiteBrightness <= 1 && yellowBrightness <= 1 && white2Brightness <= 1 && yellow2Brightness <= 1

    /**
     * Toggles the flashlight LEDs on or off.
     *
     * @param on Whether to turn the LEDs on or off.
     */
    private fun toggleLEDs(on: Boolean) {
        isLedOn = on
        if (on) {
            ledController.controlLeds(
                "on",
                WHITE_LED_PATH,
                YELLOW_LED_PATH,
                TOGGLE_PATHS,
                WHITE2_LED_PATH,
                YELLOW2_LED_PATH,
                if (whiteBrightness == 0) masterBrightness else whiteBrightness,
                if (yellowBrightness == 0) masterBrightness else yellowBrightness,
                if (white2Brightness == 0) masterBrightness else white2Brightness,
                if (yellow2Brightness == 0) masterBrightness else yellow2Brightness
            )
        } else {
            ledController.controlLeds(
                "off",
                WHITE_LED_PATH,
                YELLOW_LED_PATH,
                TOGGLE_PATHS,
                WHITE2_LED_PATH,
                YELLOW2_LED_PATH,
                1,
                1,
                1,
                1
            )
        }
    }

    /**
     * Executes the "Eye Destroyer" function, which turns on the flashlight LEDs at maximum brightness.
     */
    private fun executeExtraFunction() {
        if (eyeDestroyerCooldown) {
            Toast.makeText(this, "Please wait before using this feature again.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        android.util.Log.d("MainActivity2", "Executing Eye Destroyer function")

        ledController.controlLeds(
            "off",
            FLASH_WHITE_LED_PATH,
            FLASH_YELLOW_LED_PATH,
            TOGGLE_PATHS,
            FLASH_WHITE2_LED_PATH,
            FLASH_YELLOW2_LED_PATH,
            1000,
            1000,
            1000,
            1000
        )
        ledController.controlLeds(
            "on",
            FLASH_WHITE_LED_PATH,
            FLASH_YELLOW_LED_PATH,
            TOGGLE_PATHS,
            FLASH_WHITE2_LED_PATH,
            FLASH_YELLOW2_LED_PATH,
            1500,
            1500,
            1500,
            1500
        )
        isLedOn = true

        startEyeDestroyerCooldown()
    }

    /**
     * Starts the cooldown for the "Eye Destroyer" function.
     */
    private fun startEyeDestroyerCooldown() {
        eyeDestroyerCooldown = true
        binding.destroyer.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            eyeDestroyerCooldown = false
            binding.destroyer.isEnabled = true
        }, 5000)
    }

    /**
     * Navigates back to the main activity.
     */
    private fun navigateBackToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    /**
     * Checks if the brightness of the flashlight LEDs is within a safe range.
     */
    private fun checkBrightnessSafety() {
        if (isBurnAware) {
            brightnessExceededTime = 0L
            safetyTriggered = false
            return
        }

        val currentTime = System.currentTimeMillis()

        // Check if any brightness exceeds the limit
        if (masterBrightness > MAX_BRIGHTNESS || whiteBrightness > MAX_BRIGHTNESS || yellowBrightness > MAX_BRIGHTNESS || white2Brightness > MAX_BRIGHTNESS || yellow2Brightness > MAX_BRIGHTNESS) {
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

    /**
     * Reverts the brightness of the flashlight LEDs to a safe level.
     */
    private fun revertExceedingBrightnessToSafeLevel() {
        var adjusted = false

        // Check and revert only the brightness value that exceeds the limit
        if (masterBrightness > MAX_BRIGHTNESS) {
            masterBrightness = SAFE_BRIGHTNESS
            binding.masterSeekBar.value = SAFE_BRIGHTNESS.toFloat()
            adjusted = true
        }

        if (whiteBrightness > MAX_BRIGHTNESS) {
            whiteBrightness = SAFE_BRIGHTNESS
            binding.whiteSeekBar.value = SAFE_BRIGHTNESS.toFloat()
            adjusted = true
        }

        if (yellowBrightness > MAX_BRIGHTNESS) {
            yellowBrightness = SAFE_BRIGHTNESS
            binding.yellowSeekBar.value = SAFE_BRIGHTNESS.toFloat()
            adjusted = true
        }

        if (white2Brightness > MAX_BRIGHTNESS) {
            white2Brightness = SAFE_BRIGHTNESS
            binding.white2SeekBar2.value = SAFE_BRIGHTNESS.toFloat()
            adjusted = true
        }

        if (yellow2Brightness > MAX_BRIGHTNESS) {
            yellow2Brightness = SAFE_BRIGHTNESS
            binding.yellow2SeekBar3.value = SAFE_BRIGHTNESS.toFloat()
            adjusted = true
        }

        // Apply changes to LEDs only if any value was adjusted
        if (adjusted && isLedOn) {
            ledController.controlLeds(
                "on",
                WHITE_LED_PATH,
                YELLOW_LED_PATH,
                TOGGLE_PATHS,
                WHITE2_LED_PATH,
                YELLOW2_LED_PATH,
                whiteBrightness,
                yellowBrightness,
                white2Brightness,
                yellow2Brightness
            )
            Toast.makeText(
                this,
                "Brightness exceeded limit! Adjusted to safe levels.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Performs a secret action, which opens the experimental activity.
     */
    private fun performSecretAction() {
        VibrationUtil.vibrate(this, 200L)
        Toast.makeText(this, getString(R.string.experimental), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ExperimentalActivity::class.java))
    }
}

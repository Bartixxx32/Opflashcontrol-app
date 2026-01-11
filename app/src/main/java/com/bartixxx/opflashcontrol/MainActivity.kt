package com.bartixxx.opflashcontrol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.bartixxx.opflashcontrol.LedPaths.TOGGLE_PATHS
import com.bartixxx.opflashcontrol.databinding.ActivityMainBinding

/**
 * The main activity of the application.
 */
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences

    private var clickCount = 0
    private var lastClickTime: Long = 0
    private var brightnessCheckHandler: Handler? = null
    private var brightnessCheckRunnable: Runnable? = null
    private var brightnessExceededTime: Long = 0
    private var safetyTriggered = false
    private val MAX_BRIGHTNESS = 120
    private val SAFE_BRIGHTNESS = 80
    private val CHECK_INTERVAL = 200L 
    private var eyeDestroyerCooldown = false 
    private lateinit var ledController: LedController

    private var isBurnAware = false
    private var defaultBrightness = 80


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LedPathUtil.findLedPaths()
        ledController = LedController(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("OpFlashControlPrefs", Context.MODE_PRIVATE)
        loadPreferences()

        // Only run brightness safety check for rooted devices
        // Non-root devices use CameraManager API which has built-in safety limits
        if (ledController.isRootAvailable) {
            brightnessCheckHandler = Handler(Looper.getMainLooper())
            brightnessCheckRunnable = Runnable {
                checkBrightnessSafety()
                brightnessCheckHandler?.postDelayed(brightnessCheckRunnable!!, CHECK_INTERVAL)
            }
            brightnessCheckHandler?.postDelayed(brightnessCheckRunnable!!, CHECK_INTERVAL)
        }

        // Adapt UI based on root availability
        val isRootAvailable = ledController.isRootAvailable
        val maxTorchLevel = ledController.getMaxTorchLevel()
        
        with(binding) {
            if (!isRootAvailable && maxTorchLevel > 1) {
                // Non-root mode: hide white/yellow controls, adjust master brightness
                whiteLabel.visibility = android.view.View.GONE
                whiteSeekBar.visibility = android.view.View.GONE
                whiteTextView.visibility = android.view.View.GONE
                
                yellowLabel.visibility = android.view.View.GONE
                yellowSeekBar.visibility = android.view.View.GONE
                yellowTextView.visibility = android.view.View.GONE
                
                destroyer.visibility = android.view.View.GONE
                
                // Hide info text about Eye Destroyer - not relevant in non-root mode
                infoTextView.visibility = android.view.View.GONE
                
                // Hide burn awareness checkbox - not needed in non-root mode
                // CameraManager API limits brightness to safe levels
                burnAwareCheckbox.visibility = android.view.View.GONE
                
                // Update master label to indicate non-root mode
                masterLabel.text = getString(R.string.master) + " (Non-Root: $maxTorchLevel levels)"
                
                // Adjust master slider to match torch levels - SET RANGE FIRST!
                masterSeekBar.valueFrom = 1f
                masterSeekBar.valueTo = maxTorchLevel.toFloat()
                masterSeekBar.stepSize = 1f
                // Start with middle level for non-root (don't use defaultBrightness as it could be 80+)
                val initialLevel = (maxTorchLevel / 2).coerceAtLeast(1)
                masterSeekBar.value = initialLevel.toFloat()
                masterTextView.text = getString(R.string.brightness) + ": $initialLevel"
            } else if (isRootAvailable) {
                // Root mode: standard configuration
                masterSeekBar.valueFrom = 0f
                masterSeekBar.valueTo = 500f
                masterSeekBar.value = masterBrightness.coerceIn(0, 500).toFloat()
                masterTextView.text = "Master Brightness: $masterBrightness"

                whiteSeekBar.valueFrom = 0f
                whiteSeekBar.valueTo = 500f
                whiteSeekBar.value = whiteBrightness.coerceIn(0, 500).toFloat()

                yellowSeekBar.valueFrom = 0f
                yellowSeekBar.valueTo = 500f
                yellowSeekBar.value = yellowBrightness.coerceIn(0, 500).toFloat()
            }

            burnAwareCheckbox.isChecked = isBurnAware
            burnAwareCheckbox.setOnCheckedChangeListener { _, isChecked ->
                isBurnAware = isChecked
                prefs.edit().putBoolean("burn_aware", isChecked).apply()
            }

            setDefaultButton.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                // Jeśli suwak jest na 0 lub 1, ustawiamy domyślnie 80
                defaultBrightness = if (isRootAvailable) {
                    if (masterBrightness <= 1) 80 else masterBrightness
                } else {
                    masterSeekBar.value.toInt()
                }
                prefs.edit().putInt("default_brightness", defaultBrightness).apply()
                Toast.makeText(this@MainActivity, getString(R.string.default_brightness_set, defaultBrightness), Toast.LENGTH_SHORT).show()
            }

            setupSlider(masterSeekBar, masterTextView, if (isRootAvailable) getString(R.string.master) else getString(R.string.brightness)) { progress ->
                if (!safetyTriggered || isBurnAware) {
                    masterBrightness = if (isRootAvailable) progress else (progress * 500 / maxTorchLevel)
                    if (isLedOn && (!isRootAvailable || (whiteBrightness <= 1 && yellowBrightness <= 1))) {
                        ledController.controlLeds(
                            "on",
                            LedPaths.WHITE_LED_PATH,
                            LedPaths.YELLOW_LED_PATH,
                            LedPaths.TOGGLE_PATHS,
                            whiteBrightness = if (isRootAvailable) progress else masterBrightness,
                            yellowBrightness = if (isRootAvailable) progress else masterBrightness
                        )
                    }
                }
            }

            if (isRootAvailable) {
                setupSlider(whiteSeekBar, whiteTextView, "White Brightness") { progress ->
                    if (!safetyTriggered || isBurnAware) {
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
                    if (!safetyTriggered || isBurnAware) {
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
            }

            on.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                toggleLEDs(true)
            }
            off.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                toggleLEDs(false)
            }
            
            if (isRootAvailable) {
                destroyer.setOnClickListener {
                    VibrationUtil.vibrate(this@MainActivity, 100L)
                    executeExtraFunction()
                }
            }
            
            navigateToMainActivity2.setOnClickListener {
                VibrationUtil.vibrate(this@MainActivity, 100L)
                navigateToMainActivity2()
            }
            
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
                    performSecretAction()
                    clickCount = 0
                }
            }
            
            buymecoffe.setOnClickListener {
                val delayBetweenVibrations = 100L 
                val vibrationstrenght = 100L
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://buymeacoffee.com/bartixxx32"))
                startActivity(browserIntent)
                Handler(Looper.getMainLooper()).postDelayed({ VibrationUtil.vibrate(this@MainActivity, vibrationstrenght) }, 0)
                Handler(Looper.getMainLooper()).postDelayed({ VibrationUtil.vibrate(this@MainActivity, vibrationstrenght) }, delayBetweenVibrations)
                Handler(Looper.getMainLooper()).postDelayed({ VibrationUtil.vibrate(this@MainActivity, vibrationstrenght) }, delayBetweenVibrations * 2)
            }

            buymecoffe.setOnLongClickListener {
                startActivity(Intent(this@MainActivity, SupportersActivity::class.java))
                true
            }
        }
    }

    private fun loadPreferences() {
        isBurnAware = prefs.getBoolean("burn_aware", false)
        defaultBrightness = prefs.getInt("default_brightness", 80)
        masterBrightness = defaultBrightness 
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
        ledController.controlLeds("off", LedPaths.FLASH_WHITE_LED_PATH, LedPaths.FLASH_YELLOW_LED_PATH, LedPaths.TOGGLE_PATHS, whiteBrightness = 1000, yellowBrightness = 1000)
        ledController.controlLeds("on", LedPaths.FLASH_WHITE_LED_PATH, LedPaths.FLASH_YELLOW_LED_PATH, LedPaths.TOGGLE_PATHS, whiteBrightness = 1500, yellowBrightness = 1500)
        isLedOn = true
        startEyeDestroyerCooldown()
    }

    private fun startEyeDestroyerCooldown() {
        eyeDestroyerCooldown = true
        binding.destroyer.isEnabled = false
        Handler(Looper.getMainLooper()).postDelayed({
            eyeDestroyerCooldown = false
            binding.destroyer.isEnabled = true
        }, 5000)
    }

    private fun navigateToMainActivity2() {
        startActivity(Intent(this, MainActivity2::class.java))
    }

    private fun checkBrightnessSafety() {
        if (isBurnAware) {
            brightnessExceededTime = 0L
            safetyTriggered = false
            return
        }
        val currentTime = System.currentTimeMillis()
        if (masterBrightness > MAX_BRIGHTNESS || whiteBrightness > MAX_BRIGHTNESS || yellowBrightness > MAX_BRIGHTNESS) {
            if (brightnessExceededTime == 0L) {
                brightnessExceededTime = currentTime
            } else if (currentTime - brightnessExceededTime > 20000) {
                revertExceedingBrightnessToSafeLevel()
            }
        } else {
            brightnessExceededTime = 0L
            safetyTriggered = false
        }
    }

    private fun revertExceedingBrightnessToSafeLevel() {
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
        Toast.makeText(this, "Brightness exceeded limit! Adjusted to safe levels.", Toast.LENGTH_SHORT).show()
    }

    private fun performSecretAction() {
        VibrationUtil.vibrate(this, 200L)
        Toast.makeText(this, getString(R.string.experimental), Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ExperimentalActivity::class.java))
    }
}

package com.bartixxx.opflashcontrol

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

/**
 * Base activity class for activities that control the flashlight.
 *
 * This class provides common properties and methods for controlling the flashlight LEDs.
 */
abstract class BaseActivity : AppCompatActivity() {

    /**
     * The master brightness level for the flashlight.
     */
    protected var masterBrightness = 80
    /**
     * The brightness level for the white LED.
     */
    protected var whiteBrightness = 0
    /**
     * The brightness level for the yellow LED.
     */
    protected var yellowBrightness = 0
    /**
     * The brightness level for the second white LED.
     */
    protected var white2Brightness = 0
    /**
     * The brightness level for the second yellow LED.
     */
    protected var yellow2Brightness = 0
    /**
     * Whether the LED is currently on.
     */
    protected var isLedOn = false

    companion object {
        /**
         * The file path for the white LED brightness control.
         */
        const val WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
        /**
         * The file path for the yellow LED brightness control.
         */
        const val YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
        /**
         * The file path for the second white LED brightness control.
         */
        const val WHITE2_LED_PATH = "/sys/class/leds/led:torch_2/brightness"
        /**
         * The file path for the second yellow LED brightness control.
         */
        const val YELLOW2_LED_PATH = "/sys/class/leds/led:torch_3/brightness"
        /**
         * The file path for the white LED flash brightness control.
         */
        const val FLASH_WHITE_LED_PATH = "/sys/class/leds/led:flash_0/brightness"
        /**
         * The file path for the yellow LED flash brightness control.
         */
        const val FLASH_YELLOW_LED_PATH = "/sys/class/leds/led:flash_1/brightness"
        /**
         * The file path for the second white LED flash brightness control.
         */
        const val FLASH_WHITE2_LED_PATH = "/sys/class/leds/led:flash_2/brightness"
        /**
         * The file path for the second yellow LED flash brightness control.
         */
        const val FLASH_YELLOW2_LED_PATH = "/sys/class/leds/led:flash_3/brightness"
    }

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    /**
     * Sets up a slider for controlling brightness.
     *
     * @param slider The slider to set up.
     * @param textView The text view to display the slider's value.
     * @param label The label for the text view.
     * @param onStopTracking A callback that is invoked when the user stops tracking the slider.
     */
    protected fun setupSlider(
        slider: Slider,
        textView: TextView,
        label: String,
        onStopTracking: (Int) -> Unit
    ) {
        var lastIntegerValue = slider.value.toInt() // Track the last integer value

        slider.addOnChangeListener { _, value, _ ->
            val progress = value.toInt() // Convert from Float to Int

            // Vibrate only if the integer part of the slider value has changed
            if (progress != lastIntegerValue) {
                VibrationUtil.vibrate(this, 50L)
                lastIntegerValue = progress
            }

            Log.d("SliderProgress", "$label Progress: $progress") // Debugging log
            textView.text = "$label: $progress"
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Do nothing on touch start
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val progress =
                    slider.value.toInt() // Get the final value when user releases the slider
                Log.d("SliderProgress", "$label Finger released, Progress: $progress")
                textView.text = "$label: $progress" // Update the text view with the final value
                onStopTracking(progress) // Trigger the callback with the final value
            }
        })
    }
}

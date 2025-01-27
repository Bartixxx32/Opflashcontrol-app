package com.bartixxx.opflashcontrol

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

abstract class BaseActivity : AppCompatActivity() {

    protected var masterBrightness = 80
    protected var whiteBrightness = 0
    protected var yellowBrightness = 0
    protected var white2Brightness = 0
    protected var yellow2Brightness = 0
    protected var isLedOn = false

    companion object {
        const val WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
        const val YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
        const val WHITE2_LED_PATH = "/sys/class/leds/led:torch_2/brightness"
        const val YELLOW2_LED_PATH = "/sys/class/leds/led:torch_3/brightness"
        const val FLASH_WHITE_LED_PATH = "/sys/class/leds/led:flash_0/brightness"
        const val FLASH_YELLOW_LED_PATH = "/sys/class/leds/led:flash_1/brightness"
        const val FLASH_WHITE2_LED_PATH = "/sys/class/leds/led:flash_2/brightness"
        const val FLASH_YELLOW2_LED_PATH = "/sys/class/leds/led:flash_3/brightness"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


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

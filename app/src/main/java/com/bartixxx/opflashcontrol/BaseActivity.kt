package com.bartixxx.opflashcontrol

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import java.io.DataOutputStream
import android.util.Log
import com.bartixxx.opflashcontrol.databinding.ActivityMainBinding
import com.bartixxx.opflashcontrol.databinding.ActivityMain2Binding
import java.io.IOException

abstract class BaseActivity : AppCompatActivity() {

    protected var masterBrightness = 0
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
        val TOGGLE_PATHS = listOf(
            "/sys/class/leds/led:switch_2/brightness"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // Helper method to sanitize brightness values (ensuring no 0 values are written)
    private fun sanitizeBrightness(brightness: Int): Int {
        return if (brightness == 0) 1 else brightness
    }

    protected fun setupSlider(
        slider: Slider, // Use Slider from Material3
        textView: TextView,
        label: String,
        onStopTracking: (Int) -> Unit
    ) {
        slider.addOnChangeListener { _, value, _ ->
            val progress = value.toInt() // Convert from Float to Int
            Log.d("SliderProgress", "$label Progress: $progress") // Debugging log

            textView.text = "$label: $progress"
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Do nothing on touch start
            }

            override fun onStopTrackingTouch(slider: Slider) {
                val progress = slider.value.toInt() // Get the final value when user releases the slider
                Log.d("SliderProgress", "$label Finger released, Progress: $progress")
                textView.text = "$label: $progress" // Update the text view with the final value
                onStopTracking(progress) // Trigger the callback with the final value
            }
        })
    }

    protected fun controlLeds(
        action: String,
        whiteLedPath: String,
        yellowLedPath: String,
        white2LedPath: String? = null,
        yellow2LedPath: String? = null,
        whiteBrightness: Int,
        yellowBrightness: Int,
        white2Brightness: Int = 0,
        yellow2Brightness: Int = 0
    ) {
        // Sanitize the brightness values to ensure no 0 values are written
        val sanitizedWhiteBrightness = sanitizeBrightness(whiteBrightness)
        val sanitizedYellowBrightness = sanitizeBrightness(yellowBrightness)
        val sanitizedWhite2Brightness = sanitizeBrightness(white2Brightness)
        val sanitizedYellow2Brightness = sanitizeBrightness(yellow2Brightness)

        val commands = mutableListOf<String>()

        if (action == "on") {
            commands.addAll(commonOnCommands(whiteLedPath, yellowLedPath, white2LedPath, yellow2LedPath))
            commands.addAll(listOf(
                "echo $sanitizedWhiteBrightness > $whiteLedPath",
                "echo $sanitizedYellowBrightness > $yellowLedPath",
                white2LedPath?.let { "echo $sanitizedWhite2Brightness > $it" },
                yellow2LedPath?.let { "echo $sanitizedYellow2Brightness > $it" }
            ).filterNotNull())
            TOGGLE_PATHS.forEach { commands.add("echo 255 > $it") }
        } else if (action == "off") {
            commands.addAll(commonOffCommands(whiteLedPath, yellowLedPath, white2LedPath, yellow2LedPath))
        }

        executeRootCommands(commands)
    }

    private fun commonOnCommands(white: String, yellow: String, white2: String?, yellow2: String?): List<String> {
        return listOf(
            "echo 80 > $white",
            "echo 80 > $yellow",
            white2?.let { "echo 80 > $it" },
            yellow2?.let { "echo 80 > $it" }
        ).filterNotNull() + TOGGLE_PATHS.map { "echo 0 > $it" }
    }

    private fun commonOffCommands(white: String, yellow: String, white2: String?, yellow2: String?): List<String> {
        return listOf(
            "echo 80 > $white",
            "echo 80 > $yellow",
            white2?.let { "echo 80 > $it" },
            yellow2?.let { "echo 80 > $it" }
        ).filterNotNull() + TOGGLE_PATHS.map { "echo 0 > $it" }
    }

    protected fun executeRootCommands(commands: List<String>) {
        try {
            commands.forEach { Log.d("LEDControlApp", "Executing command: $it") }

            val process = Runtime.getRuntime().exec("su")
            process.outputStream.use { outputStream ->
                DataOutputStream(outputStream).use { dataOutputStream ->
                    val batchCommands = commands.joinToString("\n") + "\nexit\n"
                    dataOutputStream.writeBytes(batchCommands)
                    dataOutputStream.flush()
                }
            }
            process.waitFor()
            Toast.makeText(this, getString(R.string.command_executed), Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_io), Toast.LENGTH_LONG).show()
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_permission), Toast.LENGTH_LONG).show()
        } catch (e: InterruptedException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.error_interrupted), Toast.LENGTH_LONG).show()
        }
    }
}

package com.bartixxx.opflashcontrol

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream
import android.util.Log
import com.bartixxx.opflashcontrol.databinding.ActivityMainBinding
import com.bartixxx.opflashcontrol.databinding.ActivityMain2Binding

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

    protected fun setupSeekBar(
        seekBar: SeekBar,
        textView: TextView,
        label: String,
        onStopTracking: (Int) -> Unit
    ) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = "$label: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                var progress = seekBar.progress
                if (progress == 0) progress = 1 // Prevent zero brightness
                onStopTracking(progress)
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
        val commands = mutableListOf<String>()

        if (action == "on") {
            commands.addAll(commonOnCommands(whiteLedPath, yellowLedPath, white2LedPath, yellow2LedPath))
            commands.addAll(listOf(
                "echo $whiteBrightness > $whiteLedPath",
                "echo $yellowBrightness > $yellowLedPath",
                white2LedPath?.let { "echo $white2Brightness > $it" },
                yellow2LedPath?.let { "echo $yellow2Brightness > $it" }
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

            // Use 'use' function for proper resource management
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
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.command_failed), Toast.LENGTH_LONG).show()
        }
    }
}
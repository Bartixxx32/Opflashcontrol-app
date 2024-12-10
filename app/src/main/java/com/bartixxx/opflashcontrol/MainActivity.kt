package com.bartixxx.opflashcontrol

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream

class MainActivity : AppCompatActivity() {

    private var masterBrightness = 0
    private var whiteBrightness = 0
    private var yellowBrightness = 0
    private var isLedOn = false // Track if the LED is on or off

    companion object {
        const val WHITE_LED_PATH = "/sys/class/leds/led:torch_0/brightness"
        const val YELLOW_LED_PATH = "/sys/class/leds/led:torch_1/brightness"
        const val FLASH_WHITE_LED_PATH = "/sys/class/leds/led:flash_0/brightness"
        const val FLASH_YELLOW_LED_PATH = "/sys/class/leds/led:flash_1/brightness"
        val TOGGLE_PATHS = listOf(
            "/sys/class/leds/led:switch_0/brightness",
            "/sys/class/leds/led:switch_1/brightness",
            "/sys/class/leds/led:switch_2/brightness"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val masterBrightnessText: TextView = findViewById(R.id.masterTextView)
        val whiteBrightnessText: TextView = findViewById(R.id.whiteTextView)
        val yellowBrightnessText: TextView = findViewById(R.id.yellowTextView)

        val masterSeekBar: SeekBar = findViewById(R.id.masterSeekBar)
        val whiteSeekBar: SeekBar = findViewById(R.id.whiteSeekBar)
        val yellowSeekBar: SeekBar = findViewById(R.id.yellowSeekBar)

        val onButton: Button = findViewById(R.id.on)
        val offButton: Button = findViewById(R.id.off)
        val extraButton: Button = findViewById(R.id.destroyer)

        setupSeekBar(masterSeekBar, masterBrightnessText, "Master Brightness") { progress ->
            masterBrightness = progress
            if (isLedOn && whiteBrightness == 0 && yellowBrightness == 0) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, TOGGLE_PATHS, masterBrightness, masterBrightness)
            }
        }

        setupSeekBar(whiteSeekBar, whiteBrightnessText, "White Brightness") { progress ->
            whiteBrightness = progress
            if (isLedOn) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness)
            }
        }

        setupSeekBar(yellowSeekBar, yellowBrightnessText, "Yellow Brightness") { progress ->
            yellowBrightness = progress
            if (isLedOn) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness)
            }
        }

        onButton.setOnClickListener {
            isLedOn = true
            if (whiteBrightness == 0 && yellowBrightness == 0) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, TOGGLE_PATHS, masterBrightness, masterBrightness)
            } else {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness)
            }
        }

        offButton.setOnClickListener {
            isLedOn = false
            controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, TOGGLE_PATHS, 0, 0)
        }

        extraButton.setOnClickListener {
            controlLeds("off", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, TOGGLE_PATHS, 0, 0)
            controlLeds("on", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, TOGGLE_PATHS, 1500, 1500)
            isLedOn = true
        }
    }

    private fun setupSeekBar(
        seekBar: SeekBar,
        textView: TextView,
        label: String,
        onStopTracking: (progress: Int) -> Unit
    ) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = "$label: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onStopTracking(seekBar.progress)
            }
        })
    }

    private fun controlLeds(
        action: String,
        whiteLedPath: String,
        yellowLedPath: String,
        togglePaths: List<String>,
        whiteBrightness: Int,
        yellowBrightness: Int
    ) {
        val commands = mutableListOf<String>()

        if (action == "on") {
            commands.add("echo 0 > $whiteLedPath")
            commands.add("echo 0 > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }

            commands.add("echo $whiteBrightness > $whiteLedPath")
            commands.add("echo $yellowBrightness > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 255 > $it") }
        } else if (action == "off") {
            commands.add("echo 0 > $whiteLedPath")
            commands.add("echo 0 > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }
        }

        executeRootCommands(commands)
    }

    private fun executeRootCommands(commands: List<String>) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)

            val batchCommands = commands.joinToString("\n") + "\nexit\n"
            outputStream.writeBytes(batchCommands)
            outputStream.flush()
            outputStream.close()

            process.waitFor()
            Toast.makeText(this, getString(R.string.command_executed), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.command_failed), Toast.LENGTH_LONG).show()
        }
    }
}

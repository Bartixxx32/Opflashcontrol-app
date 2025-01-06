package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream
import android.util.Log

class MainActivity2 : AppCompatActivity() {

    private var masterBrightness = 0
    private var whiteBrightness = 0
    private var yellowBrightness = 0
    private var white2Brightness = 0
    private var yellow2Brightness = 0
    private var isLedOn = false // Track if the LED is on or off

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
            //"/sys/class/leds/led:switch_0/brightness",
            //"/sys/class/leds/led:switch_1/brightness",
            "/sys/class/leds/led:switch_2/brightness"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val masterBrightnessText: TextView = findViewById(R.id.masterTextView)
        val whiteBrightnessText: TextView = findViewById(R.id.whiteTextView)
        val yellowBrightnessText: TextView = findViewById(R.id.yellowTextView)
        val white2BrightnessText: TextView = findViewById(R.id.white2TextView3)
        val yellow2BrightnessText: TextView = findViewById(R.id.yellow2TextView2)

        val masterSeekBar: SeekBar = findViewById(R.id.masterSeekBar)
        val whiteSeekBar: SeekBar = findViewById(R.id.whiteSeekBar)
        val yellowSeekBar: SeekBar = findViewById(R.id.yellowSeekBar)
        val white2SeekBar: SeekBar = findViewById(R.id.white2SeekBar2)
        val yellow2SeekBar: SeekBar = findViewById(R.id.yellow2SeekBar3)

        val onButton: Button = findViewById(R.id.on)
        val offButton: Button = findViewById(R.id.off)
        val extraButton: Button = findViewById(R.id.destroyer)

        // Navigate to MainActivity2 button
        val navigateBackToMainActivityButton: Button = findViewById(R.id.navigateBackToMainActivity)

        setupSeekBar(masterSeekBar, masterBrightnessText, "Master Brightness") { progress ->
            masterBrightness = progress
            if (isLedOn && whiteBrightness <= 1 && yellowBrightness <= 1) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, masterBrightness, masterBrightness, masterBrightness, masterBrightness)
            }
        }

        setupSeekBar(whiteSeekBar, whiteBrightnessText, "White Brightness") { progress ->
            whiteBrightness = progress
            if (isLedOn) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness, white2Brightness, yellow2Brightness)
            }
        }

        setupSeekBar(yellowSeekBar, yellowBrightnessText, "Yellow Brightness") { progress ->
            yellowBrightness = progress
            if (isLedOn) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness, white2Brightness, yellow2Brightness)
            }
        }
        setupSeekBar(white2SeekBar, white2BrightnessText, "White2 Brightness") { progress ->
            white2Brightness = progress
            if (isLedOn) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness, white2Brightness, yellow2Brightness)
            }
        }

        setupSeekBar(yellow2SeekBar, yellow2BrightnessText, "Yellow2 Brightness") { progress ->
            yellow2Brightness = progress
            if (isLedOn) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness, white2Brightness, yellow2Brightness)
            }
        }

        onButton.setOnClickListener {
            isLedOn = true
            if (whiteBrightness == 0 && yellowBrightness == 0) {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, masterBrightness, masterBrightness, masterBrightness, masterBrightness)
            } else {
                controlLeds("on", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, whiteBrightness, yellowBrightness, white2Brightness, yellow2Brightness)
            }
        }

        offButton.setOnClickListener {
            isLedOn = false
            controlLeds("off", WHITE_LED_PATH, YELLOW_LED_PATH, WHITE2_LED_PATH, YELLOW2_LED_PATH, TOGGLE_PATHS, 1, 1,1,1)
        }

        extraButton.setOnClickListener {
            controlLeds("off", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, FLASH_WHITE2_LED_PATH, FLASH_YELLOW2_LED_PATH, TOGGLE_PATHS, 1000, 1000,1000,1000)
            controlLeds("on", FLASH_WHITE_LED_PATH, FLASH_YELLOW_LED_PATH, FLASH_WHITE2_LED_PATH, FLASH_YELLOW2_LED_PATH, TOGGLE_PATHS, 1500, 1500,1500,1500)
            isLedOn = true
        }

        // Set onClickListener for the button
        navigateBackToMainActivityButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
                var progress = seekBar.progress
                if (progress == 0) {
                    progress = 1 // Prevent zero brightness
                }
                onStopTracking(progress)
            }
        })
    }

    private fun controlLeds(
        action: String,
        whiteLedPath: String,
        yellowLedPath: String,
        white2LedPath: String,
        yellow2LedPath: String,
        togglePaths: List<String>,
        whiteBrightness: Int,
        yellowBrightness: Int,
        white2Brightness: Int,
        yellow2Brightness: Int
    ) {
        val commands = mutableListOf<String>()

        if (action == "on") {
            commands.add("echo 80 > $whiteLedPath")
            commands.add("echo 80 > $yellowLedPath")
            commands.add("echo 80 > $white2LedPath")
            commands.add("echo 80 > $yellow2LedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }

            commands.add("echo $whiteBrightness > $whiteLedPath")
            commands.add("echo $yellowBrightness > $yellowLedPath")
            commands.add("echo $white2Brightness > $white2LedPath")
            commands.add("echo $yellow2Brightness > $yellow2LedPath")
            togglePaths.forEach { commands.add("echo 255 > $it") }
        } else if (action == "off") {
            commands.add("echo 80 > $whiteLedPath")
            commands.add("echo 80 > $yellowLedPath")
            commands.add("echo 80 > $white2LedPath")
            commands.add("echo 80 > $yellow2LedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }
        }

        executeRootCommands(commands)
    }

    private fun executeRootCommands(commands: List<String>) {
        try {
            // Log only the commands being executed
            commands.forEach { command ->
                Log.d("LEDControlApp", "Executing command: $command")
            }

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

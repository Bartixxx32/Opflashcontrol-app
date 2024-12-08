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

        // Master SeekBar Logic
        masterSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                masterBrightness = progress
                masterBrightnessText.text = "Master Brightness: $masterBrightness"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isLedOn && whiteBrightness == 0 && yellowBrightness == 0) {
                    controlLed("on", masterBrightness, masterBrightness)
                }
            }
        })

        // White SeekBar Logic
        whiteSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                whiteBrightness = progress
                whiteBrightnessText.text = "White Brightness: $whiteBrightness"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isLedOn) {
                    controlLed("on", whiteBrightness, yellowBrightness)
                }
            }
        })

        // Yellow SeekBar Logic
        yellowSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                yellowBrightness = progress
                yellowBrightnessText.text = "Yellow Brightness: $yellowBrightness"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (isLedOn) {
                    controlLed("on", whiteBrightness, yellowBrightness)
                }
            }
        })

        // Set up the "on" button
        onButton.setOnClickListener {
            isLedOn = true // Set LED state to "on"
            controlLed("on", whiteBrightness, yellowBrightness)
        }

        // Set up the "off" button
        offButton.setOnClickListener {
            isLedOn = false // Set LED state to "off"
            controlLed("off", 0, 0) // Turn off LEDs
        }

        // Set up the "Extra On" button
        extraButton.setOnClickListener {
            isLedOn = true // Set LED state to "on"
            controlFlashLeds("on", 1500, 1500) // Use predefined brightness
        }
    }

    private fun controlLed(action: String, whiteBrightness: Int, yellowBrightness: Int) {
        val whiteLedPath = "/sys/class/leds/led:torch_0/brightness"
        val yellowLedPath = "/sys/class/leds/led:torch_1/brightness"
        val togglePaths = listOf(
            "/sys/class/leds/led:switch_0/brightness",
            "/sys/class/leds/led:switch_1/brightness",
            "/sys/class/leds/led:switch_2/brightness"
        )

        val commands = mutableListOf<String>()

        if (action == "on") {
            // Turn off all LEDs before turning them on
            commands.add("echo 80 > $whiteLedPath")
            commands.add("echo 80 > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }

            // Set the brightness for each LED independently
            if (whiteBrightness > 0) commands.add("echo $whiteBrightness > $whiteLedPath")
            if (yellowBrightness > 0) commands.add("echo $yellowBrightness > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 255 > $it") }
        } else if (action == "off") {
            // Turn off all LEDs
            commands.add("echo 80 > $whiteLedPath")
            commands.add("echo 80 > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }
        }

        executeRootCommands(commands)
    }

    private fun controlFlashLeds(action: String, flashWhiteBrightness: Int, flashYellowBrightness: Int) {
        val flashWhiteLedPath = "/sys/class/leds/led:flash_0/brightness"
        val flashYellowLedPath = "/sys/class/leds/led:flash_1/brightness"
        val togglePaths = listOf(
            "/sys/class/leds/led:switch_0/brightness",
            "/sys/class/leds/led:switch_1/brightness",
            "/sys/class/leds/led:switch_2/brightness"
        )

        val commands = mutableListOf<String>()

        if (action == "on") {
            commands.add("echo 1500 > $flashWhiteLedPath")
            commands.add("echo 1500 > $flashYellowLedPath")
            togglePaths.forEach { commands.add("echo 1500 > $it") }
        } else if (action == "off") {
            commands.add("echo 0 > $flashWhiteLedPath")
            commands.add("echo 0 > $flashYellowLedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }
        }

        executeRootCommands(commands)
    }

    private fun executeRootCommands(commands: List<String>) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            for (command in commands) {
                outputStream.writeBytes("$command\n")
            }
            outputStream.writeBytes("exit\n")
            outputStream.flush()
            outputStream.close()
            process.waitFor()
            Toast.makeText(this, "Command executed", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to execute command", Toast.LENGTH_LONG).show()
        }
    }
}

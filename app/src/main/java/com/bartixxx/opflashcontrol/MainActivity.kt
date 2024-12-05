package com.bartixxx.opflashcontrol

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.DataOutputStream

class MainActivity : AppCompatActivity() {

    private var brightness = 0
    private var isLedOn = false  // Track if the LED is on or off

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val brightnessText: TextView = findViewById(R.id.textView)
        val brightnessSeekBar: SeekBar = findViewById(R.id.seekBar)
        val onButton: Button = findViewById(R.id.on)
        val offButton: Button = findViewById(R.id.off)

        // Set up the slider listener
        brightnessSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                brightness = progress
                brightnessText.text = "Brightness: $brightness"

                // Only apply brightness change if the LED is on
                if (isLedOn) {
                    controlLed("on", brightness, "both")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Set up the "on" button
        onButton.setOnClickListener {
            isLedOn = true  // Set LED state to "on"
            controlLed("on", brightness, "both")  // Apply brightness when turning on
        }

        // Set up the "off" button
        offButton.setOnClickListener {
            isLedOn = false  // Set LED state to "off"
            controlLed("off", 0, "both")  // Turn off LEDs
        }
    }

    private fun controlLed(action: String, brightness: Int, ledType: String) {
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
            commands.add("echo 0 > $whiteLedPath")
            commands.add("echo 0 > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 0 > $it") }

            // Turn on the specified LEDs
            if (ledType == "white" || ledType == "both") commands.add("echo $brightness > $whiteLedPath")
            if (ledType == "yellow" || ledType == "both") commands.add("echo $brightness > $yellowLedPath")
            togglePaths.forEach { commands.add("echo 255 > $it") }
        } else if (action == "off") {
            // Turn off all LEDs
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

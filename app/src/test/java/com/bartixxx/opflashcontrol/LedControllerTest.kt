package com.bartixxx.opflashcontrol

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

class LedControllerTest {

    private lateinit var ledController: LedController
    private val mockContext: Context = mock()

    @Before
    fun setUp() {
        ledController = spy(LedController(mockContext))
        // Stub the executeRootCommands method to do nothing
        doNothing().`when`(ledController).executeRootCommands(any(), any())
    }

    @Test
    fun `controlLeds with action on generates correct commands`() {
        val whiteLedPath = "/sys/class/leds/white/brightness"
        val yellowLedPath = "/sys/class/leds/yellow/brightness"
        val togglePaths = listOf("/sys/class/leds/switch/brightness")

        ledController.controlLeds(
            action = "on",
            whiteLedPath = whiteLedPath,
            yellowLedPath = yellowLedPath,
            togglePaths = togglePaths,
            whiteBrightness = 100,
            yellowBrightness = 200,
            showToast = false
        )

        val expectedCommands = listOf(
            "echo 0 > ${togglePaths[0]}",
            "echo 100 > $whiteLedPath",
            "echo 200 > $yellowLedPath",
            "echo 255 > ${togglePaths[0]}"
        )

        verify(ledController).executeRootCommands(expectedCommands, false)
    }

    @Test
    fun `controlLeds with action off generates correct commands`() {
        val whiteLedPath = "/sys/class/leds/white/brightness"
        val yellowLedPath = "/sys/class/leds/yellow/brightness"
        val togglePaths = listOf("/sys/class/leds/switch/brightness")

        ledController.controlLeds(
            action = "off",
            whiteLedPath = whiteLedPath,
            yellowLedPath = yellowLedPath,
            togglePaths = togglePaths,
            whiteBrightness = 100,
            yellowBrightness = 200,
            showToast = false
        )

        val expectedCommands = listOf(
            "echo 80 > $whiteLedPath",
            "echo 80 > $yellowLedPath",
            "echo 0 > ${togglePaths[0]}"
        )

        verify(ledController).executeRootCommands(expectedCommands, false)
    }

    @Test
    fun `controlLeds with 0 brightness sanitizes to 1`() {
        val whiteLedPath = "/sys/class/leds/white/brightness"
        val yellowLedPath = "/sys/class/leds/yellow/brightness"
        val togglePaths = emptyList<String>()

        ledController.controlLeds(
            action = "on",
            whiteLedPath = whiteLedPath,
            yellowLedPath = yellowLedPath,
            togglePaths = togglePaths,
            whiteBrightness = 0,
            yellowBrightness = 0,
            showToast = false
        )

        val expectedCommands = listOf(
            "echo 1 > $whiteLedPath",
            "echo 1 > $yellowLedPath"
        )

        verify(ledController).executeRootCommands(expectedCommands, false)
    }
}

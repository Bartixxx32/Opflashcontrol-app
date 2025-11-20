package com.bartixxx.opflashcontrol

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class LedPathUtilTest {

    @Mock
    private lateinit var mockBaseDir: File

    @Mock
    private lateinit var mockTorch1: File

    @Mock
    private lateinit var mockTorch2: File

    @Mock
    private lateinit var mockTorch3: File

    @Mock
    private lateinit var mockTorch4: File

    @Mock
    private lateinit var mockFlash1: File

    @Mock
    private lateinit var mockFlash2: File

    @Mock
    private lateinit var mockFlash3: File

    @Mock
    private lateinit var mockFlash4: File

    @Mock
    private lateinit var mockSwitch1: File

    @Before
    fun setUp() {
        // Mock the directory structure
        whenever(mockBaseDir.listFiles()).thenReturn(arrayOf(mockTorch1, mockTorch2, mockTorch3, mockTorch4, mockFlash1, mockFlash2, mockFlash3, mockFlash4, mockSwitch1))

        // Mock the names of the directories
        whenever(mockTorch1.name).thenReturn("led:torch1")
        whenever(mockTorch2.name).thenReturn("led:torch2")
        whenever(mockTorch3.name).thenReturn("led:torch3")
        whenever(mockTorch4.name).thenReturn("led:torch4")
        whenever(mockFlash1.name).thenReturn("led:flash1")
        whenever(mockFlash2.name).thenReturn("led:flash2")
        whenever(mockFlash3.name).thenReturn("led:flash3")
        whenever(mockFlash4.name).thenReturn("led:flash4")
        whenever(mockSwitch1.name).thenReturn("led:switch1")

        // Mock the absolute paths
        whenever(mockTorch1.absolutePath).thenReturn("/sys/class/leds/led:torch1")
        whenever(mockTorch2.absolutePath).thenReturn("/sys/class/leds/led:torch2")
        whenever(mockTorch3.absolutePath).thenReturn("/sys/class/leds/led:torch3")
        whenever(mockTorch4.absolutePath).thenReturn("/sys/class/leds/led:torch4")
        whenever(mockFlash1.absolutePath).thenReturn("/sys/class/leds/led:flash1")
        whenever(mockFlash2.absolutePath).thenReturn("/sys/class/leds/led:flash2")
        whenever(mockFlash3.absolutePath).thenReturn("/sys/class/leds/led:flash3")
        whenever(mockFlash4.absolutePath).thenReturn("/sys/class/leds/led:flash4")
        whenever(mockSwitch1.absolutePath).thenReturn("/sys/class/leds/led:switch1")
    }

    @Test
    fun `findLedPaths correctly identifies and assigns paths`() {
        val ledPaths = LedPathUtil.findLedPaths(mockBaseDir)

        assertEquals("/sys/class/leds/led:torch1/brightness", ledPaths.WHITE_LED_PATH)
        assertEquals("/sys/class/leds/led:torch2/brightness", ledPaths.YELLOW_LED_PATH)
        assertEquals("/sys/class/leds/led:torch3/brightness", ledPaths.WHITE2_LED_PATH)
        assertEquals("/sys/class/leds/led:torch4/brightness", ledPaths.YELLOW2_LED_PATH)
        assertEquals("/sys/class/leds/led:flash1/brightness", ledPaths.FLASH_WHITE_LED_PATH)
        assertEquals("/sys/class/leds/led:flash2/brightness", ledPaths.FLASH_YELLOW_LED_PATH)
        assertEquals("/sys/class/leds/led:flash3/brightness", ledPaths.FLASH_WHITE2_LED_PATH)
        assertEquals("/sys/class/leds/led:flash4/brightness", ledPaths.FLASH_YELLOW2_LED_PATH)
        assertEquals(listOf("/sys/class/leds/led:switch1/brightness"), ledPaths.TOGGLE_PATHS)
    }
}

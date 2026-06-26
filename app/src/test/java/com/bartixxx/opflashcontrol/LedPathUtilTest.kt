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

    @Mock
    private lateinit var mockBrightness: File

    @Before
    fun setUp() {
        whenever(mockBaseDir.listFiles()).thenReturn(arrayOf(
            mockTorch1, mockTorch2, mockTorch3, mockTorch4,
            mockFlash1, mockFlash2, mockFlash3, mockFlash4,
            mockSwitch1
        ))

        whenever(mockTorch1.name).thenReturn("led:torch1")
        whenever(mockTorch2.name).thenReturn("led:torch2")
        whenever(mockTorch3.name).thenReturn("led:torch3")
        whenever(mockTorch4.name).thenReturn("led:torch4")
        whenever(mockFlash1.name).thenReturn("led:flash1")
        whenever(mockFlash2.name).thenReturn("led:flash2")
        whenever(mockFlash3.name).thenReturn("led:flash3")
        whenever(mockFlash4.name).thenReturn("led:flash4")
        whenever(mockSwitch1.name).thenReturn("led:switch1")

        whenever(mockTorch1.absolutePath).thenReturn("/sys/class/leds/led:torch1")
        whenever(mockTorch2.absolutePath).thenReturn("/sys/class/leds/led:torch2")
        whenever(mockTorch3.absolutePath).thenReturn("/sys/class/leds/led:torch3")
        whenever(mockTorch4.absolutePath).thenReturn("/sys/class/leds/led:torch4")
        whenever(mockFlash1.absolutePath).thenReturn("/sys/class/leds/led:flash1")
        whenever(mockFlash2.absolutePath).thenReturn("/sys/class/leds/led:flash2")
        whenever(mockFlash3.absolutePath).thenReturn("/sys/class/leds/led:flash3")
        whenever(mockFlash4.absolutePath).thenReturn("/sys/class/leds/led:flash4")
        whenever(mockSwitch1.absolutePath).thenReturn("/sys/class/leds/led:switch1")

        // Mock brightness file exists() for all LED dirs
        val allLedDirs = arrayOf(mockTorch1, mockTorch2, mockTorch3, mockTorch4,
            mockFlash1, mockFlash2, mockFlash3, mockFlash4, mockSwitch1)
        allLedDirs.forEach { dir ->
            val mockFile = org.mockito.kotlin.mock<File>()
            whenever(mockFile.exists()).thenReturn(true)
            whenever(dir.listFiles()).thenReturn(arrayOf(mockFile))
        }
    }

    @Test
    fun `findLedPaths correctly identifies standard OnePlus naming`() {
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

    @Test
    fun `findLedPaths handles single torch and flash LED`() {
        whenever(mockBaseDir.listFiles()).thenReturn(arrayOf(mockTorch1, mockFlash1))
        whenever(mockTorch1.name).thenReturn("torch0")
        whenever(mockTorch1.absolutePath).thenReturn("/sys/class/leds/torch0")
        whenever(mockFlash1.name).thenReturn("flash0")
        whenever(mockFlash1.absolutePath).thenReturn("/sys/class/leds/flash0")
        whenever(mockSwitch1.name).thenReturn("switch0")
        whenever(mockSwitch1.absolutePath).thenReturn("/sys/class/leds/switch0")

        val ledPaths = LedPathUtil.findLedPaths(mockBaseDir)

        // Single torch should be mapped to both white and yellow
        assertEquals("/sys/class/leds/torch0/brightness", ledPaths.WHITE_LED_PATH)
        assertEquals("/sys/class/leds/torch0/brightness", ledPaths.YELLOW_LED_PATH)
        // Single flash should be mapped to both flash white and yellow
        assertEquals("/sys/class/leds/flash0/brightness", ledPaths.FLASH_WHITE_LED_PATH)
        assertEquals("/sys/class/leds/flash0/brightness", ledPaths.FLASH_YELLOW_LED_PATH)
    }

    @Test
    fun `findLedPaths handles color-based naming`() {
        whenever(mockBaseDir.listFiles()).thenReturn(arrayOf(mockTorch1, mockTorch2))
        whenever(mockTorch1.name).thenReturn("white")
        whenever(mockTorch1.absolutePath).thenReturn("/sys/class/leds/white")
        whenever(mockTorch2.name).thenReturn("yellow")
        whenever(mockTorch2.absolutePath).thenReturn("/sys/class/leds/yellow")

        val ledPaths = LedPathUtil.findLedPaths(mockBaseDir)

        assertEquals("/sys/class/leds/white/brightness", ledPaths.WHITE_LED_PATH)
        assertEquals("/sys/class/leds/yellow/brightness", ledPaths.YELLOW_LED_PATH)
    }

    @Test
    fun `findLedPaths handles no brightness file gracefully`() {
        whenever(mockBaseDir.listFiles()).thenReturn(arrayOf(mockTorch1))
        whenever(mockTorch1.name).thenReturn("led:torch0")
        whenever(mockTorch1.listFiles()).thenReturn(emptyArray())

        val ledPaths = LedPathUtil.findLedPaths(mockBaseDir)

        // Should keep default paths if nothing found with brightness file
        assertEquals("/sys/class/leds/led:torch_0/brightness", ledPaths.WHITE_LED_PATH)
    }

    @Test
    fun `findLedPaths handles empty LED directory`() {
        whenever(mockBaseDir.listFiles()).thenReturn(null)

        val ledPaths = LedPathUtil.findLedPaths(mockBaseDir)

        assertEquals("/sys/class/leds/led:torch_0/brightness", ledPaths.WHITE_LED_PATH)
    }
}
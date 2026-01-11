package com.bartixxx.opflashcontrol

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi

/**
 * Fallback controller for flashlight using CameraManager API (Android 13+).
 * Used when root access is not available.
 *
 * @param context The application context
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class FlashlightFallbackController(private val context: Context) {
    private val cameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var maxTorchLevel: Int = 1
    private var isTorchOn: Boolean = false

    companion object {
        private const val TAG = "FlashlightFallback"
    }

    init {
        initializeCamera()
    }

    /**
     * Initializes the camera and gets the maximum torch strength level.
     */
    private fun initializeCamera() {
        try {
            // Find the back camera
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                
                // We want the back camera
                if (facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK) {
                    val hasFlash = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE)
                    if (hasFlash == true) {
                        cameraId = id
                        
                        // Get maximum torch strength level (Android 13+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            maxTorchLevel = characteristics.get(
                                android.hardware.camera2.CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL
                            ) ?: 1
                        }
                        
                        Log.d(TAG, "Camera initialized. ID: $cameraId, Max torch level: $maxTorchLevel")
                        break
                    }
                }
            }
            
            if (cameraId == null) {
                Log.e(TAG, "No back camera with flash found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing camera", e)
        }
    }

    /**
     * Gets the maximum torch strength level available.
     *
     * @return Maximum torch level (typically 1-5)
     */
    fun getMaxTorchLevel(): Int = maxTorchLevel

    /**
     * Turns on the torch with specified brightness level.
     *
     * @param level Brightness level (1 to maxTorchLevel)
     */
    fun turnOnTorch(level: Int) {
        val cameraId = this.cameraId
        if (cameraId == null) {
            Log.e(TAG, "Camera not initialized")
            showToast("Camera not available")
            return
        }

        try {
            val clampedLevel = level.coerceIn(1, maxTorchLevel)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                cameraManager.turnOnTorchWithStrengthLevel(cameraId, clampedLevel)
                isTorchOn = true
                Log.d(TAG, "Torch turned on at level $clampedLevel")
            } else {
                // Fallback for Android < 13
                cameraManager.setTorchMode(cameraId, true)
                isTorchOn = true
                Log.d(TAG, "Torch turned on (legacy mode)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error turning on torch", e)
            showToast("Error controlling flashlight")
        }
    }

    /**
     * Turns off the torch.
     */
    fun turnOffTorch() {
        val cameraId = this.cameraId
        if (cameraId == null) {
            Log.e(TAG, "Camera not initialized")
            return
        }

        try {
            cameraManager.setTorchMode(cameraId, false)
            isTorchOn = false
            Log.d(TAG, "Torch turned off")
        } catch (e: Exception) {
            Log.e(TAG, "Error turning off torch", e)
            showToast("Error controlling flashlight")
        }
    }

    /**
     * Checks if torch is currently on.
     *
     * @return true if torch is on, false otherwise
     */
    fun isTorchOn(): Boolean = isTorchOn

    /**
     * Maps application brightness (0-500) to torch level (1-maxTorchLevel).
     *
     * @param appBrightness Brightness value from app (0-500)
     * @return Torch level (1-maxTorchLevel)
     */
    fun mapBrightnessToTorchLevel(appBrightness: Int): Int {
        if (appBrightness <= 0) return 1
        
        // Map 0-500 to 1-maxTorchLevel
        val normalized = (appBrightness.toFloat() / 500f) * maxTorchLevel
        return normalized.toInt().coerceIn(1, maxTorchLevel)
    }

    /**
     * Shows a toast message on the UI thread.
     */
    private fun showToast(message: String) {
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

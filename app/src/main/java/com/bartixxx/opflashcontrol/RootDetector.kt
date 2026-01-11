package com.bartixxx.opflashcontrol

import android.util.Log
import java.io.File

/**
 * Utility class to detect root access availability.
 */
object RootDetector {
    private const val TAG = "RootDetector"
    private var rootAvailable: Boolean? = null

    /**
     * Checks if root access is available on the device.
     * Caches the result for performance.
     *
     * @return true if root access is available, false otherwise
     */
    fun isRootAvailable(): Boolean {
        // Return cached result if available
        rootAvailable?.let { return it }

        // Check for su binary in common paths
        val suPaths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/magisk/.core/bin/su",
            "/system/su/bin/su"
        )

        for (path in suPaths) {
            val file = File(path)
            if (file.exists()) {
                Log.d(TAG, "Found su binary at: $path")
                rootAvailable = true
                return true
            }
        }

        // Additional check: try to find su in PATH
        val whichSu = findExecutable("su")
        if (whichSu != null) {
            Log.d(TAG, "Found su in PATH: $whichSu")
            rootAvailable = true
            return true
        }

        // If no su binary found, root is not available
        Log.d(TAG, "No su binary found - device is not rooted")
        rootAvailable = false
        return false
    }

    /**
     * Finds an executable in the PATH.
     */
    private fun findExecutable(name: String): String? {
        val paths = System.getenv("PATH")?.split(":") ?: return null
        for (path in paths) {
            val file = File(path, name)
            if (file.exists() && file.canExecute()) {
                return file.absolutePath
            }
        }
        return null
    }

    /**
     * Resets the cached root availability state.
     * Useful for testing or when permission state might have changed.
     */
    fun reset() {
        rootAvailable = null
    }
}

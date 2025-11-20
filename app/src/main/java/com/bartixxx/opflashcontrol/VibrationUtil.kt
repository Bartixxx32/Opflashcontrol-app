@file:Suppress("DEPRECATION")

package com.bartixxx.opflashcontrol

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * A utility object for controlling the device's vibrator.
 */
object VibrationUtil {
    /**
     * Vibrates the device for a specified duration.
     *
     * @param context The context.
     * @param duration The duration to vibrate for, in milliseconds.
     */
    fun vibrate(context: Context, duration: Long) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            // Deprecated in API 26
            vibrator.vibrate(duration)
        }
    }
}

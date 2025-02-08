package com.bartixxx.opflashcontrol

import android.app.Activity
import android.os.Bundle

class DummySettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Immediately finish so nothing is shown to the user.
        finish()
    }
}

package com.bartixxx.opflashcontrol

import android.app.Activity
import android.os.Bundle

/**
 * An empty activity that is used as a placeholder for the settings activity.
 *
 * This activity is used to provide a settings button in the app's notification settings.
 * When the user clicks on the settings button, this activity is opened and immediately closed.
 */
class DummySettingsActivity : Activity() {
    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Immediately finish so nothing is shown to the user.
        finish()
    }
}

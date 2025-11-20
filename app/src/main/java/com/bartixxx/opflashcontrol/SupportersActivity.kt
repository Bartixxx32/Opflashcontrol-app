package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bartixxx.opflashcontrol.databinding.ActivitySupportersBinding

/**
 * An activity that displays a list of supporters.
 *
 * This activity uses a RecyclerView to display a list of supporters.
 */
class SupportersActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportersBinding

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Use the data from SupportersData
        val supporters = SupportersData.supporters

        // Setup RecyclerView
        binding.supportersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.supportersRecyclerView.adapter = SupportersAdapter(supporters)

        // Setup back button
        binding.backbutton.setOnClickListener {
            VibrationUtil.vibrate(this@SupportersActivity, 100L)
            backToMainActivity()
        }
    }

    /**
     * Navigates back to the main activity.
     */
    private fun backToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Optional: close SupportersActivity
    }
}

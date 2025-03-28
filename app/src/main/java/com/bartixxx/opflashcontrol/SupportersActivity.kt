package com.bartixxx.opflashcontrol

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bartixxx.opflashcontrol.databinding.ActivitySupportersBinding

class SupportersActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportersBinding

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

    private fun backToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Optional: close SupportersActivity
    }
}

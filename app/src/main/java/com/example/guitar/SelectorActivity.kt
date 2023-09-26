package com.example.guitar

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.guitar.databinding.ActivitySelectorBinding

class SelectorActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cvUploadAudio.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.cvRecordAudio.setOnClickListener {
            startActivity(Intent(this, RecordAudio::class.java))
        }

    }
}
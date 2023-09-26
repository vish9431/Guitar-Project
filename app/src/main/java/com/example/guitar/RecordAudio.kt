package com.example.guitar

import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.guitar.databinding.ActivityRecordAudioBinding
import java.io.File
import java.io.IOException

class RecordAudio : AppCompatActivity() {
    private lateinit var binding: ActivityRecordAudioBinding
    //private lateinit var chronometer: Chronometer
    private lateinit var startButton: ImageButton
    private lateinit var stopButton: ImageButton
    private lateinit var saveButton: ImageButton

    private var isRecording = false
    private lateinit var mediaRecorder: MediaRecorder
    private var outputFilePath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)

        //chronometer = findViewById(R.id.playchronometer)
        startButton = findViewById(R.id.start)
        stopButton = findViewById(R.id.stop)
        saveButton = findViewById(R.id.save)

        mediaRecorder = MediaRecorder()
        outputFilePath = getOutputFilePath()

        startButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
            }
        }

        stopButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
        }

        saveButton.setOnClickListener {
            if (!isRecording) {
                // Save the recorded audio (outputFilePath) to a location like "android->media->package name"
                val newLocation = getNewStorageLocation()
                val newFilePath = "$newLocation/RecordedAudio.3gp"

                try {
                    val sourceFile = File(outputFilePath)
                    val destinationFile = File(newFilePath)

                    sourceFile.copyTo(destinationFile)

                    // Display a success message or perform other actions as needed
                    Toast.makeText(this, "Audio saved to $newFilePath", Toast.LENGTH_SHORT).show()

                    // Pass the recorded audio file path to the next activity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("audioPath", newFilePath)
                    startActivity(intent)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to save audio", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getNewStorageLocation(): String {
        val packageName = applicationContext.packageName
        val storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val appDirectory = File(storageDirectory, packageName)

        if (!appDirectory.exists()) {
            appDirectory.mkdirs()
        }

        val newLocation = appDirectory.absolutePath

        return newLocation
    }


    private fun startRecording() {
        if (checkPermissions()) {
            try {
                mediaRecorder.reset()
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                mediaRecorder.setOutputFile(outputFilePath)
                mediaRecorder.prepare()
                mediaRecorder.start()
                isRecording = true

                val recorder = MediaRecorder()
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setOutputFile(
                    Environment.getExternalStorageDirectory()
                        .absolutePath + "/myrecording.mp3"
                )
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.prepare()
                recorder.start()



                // Start the chronometer
//                chronometer.base = SystemClock.elapsedRealtime()
//                chronometer.start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            mediaRecorder.stop()
            mediaRecorder.reset()
            mediaRecorder.release()
            isRecording = false

            // Stop the chronometer
            //chronometer.stop()
        }
    }

    private fun checkPermissions(): Boolean {
        val recordAudioPermission = android.Manifest.permission.RECORD_AUDIO
        val writeStoragePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        val permissions = arrayOf(recordAudioPermission, writeStoragePermission)
        var granted = true

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
        }

        return granted
    }

    private fun getOutputFilePath(): String {
        val timeStamp = System.currentTimeMillis()
        val fileName = "Recording_$timeStamp.3gp"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath ?: ""
        return "$storageDir/$fileName"
    }
}

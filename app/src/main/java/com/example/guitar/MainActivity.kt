package com.example.guitar

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.guitar.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pl.droidsonroids.gif.GifImageView
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val PICK_AUDIO_REQUEST = 1
    private val PERMISSION_REQUEST = 2
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //gif homepage
        binding.gifImageView.setImageResource(R.drawable.anibg)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openFilePicker()
            }
        }

        //audio upload
        binding.btnupload.setOnClickListener {
            if (checkPermission()) {
                openFilePicker()
            } else {
                requestPermissions()
            }
        }
    }


    private fun checkPermission():Boolean {
        val readStoragePermission = ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return readStoragePermission == PackageManager.PERMISSION_GRANTED
    }
    private fun requestPermissions() {
        requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)

    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "audio/*" // Set the MIME type to restrict to audio files
        startActivityForResult(intent, PICK_AUDIO_REQUEST)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFilePicker()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { audioUri ->
                val audioPath = getAudioPath(audioUri)


                val displayName = getDisplayName(audioUri)
                binding.etAudio.setText(displayName)

                //API
                sendAudioToAPI(audioPath) //ERROR CAUSING


            }
        }
    }

    private fun sendAudioToAPI(audioPath: String) {
        val audioFile = File(audioPath)
        Log.d("audioPath","qwerty: $audioPath")
        Log.d("audioFile","qwerty: $audioFile")

        if (!audioFile.exists()) {
            // Handling the case where the file does not exist
            return
        }

        val requestFile = RequestBody.create(MediaType.parse("audio/*"), audioFile)
        val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, requestFile)

        //ERROR CAUSING
        val apiService = Retrofitinstance.Apiinterface

        // Ensure that the binding and progressBar are not null
        binding?.let { binding ->
            binding.progressBar?.visibility = View.VISIBLE
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response: Response<responseDataClass> = apiService.uploadAudio(audioPart)

                if (response.isSuccessful) {
                    val result = response.body()

                    //  responseDataClass has a 'data' field that's a list of strings
                    val data = result?.data?.joinToString("||")
                    Log.d("RESULT", "Data: $data")

                    withContext(Dispatchers.Main) {
                        binding.let { binding ->
                            binding.etDesc.setText(data)
                            binding.progressBar.visibility = View.INVISIBLE
                        }
                    }
                } else {
                    // Handling API error
                    withContext(Dispatchers.Main) {
                        binding?.let { binding ->
                            binding.etDesc?.setText("API Error: ${response.code()}")
                            binding.progressBar?.visibility = View.INVISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    binding?.let { binding ->
                        binding.etDesc?.setText("Network Error: ${e.message}")
                        binding.progressBar?.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }




    private fun getDisplayName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.getString(nameIndex)
        } ?: ""
    }


    private fun getAudioPath(uri: Uri): String {
        var path = ""
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            path = cacheDir.absolutePath + File.separator + cursor.getString(nameIndex)
            val inputStream = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(File(path))
            inputStream?.copyTo(outputStream)
        }
        return path
    }






}
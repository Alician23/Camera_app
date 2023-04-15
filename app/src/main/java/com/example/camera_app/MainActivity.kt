package com.example.camera_app

import android.app.Instrumentation
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {

    private lateinit var imageButton: ImageButton
    private lateinit var mainView: View

    private var newPhotoPath: String? = null
    private  var visibleImagePath: String? = null

    private val NEW_PHOTO_PATH_KEY = "new photo path key"
    private val VISIBLE_IMAGE_PATH_KEY = "visible image path key"

    private val cameraActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        result -> handleImage(result)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newPhotoPath = savedInstanceState?.getString(NEW_PHOTO_PATH_KEY)
        visibleImagePath = savedInstanceState?.getString((VISIBLE_IMAGE_PATH_KEY))

        imageButton = findViewById(R.id.imageButton)
        imageButton.setOnClickListener {
            takePicture()

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NEW_PHOTO_PATH_KEY, newPhotoPath)
        outState.putString(VISIBLE_IMAGE_PATH_KEY, visibleImagePath)

    }

    private fun takePicture() {
       val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
       val (photoFile, photoFilePath) = createImageFile()
       if (photoFile != null) {
           newPhotoPath = photoFilePath
           val photoUri = FileProvider.getUriForFile(
               this,
               "com.example.camera_app.fileprovider",
               photoFile
           )
           takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
           cameraActivityLauncher.launch(takePictureIntent)

       }

        startActivity(takePictureIntent)
    }

    private fun createImageFile(): Pair<File?, String?> {
        try {
            val dateTime = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val imageFileName = "COLLAGE_$dateTime"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File.createTempFile(imageFileName, ".jpg", storageDir)
            val filePath = file.absolutePath
            return file to filePath
        }catch (ex: IOException) {
            return null to null
        }
    }

    private fun handleImage(result: Instrumentation.ActivityResult) {

        when (result.resultCode) {
            RESULT_OK -> {
                Log.d(TAG, "Result ok, user took picture,image at $newPhotoPath")
                visibleImagePath = newPhotoPath

            }
            RESULT_CANCELED ->
                Log.d(TAG, "Result cancelled, no picture taken")
            }
        }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG, "on window focus changed $hasFocus visible image at $visibleImagePath")
        if (hasFocus) {
            visibleImagePath?. let {imagePath ->
                loadImage(imageButton, imagePath)

            }
        }
    }

    private fun loadImage(imageButton: ImageButton, imagePath: String) {
         Picasso.get()
             .load(File(imagePath))
             .error(android.R.drawable.stat_notify_error)
             .fit()
             .centerCrop()
             .into(imageButton, object: Callback {
                 override fun onSuccess() {
                     Log.d(TAG, "Loaded image $imagePath")
                 }

                 override fun onError(e: Exception?) {
                     Log.e(TAG, "Error loading image $imagePath", e)
                 }
             })

    }
}
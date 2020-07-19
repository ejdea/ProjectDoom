package com.doomteam.doodlemaze

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create_maze.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata

const val TAG_INFO = "INFO"

class CreateMaze : AppCompatActivity() {
    companion object {
        init {
            /* Load OpenCV before class is instantiated */
            if (!OpenCVLoader.initDebug()) {
                // Handle initialization error
                Log.d(TAG_INFO, "OpenCV failed to load")
            } else {
                Log.d(TAG_INFO, "OpenCV loaded successfully")
            }
        }

        var currentImage: ImageMarkup? = null
        const val app_folder = "app_height_maps";
        const val height_map_name = "mobile_height_map.raw"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_maze)

        // Check if camera and write permission is available
        if (!this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this,
                "No camera available. Please enable camera permissions.",
                Toast.LENGTH_LONG).show()
        }

        // Take picture with the camera or load an image from gallery. Then, crop image.
        cropImage()
    }

    fun onClickCancel(view: View) {
        finish()
    }

    /**
     * Uploads the currently cropped maze heightmap to Firebase Storage
     *
     */
    fun onClickBuildMaze(view: View) {
        Log.d(TAG_INFO, "onClickBuildMaze")

        // Get storage reference
        val storage = Firebase.storage
        val sRef = storage.reference

        // Retrieve user info from intent
        val user:String? = intent.getStringExtra("current_user")
            ?: //user made it here without logging in
            return

        val heightMapRef: StorageReference? = sRef.child(app_folder).child(user!!).child(
            height_map_name);

        if(heightMapRef == null || currentImage == null)
        {
            // If the currentImage is null, an error occurred while processing heightmap
            // If the heightMapRef is null, an error occurred while connecting to Firebase storage
            return
        }

        // Write height map to {app_folder}/{user_id}/mobile_height_map.raw
        val uploadTask = heightMapRef.putBytes(currentImage!!.GetHeightMap())
        uploadTask.addOnFailureListener{
            // Unable to upload the file
            // TODO: Add handler, but for now just restart process
            Log.d(TAG_INFO, "Upload failed!")
            cropImage()

        }.addOnSuccessListener {
            // Done
            finish()
            Log.d(TAG_INFO, "Upload done!")

            // Launch Unity
            Log.d(TAG_INFO, "Launch Unity")
            val unityIntent:Intent? = applicationContext.packageManager.getLaunchIntentForPackage("com.doomteam.unitydoodlemaze")
            unityIntent?.addCategory(Intent.CATEGORY_LAUNCHER)
            if (unityIntent != null) {
                applicationContext.startActivity(unityIntent)
            } else {
                Log.d(TAG_INFO, "unityIntent == null")
            }
        }
    }

    fun onClickCreateNewMaze(view: View) {
        cropImage()
    }

    private fun cropImage() {
        // Take picture with the camera or load an image from gallery. Then, crop image.
        // Reference: https://github.com/ArthurHub/Android-Image-Cropper
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode != RESULT_OK) {
                    Log.d(TAG_INFO, "Error occurred when image cropping (Code $resultCode)")
                    finish()
                }

                // Get cropped image result
                val result = CropImage.getActivityResult(data)
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val resultUri: Uri = result.uri

                        // Convert image to Bitmap
                        val bmp = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)

                        // Generate height map from bitmap
                        val croppedBmp = generateHeightMap(bmp)
                        // Display the heightmap to the user
                        mazeImageView.setImageBitmap(croppedBmp)

                    }
                    else -> {
                        Log.d(TAG_INFO, "Error occurred during image cropping ($result.error)")
                    }
                }
            }
            else -> {
                Log.d(TAG_INFO, "Unrecognized request code $requestCode")
            }
        }
    }

    override fun onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG_INFO, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG_INFO, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    private fun generateHeightMap(imageBitmap: Bitmap): Bitmap {
        if (imageBitmap == null) {
            Log.d(TAG_INFO, "WARNING: imageBitmap == null")
        }

        currentImage = ImageMarkup(imageBitmap, imageBitmap.width, imageBitmap.height)
        currentImage!!.Filter(10)
        currentImage!!.GenerateHeightMap()

        return currentImage!!.GetBitmap()
    }
}
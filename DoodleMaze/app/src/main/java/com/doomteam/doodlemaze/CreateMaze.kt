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

const val IMAGE_CAPTURE_REQUEST_CODE = 1000
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

    fun onClickBuildMaze(view: View) {
        Log.d(TAG_INFO, "onClickBuildMaze")
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

    /*fun onClickCaptureMaze(view: View) {
        // Check if camera and write permission is available
        if (!this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this,
                "No camera available. Please enable camera permissions.",
                Toast.LENGTH_LONG).show()
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity((packageManager)) != null) {
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_REQUEST_CODE)
        }
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG_INFO, "requestCode = $requestCode")
        Log.d(TAG_INFO, "CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE = " + CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)
        Log.d(TAG_INFO, "CROP_IMAGE_ACTIVITY_REQUEST_CODE = " + CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)

        when (requestCode) {
            /*CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE -> {
                if (resultCode != RESULT_OK) {
                    Log.d(TAG_INFO, "Error occurred during image capture (Code $resultCode)")
                    return
                }

                if (data != null) {
                    // Get the image captured
                    val imageBitmap = data.extras?.get("data") as Bitmap
                    mazeImageView.setImageBitmap(imageBitmap)
                }
            }*/
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

        Log.d(TAG_INFO, "onActivityResult END")
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

        val img = ImageMarkup(imageBitmap, imageBitmap.width, imageBitmap.height)
        img.Filter(10)
        img.GenerateHeightMap()

        return img.GetBitmap()
    }
}
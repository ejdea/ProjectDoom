package com.doomteam.doodlemaze

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create_maze.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader


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
        const val HEIGHTMAP_RESOLUTION = 1025
        const val app_folder = "app_height_maps";
        const val height_map_name = "mobile_height_map.raw"

        var positionData: List<Int>? = null
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
     * On click, creates a map and uploads a map to Google Firebase Storage
     *
     * @param view Current view
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

        if(heightMapRef == null || currentImage == null || positionData == null)
        {
            // If the currentImage is null, an error occurred while processing heightmap
            // If the heightMapRef is null, an error occurred while connecting to Firebase storage
            // If the positionData is null, an error occurred with text recognition
            return
        }


        // Create Byte buffer to hold position data along with heightmap data
        val dataBuffer = buildMapData(currentImage!!.GetHeightMap(), positionData!!)


        val uploadTask = heightMapRef.putBytes(dataBuffer)
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

    /**
     * Builds a map with all the necessary information needed to play the game
     *
     * n = number of bytes in position data
     * m = number of bytes in heightmap data
     *
     * @param heightmap heightmap data from the image, 1025x1025x16 bits 2,101,250 bytes
     * @param objectPositions position data for start and end positions, 32 bytes
     * @return complete man with all the needed data to build a game state in unity
     */
    private fun buildMapData(heightmap: ByteArray, objectPositions: List<Int>): ByteArray{

        // allocate n + m byte buffer
        val dataBuffer = ByteArray((objectPositions.size * 4) + heightmap.size)
        // convert integer positions into byte data using utility function in ImageMarkup
        val bytePosData = ImageMarkup.GetPositionBytes(objectPositions)
        // copy n bytes from position data into byte buffer
        var offset = 0
        for(i in bytePosData.indices){
            dataBuffer[i] = bytePosData[i]
            offset++
        }
        // copy m bytes from heightmap data into byte buffer
        for(i in heightmap.indices){
            dataBuffer[i+offset] = heightmap[i]
        }

        return dataBuffer
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

                        // Image retrieved from source, start image processing
                        detectText(result.uri)

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

    /**
     * Runs google's Text Detection ML kit for Firebase on the selected picture
     *
     *
     * @param resultUri location of the picture to run text detection on
     */
    private fun detectText(resultUri: Uri){

        // Convert image to Bitmap
        var bmp = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
        // resize bitmap to 4k x 4k maintaining aspect ratio
        bmp = resizeBmp(bmp)

        val image = InputImage.fromBitmap(bmp, 0)
        val recognizer = TextRecognition.getClient()

        // Start OCR task to look for start/end features
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                //process results
                var startBoxTopLeft: Point? = null
                var startBoxBottomRight: Point? = null
                var endBoxTopLeft: Point? = null
                var endBoxBottomRight: Point? = null
                for (block in visionText.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val elementText = element.text
                            val elementCornerPoints = element.cornerPoints
                            if(elementText[0] == 'X' || elementText[0] == 'x') {
                                startBoxTopLeft = elementCornerPoints!![0]
                                startBoxBottomRight = elementCornerPoints[2]
                            }
                            else if(elementText[0] == 'O' || elementText[0] == 'o')
                            {
                                endBoxTopLeft = elementCornerPoints!![0]
                                endBoxBottomRight = elementCornerPoints[2]
                            }
                        }
                    }
                }

                if(startBoxTopLeft != null && startBoxBottomRight != null && endBoxTopLeft != null && endBoxBottomRight != null)
                {
                    Log.d(TAG_INFO, "Text recognition succeeded!")

                    // Generate height map from the image with features correctly recognized
                    generateHeightMap(bmp, HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION)
                    positionData = removeBoundingBoxes(startBoxTopLeft, startBoxBottomRight, endBoxTopLeft, endBoxBottomRight, bmp.width, bmp.height)
                    // Display converted height map with features removed
                    mazeImageView.setImageBitmap(currentImage!!.GetBitmap())

                }
                else{
                    Log.d(TAG_INFO, "Character not located, try cropping again")
                    cropImage()
                }


            }
            .addOnFailureListener{ e ->
                Log.d(TAG_INFO, "Text recognition failed")
            }

        return
    }

    /**
     * Utility function to resize an Android Bitmap
     *
     * Resizes a bitmap to a max of 4k x 4k resolution while maintaining aspect ratio
     *
     * @param bmp Bitmap to be resized
     * @return resized bitmap
     */
    private fun resizeBmp(bmp: Bitmap): Bitmap{
        val maxHeight = 4000
        val maxWidth = 4000
        val scale: Float = Math.min(
            maxHeight.toFloat() / bmp.width,
            maxWidth.toFloat() / bmp.height
        )

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        return Bitmap.createBitmap(
            bmp,
            0,
            0,
            bmp.width,
            bmp.height,
            matrix,
            true
        )
    }

    /**
     * Function that removes the bounding boxes detected in the CV text detection method
     *
     *
     * @param startBoxTopLeft top left 'X'
     * @param startBoxBottomRight bottom right 'X'
     * @param endBoxTopLeft top left 'O'
     * @param endBoxBottomRight bottom right 'O'
     */
    private fun removeBoundingBoxes(startBoxTopLeft: Point?, startBoxBottomRight: Point?, endBoxTopLeft: Point?, endBoxBottomRight: Point?, maxWidth: Int, maxHeight: Int): List<Int>?{
        //convert pixel locations to 1025x1025 space
        val sTLx = (((startBoxTopLeft!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val sTLy =  (((startBoxTopLeft.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val sBRx = (((startBoxBottomRight!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val sBRy =  (((startBoxBottomRight.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()

        val eTLx = (((endBoxTopLeft!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val eTLy =  (((endBoxTopLeft.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val eBRx = (((endBoxBottomRight!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val eBRy =  (((endBoxBottomRight.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()


        if(currentImage == null)
        {
            Log.d(TAG_INFO, "Warning: currentImage needs to be created before removing bounding boxes!")
            return null
        }
        // remove the content in the bounding boxes mark
        currentImage!!.Resize(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION)
        currentImage!!.RemoveBoundingBox(sTLx, sTLy, sBRx, sBRy, -10, -10)
        currentImage!!.RemoveBoundingBox(eTLx, eTLy, eBRx, eBRy, -10, -10)

        // recreate the height map with X and O removed
        currentImage!!.GenerateHeightMap()

        //(player.x1,y1); (player.x2,y1); (endBox.x1,y1); (endBox.x2,y2)
        return listOf(sTLx, sTLy, sBRx, sBRy, eTLx, eTLy, eBRx, eBRy)

    }

    private fun generateHeightMap(imageBitmap: Bitmap?, width: Int, height: Int): Bitmap {
        if (imageBitmap == null) {
            Log.d(TAG_INFO, "WARNING: imageBitmap == null")
        }

        currentImage = ImageMarkup(imageBitmap, width, height)
        currentImage!!.Filter(10)
        currentImage!!.GenerateHeightMap()

        return currentImage!!.GetBitmap()
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


}
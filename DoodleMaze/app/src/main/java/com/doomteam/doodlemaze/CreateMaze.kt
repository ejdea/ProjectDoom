package com.doomteam.doodlemaze

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_create_maze.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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
        const val PERMISSION_REQUEST_CODE = 1723
        const val DETECTION_ERROR = 100
        const val app_folder = "app_height_maps"
        const val height_map_name = "mobile_height_map.raw"

        var originalImageUri: Uri? = null
        var manualCropMode: Boolean = false
        var idStartObject: Boolean = false
        var idEndObject: Boolean = false
        var startIdentified: Boolean = false
        var endIdentified: Boolean = false
        var onManualResult: Boolean = false

        var mXTL: Point = Point()
        var mXBR: Point = Point()
        var mOTL: Point = Point()
        var mOBR: Point = Point()

        var processor:OCRProcessor? = null;
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_maze)

        buildMazeButton.isEnabled = false
        buildMazeButton.isClickable = false

        // Check if camera and write permission is available
        if (!this.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this,
                "No camera available. Please enable camera permissions.",
                Toast.LENGTH_LONG).show()
        }

        // Request app permissions to camera and storage
        var permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (!checkAppPermissions()) {
            Log.d(TAG_INFO, "Requesting app permissions")
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            // Take picture with the camera or load an image from gallery. Then, crop image.
            CropImage.startPickImageActivity(this)
        }
    }

    private fun checkAppPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
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

        buildMazeButton.isEnabled = false
        buildMazeButton.isClickable = false

        // Get storage reference
        val storage = Firebase.storage
        val sRef = storage.reference

        // Retrieve user info from intent
        val user:String? = intent.getStringExtra("current_user")
            ?: //user made it here without logging in
            return

        val heightMapRef: StorageReference? = sRef.child(app_folder).child(user!!).child(
            height_map_name)

        if(!OCRProcessor.good || heightMapRef == null)
        {
            Log.d(TAG_INFO, "Error occurred with processing image")
            return
        }

        // Build level data
        val levelData = LevelData(OCRProcessor.ocvImage.GetHeightMap(), OCRProcessor.positionData)

        // Save level for later play
        val fileName = fileNameGenerator()

        if (fileName != null) {
            val bitmapImage = levelData.GetImage()
            createThumbnail(bitmapImage, fileName)
            saveLevelData(levelData, fileName)
        }

        val uploadTask = heightMapRef.putBytes(levelData.GetData())
        uploadTask.addOnFailureListener{
            // Unable to upload the file
            // TODO: Add handler, but for now just restart process
            Log.d(TAG_INFO, "Upload failed!")
            cropImage()

        }.addOnSuccessListener {
            Log.d(TAG_INFO, "Upload done!")

            dbStoreMap(levelData.GetHash())
        }
    }

    fun onClickCreateNewMaze(view: View) {
        buildMazeButton.isEnabled = false
        buildMazeButton.isClickable = false

        // Take picture with the camera or load an image from gallery. Then, crop image.
        CropImage.startPickImageActivity(this)
    }

    /**
     * Returns the current date and time in a predetermined format.
     * Reference: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
     */
    private fun fileNameGenerator(): String? {
        val time = LocalDateTime.now()
        val timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh.mm.ss.SSS")
        return time.format(timeFormatter)
    }

    /**
     * Creates and stores a 1200px by 1200px thumbnail
     * Reference for directory creation: https://stackoverflow.com/a/44425281
     */
    private fun createThumbnail(incBitmap: Bitmap, incFileName: String){
        // Create image from bitmap
        val thumbnailBmp = ThumbnailUtils.extractThumbnail(incBitmap, 1200, 1200)
        val filePath = getExternalFilesDir(null).toString() + "/maze_thumbnails"
        val fileName = incFileName + "_thumb.png"

        // Create parent directory for file object
        val externalDir = File(filePath)
        externalDir.mkdirs()

        val fileObject = File(externalDir, fileName)
        try {
            val fileOutStream = FileOutputStream(fileObject)
            thumbnailBmp.compress(Bitmap.CompressFormat.PNG, 100, fileOutStream)
        } catch (e: FileNotFoundException) {
            Log.e(TAG_INFO, "A file not found exception has occurred.")
            e.printStackTrace()
        }
    }

    /**
     * Saves the LevelData locally to be used later.
     */
    private fun saveLevelData(levelData: LevelData, incFileName: String){
        val filePath = getExternalFilesDir(null).toString() + "/saved_mazes"

        // Create parent directory for file object
        val externalDir = File(filePath)
        externalDir.mkdirs()

        try {
            levelData.Save(filePath,incFileName)
        } catch (e: FileNotFoundException) {
            Log.e(TAG_INFO, "A file not found exception has occurred.")
            e.printStackTrace()
        }
    }

    private fun dbStoreMap(hash: String){
        val db = Firebase.firestore
        val entityData = hashMapOf(
            "score" to -1.0f,
            "hash" to hash
        )

        // check to see if map already exists
        val mapRef = db.collection("Maps")
        val query = mapRef.whereEqualTo("hash", hash)
        query.get().addOnCompleteListener { result ->
            if(result.isSuccessful){
                if(result.result!!.size() == 0)
                {
                    mapRef.document().set(entityData)
                }
            }
            else{
                Log.d(TAG_INFO, "Failed to upload to database!")
            }

            // Done
            finish()

            // Launch Unity
            Log.d(TAG_INFO, "Launch Unity")
            val unityIntent:Intent? = applicationContext.packageManager.getLaunchIntentForPackage("com.doomteam.unitydoodlemaze")
            unityIntent?.addCategory(Intent.CATEGORY_LAUNCHER)
            if (unityIntent != null) {
                applicationContext.startActivity(unityIntent)
            } else {
                Log.d(TAG_INFO, "unityIntent == null")
                Toast.makeText(this,
                    "Unable to play maze. Please install unitydoodle-maze.",
                    Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun cropImage(){
        // Take picture with the camera or load an image from gallery. Then, crop image.
        // Reference: https://github.com/ArthurHub/Android-Image-Cropper
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }

    private fun cropImage(uri: Uri) {
        // Take picture with the camera or load an image from gallery. Then, crop image.
        // Reference: https://github.com/ArthurHub/Android-Image-Cropper
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (checkAppPermissions()) {
                    Log.d(TAG_INFO, "start image picker")
                    CropImage.startPickImageActivity(this)
                } else {
                    Toast.makeText(this,
                        "Please enable camera and storage permission to load mazes",
                        Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
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

                        // if automatic detection failed
                        if(manualCropMode) {
                            // if a result was returned through manual cropping
                            if(onManualResult)
                            {
                                // if result target was the start object and it hasn't been identified
                                if(idStartObject && !startIdentified)
                                {
                                    mXTL.x = result.cropRect.left
                                    mXTL.y = result.cropRect.top
                                    mXBR.x = result.cropRect.right
                                    mXBR.y = result.cropRect.bottom
                                    onManualResult = false
                                    idStartObject = false
                                    startIdentified = true
                                }

                                // if result target was the end object and it hasn't been identified
                                else if(idEndObject && !endIdentified)
                                {
                                    mOTL.x = result.cropRect.left
                                    mOTL.y = result.cropRect.top
                                    mOBR.x = result.cropRect.right
                                    mOBR.y = result.cropRect.bottom
                                    onManualResult = false
                                    idEndObject = false
                                    endIdentified = true
                                }
                                if(startIdentified && endIdentified)
                                {
                                    // stop manually detecting
                                    manualCropMode = false
                                }
                            }

                            // if start or end hasn't been identified, launch activity with flags set
                            if(!startIdentified || !endIdentified)
                            {
                                if(!startIdentified)
                                {
                                    onManualResult = true
                                    idStartObject = true
                                    cropImage(originalImageUri!!)
                                }
                                else if(!endIdentified)
                                {
                                    onManualResult = true
                                    idEndObject = true
                                    cropImage(originalImageUri!!)
                                }
                            }
                            else{
                                // both the start and end have been identified and absolute positions have been set.
                                processor!!.RunManualRoutine(mXTL, mXBR, mOTL, mOBR)
                                mazeImageView.setImageBitmap(OCRProcessor.ocvImage.GetBitmap());
                                buildMazeButton.isEnabled = true
                                buildMazeButton.isClickable = true

                                // save a tmp version of this image to local file storage
                                val filePath = getExternalFilesDir(null).toString() + "/tmp"
                                // Create parent directory for file object
                                val externalDir = File(filePath)
                                externalDir.mkdirs()
                                val file = File(filePath, "tmp.bmp")
                                val outFile = FileOutputStream(file)
                                // write bitmap to local storage for later retrieval
                                OCRProcessor.ocvImage.GetBitmap().compress(Bitmap.CompressFormat.PNG, 100, outFile)
                                outFile.flush()
                                outFile.close()
                                manualCropMode = false
                                idStartObject = false
                                idEndObject = false
                                onManualResult = false
                                endIdentified = false
                                startIdentified = false

                            }
                        }
                        else{
                            processor = OCRProcessor(result.cropRect.left, result.cropRect.right, result.cropRect.top, result.cropRect.bottom)
                            if(!OCRProcessor.good)
                            {
                                //Start manual cropping
                                manualCropMode = true
                                idStartObject = true
                                onManualResult = true
                                cropImage(originalImageUri!!)

                                //val returnIntent = Intent()
                                //setResult(DETECTION_ERROR, returnIntent)
                                //returnIntent.putExtra("error_code", "1")
                                //finish()
                                return
                            }
                            //else
                            //{
                            //    mazeImageView.setImageBitmap(OCRProcessor.ocvImage.GetBitmap());
                            //    buildMazeButton.isEnabled = true
                            //    buildMazeButton.isClickable = true
                            //}
                        }
                    }
                    else -> {
                        Log.d(TAG_INFO, "Error occurred during image cropping ($result.error)")
                    }
                }
            }

            CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE -> {
                //User selected an ORIGINAL image, initialize cropping on image
                if(resultCode != RESULT_OK){
                    Log.d(TAG_INFO, "Error occurred when picking image (Code $resultCode)")
                    finish()
                    return
                }

                val result = CropImage.getPickImageResultUri(this, data)
                OCRProcessor.originalImage = MediaStore.Images.Media.getBitmap(this.contentResolver, result)

                // save a tmp version of this image to local file storage
                val filePath = getExternalFilesDir(null).toString() + "/tmp"
                // Create parent directory for file object
                val externalDir = File(filePath)
                externalDir.mkdirs()
                val file = File(filePath, "tmp.bmp")
                val outFile = FileOutputStream(file)
                // write bitmap to local storage for later retrieval
                OCRProcessor.originalImage.compress(Bitmap.CompressFormat.PNG, 100, outFile)
                outFile.flush()
                outFile.close()

                originalImageUri = Uri.fromFile(file)

                //Init crop activity on selected uri
                cropImage(result)
            }
            else -> {
                Log.d(TAG_INFO, "Unrecognized request code $requestCode")
            }
        }
    }

    private fun setXLocation(){

    }

    private fun setOLocation(){

    }

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG_INFO, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG_INFO, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
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
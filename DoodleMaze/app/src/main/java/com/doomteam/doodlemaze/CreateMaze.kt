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
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
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
        const val HEIGHTMAP_RESOLUTION = 1025
        const val app_folder = "app_height_maps"
        const val height_map_name = "mobile_height_map.raw"

        var ocvImage: ImageMarkup? = null
        var originalImage: Bitmap? = null
        var croppedBmp: Bitmap? = null
        var ocrImage: InputImage? = null

        var positionData: List<Int>? = null

        // Source image dimensions
        var sxDim: Int = 0
        var syDim: Int = 0

        // Crop image dimensions
        var cx1Dim: Int = 0
        var cx2Dim: Int = 0
        var cy1Dim: Int = 0
        var cy2Dim: Int = 0
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

        // Get storage reference
        val storage = Firebase.storage
        val sRef = storage.reference

        // Retrieve user info from intent
        val user:String? = intent.getStringExtra("current_user")
            ?: //user made it here without logging in
            return

        val heightMapRef: StorageReference? = sRef.child(app_folder).child(user!!).child(
            height_map_name)

        if(heightMapRef == null || ocvImage == null || positionData == null)
        {
            // If the currentImage is null, an error occurred while processing heightmap
            // If the heightMapRef is null, an error occurred while connecting to Firebase storage
            // If the positionData is null, an error occurred with text recognition
            return
        }

        // recreate the height map with X and O removed
        ocvImage!!.GenerateHeightMap()

        // Build level data
        val levelData = LevelData(ocvImage!!.GetHeightMap(), positionData!!)

        // Save level for later play
        val fileName = fileNameGenerator()

        if (fileName != null) {
            val bitmapImage =levelData.GetImage()
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

                        // Image retrieved from source, start image processing
                        initCroppedBmp(result.uri)
                        cx1Dim = result.cropRect.left
                        cx2Dim = result.cropRect.right
                        cy1Dim = result.cropRect.top
                        cy2Dim = result.cropRect.bottom

                        resizeCroppedBmp(4000, 4000)
                        ocrImage = InputImage.fromBitmap(croppedBmp!!, 0)

                        val ocrResult = startDetectionRoutine()
                        if(!ocrResult)
                        {
                            Log.d(TAG_INFO, "Unable to detect X and O in detection routine")
                            val returnIntent = Intent()
                            setResult(DETECTION_ERROR, returnIntent)
                            returnIntent.putExtra("error_code", "1")
                            finish()
                        }
                        else
                        {
                            mazeImageView.setImageBitmap(ocvImage!!.GetBitmap())
                        }
                    }
                    else -> {
                        Log.d(TAG_INFO, "Error occurred during image cropping ($result.error)")
                    }
                }
            }
            CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE -> { //User selected an image, initialize cropping on image
                if(resultCode != RESULT_OK){
                    Log.d(TAG_INFO, "Error occurred when picking image (Code $resultCode)")
                    finish()
                    return
                }
                Log.d(TAG_INFO, "Picked an image")
                val result = CropImage.getPickImageResultUri(this, data)
                originalImage = MediaStore.Images.Media.getBitmap(this.contentResolver, result)
                if(originalImage == null) {
                    finish()
                    Log.d(TAG_INFO, "Could not convert selected image to bitmap")
                    return
                }
                sxDim = originalImage!!.width
                syDim = originalImage!!.height

                //Init crop activity on selected uri
                cropImage(result)
            }
            else -> {
                Log.d(TAG_INFO, "Unrecognized request code $requestCode")
            }
        }
    }

    private fun startDetectionRoutine(): Boolean{
        var attempts = 0
        val maxAttempts = 10
        var startBoxTopLeft: Point? = null; var startBoxBottomRight: Point? = null
        var endBoxTopLeft: Point? = null; var endBoxBottomRight: Point? = null

        while(attempts < maxAttempts){

            //run text detection
            val result = detectText()

            //wait for task to complete
            while(!result.isComplete) {}
            val text = result.result!!.text

            //check for recognition of X and O characters
            if((text.contains('X') || text.contains('x')) && (text.contains('O') || text.contains('o'))) {
                for (block in result.result!!.textBlocks) {
                    for (line in block.lines) {
                        for (element in line.elements) {
                            val elementText = element.text
                            val elementCornerPoints = element.cornerPoints
                            //retrieve bounding rect dimensions for 'x'
                            if(startBoxTopLeft == null && (elementText[0] == 'X' || elementText[0] == 'x')) {
                                startBoxTopLeft = elementCornerPoints!![0]
                                startBoxBottomRight = elementCornerPoints[2]
                            }
                            //retrieve bounding rect dimensions for 'o'
                            else if(endBoxTopLeft == null && (elementText[0] == 'O' || elementText[0] == 'o')) {
                                endBoxTopLeft = elementCornerPoints!![0]
                                endBoxBottomRight = elementCornerPoints[2]
                            }
                        }
                    }
                }
            }
            // check if detection routine is done
            if(startBoxTopLeft != null && startBoxBottomRight != null && endBoxTopLeft != null && endBoxBottomRight != null)
            {
                Log.d(TAG_INFO, "Text recognition succeeded!")
                originalImage!!.recycle()
                // Generate ocvImage with all features recognized
                cleanImage(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION)
                positionData = removeOCRText(startBoxTopLeft, startBoxBottomRight, endBoxTopLeft, endBoxBottomRight, croppedBmp!!.width, croppedBmp!!.height)
                croppedBmp!!.recycle()

                return true
            }
            else{
                //hint for garbage collection to avoid heap exhaustion
                croppedBmp!!.recycle()

                //increase size of cropped by 1%
                cropOriginal(0.01)

                resizeCroppedBmp(4000, 4000)
                ocrImage = InputImage.fromBitmap(croppedBmp!!, 0)
            }
            attempts++
        }
        return false
    }

    /**
     * This program is used to modify the original image in an attempt for a better
     * OCR result
     *
     */
    private fun cropOriginal(amount: Double){
        //increase cropped size by amount%
        val width = cx2Dim - cx1Dim
        val height = cy2Dim - cy1Dim
        val widthResize = amount * width
        val heightResize = amount * height

        //update new cropped image dimensions
        cx1Dim = maxOf(0, (cx1Dim - widthResize).toInt())
        cx2Dim = minOf(sxDim, (cx2Dim + widthResize).toInt())
        cy1Dim = maxOf(0, (cy1Dim - heightResize).toInt())
        cy2Dim = minOf(syDim, (cy2Dim + heightResize).toInt())

        //create new cropped image
        croppedBmp = Bitmap.createBitmap(cx2Dim - cx1Dim, cy2Dim - cy1Dim, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(croppedBmp!!)

        //copy from original image into the croppedBmp
        canvas.drawBitmap(originalImage!!, Rect(cx1Dim, cy1Dim, cx2Dim, cy2Dim), Rect(0, 0, croppedBmp!!.width, croppedBmp!!.height), null)
    }


    /**
     * Runs google's Text Detection ML kit for Firebase on the selected picture
     *
     *
     * @param resultUri location of the picture to run text detection on
     */
    private fun detectText(): Task<Text> {
        //val image = InputImage.fromBitmap(bmp, 0)
        val recognizer = TextRecognition.getClient()
        // Start OCR task to look for start/end features
        return recognizer.process(ocrImage!!)
            .addOnSuccessListener { visionText ->
                Log.d(TAG_INFO, "Text recognition succeeded")
            }
            .addOnFailureListener{ e ->
                Log.d(TAG_INFO, "Text recognition failed")
            }
    }

    /**
     * Utility function to resize an Android Bitmap
     *
     * Resizes a bitmap to a max of 4k x 4k resolution while maintaining aspect ratio
     *
     * @param bmp Bitmap to be resized
     * @param maxWidth max width to target
     * @param maxHeigt max height to target
     * @return resized bitmap
     */
    private fun resizeCroppedBmp(maxWidth: Int, maxHeight: Int){
        val scale: Float = Math.min(
            maxHeight.toFloat() / croppedBmp!!.width,
            maxWidth.toFloat() / croppedBmp!!.height
        )

        val matrix = Matrix()
        matrix.postScale(scale, scale)

        Bitmap.createBitmap(
            croppedBmp!!,
            0,
            0,
            croppedBmp!!.width,
            croppedBmp!!.height,
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
    private fun removeOCRText(startBoxTopLeft: Point?, startBoxBottomRight: Point?, endBoxTopLeft: Point?, endBoxBottomRight: Point?, maxWidth: Int, maxHeight: Int): List<Int>?{
        //convert pixel locations to 1025x1025 space
        val sTLx = (((startBoxTopLeft!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val sTLy =  (((startBoxTopLeft.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val sBRx = (((startBoxBottomRight!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val sBRy =  (((startBoxBottomRight.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()

        val eTLx = (((endBoxTopLeft!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val eTLy =  (((endBoxTopLeft.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val eBRx = (((endBoxBottomRight!!.x).toFloat() / maxWidth.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()
        val eBRy =  (((endBoxBottomRight.y).toFloat() / maxHeight.toFloat()) * HEIGHTMAP_RESOLUTION).toInt()


        if(ocvImage == null)
        {
            Log.d(TAG_INFO, "Warning: currentImage needs to be created before removing bounding boxes!")
            return null
        }
        // remove the content in the bounding boxes mark
        ocvImage!!.Resize(HEIGHTMAP_RESOLUTION, HEIGHTMAP_RESOLUTION)
        ocvImage!!.RemoveBoundingBox(sTLx, sTLy, sBRx, sBRy, -10, -10)
        ocvImage!!.RemoveBoundingBox(eTLx, eTLy, eBRx, eBRy, -10, -10)

        //(player.x1,y1); (player.x2,y1); (endBox.x1,y1); (endBox.x2,y2)
        return listOf(sTLx, sTLy, sBRx, sBRy, eTLx, eTLy, eBRx, eBRy)

    }

    private fun cleanImage(width: Int, height: Int) {
        if (croppedBmp == null) {
            Log.d(TAG_INFO, "WARNING: imageBitmap == null")
        }
        ocvImage = ImageMarkup(croppedBmp!!, width, height)
        ocvImage!!.Filter(10)
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

    private fun initCroppedBmp(uri: Uri){
        croppedBmp = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
    }
}
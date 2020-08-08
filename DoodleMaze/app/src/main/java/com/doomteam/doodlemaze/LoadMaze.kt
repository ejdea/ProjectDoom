package com.doomteam.doodlemaze

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_load_maze.*
import java.io.File
import java.io.FileInputStream

class LoadMaze : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_load_maze)
        super.onCreate(savedInstanceState)

        /**
         * Source for loading and setting bitmap in ImageView:
         * https://stackoverflow.com/a/56781346
         * References used to determine how to dynamically create Imageviews:
         * https://stackoverflow.com/a/52567656
         * https://stackoverflow.com/a/15356823
         * https://medium.com/@NumberShapes/kotlin-dynamically-creating-an-imageview-during-runtime-aec9268f9ccf
         *
         * Reference for implementing long click listener:
         * https://stackoverflow.com/questions/49712663/how-to-properly-use-setonlongclicklistener-with-kotlin
         * Source for loading and setting bitmap in ImageView: https://stackoverflow.com/a/56781346
         */
        val targetDir = this.getExternalFilesDir(null)
        val filePath = "/maze_thumbnails/"
        val file = File(targetDir, filePath)
        // Show thumbnails for each saved map
        file.list()?.forEach { it ->
            val thumbImageView = ImageView(this)
            thumbImageView.setPadding(0, 10, 0, 10)
            val imageFile =
                BitmapFactory.decodeStream(FileInputStream(targetDir.toString() + filePath + it))
            thumbImageView.setImageBitmap(imageFile)
            val fileName = it.toString()
            thumbImageView.setOnClickListener {
                onClickLoadMaze(fileName, thumbImageView)
            }
            thumbImageView.setOnLongClickListener {
                onLongClickLoadMaze(fileName)
                return@setOnLongClickListener true
            }
            thumbnail_layout.addView(thumbImageView)
        }
    }

    /**
    * Upload map to Firebase and launch Unity
    */
    private fun dbStoreMap(hash: String){
        val db = Firebase.firestore
        val entityData = hashMapOf(
            "score" to -1.0f,
            "hash" to hash
        )

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
            val unityIntent: Intent? = applicationContext.packageManager.getLaunchIntentForPackage("com.doomteam.unitydoodlemaze")
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

    /**
     *  Loads selected maze
    */
    private fun onClickLoadMaze(fileName: String, thumbImageView: ImageView) {
        // Get storage reference
        val storage = Firebase.storage
        val sRef = storage.reference

        // Fade image when selected
        // Reference for overlay to image:
        // https://stackoverflow.com/a/18639984
        val color = (Color.argb(225,255,255, 255))
        thumbImageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)

        // Confirm that the user wants to load the maze.
        val confirmLoad = AlertDialog.Builder(this)
        confirmLoad.setMessage("Would you like to play this maze?")
        confirmLoad.setPositiveButton("Confirm") { _, _->
            thumbImageView.isClickable = false
            thumbImageView.isEnabled = false

            // Remove the last 10 characters from the file name to give the name of the saved
            // level data.
            val dataFileName = fileName.dropLast(10)
            val targetDir = this.getExternalFilesDir(null)
            val filePath = targetDir.toString() + "/saved_mazes/"
            val height_map_name = "mobile_height_map.raw"
            val app_folder = "app_height_maps"
            val levelData = LevelData(filePath, dataFileName)

            // Retrieve user info from intent
            val user:String? = intent.getStringExtra("current_user")
            val heightMapRef: StorageReference? = sRef.child(app_folder).child(user!!)
                .child(height_map_name)
            val uploadTask = heightMapRef?.putBytes(levelData.GetData())
            uploadTask?.addOnSuccessListener {
                Log.d(TAG_INFO, "Upload done!")
                dbStoreMap(levelData.GetHash())
            }

            // Let the user know to wait for Unity to load
            val loadingToast = Toast.makeText(this, "Please wait while the game loads."
                , Toast.LENGTH_LONG)
            loadingToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            loadingToast.show()
        }

        confirmLoad.setNegativeButton("Cancel"){ _, _ ->
            thumbImageView.isClickable = true
            thumbImageView.isEnabled = true

            // Remove fade from image
            // Reference for overlay to image:
            // https://stackoverflow.com/a/18639984
            val originalColor = (Color.argb(0,255,255, 255))
            thumbImageView.setColorFilter(originalColor, PorterDuff.Mode.SRC_ATOP)
        }
        confirmLoad.show()
    }

    /**
     * Delete selected map levelData and map thumbnail file.
     * References for delete conformation:
     https://www.journaldev.com/309/android-alert-dialog-using-kotlin#alert-dialogs
    */
    private fun onLongClickLoadMaze(fileName: String) {

        // Confirm that the user wants to delete the maze.
        val confirmDelete = AlertDialog.Builder(this)
        confirmDelete.setTitle("Confirm Maze Deletion")
        confirmDelete.setMessage("Are you sure you want to delete this maze?")
        confirmDelete.setPositiveButton("Confirm") { _, _->
            val targetDir = this.getExternalFilesDir(null)
            val mazeFilePath = targetDir.toString() + "/saved_mazes/"
            val thumbFilePath= targetDir.toString() + "/maze_thumbnails/"

            // Remove the last 10 characters from the file name to give the name of the saved
            // level data.
            val dataFileName = fileName.dropLast(10)
            val levelDataToDelete = File(mazeFilePath, dataFileName)
            val thumbnailToDelete = File(thumbFilePath, fileName)
            levelDataToDelete.delete()
            thumbnailToDelete.delete()
            finish()

            // Let the user know the maze was deleted so they know why they were sent back to the
            // main menu.
            val deleteToast = Toast.makeText(this, "Maze deleted", Toast.LENGTH_LONG)
            deleteToast.setGravity(Gravity.TOP, 0, 700)
            deleteToast.show()
        }
        confirmDelete.setNegativeButton("Cancel"){ _, _ ->
        }
        confirmDelete.show()
    }
}


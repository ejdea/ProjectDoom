package com.doomteam.doodlemaze

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
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

        // Source for loading and setting bitmap in ImageView: https://stackoverflow.com/a/56781346
        // References used to determine how to dynamically create Imageviews:
        // https://stackoverflow.com/a/52567656
        // https://stackoverflow.com/a/15356823
        // https://medium.com/@NumberShapes/kotlin-dynamically-creating-an-imageview-during-runtime-aec9268f9ccf
        //
        // Reference for implementing long click listener:
        // https://stackoverflow.com/questions/49712663/how-to-properly-use-setonlongclicklistener-with-kotlin
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
                onClickLoadMaze(fileName)
            }
            thumbImageView.setOnLongClickListener {
                onLongClickLoadMaze(fileName)
                return@setOnLongClickListener true
            }
            thumbnail_layout.addView(thumbImageView)
        }
    }

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

    /*
    *  Loads selected maze
    */
    private fun onClickLoadMaze(fileName: String) {
        // Get storage reference
        val storage = Firebase.storage
        val sRef = storage.reference

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
            ?: //user made it here without logging in
            return

        val heightMapRef: StorageReference? = sRef.child(app_folder).child(user!!)
            .child(height_map_name)
        val uploadTask = heightMapRef?.putBytes(levelData.GetData())
        uploadTask?.addOnSuccessListener {
            Log.d(TAG_INFO, "Upload done!")
            dbStoreMap(levelData.GetHash())
        }
    }

    // Delete selected file.
    private fun onLongClickLoadMaze(fileName: String) {
        val dataFileName = fileName.dropLast(10)
        Toast.makeText(
            this,
            "Delete $dataFileName",
            Toast.LENGTH_SHORT
        ).show()
    }
}

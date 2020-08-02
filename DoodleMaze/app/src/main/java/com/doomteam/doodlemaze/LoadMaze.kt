package com.doomteam.doodlemaze

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
        val targetDir = this.getExternalFilesDir(null)
        val filePath = "/maze_thumbnails/"
        val file = File(targetDir, filePath)


        file.list()?.forEach {
            val thumbImageView = ImageView(this)
            Log.d(TAG_INFO, "Full filepath is: $it")
            val imageFile = BitmapFactory.decodeStream(FileInputStream(targetDir.toString() + filePath + it))
            thumbImageView.setImageBitmap(imageFile)
            thumbImageView.setPadding(0, 10, 0, 10)
            thumbnail_layout.addView(thumbImageView)

        }
    }

    fun onClickLoadMaze(view: View) {
        displayToast()
    }

    /*
    * Reference for listing files: https://stackoverflow.com/a/44567568
     */
    private fun displayToast() {
        Toast.makeText(applicationContext, "Hello!", Toast.LENGTH_SHORT).show()
        val targetDir = this.getExternalFilesDir(null)
        val filePath = "/maze_thumbnails/"
        val file = File(targetDir, filePath)
        Log.d(TAG_INFO, "File is $file")
//        val thumbList = file.list().forEach {
//            Log.d(TAG_INFO, "List is $it")
//        }

    }
}

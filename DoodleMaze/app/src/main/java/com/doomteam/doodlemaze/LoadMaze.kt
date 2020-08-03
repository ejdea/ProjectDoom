package com.doomteam.doodlemaze

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
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
            val imageFile = BitmapFactory.decodeStream(FileInputStream(targetDir.toString() + filePath + it))
            thumbImageView.setImageBitmap(imageFile)
            thumbImageView.setPadding(0, 10, 0, 10)
            thumbnail_layout.addView(thumbImageView)

        }
    }

    fun onClickLoadMaze(view: View) {
    }
}

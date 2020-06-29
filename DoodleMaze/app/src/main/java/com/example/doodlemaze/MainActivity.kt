package com.example.doodlemaze

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openLoadMazeScreen(view: View) {
        val loadMazeIntent = Intent (this, LoadMaze::class.java)
        startActivity(loadMazeIntent)
    }
}
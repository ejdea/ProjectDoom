package com.example.doodlemaze

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class LoadMaze : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_maze)

        val loadMazeSpinner: Spinner = findViewById(R.id.spinner1)
        val loadMazeSpinner2: Spinner = findViewById(R.id.spinner2)
        val loadMazeSpinner3: Spinner = findViewById(R.id.spinner3)
        val loadMazeSpinner4: Spinner = findViewById(R.id.spinner4)
        val loadMazeSpinner5: Spinner = findViewById(R.id.spinner5)
        val loadMazeSpinner6: Spinner = findViewById(R.id.spinner6)
        ArrayAdapter.createFromResource(this, R.array.maze_options, android.R.layout.simple_spinner_item)
            .also {adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                loadMazeSpinner.adapter = adapter
                loadMazeSpinner2.adapter = adapter
                loadMazeSpinner3.adapter = adapter
                loadMazeSpinner4.adapter = adapter
                loadMazeSpinner5.adapter = adapter
                loadMazeSpinner6.adapter = adapter
            }
    }
}
package com.doomteam.doodlemaze

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Instructions: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.instructions_page)
    }

    fun goBackToMain(view: View) {
        finish()
    }

}
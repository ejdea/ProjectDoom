package com.doomteam.doodlemaze


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class Credits: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credits_page)
    }

    fun goBack(view: View) {
        finish()
    }

}
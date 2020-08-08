package com.doomteam.doodlemaze


import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.credits_page.*
import java.util.regex.Pattern

class Credits: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.credits_page)

        img1_id.movementMethod = LinkMovementMethod.getInstance()
        img2_id.movementMethod = LinkMovementMethod.getInstance()
        img3_id.movementMethod = LinkMovementMethod.getInstance()
        vid1_id.movementMethod = LinkMovementMethod.getInstance()
        vid2_id.movementMethod = LinkMovementMethod.getInstance()
        audio1_id.movementMethod = LinkMovementMethod.getInstance()
        audio2_id.movementMethod = LinkMovementMethod.getInstance()
        audio3_id.movementMethod = LinkMovementMethod.getInstance()
    }

    fun goBack(view: View) {
        finish()
    }

}
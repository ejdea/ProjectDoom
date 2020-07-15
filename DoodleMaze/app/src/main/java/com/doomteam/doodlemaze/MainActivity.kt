package com.doomteam.doodlemaze

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "MainActivity"
        private const val RC_AUTH = 125
        private var current_user: String? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Authenticate the user
        val intent = Intent(this, AuthenticateActivity::class.java)
        startActivityForResult(intent, RC_AUTH)
    }

    // [START auth_fui_result]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            //check whether user was authenticated
            if (resultCode == Activity.RESULT_OK){
                current_user = data?.getStringExtra("auth_result_uid")
                if(current_user == null)
                {
                    //TODO ADD HANDLER: Something went wrong during sign in
                }
                else{
                    setContentView(R.layout.activity_main)
                    val text = findViewById<TextView>(R.id.name_holder)
                    val name = "Welcome " + data?.getStringExtra("auth_result_email")
                    text.text = name
                }
            }
            else{
                //TODO ADD HANDLER: user didn't create an account
            }
        }
    }

    fun openLoadMazeScreen(view: View) {
        val loadMazeIntent = Intent(this, LoadMaze::class.java)
        loadMazeIntent.putExtra("current_user", current_user)
        startActivity(loadMazeIntent)
    }

    fun openCreateMazeScreen(view: View) {
        val createMazeIntent = Intent(this, CreateMaze::class.java)
        createMazeIntent.putExtra("current_user", current_user)
        startActivity(createMazeIntent)
    }

    fun onClickExit(view: View) {
        finishAffinity()
    }

    //TODO: sign out the account?
}
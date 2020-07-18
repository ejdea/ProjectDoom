package com.doomteam.doodlemaze

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

/**
 * @author Martin Edmunds
 * @version 1.0
 * @since 07/15/2020
 *
 * Activity that controls authentication flow for email/passsword Firebase accounts
 */
class AuthenticateActivity : AppCompatActivity(){

    private val TAG = "AuthActivity"
    companion object {

        private const val RC_SIGN_IN = 123
        private lateinit var auth: FirebaseAuth

        //flag used for testing, if set to true the app will always force the authFlow without checking if
        //an existing user is logged in
        private const val DEBUG = false

    }

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        if(DEBUG)
        {
            startAuthFlow()
        }
        else
        {
            checkSignIn()
            startAuthFlow()
        }
    }

    /**
     * Checks to see if a user was able to sign in, if a valid user exists in the instance, finish the activity
     * Returns email/uid to calling activity
     */
    private fun checkSignIn() {
        //val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val returnIntent = Intent()
            returnIntent.putExtra("auth_result_email", FirebaseAuth.getInstance().currentUser?.email)
            returnIntent.putExtra("auth_result_uid", FirebaseAuth.getInstance().currentUser?.uid)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }

    private fun startAuthFlow() {
        setContentView(R.layout.authenticate_main)
    }

    fun onClickSignIn(view: View){
        //val auth = FirebaseAuth.getInstance()
        val email = findViewById<EditText>(R.id.email_field).text.toString()
        val password = findViewById<EditText>(R.id.password_field).text.toString()

        //validate input
        if(!validateEntry(email, password))
        {
            displayErrorToast("Invalid Username/Password")
            return
        }

        //attempt sign in
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "SignIn:success")
                    checkSignIn()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    displayErrorToast("Authentication Failed")
                }
            }

    }

    fun onClickSignUp(view: View){
        //val auth = FirebaseAuth.getInstance()
        val email = findViewById<EditText>(R.id.email_field).text.toString()
        val password = findViewById<EditText>(R.id.password_field).text.toString()

        //validate input
        if(!validateEntry(email, password))
        {
            displayErrorToast("Invalid Username/Password")
            return
        }

        //attempt account creation
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "SignUp:success")
                    checkSignIn()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    displayErrorToast("Authentication Failed")
                }
            }
    }

    private fun validateEntry(email: String?, password: String?): Boolean {
        if(email == null || password == null || email == "" || password == ""){
            return false
        }
        //required for Firebase password
        if(password.length < 8){
            return false
        }
        return true
    }

    private fun displayErrorToast(message: String) {
        val toast = Toast.makeText(baseContext, message,
            Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.TOP, 0, 0)
        toast.show()
    }

}

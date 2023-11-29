package com.example.firebase_login

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    //Buttons for the layout
    private lateinit var btnSignUp: Button
    private lateinit var btnLogin: Button
    private lateinit var btnUpdateProf: Button

    //Edit Text for the app main screen
    private lateinit var txtEmailLog: EditText
    private lateinit var txtPassLog: EditText
    private lateinit var txtEmailSignUp: EditText
    private lateinit var txtPassSignUp: EditText
    private lateinit var txtUsername: EditText

    //text Views of the project
    private lateinit var txtVerifLog: TextView

    //Images
    private lateinit var profilePic: ImageView


    lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSignUp = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        btnUpdateProf = findViewById(R.id.btnUpdateProfile)

        txtEmailLog = findViewById(R.id.etEmailLogin)
        txtPassLog = findViewById(R.id.etPasswordLogin)

        txtEmailSignUp = findViewById(R.id.etEmailRegister)
        txtPassSignUp = findViewById(R.id.etPasswordRegister)

        txtUsername = findViewById(R.id.etUsername)

        txtVerifLog = findViewById(R.id.tvLoggedIn)

        profilePic = findViewById(R.id.ivProfilePicture)

        auth = FirebaseAuth.getInstance()
        auth.signOut()

        btnSignUp.setOnClickListener{
            registerUser()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnUpdateProf.setOnClickListener {
            updateProfile()
        }

    }

    private fun updateProfile() {
        val user = auth.currentUser
        user?.let { user ->
            val username = txtUsername.text.toString()
            val photoURI = Uri.parse("android.resource://$packageName/${R.drawable.ic_android_black_24dp}")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(photoURI)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updateProfile(profileUpdates).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                        Toast.makeText(this@MainActivity, "Successfully updated profile",
                            Toast.LENGTH_LONG).show()
                    }
                } catch(e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }

            }
        }
    }


    private fun registerUser() {
        val email = txtEmailSignUp.text.toString()
        val password = txtPassSignUp.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun loginUser() {
        val email = txtEmailLog.text.toString()
        val password = txtPassLog.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        checkLoggedInState()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun checkLoggedInState() {
        val user = auth.currentUser
        if (user == null) { // not logged in
            txtVerifLog.text = "You are not logged in"
        } else {
            txtVerifLog.text = "You are logged in!"
            txtUsername.setText(user.displayName)
            profilePic.setImageURI(user.photoUrl)
        }
    }

    override fun onStart() {
        super.onStart()
        checkLoggedInState()
    }
}
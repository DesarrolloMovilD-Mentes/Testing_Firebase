package com.example.firebase_storage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


private const val REQUEST_CODE_IMAGE_PICK = 0

class MainActivity : AppCompatActivity() {
    private lateinit var btnUploadImg: Button
    private lateinit var imgViw: ImageView
    var curfile: Uri? = null
    var imageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgViw = findViewById(R.id.ivImage)
        btnUploadImg = findViewById(R.id.btnUploadImage)

        imgViw.setOnClickListener{
            Intent(Intent.ACTION_GET_CONTENT).also {

                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE_IMAGE_PICK)

            }
        }

        btnUploadImg.setOnClickListener {
            uploadImageToStorage("myImage")
        }

    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch{
         try {
              curfile?.let{
                  imageRef.child("images/$filename").putFile(it).await()
                  withContext(Dispatchers.Main){
                      Toast.makeText(this@MainActivity, "Successfully uploaded Image", Toast.LENGTH_LONG).show()
                  }
              }


         }   catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
         }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE_PICK){
            data?.data?.let{
                curfile = it
                imgViw.setImageURI(it)
            }
        }
    }

}
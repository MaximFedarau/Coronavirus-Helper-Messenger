package com.example.kotlinmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        aready_have_account_textview.setOnClickListener {
            //Log.d("RegisterActivity","Try to show login activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener {
            //Log.d("RegisterActivity","He pressed image button.")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
        register_button_register.setOnClickListener {
            val email = email_edittext_register.text.toString()
            val password = password_edittext_register.text.toString()
            val username = username_edittext_register.text.toString()
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(this,"Please enter correct data",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length<6) {
                Toast.makeText(this,"Minimum length of password is 6!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //Log.d("RegisterActivity","Email is ${email}")
            //Log.d("RegisterActivity","Password is ${password}")
            //Log.d("RegisterActivity","Username is ${username}")

            if (select_photo_image_view.drawable != null) {
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    else {
                        val result = it.result  // Kotlin says result is of type "AuthResult?"
                        if (result != null) {
                            val user = result.user   // Kotlin says user is of type "FirebaseUser?"
                            if (user != null) {
                                Log.d("Main", "Successfully created user with uid: ${user.uid}")
                            }
                        }
                        uploadImageToFirebaseStorage()
                    }
                }}

        }
    }
    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Log.d("UploadingImage","I am here")
        if (requestCode==0 && resultCode== Activity.RESULT_OK && data !=null) {
            //Log.d("RegisterActivity","Trying to upload image")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            select_photo_image_view.setImageBitmap(bitmap)
            selectphoto_button_register.alpha = 0f
        }

    }
    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri==null) {
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("images/${filename}")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                //Log.d("Register","Upload picture successfully!")
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    //Log.d("RegisterActivity","${it}")
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                return@addOnFailureListener
            }
    }
    private lateinit var database: DatabaseReference
    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        database = Firebase.database.reference
        val uid = FirebaseAuth.getInstance().uid.toString()
        //val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
        val user = User(uid,username_edittext_register.text.toString(),profileImageUrl)
        database.child("users").child(uid).setValue(user)
            .addOnSuccessListener {
                //Log.d("RegisterActivity","Success database")

                val intent = Intent(this,LatestMessagesActivity::class.java)
                intent.flags =- Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this,"Some error occurred. Try one more time!",Toast.LENGTH_SHORT).show()
            }
    }
}
@IgnoreExtraProperties
@Parcelize
class User(val uid: String?=null,val username: String? = null, val profileImageUrl: String? = null):Parcelable {
    constructor(): this("","","")
}
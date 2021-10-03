package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_button_login.setOnClickListener {
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()
            //Log.d("LoginActivity","${email} and ${password}")
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,"Your email or password is wrong! Try one more time.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    val intent = Intent(this,LatestMessagesActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Your email or password is wrong! Try one more time.",Toast.LENGTH_SHORT).show()
                }
        }
        back_to_register_textview.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)//think about this function
            startActivity(intent)
        }
    }

}
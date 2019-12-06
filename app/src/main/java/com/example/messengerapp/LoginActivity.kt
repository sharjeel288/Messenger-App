package com.example.messengerapp

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity:AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Login_button.setOnClickListener {
            val email=Email_Login.text.toString()
            val password=Password_login.text.toString()
            if(email.isEmpty() || password.isEmpty())
            {
                Toast.makeText(this,"Please Type Email/Password",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener
                    //Else SuccessFull Creation
                    if(FirebaseAuth.getInstance().currentUser?.isEmailVerified!!)
                    {
                        Toast.makeText(this, "Login SuccessFully", Toast.LENGTH_SHORT).show()
                        val intent= Intent(this,LatestMessagesActivity::class.java)
                        intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(this, "Please Verify your Email", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Failed To Login",Toast.LENGTH_SHORT).show()
                }
        }
        Backto_Registration.setOnClickListener {
            finish()
        }
    }

}
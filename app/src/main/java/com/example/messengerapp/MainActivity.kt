package com.example.messengerapp
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Register_button.setOnClickListener {
            performRegister()
        }
        account_textView.setOnClickListener {
            val intent=Intent(this,LoginActivity::class.java)
            startActivity((intent))
        }
        img_btn.setOnClickListener {
            val intent=Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            startActivityForResult(intent,0)
        }
    }
    var selecteduri: Uri?=null
       //UpLoading Image To Register Activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode== Activity.RESULT_OK && data !=null)
        {//Proceed and check the selected img....
            selecteduri=data.data
            val bitmap=MediaStore.Images.Media.getBitmap(contentResolver,selecteduri )
            selectphoto_imageview_Register.setImageBitmap(bitmap)
            img_btn.alpha=0f
        }
    }
    private  fun performRegister(){
        val email=Email_Register.text.toString()
        val password=password_Register.text.toString()
        if(email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this,"Please Type Email/Password",Toast.LENGTH_SHORT).show()
            return
        }
        //FireBase Authentication to Create user eamil and Password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener
                //Else SuccesFull Create User
                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()?.addOnCompleteListener {
                    if(it.isSuccessful) {
                        Log.d("Main","Successfully Create User with uid ${it.result}")
                        Toast.makeText(this,"Account SuccesFully Created Please Verify Your Email",Toast.LENGTH_SHORT).show()
                        uploadImgFireBase()
                    }
                }
            }
            .addOnFailureListener {
                Log.d("Main","Failed To Create Account ${it.message}")
                Toast.makeText(this,"Failed to Create Account",Toast.LENGTH_SHORT).show()
            }
    }
    //UpLoadung Image To FireBase Authentication
    private  fun uploadImgFireBase(){
        if(selecteduri==null) return
        //Else Upload the image
        val filename=UUID.randomUUID().toString()
        val ref=FirebaseStorage.getInstance().getReference("/image/$filename")
        ref.putFile(selecteduri!!)
            .addOnSuccessListener {
                Log.d("Main","Successfully Upload Image::" + it.metadata?.path)
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("Main","File Location Download::$it")
                    uploadUserDataToFireBaseDataBase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("Main","Can't Upload Image")
            }
    }
    // Saving User InFo In Fire Base Data Base
    private fun uploadUserDataToFireBaseDataBase(profileimgurl: String ){
        val uid=FirebaseAuth.getInstance().uid?:""
      val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user=User(uid,username_Register.text.toString(),profileimgurl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("Main","Successfully Load Data to DATABASE")
                val intent=Intent(this,LoginActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Main","Data Not Upload To DATABASE")
            }
    }
}
@Parcelize
class User(val uid:String,val username:String,val profileimgurl:String):Parcelable{
    constructor():this("","","")
}




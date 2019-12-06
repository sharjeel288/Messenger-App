package com.example.messengerapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_login.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import java.util.*

class chatLoginActivity : AppCompatActivity() {

    val adapter=GroupAdapter<ViewHolder>()
    var touser:User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_login)
        recyclerview_chat_log.adapter=adapter
        touser=intent.getParcelableExtra<User>(NewMessagesActivity.User_Key)
        supportActionBar?.title=touser?.username
        listenToMessage()
        sendbtn_message.setOnClickListener {
            Log.d("ChatMessage","Attempt Message Send")
            performSendMessage()
    }
    }
    // Retriving saved Messages from Data Base
    private  fun listenToMessage(){
        val fromId=FirebaseAuth.getInstance().uid
        val toId=touser?.uid
        val ref=FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java)
                if(chatMessage!=null){
                    //Adding Messages of From User To RecyclerView
                    if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                        val currentUser=LatestMessagesActivity.currentUser
                        adapter.add(ChatFromItem(chatMessage.text,currentUser!!))
                    }
                    else{
                        adapter.add(ChatToItem(chatMessage.text,touser!!))
                    }
                }
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)

            }
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }
    //Sending Messages To Data Base and save them
    private fun performSendMessage(){
        val user=intent.getParcelableExtra<User>(NewMessagesActivity.User_Key)
        val text=edittext_message.text.toString()
        val fromId=FirebaseAuth.getInstance().uid
        val toId=user.uid
        if(fromId==null)return
        val referance=FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toreferance=FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatmessage=ChatMessage(referance.key!!,text,fromId,toId,System.currentTimeMillis()/1000)
        referance.setValue(chatmessage)
            .addOnSuccessListener {
                Log.d("ChatMessage","Message save ${referance.key}")
                edittext_message.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
            }
        toreferance.setValue(chatmessage)
        val latestMessagesFromRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagesFromRef.setValue(chatmessage)

        val latestMessagesToRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessagesToRef.setValue(chatmessage)
    }
}
// class for saving messages
class ChatMessage(val id:String,val text:String,val fromId:String,val toId:String,val timestamp:Long){
    constructor():this("","","","",-1)
}
// RecyclerView For Current User chating
class ChatFromItem(val text:String,val user:User):Item<ViewHolder>(){
    override fun getLayout(): Int {
      return R.layout.chat_from_row
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textView_from_row.text=text
        val uri=user.profileimgurl
        val targetImageView=viewHolder.itemView.imageview
        Picasso.get().load(uri).into(targetImageView)
    }
}
// RecyclerView For To User that we are going to send messages
class ChatToItem(val text:String,val user:User):Item<ViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val uri=user.profileimgurl
        val targetImageView=viewHolder.itemView.ImageView_to_row
        Picasso.get().load(uri).into(targetImageView)
        viewHolder.itemView.textView_To_row.text=text
    }
}
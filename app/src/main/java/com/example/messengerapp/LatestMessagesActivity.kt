package com.example.messengerapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_messages_row.view.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object {
        var currentUser:User?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        recyclerview_latest_messages.adapter=adpater
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        //Click  RecyclerView Item
        adpater.setOnItemClickListener { item, view ->
            val intent=Intent(this,chatLoginActivity::class.java)
            val row=item as LatestMessages
            intent.putExtra(NewMessagesActivity.User_Key,row.chatPartnerUser)
            startActivity(intent)
        }
        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserLoggedIn()
    }
    class LatestMessages(val chatMessage: ChatMessage):Item<ViewHolder>(){
        var chatPartnerUser:User?=null
        override fun getLayout(): Int {
            return R.layout.latest_messages_row
        }
        override fun bind(viewHolder: ViewHolder, position: Int) {
         viewHolder.itemView.message_textView_latest_message.text=chatMessage.text

            val chatPartner:String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid)
            {
                chatPartner=chatMessage.toId
            }else{
                chatPartner=chatMessage.fromId
            }
            val ref=FirebaseDatabase.getInstance().getReference("/users/$chatPartner")
            ref.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser=p0.getValue(User::class.java)
                    viewHolder.itemView.message_textview_username.text= chatPartnerUser!!.username

                    val imageViewTarget=viewHolder.itemView.imageView_latest_message
                    Picasso.get().load(chatPartnerUser!!.profileimgurl).into(imageViewTarget)
                }
                override fun onCancelled(p0: DatabaseError) {
                }
            })
        }
    }
    //Get Latest Messages From DataBaase
    val latestMessagesMap=HashMap<String,ChatMessage>()
    private  fun refreshRecyclerView(){
        adpater.clear()
        latestMessagesMap.values.forEach {
            adpater.add(LatestMessages(it))
        }
    }
    private fun listenForLatestMessages(){
        val fromId=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[p0.key!!]=chatMessage
                refreshRecyclerView()
            }
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatMessage::class.java)?:return
                latestMessagesMap[p0.key!!]=chatMessage
                refreshRecyclerView()
            }
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }
            override fun onChildRemoved(p0: DataSnapshot) {
            }
        })
    }
    val adpater=GroupAdapter<ViewHolder>()
//  Get Users From FireBase And Display them in Select User
    private fun fetchCurrentUser(){
        val uid=FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                currentUser=p0.getValue(User::class.java)
                Log.d("latest Messages","Current User ${currentUser?.username}")
            }
        })
    }

    private fun verifyUserLoggedIn(){
        val uid=FirebaseAuth.getInstance().uid
        if(uid==null)
        {
            val intent= Intent(this,MainActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId)
        {
            // Return To the Regesitration Activity
         R.id.menu_sign_out -> {
             FirebaseAuth.getInstance().signOut()
             val intent= Intent(this,MainActivity::class.java)
             intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
             startActivity(intent)
         }
            R.id.menu_new_message -> {
                val intent=Intent(this,NewMessagesActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_manu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}

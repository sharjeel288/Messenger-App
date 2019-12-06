package com.example.messengerapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_messages.*
import kotlinx.android.synthetic.main.user_row_message.view.*

class NewMessagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messages)

        supportActionBar?.title="Select User"

        fetchUsers()
    }
    companion object {
        val User_Key="User_Key"
    }

    private fun fetchUsers(){//From Data Base
        val ref=FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            // Fetching Each User From Data Base
            override fun onDataChange(p0: DataSnapshot) {
                val adapter=GroupAdapter<ViewHolder>()
                p0.children.forEach {
                    Log.d("NewMessage",it.toString())
                    val user=it.getValue(User::class.java)
                    if(user!=null){
                        adapter.add(Useritem(user))
                    }
                }
                // Select User For Chating
                adapter.setOnItemClickListener { item, view ->
                    val userItem=item as Useritem
                    val intent= Intent(view.context,chatLoginActivity::class.java)
                    //Send User Object to the next activity
                    intent.putExtra(User_Key,userItem.user)
                    startActivity(intent)
                    finish()
                }
                recyclerview_newmessages.adapter=adapter
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }
}
// Recycler View
class Useritem(val user: User):Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        //Adding Data in Recycler View
        viewHolder.itemView.username_textView_Message.text=user.username
        Picasso.get().load(user.profileimgurl).into(viewHolder.itemView.imageView_new_Message)
    }
    override fun getLayout(): Int {
        return R.layout.user_row_message
    }
}

package com.example.kotlinmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {
    companion object {
        var currentUser: User? = null
    }


    class LatestMessageRow(val chatMessage: ChatLogActivity.ChatMessage): Item<GroupieViewHolder>() {
        var chatPartnerUser: User?= null
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val authorMessageId = chatMessage.fromId
            val refUsername = FirebaseDatabase.getInstance().getReference("/users/$authorMessageId")
            refUsername.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val usernameForLatestMessages = snapshot.getValue(User::class.java)?.username
                    var completeMessage="Nothing"
                    if (authorMessageId == FirebaseAuth.getInstance().uid) {completeMessage = "You: ${chatMessage.text}"}
                    else {completeMessage = usernameForLatestMessages+": "+chatMessage.text}
                    if (completeMessage.length<=55) viewHolder.itemView.message_textview_latest_message.text = completeMessage//chatMessage.text
                    else {
                        var shortedText = ""
                        for (i in 0..55) {
                            shortedText += completeMessage[i]
                        }
                        shortedText+="..."
                        viewHolder.itemView.message_textview_latest_message.text = shortedText
                    }
                }
            })

            val chatPartnerId: String
            if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                chatPartnerId = chatMessage.toId
            }
            else {
                chatPartnerId = chatMessage.fromId
            }

            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatPartnerUser = snapshot.getValue(User::class.java)
                    viewHolder.itemView.username_textview_latest_message.text = chatPartnerUser?.username

                    Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.imageview_latest_message)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }

    val latestMessagesMap = HashMap<String,ChatLogActivity.ChatMessage>()

    private fun  refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] =  chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?: return

                latestMessagesMap[snapshot.key!!] =  chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    val adapter = GroupAdapter<GroupieViewHolder>()
    //private fun setupDummyRows() {

        //adapter.add(LatestMessageRow())
        //adapter.add(LatestMessageRow())
        //adapter.add(LatestMessageRow())


    //}

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/${uid}")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        //Log.d("ChatActivity","${uid}")
        if (uid == null) {
            val intent = Intent(this,LoginActivity::class.java)
            intent.flags =- Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this,NewMessageActivity::class.java)
                startActivity(intent)

            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,LoginActivity::class.java)
                //intent.flags =- Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        //setupDummyRows()
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }
        verifyUserIsLoggedIn()

        listenForLatestMessages()

        fetchCurrentUser()

    }
}
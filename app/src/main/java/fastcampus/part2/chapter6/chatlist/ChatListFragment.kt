package fastcampus.part2.chapter6.chatlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fastcampus.part2.chapter6.Key
import fastcampus.part2.chapter6.R
import fastcampus.part2.chapter6.chatdetail.ChatActivity
import fastcampus.part2.chapter6.databinding.FragmentChatlistBinding
import android.util.Log

class ChatListFragment : Fragment(R.layout.fragment_chatlist) {

    private lateinit var binding: FragmentChatlistBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChatlistBinding.bind(view)

        val chatListAdapter = ChatListAdapter { chatRoomItem ->
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, chatRoomItem.otherUserId)
            intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_ID, chatRoomItem.chatRoomId)

            startActivity(intent)
        }

        binding.chatListRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatListAdapter
        }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatRoomsDB = Firebase.database.reference.child(Key.DB_CHAT_ROOMS).child(currentUserId)

        chatRoomsDB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomList = mutableListOf<ChatRoomItem>()

                snapshot.children.forEach { chatRoomSnapshot ->
                    val chatRoomItemMap = chatRoomSnapshot.value as? Map<*, *>

                    chatRoomItemMap?.let {
                        val chatRoomId = it["chatRoomId"] as? String
                        val lastMessage = it["lastMessage"] as? String
                        val otherUserName = it["otherUserName"] as? String
                        val otherUserId = it["otherUserId"] as? String

                        if (chatRoomId != null && otherUserId != null) {
                            val chatRoomItem = ChatRoomItem(
                                chatRoomId = chatRoomId,
                                lastMessage = lastMessage ?: "",
                                otherUserId = otherUserId,
                                otherUserName = otherUserName ?: ""
                            )
                            chatRoomList.add(chatRoomItem)
                        }
                    }
                }

                chatListAdapter.submitList(chatRoomList)
            }


            override fun onCancelled(error: DatabaseError) {}
        })




    }
}
